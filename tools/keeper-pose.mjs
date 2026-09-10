import {clips,sample} from './cast-clip-utils.mjs';
// Uses the same authored keyframes as the generated Java runtime player.
export function applyArmPose(model,kind='idle',t=0,age=0){
  const g=new Map(model.groups.map(g=>[g.name,g])),start=new Map(model.groups.map(g=>[g.name,[...g.rotation]]));
  const add=(name,axis,radians)=>g.get(name).rotation[axis]+=radians*180/Math.PI;
  add('staff_arm',0,.045+Math.sin(age*.04)*.035);
  add('book_arm',0,.10+Math.sin(age*.04+1)*.04);
  const translations=new Map(),clip=clips[kind];
  if(clip&&t>=0&&t<clip.length*20){
    for(const [bone,channels] of Object.entries(clip.bones)){
      if(channels.rotation)g.get(bone).rotation=g.get(bone).rotation.map((v,i)=>v+sample(channels.rotation,t/20)[i]);
      if(channels.position)translations.set(g.get(bone).uuid,sample(channels.position,t/20));
    }
  }
  for(const [hand,arm,forearm] of [['staff_hand','staff_arm','forearm_-1'],['book_hand','book_arm','forearm_1']])
    g.get(hand).rotation[0]-=g.get(arm).rotation[0]-start.get(arm)[0]+g.get(forearm).rotation[0]-start.get(forearm)[0];
  // A ModelPart translation moves its entire subtree in the parent's space.
  const bookId=g.get('held_book').uuid,cubes=new Map(model.elements.map(c=>[c.uuid,c]));
  const hover=translations.get(bookId)??[0,0,0];hover[2]-=Math.sin(age*.09)*.3;translations.set(bookId,hover);
  function move(n,inherited=[0,0,0]){
    const local=translations.get(n.uuid)??[0,0,0],delta=inherited.map((v,i)=>v+local[i]);
    const bone=model.groups.find(v=>v.uuid===n.uuid);
    bone.origin=bone.origin.map((v,i)=>v+delta[i]);
    for(const c of n.children){
      if(typeof c!=='string')move(c,delta);
      else for(const key of ['from','to','origin'])cubes.get(c)[key]=cubes.get(c)[key].map((v,i)=>v+delta[i]);
    }
  }
  model.outliner.forEach(n=>move(n));
  return model;
}
export function rotate(point,origin,r){
  let [x,y,z]=point.map((v,i)=>v-origin[i]);
  for(let i=0;i<3;i++){const a=r[i]*Math.PI/180,c=Math.cos(a),s=Math.sin(a);
    if(i===0)[y,z]=[y*c-z*s,y*s+z*c];if(i===1)[x,z]=[x*c+z*s,-x*s+z*c];if(i===2)[x,y]=[x*c-y*s,x*s+y*c];}
  return [x+origin[0],y+origin[1],z+origin[2]];
}
export function worldBoxes(model){
  const groups=new Map(model.groups.map(g=>[g.uuid,g])),cubes=new Map(model.elements.map(c=>[c.uuid,c])),result=[];
  function visit(n,chain=[]){
    if(typeof n==='string'){
      const c=cubes.get(n);if(!c||c.visibility===false||c.export===false)return;
      const points=[];
      for(const x of [c.from[0],c.to[0]])for(const y of [c.from[1],c.to[1]])for(const z of [c.from[2],c.to[2]]){
        let p=[x,y,z];for(const g of [...chain].reverse())p=rotate(p,g.origin,g.rotation);points.push(p);
      }
      result.push({name:c.name,chain:chain.map(g=>g.name),points});
    }else{
      const g=groups.get(n.uuid);if(g.visibility===false||g.export===false)return;
      n.children.forEach(c=>visit(c,[...chain,g]));
    }
  }
  model.outliner.forEach(n=>visit(n));return result;
}
const sub=(a,b)=>a.map((v,i)=>v-b[i]),dot=(a,b)=>a.reduce((v,n,i)=>v+n*b[i],0);
const cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];
export function intersects(a,b){
  // Broad-phase then the 15 separating axes for two oriented cuboids.
  for(let i=0;i<3;i++)if(Math.max(...a.points.map(p=>p[i]))<=Math.min(...b.points.map(p=>p[i]))+.02||
    Math.max(...b.points.map(p=>p[i]))<=Math.min(...a.points.map(p=>p[i]))+.02)return false;
  const axes=box=>[4,2,1].map(i=>sub(box.points[i],box.points[0]));
  const aa=axes(a),bb=axes(b);
  for(const n of [...aa,...bb,...aa.flatMap(x=>bb.map(y=>cross(x,y)))]){
    const length=Math.hypot(...n);if(length<1e-7)continue;
    const axis=n.map(v=>v/length),pa=a.points.map(p=>dot(p,axis)),pb=b.points.map(p=>dot(p,axis));
    if(Math.min(Math.max(...pa),Math.max(...pb))-Math.max(Math.min(...pa),Math.min(...pb))<=.02)return false;
  }
  return true;
}
