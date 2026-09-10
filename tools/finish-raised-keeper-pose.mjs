import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const file=path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel');
const model=JSON.parse(fs.readFileSync(file,'utf8'));
const groups=new Map(model.groups.map(g=>[g.uuid,g])),byName=new Map(model.groups.map(g=>[g.name,g]));
if(byName.get('held_book').rotation[0]!==-25)throw new Error('Hovering-book migration already applied');
const cubes=new Map(model.elements.map(c=>[c.uuid,c]));
function uv(c){
  const [u,v]=c.uv_offset,[w,h,d]=c.to.map((n,i)=>n-c.from[i]);
  const faces={north:[u+d,v+d,u+d+w,v+d+h],east:[u,v+d,u+d,v+d+h],
    south:[u+2*d+w,v+d,u+2*d+2*w,v+d+h],west:[u+d+w,v+d,u+2*d+w,v+d+h],
    up:[u+d+w,v+d,u+d,v],down:[u+d+2*w,v,u+d+w,v+d]};
  c.faces=Object.fromEntries(Object.entries(faces).map(([name,uv])=>[name,{uv,texture:0}]));
}
// Recess the outer bracer lip by 1.5 px; preserve the hand/shaft grip.
const bracer=model.elements.find(c=>c.name==='gauntlet_shell_-1');
bracer.from[1]+=1.5;uv(bracer);
function visit(n,inside=false){
  const g=groups.get(n.uuid),active=inside||g.name==='held_book';
  if(active)g.origin=g.origin.map((v,i)=>v+[0,-3,-4][i]);
  for(const c of n.children){
    if(typeof c!=='string'){visit(c,active);continue;}
    if(active)for(const key of ['from','to','origin'])cubes.get(c)[key]=cubes.get(c)[key].map((v,i)=>v+[0,-3,-4][i]);
  }
}
model.outliner.forEach(n=>visit(n));
byName.get('held_book').rotation=[0,0,0];
// Solve the neutral wrist rotation so the open palm and book are horizontal.
const mul=(a,b)=>a.map(row=>b[0].map((_,j)=>row.reduce((s,v,k)=>s+v*b[k][j],0)));
const matrix=r=>{
  const [x,y,z]=r.map(v=>v*Math.PI/180),cx=Math.cos(x),sx=Math.sin(x),cy=Math.cos(y),sy=Math.sin(y),cz=Math.cos(z),sz=Math.sin(z);
  return mul(mul([[cz,-sz,0],[sz,cz,0],[0,0,1]],[[cy,0,sy],[0,1,0],[-sy,0,cy]]),[[1,0,0],[0,cx,-sx],[0,sx,cx]]);
};
const parent=mul(matrix(byName.get('book_arm').rotation),matrix(byName.get('forearm_1').rotation));
const inverse=parent[0].map((_,i)=>parent.map(row=>row[i])),wrist=mul(inverse,matrix([90,0,0]));
byName.get('book_hand').rotation=[Math.atan2(wrist[2][1],wrist[2][2]),Math.asin(-wrist[2][0]),Math.atan2(wrist[1][0],wrist[0][0])].map(v=>v*180/Math.PI);
for(let i=0;i<3;i++){
  const c=model.elements.find(c=>c.name==='curled_finger_1_'+i);
  c.name='open_finger_1_'+i;c.from=[8.6+i*.95,13.8,-1.3];c.to=[9.35+i*.95,16.3,-.3];uv(c);
}
const thumb=model.elements.find(c=>c.name==='gripping_thumb_1');
thumb.from=[7.7,15.3,-1.2];thumb.to=[8.6,17.4,-.2];uv(thumb);
fs.writeFileSync(file,JSON.stringify(model,null,2)+'\n');
console.log('Recessed staff bracer; opened palm and leveled hovering book. Wrist:',byName.get('book_hand').rotation);
