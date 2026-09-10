import fs from 'node:fs';
import {randomUUID} from 'node:crypto';
import {clips,mcpClip} from './cast-clip-utils.mjs';
const file=new URL('../model_source/chronicle_keeper_casts.bbmodel',import.meta.url);
const backup=new URL('../model_source/chronicle_keeper_casts-before-0.1.12.bbmodel',import.meta.url);
if(!fs.existsSync(backup))fs.copyFileSync(file,backup);
const model=JSON.parse(fs.readFileSync(file,'utf8'));
const old=model.animations;
model.animations=Object.values(clips).map(clip=>{
  const animation={...(old.find(a=>a.name===clip.name)??{uuid:randomUUID(),name:clip.name,loop:'once',snapping:20}),length:clip.length,animators:{}};
  for(const [name,frames] of Object.entries(mcpClip(clip).bones)){
    const bone=model.groups.find(b=>b.name===name);if(!bone)throw Error('Missing '+name);
    const keys=[];
    for(const frame of frames)for(const channel of ['rotation','position'])if(frame[channel])keys.push({channel,uuid:randomUUID(),time:frame.time,interpolation:'linear',data_points:[Object.fromEntries(['x','y','z'].map((axis,i)=>[axis,String(frame[channel][i])]))]});
    animation.animators[bone.uuid]={name,type:'bone',rotation_global:false,keyframes:keys};
  }
  return animation;
});
fs.writeFileSync(file,JSON.stringify(model));
console.log('Saved local Blockbench project with '+model.animations.length+' casts (offline export, not an MCP capture).');
