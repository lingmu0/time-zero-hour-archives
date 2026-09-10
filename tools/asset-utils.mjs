import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

function crc32(data) {
  let crc = 0xffffffff;
  for (const byte of data) {
    crc ^= byte;
    for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
  }
  return (crc ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const name = Buffer.from(type), result = Buffer.alloc(data.length + 12);
  result.writeUInt32BE(data.length); name.copy(result, 4); data.copy(result, 8);
  result.writeUInt32BE(crc32(Buffer.concat([name, data])), data.length + 8);
  return result;
}
export function png(width, height, pixels) {
  const header = Buffer.alloc(13);
  header.writeUInt32BE(width); header.writeUInt32BE(height, 4); header[8] = 8; header[9] = 6;
  const rows = Buffer.alloc(height * (width * 4 + 1));
  for (let y = 0; y < height; y++) Buffer.from(pixels).copy(rows, y * (width * 4 + 1) + 1, y * width * 4, (y + 1) * width * 4);
  return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]), chunk('IHDR', header), chunk('IDAT', zlib.deflateSync(rows)), chunk('IEND', Buffer.alloc(0))]);
}
export function write(file, data) { fs.mkdirSync(path.dirname(file), {recursive:true}); fs.writeFileSync(file, data); }
export function pixels(width, height, fn) {
  const data = new Uint8Array(width * height * 4);
  for (let y=0;y<height;y++) for(let x=0;x<width;x++) data.set(fn(x,y), (y*width+x)*4);
  return data;
}
export const palette = [[22,43,52],[84,66,47],[211,161,68],[112,236,225]];
export function renderPreview(model, file) {
  const width=900,height=1000, data=pixels(width,height,()=>[12,21,29,255]), depth=new Float64Array(width*height).fill(Infinity);
  const elements=new Map(model.elements.map(e=>[e.uuid,e]));
  const rotate=(p,o,r)=>{
    let [x,y,z]=p.map((v,i)=>v-o[i]);
    for(let axis=0;axis<3;axis++) {
      const a=(r?.[axis]??0)*Math.PI/180,c=Math.cos(a),s=Math.sin(a);
      if(axis===0) [y,z]=[y*c-z*s,y*s+z*c];
      if(axis===1) [x,z]=[x*c+z*s,-x*s+z*c];
      if(axis===2) [x,y]=[x*c-y*s,x*s+y*c];
    }
    return [x+o[0],y+o[1],z+o[2]];
  };
  const project=p=>{
    const x=p[0]*.906+p[2]*.423,z=-p[0]*.423+p[2]*.906;
    return [450+x*15,850-(p[1]*.951-z*.309)*15,z*.951-p[1]*.309];
  };
  function triangle(a,b,c,color) {
    const minX=Math.max(0,Math.floor(Math.min(a[0],b[0],c[0]))),maxX=Math.min(width-1,Math.ceil(Math.max(a[0],b[0],c[0])));
    const minY=Math.max(0,Math.floor(Math.min(a[1],b[1],c[1]))),maxY=Math.min(height-1,Math.ceil(Math.max(a[1],b[1],c[1])));
    const denominator=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1]);
    if(Math.abs(denominator)<.001)return;
    for(let y=minY;y<=maxY;y++) for(let x=minX;x<=maxX;x++) {
      const u=((b[1]-c[1])*(x-c[0])+(c[0]-b[0])*(y-c[1]))/denominator;
      const v=((c[1]-a[1])*(x-c[0])+(a[0]-c[0])*(y-c[1]))/denominator,w=1-u-v;
      if(u<0||v<0||w<0)continue;
      const z=u*a[2]+v*b[2]+w*c[2],idx=y*width+x;
      if(z<depth[idx]) {depth[idx]=z;data.set([...color,255],idx*4);}
    }
  }
  function draw(cube, transforms) {
    const [x,y,z]=cube.from,[xx,yy,zz]=cube.to;
    let v=[[x,y,z],[xx,y,z],[xx,yy,z],[x,yy,z],[x,y,zz],[xx,y,zz],[xx,yy,zz],[x,yy,zz]];
    v=v.map(p=>{for(const t of [...transforms].reverse())p=rotate(p,t.origin,t.rotation);return project(p);});
    const base=palette[Math.min(3,Math.floor(cube.uv_offset[0]/64))];
    for(const [ids,shade] of [[[0,1,2,3],1],[[4,7,6,5],.65],[[0,4,5,1],.58],[[3,2,6,7],1.18],[[1,5,6,2],.8],[[0,3,7,4],.68]]) {
      const color=base.map(c=>Math.min(255,Math.round(c*shade)));
      triangle(v[ids[0]],v[ids[1]],v[ids[2]],color);triangle(v[ids[0]],v[ids[2]],v[ids[3]],color);
    }
  }
  function visit(node, transforms=[]) {
    if(typeof node==='string') {const e=elements.get(node);if(e)draw(e,transforms);return;}
    for(const child of node.children??[])visit(child,[...transforms,{origin:node.origin??[0,0,0],rotation:node.rotation??[0,0,0]}]);
  }
  model.outliner.forEach(x=>visit(x));
  write(file,png(width,height,data));
}
