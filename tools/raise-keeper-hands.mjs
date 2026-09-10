import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const file=path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel');
const model=JSON.parse(fs.readFileSync(file,'utf8'));
if(model.groups.some(g=>g.name==='staff_hand'))throw new Error('Raised-hand migration already applied');
fs.copyFileSync(file,path.join(root,'model_source/chronicle_keeper_sovereign-before-0.1.10.bbmodel'),fs.constants.COPYFILE_EXCL);
const groups=new Map(model.groups.map(g=>[g.uuid,g])),nodes=new Map(),cubes=new Map(model.elements.map(c=>[c.uuid,c]));
function visit(n){nodes.set(groups.get(n.uuid).name,n);for(const c of n.children)if(typeof c!=='string')visit(c);}
model.outliner.forEach(visit);
const group=n=>groups.get(nodes.get(n).uuid);
group('staff_arm').rotation=[8,0,-10];
group('book_arm').rotation=[8,0,10];
group('forearm_-1').rotation=[82,0,10];
group('forearm_1').rotation=[78,0,-10];
for(const [s,name,rotation] of [[-1,'staff_hand',[-90,0,0]],[1,'book_hand',[-48,0,0]]]){
  const forearm=nodes.get('forearm_'+s),uuid=crypto.randomUUID();
  const g={...structuredClone(group('forearm_'+s)),uuid,name,origin:[s*10,18.5,-1],rotation,children:[]};
  const moved=forearm.children.filter(c=>typeof c!=='string'||/^(gripping_|curled_finger_)/.test(cubes.get(c).name));
  forearm.children=forearm.children.filter(c=>!moved.includes(c));
  forearm.children.push({uuid,isOpen:true,children:moved});model.groups.push(g);
}
fs.writeFileSync(file,JSON.stringify(model,null,2)+'\n');
console.log('Raised elbows, added independent wrist bones; meshes and atlases unchanged.');
