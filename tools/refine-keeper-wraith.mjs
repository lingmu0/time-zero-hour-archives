import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';

// One-time, scoped migration of the artist-edited Blockbench 5 rig.
// Reuses the existing UV atlas without repainting or repacking it.
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const file=path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel');
const model=JSON.parse(fs.readFileSync(file,'utf8'));
if(model.groups.some(g=>g.name==='ghost_tail'))throw new Error('Wraith migration already applied; edit the resulting model instead.');
const backup=path.join(root,'model_source/chronicle_keeper_sovereign-before-0.1.9.bbmodel');
fs.copyFileSync(file,backup,fs.constants.COPYFILE_EXCL);
const groups=new Map(model.groups.map(g=>[g.uuid,g]));
const cubes=new Map(model.elements.map(c=>[c.uuid,c]));
const templates=new Map(model.elements.map(c=>[c.name,structuredClone(c)]));
const nodes=new Map();
function index(n){nodes.set(groups.get(n.uuid).name,n);for(const c of n.children)if(typeof c!=='string')index(c);}
model.outliner.forEach(index);
const group=name=>groups.get(nodes.get(name).uuid);
const guid=name=>{const h=crypto.createHash('md5').update('time:wraith:'+name).digest('hex');return [h.slice(0,8),h.slice(8,12),h.slice(12,16),h.slice(16,20),h.slice(20)].join('-');};
function removeCubes(test){
  const removed=new Set(model.elements.filter(test).map(c=>c.uuid));
  model.elements=model.elements.filter(c=>!removed.has(c.uuid));
  for(const n of nodes.values())n.children=n.children.filter(c=>typeof c!=='string'||!removed.has(c));
}
function bone(name,parent,origin,rotation=[0,0,0]){
  const uuid=guid(name),g={...structuredClone(group('root')),name,uuid,origin,rotation};
  model.groups.push(g);groups.set(uuid,g);
  const n={uuid,isOpen:true,children:[]};nodes.get(parent).children.push(n);nodes.set(name,n);
  return name;
}
function box(parent,name,from,size,template){
  const src=templates.get(template),c=structuredClone(src);
  c.uuid=guid(name);c.name=name;c.from=from;c.to=from.map((v,i)=>v+size[i]);
  c.origin=[...group(parent).origin];c.rotation=[0,0,0];c.inflate=0;
  const [u,v]=c.uv_offset,[w,h,d]=size;
  const [sw,sh,sd]=src.to.map((n,i)=>n-src.from[i]);
  if(2*(w+d)>2*(sw+sd)+.001||h+d>sh+sd+.001)throw new Error('UV patch too small: '+name);
  const uv={north:[u+d,v+d,u+d+w,v+d+h],east:[u,v+d,u+d,v+d+h],
    south:[u+2*d+w,v+d,u+2*d+2*w,v+d+h],west:[u+d+w,v+d,u+2*d+w,v+d+h],
    up:[u+d+w,v+d,u+d,v],down:[u+d+2*w,v,u+d+w,v+d]};
  c.faces=Object.fromEntries(Object.entries(uv).map(([face,uv])=>[face,{uv,texture:0}]));
  model.elements.push(c);cubes.set(c.uuid,c);nodes.get(parent).children.push(c.uuid);
}
function translate(name,delta){
  function visit(n){
    const g=groups.get(n.uuid);g.origin=g.origin.map((v,i)=>v+delta[i]);
    for(const child of n.children){
      if(typeof child!=='string'){visit(child);continue;}
      const c=cubes.get(child);
      for(const key of ['from','to','origin'])if(c[key])c[key]=c[key].map((v,i)=>v+delta[i]);
    }
  }
  visit(nodes.get(name));
}
function reparent(name,parent){
  const n=nodes.get(name);
  for(const p of nodes.values())p.children=p.children.filter(c=>c!==n);
  nodes.get(parent).children.push(n);
}

// Props and gripping fingers share the forearm transform, including every cast.
translate('chronostaff',[6,-4.5,-2.3]);
group('chronostaff').origin=[-10,17.5,-2.3];group('chronostaff').rotation=[0,0,0];
reparent('chronostaff','forearm_-1');
group('forearm_-1').rotation=[6,0,20];
translate('floating_book',[-5,-3,1.5]);
group('floating_book').origin=[10,17,-2];group('floating_book').rotation=[-25,0,0];
reparent('floating_book','forearm_1');
group('floating_book').name='held_book';
group('forearm_1').rotation=[55,0,0];
removeCubes(c=>c.name.startsWith('finger_')||c.name.startsWith('palm_'));
for(const s of [-1,1]){
  const arm='forearm_'+s;
  box(arm,'gripping_palm_'+s,[s*10-1.8,16,-1.3],[3.6,3,2.3],'palm_'+s);
  for(let f=0;f<3;f++)
    box(arm,'curled_finger_'+s+'_'+f,[s*10-1.3,16.1+f*.9,-3.7],[2.6,.7,1.2],'palm_'+s);
  box(arm,'gripping_thumb_'+s,[s*10+(s<0?1:-1.7),16.5,-3.1],[.7,2,2],'palm_'+s);
}

// Remove the two boots and long leg-like armor. Retain short waist plates.
removeCubes(c=>/^(boot_|gilded_toe_|tasset_|cape_)/.test(c.name));
for(const s of [-1,1]){
  const p='front_tasset_'+s;
  box(p,'waist_plate_'+s,[s*4-2.5,17,-4],[5,6,2],'tasset_core_'+s);
  box(p,'waist_plate_edge_'+s,[s*4-2.5,17,-4.2],[5,.8,2.2],'tasset_hem_'+s);
  box(p,'waist_plate_seal_'+s,[s*4-.8,19,-4.45],[1.6,.7,.4],'tasset_rune_'+s+'_1');
}
for(let i=0;i<5;i++){
  const p='mantle_panel_'+i,x=(i-2)*4;
  box(p,'ragged_mantle_top_'+i,[x-2.4,17+Math.abs(i-2),4],[4.8,8-Math.abs(i-2),2],'cape_panel_'+i);
  box(p,'ragged_mantle_point_'+i,[x-1.5,14+Math.abs(i-2),4],[3,4,1.6],'cape_panel_'+i);
}
bone('ghost_tail','root',[0,23,1]);
box('ghost_tail','spectral_waist',[-4,17,-2],[8,6,6],'mechanical_ribcage');
box('ghost_tail','spectral_taper_upper',[-3.2,12,-1.6],[6.4,6,5.2],'mechanical_ribcage');
bone('ghost_mid','ghost_tail',[0,14,1],[0,0,12]);
box('ghost_mid','spectral_taper_middle',[-2.3,7,-.8],[4.6,7,4],'mechanical_ribcage');
bone('ghost_tip','ghost_mid',[0,8,1],[0,0,-40]);
box('ghost_tip','spectral_curl',[-.8,3,0],[2.8,5.5,2.8],'cape_panel_2');
box('ghost_tip','spectral_tip',[.6,1.4,.7],[1.6,2.4,1.8],'cape_panel_2');
box('ghost_tip','spectral_hook',[1.5,1.5,1],[2.2,1,1.2],'cape_panel_2');
// Unequal tapering ribbons break the solid lower silhouette; no paired legs.
for(let i=0;i<5;i++){
  const x=(i-2)*2.15,front=i%2===0,z=front?-3:2.7;
  const p=bone('ghost_ribbon_'+i,'ghost_tail',[x,22,z],[i%2?8:-5,0,(i-2)*-8]);
  box(p,'soul_shroud_upper_'+i,[x-1.5,14+i%2, z],[3,8-i%2,1.6],'cape_panel_'+i);
  box(p,'soul_shroud_middle_'+i,[x-1.1,8+i%3,z+.4],[2.2,6.5,1.3],'cape_panel_'+i);
  box(p,'soul_shroud_point_'+i,[x-.45,5+i%3,z+.7],[.9,3.5,.7],'cape_panel_'+i);
  box(p,'soul_streak_'+i,[x-.25,12+i%3,z-.15],[.5,3,.5],'gauntlet_light_1');
}
for(let i=0;i<3;i++){
  const p=bone('ghost_ember_'+i,'ghost_tail',[(i-1)*3.5,5+i*2,-1],[0,0,20+i*25]);
  box(p,'detached_soul_'+i,[(i-1)*3.5-.3,4+i*2,-1],[.6,1.2,.5],'gauntlet_light_1');
}
model.name='Chronicle Keeper - Wraith Sovereign';
fs.writeFileSync(file,JSON.stringify(model,null,2)+'\n');
console.log('Migrated keeper only:',model.elements.length,'cubes;',model.groups.length,'bones. Backup:',backup);
