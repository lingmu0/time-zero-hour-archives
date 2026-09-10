import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {exportGeometry,modelExports} from './export-bbmodel.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..'),res=path.join(root,'src/main/resources');
const all=[];
function walk(dir){for(const entry of fs.readdirSync(dir,{withFileTypes:true})){const p=path.join(dir,entry.name);entry.isDirectory()?walk(p):all.push(p);}}
walk(res);
const docs=new Map(all.filter(p=>p.endsWith('.json')||p.endsWith('.mcmeta')).map(p=>[path.relative(res,p).replaceAll('\\','/'),JSON.parse(fs.readFileSync(p,'utf8'))]));
const content=fs.readFileSync(path.join(root,'src/main/java/net/xuwu/time/registry/TimeContent.java'),'utf8');
const items=new Set([...content.matchAll(/(?:item|ITEMS\.register|BLOCKS\.register)\("([a-z0-9_]+)"/g)].map(m=>'time:'+m[1]));
const zh=docs.get('assets/time/lang/zh_cn.json');
// Non-launch regression checks for the combat prompt/swap patch.
for (const language of ['zh_cn','en_us']) {
  const lang=docs.get('assets/time/lang/'+language+'.json');
  for (const [i,label] of ['西北 · 记忆相位','东北 · 位置相位','西南 · 年龄相位','东南 · 未来相位'].entries())
    assert.equal(lang['present_phase.time.'+i],label,'Present direction/phase mismatch');
  assert(!('message.time.ring_warning' in lang),'Ring broadcast text should be removed');
}
const keeperSource=fs.readFileSync(path.join(root,'src/main/java/net/xuwu/time/entity/ChronicleKeeperEntity.java'),'utf8');
assert(!keeperSource.includes('announce("ring_warning")'),'Ring broadcast is still active');
assert(keeperSource.includes('Component.translatable("present_phase.time." + safeQuadrant())'),'Present prompt must use direction plus phase');
assert(keeperSource.includes('Component.translatable("present_phase.time." + q)'),'Future debt prompt must use direction plus phase');
for (const name of ['ChronicleKeeperEntity','TemporalEchoEntity']) {
  const java=fs.readFileSync(path.join(root,'src/main/java/net/xuwu/time/entity/'+name+'.java'),'utf8');
  assert(java.includes('super.lerpTo(x, y, z, yaw, pitch, snap ? 0 : steps)')&&java.includes('setDeltaMovement(Vec3.ZERO); setOldPosAndRot();'),'Swap must reset interpolation and frame history: '+name);
}
let refs=0;
function exists(p){assert(fs.existsSync(path.join(res,p)),'Missing resource: '+p);refs++;}
function item(id){if(id.startsWith('time:'))assert(items.has(id),'Unregistered item '+id);refs++;}
function tag(id,kind='item'){if(id.startsWith('time:'))exists('data/time/tags/'+kind+'/'+id.slice(5)+'.json');}
function model(id){if(id.startsWith('time:'))exists('assets/time/models/'+id.slice(5)+'.json');}
function validateTree(value,filename){
  if(Array.isArray(value)){value.forEach(v=>validateTree(v,filename));return;}
  if(!value||typeof value!=='object')return;
  if(value.translate?.startsWith('time.')||value.translate?.includes('.time.'))assert(value.translate in zh,'Missing translation '+value.translate);
  if(value.type==='minecraft:item')item(value.name);
  if(typeof value.item==='string')item(value.item);
  if(typeof value.tag==='string')tag(value.tag);
  if(value.model)model(value.model);
  if(filename.includes('/models/')){
    if(value.parent)model(value.parent);
    if(value.textures)for(const texture of Object.values(value.textures))if(typeof texture==='string'&&texture.startsWith('time:'))exists('assets/time/textures/'+texture.slice(5)+'.png');
  }
  for(const v of Object.values(value))validateTree(v,filename);
}
for(const [filename,doc] of docs){
  validateTree(doc,filename);
  if(filename.startsWith('data/time/recipe/')){
    if(doc.result?.id)item(doc.result.id);
    if(doc.type==='time:research'){
      for(const key of ['evidence','catalyst','reference','result'])assert(doc[key],filename+' lacks '+key);
      assert(doc.duration>0&&doc.duration<=72000,'Invalid research duration');
      assert(doc.result.count>0,'Empty research result');
    }
  }
  if(filename.includes('/blockstates/'))assert(Object.keys(doc.variants??{}).length>0,'Empty blockstate');
  if(filename.includes('/worldgen/structure_set/')){
    assert(doc.placement.spacing>doc.placement.separation&&doc.placement.separation>0,'Invalid spacing');
    for(const value of doc.structures)exists('data/time/worldgen/structure/'+value.structure.slice(5)+'.json');
  }
  if(filename.startsWith('data/time/worldgen/structure/')){
    assert(doc.type==='time:time_ruin'&&doc.variant>=0&&doc.variant<=2,'Invalid custom structure');
    exists('data/time/tags/worldgen/biome/'+doc.biomes.slice('#time:'.length)+'.json');
    assert(['surface_structures','underground_structures'].includes(doc.step),'Invalid generation stage');
  }
  if(filename.startsWith('data/time/tags/item/')){
    for(const value of doc.values){
      if(typeof value==='object'&&value.id.startsWith('kubejs:'))assert(value.required===false,'Optional KubeJS content became a hard dependency');
      else if(typeof value==='string'&&!value.startsWith('#'))item(value);
    }
  }
}
for(const id of items){
  const name=id.slice(5);exists('assets/time/models/item/'+name+'.json');
  assert(('item.time.'+name in zh)||('block.time.'+name in zh),'Missing item name '+name);
}
const kinds=['day_sequence','shadow_dials','frozen_records','archive_order','mirror_path','delay_bells','archive_guardian','phase_seals','boss_arena'];
for(const kind of kinds){
  assert('clue.time.'+kind in zh,'Missing in-world clue '+kind);
  exists('data/time/advancement/puzzles/'+kind+'.json');
}
const counts={day_sequence:4,shadow_dials:4,frozen_records:1,archive_order:6,mirror_path:6,delay_bells:3,phase_seals:3,boss_arena:5};
for(const [kind,count] of Object.entries(counts))for(let i=0;i<count;i++)assert('node.time.'+kind+'.'+i in zh,'Missing node clue '+kind+' '+i);
for(const p of all.filter(p=>p.endsWith('.png'))){
  const data=fs.readFileSync(p);assert.equal(data.subarray(0,8).toString('hex'),'89504e470d0a1a0a','Invalid PNG '+p);
  assert(data.readUInt32BE(16)>0&&data.readUInt32BE(20)>0,'Invalid image size');
}
let cubeCount=0;
for(const model of modelExports){
  const bb=path.join(root,'model_source',model.source);
  exportGeometry(bb,path.join(root,'src/main/java/net/xuwu/time/client',model.java),true,model);
  const rig=JSON.parse(fs.readFileSync(bb,'utf8'));cubeCount+=rig.elements.length;
  for(const cube of rig.elements){
    assert(cube.from.every((v,i)=>v<cube.to[i]),'Degenerate model cube '+cube.name);
    const [w,h,d]=cube.to.map((v,i)=>v-cube.from[i]);
    const uv=cube.uv_offset??[0,0];
    assert(uv.every(v=>v>=0)&&uv[0]+2*(w+d)<=rig.resolution.width&&uv[1]+h+d<=rig.resolution.height,'UV outside atlas '+cube.name);
  }
}
assert.notDeepEqual(fs.readFileSync(path.join(res,'assets/time/textures/entity/archive_scribe.png')),fs.readFileSync(path.join(res,'assets/time/textures/entity/chronicle_keeper.png')),'Scribe and keeper must not share a texture');
assert(!fs.readFileSync(path.join(root,'src/main/java/net/xuwu/time/TimeMod.java'),'utf8').includes('net.minecraft.client'),'Client code leaked into common entry point');
console.log('PASS: '+docs.size+' JSON documents; '+refs+' references; '+items.size+' item models; nine clue sets; '+cubeCount+' cubes in '+modelExports.length+' distinct rigs; geometry, textures and emissive mask match.');
