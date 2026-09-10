// Offline orthographic rendering of the actual BB cuboids/UVs; not an in-game screenshot.
import fs from 'node:fs';
import path from 'node:path';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import {applyArmPose} from './keeper-pose.mjs';
const require=createRequire(path.resolve(process.execPath,'../../package.json'));
const {PNG}=require('pngjs');
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const source=JSON.parse(fs.readFileSync(path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel'),'utf8'));
const atlas=PNG.sync.read(Buffer.from(source.textures[0].source.split(',')[1],'base64'));
const rotate=(point,origin,rotation)=>{
  let [x,y,z]=point.map((v,i)=>v-origin[i]);
  for(let axis=0;axis<3;axis++){
    const a=(rotation?.[axis]??0)*Math.PI/180,c=Math.cos(a),s=Math.sin(a);
    if(axis===0)[y,z]=[y*c-z*s,y*s+z*c];
    if(axis===1)[x,z]=[x*c+z*s,-x*s+z*c];
    if(axis===2)[x,y]=[x*c-y*s,x*s+y*c];
  }
  return [x+origin[0],y+origin[1],z+origin[2]];
};
for(const [view,yaw,kind,t] of [['front',0,'idle',0],['three-quarter',-.6,'idle',0],['side',-1.35,'idle',0],['cast',.4,'bolt',14],['ring',-.6,'ring',20],['staff-windup',-.6,'staff',13],['staff-impact',-.6,'staff',18]]){
  const model=applyArmPose(structuredClone(source),kind,t,20),groups=new Map(model.groups.map(g=>[g.uuid,g])),elements=new Map(model.elements.map(c=>[c.uuid,c]));
  const faces=[];
  const project=p=>{
    const x=-p[0]*Math.cos(yaw)+p[2]*Math.sin(yaw),z=p[0]*Math.sin(yaw)+p[2]*Math.cos(yaw);
    const tilt=.08;
    return [x,-p[1]*Math.cos(tilt)+z*Math.sin(tilt),z*Math.cos(tilt)+p[1]*Math.sin(tilt)];
  };
  function visit(node,chain=[]){
    if(typeof node==='string'){
      const c=elements.get(node);if(!c||c.visibility===false||c.export===false)return;
      const [x,y,z]=c.from,[X,Y,Z]=c.to;
      const points=[[x,y,z],[X,y,z],[X,Y,z],[x,Y,z],[x,y,Z],[X,y,Z],[X,Y,Z],[x,Y,Z]].map(p=>{
        for(const g of [...chain].reverse())p=rotate(p,g.origin,g.rotation);
        return project(p);
      });
      for(const [face,ids,shade] of [['north',[0,1,2,3],1],['south',[5,4,7,6],.75],['east',[1,5,6,2],.85],['west',[4,0,3,7],.85],['up',[3,2,6,7],1.08],['down',[4,5,1,0],.65]]){
        const uv=c.faces[face].uv,[u,v,U,V]=uv;
        faces.push({points:ids.map(i=>points[i]),uv:[[u,V],[U,V],[U,v],[u,v]],shade});
      }
      return;
    }
    const g=groups.get(node.uuid);if(g.visibility===false||g.export===false)return;
    node.children.forEach(c=>visit(c,[...chain,g]));
  }
  model.outliner.forEach(n=>visit(n));
  const points=faces.flatMap(f=>f.points),min=[0,1].map(i=>Math.min(...points.map(p=>p[i]))),max=[0,1].map(i=>Math.max(...points.map(p=>p[i])));
  const width=900,height=1000,scale=Math.min((width-100)/(max[0]-min[0]),(height-100)/(max[1]-min[1]));
  const image=new PNG({width,height}),depth=new Float64Array(width*height).fill(Infinity);
  for(let i=0;i<width*height;i++)image.data.set([16,24,34,255],i*4);
  function tri(a,b,c,ta,tb,tc,shade){
    const D=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1]);if(Math.abs(D)<1e-6)return;
    for(let y=Math.max(0,Math.floor(Math.min(a[1],b[1],c[1])));y<=Math.min(height-1,Math.ceil(Math.max(a[1],b[1],c[1])));y++)
    for(let x=Math.max(0,Math.floor(Math.min(a[0],b[0],c[0])));x<=Math.min(width-1,Math.ceil(Math.max(a[0],b[0],c[0])));x++){
      const u=((b[1]-c[1])*(x+.5-c[0])+(c[0]-b[0])*(y+.5-c[1]))/D;
      const v=((c[1]-a[1])*(x+.5-c[0])+(a[0]-c[0])*(y+.5-c[1]))/D,w=1-u-v;
      if(u<0||v<0||w<0)continue;
      const i=y*width+x,z=u*a[2]+v*b[2]+w*c[2];if(z>=depth[i])continue;
      const tx=Math.max(0,Math.min(atlas.width-1,Math.floor(u*ta[0]+v*tb[0]+w*tc[0])));
      const ty=Math.max(0,Math.min(atlas.height-1,Math.floor(u*ta[1]+v*tb[1]+w*tc[1])));
      const j=(ty*atlas.width+tx)*4;if(!atlas.data[j+3])continue;
      depth[i]=z;
      for(let k=0;k<3;k++)image.data[i*4+k]=Math.min(255,Math.round(atlas.data[j+k]*shade));
    }
  }
  for(const face of faces){
    const p=face.points.map(p=>[(p[0]-(min[0]+max[0])/2)*scale+width/2,(p[1]-(min[1]+max[1])/2)*scale+height/2,p[2]]);
    for(const [a,b,c] of [[0,1,2],[0,2,3]])tri(p[a],p[b],p[c],face.uv[a],face.uv[b],face.uv[c],face.shade);
  }
  const file=path.join(root,'build/model-preview/keeper-wraith-'+view+'.png');
  fs.mkdirSync(path.dirname(file),{recursive:true});fs.writeFileSync(file,PNG.sync.write(image));console.log(file);
}
