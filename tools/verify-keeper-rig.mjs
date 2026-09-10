import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const model=JSON.parse(read('model_source/chronicle_keeper_sovereign.bbmodel'));
const before=JSON.parse(read('model_source/chronicle_keeper_sovereign-before-0.1.9.bbmodel'));
const groups=new Map(model.groups.map(g=>[g.uuid,g])),byName=new Map(model.groups.map(g=>[g.name,g]));
const owner=new Map(),parent=new Map(),seen=new Set();
function visit(n,p){
  assert(groups.has(n.uuid),'Unresolved group');assert(!seen.has(n.uuid),'Duplicate group reference');seen.add(n.uuid);
  if(p)parent.set(n.uuid,p);
  for(const c of n.children){
    if(typeof c==='string'){assert(!owner.has(c),'Cube has two owners');owner.set(c,n.uuid);}
    else visit(c,n.uuid);
  }
}
model.outliner.forEach(n=>visit(n));
assert.equal(owner.size,model.elements.length);assert.equal(seen.size,model.groups.length);
for(const c of model.elements)assert(owner.has(c.uuid),'Orphan cube '+c.name);
const parentName=name=>groups.get(parent.get(byName.get(name).uuid))?.name;
assert.equal(parentName('chronostaff'),'staff_hand');
assert.equal(parentName('held_book'),'book_hand');
assert.equal(parentName('staff_hand'),'forearm_-1');
assert.equal(parentName('book_hand'),'forearm_1');
assert.equal(parentName('ghost_mid'),'ghost_tail');assert.equal(parentName('ghost_tip'),'ghost_mid');
assert(!model.elements.some(c=>/^(boot_|gilded_toe_|tasset_core_)/.test(c.name)));
assert(model.elements.some(c=>c.name==='spectral_tip'));
assert.deepEqual(model.textures.map(t=>t.source),before.textures.map(t=>t.source),'Original atlases must remain intact');
const java=read('src/main/java/net/xuwu/time/client/KeeperModel.java');
assert(!/\b(?:staff|book)\.(?:x|y|z|xRot|yRot|zRot)\b/.test(java),'Props must not drift independently of hands');
assert(java.includes('ModelPart::resetPose')&&java.includes('falseBody ? -1 : 1'),'Pose reset/fake reverse hands lost');
for(const name of ['ghost_tail','ghost_mid','ghost_tip','forearm_-1','forearm_1'])
  assert(java.includes('getChild("'+name+'")'),'Animation bone missing '+name);

// The staff grip touches its palm. The tome intentionally floats over the
// other open palm, while sharing its wrist transform.
const rotate=(p,o,r)=>{
  let [x,y,z]=p.map((v,i)=>v-o[i]);
  for(let i=0;i<3;i++){const a=r[i]*Math.PI/180,c=Math.cos(a),s=Math.sin(a);
    if(i===0)[y,z]=[y*c-z*s,y*s+z*c];if(i===1)[x,z]=[x*c+z*s,-x*s+z*c];if(i===2)[x,y]=[x*c-y*s,x*s+y*c];}
  return [x+o[0],y+o[1],z+o[2]];
};
assert.deepEqual(byName.get('held_book').rotation,[0,0,0]);
assert(model.elements.filter(c=>c.name.startsWith('open_finger_1_')).length===3,'Open palm missing');
assert(java.includes('hoveringBook.z -= Mth.sin(age * .09f) * .3f'),'Bounded book hover missing');
const palm=model.elements.find(c=>c.name==='gripping_palm_-1');
const staffContact=[-10,17.5,-1.3];
assert(staffContact.every((v,i)=>v>=palm.from[i]-.01&&v<=palm.to[i]+.01),'Staff grip no longer touches palm');
let poses=0;
function world(point,boneName,t){
  let uuid=byName.get(boneName).uuid,p=[...point];
  while(uuid){
    const g=groups.get(uuid),r=[...g.rotation];
    if(g.name==='staff_arm'||g.name==='book_arm'){
      r[0]+=t*2;r[1]+=t-20;r[2]+=Math.sin(t)*25;
    }
    if(g.name==='forearm_-1'||g.name==='forearm_1')r[0]+=Math.cos(t)*18;
    p=rotate(p,g.origin,r);uuid=parent.get(uuid);
  }
  return p;
}
for(let t=0;t<=40;t++){
  for(const [prop,point,hand,contact] of [
    ['chronostaff',staffContact,'staff_hand',staffContact]
  ]){
    const a=world(point,prop,t),b=world(contact,hand,t);
    assert(Math.hypot(...a.map((v,i)=>v-b[i]))<1e-9);poses++;
  }
}
console.log('PASS: '+model.elements.length+' cubes / '+model.groups.length+' bones; complete hierarchy; no boots; shared forearm bindings and grip contacts; original atlases, reset and reverse-pointer guards. '+poses+' common-transform contact checks (not an in-game animation test).');
