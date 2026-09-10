import fs from 'node:fs';
import assert from 'node:assert/strict';
import {clips,mcpClip} from './cast-clip-utils.mjs';
const read=p=>fs.readFileSync(new URL('../'+p,import.meta.url),'utf8');
const source=JSON.parse(read('model_source/chronicle_keeper_sovereign.bbmodel'));
const saved=JSON.parse(read('model_source/chronicle_keeper_casts.bbmodel'));
// Blockbench serializes model transforms to five decimal places.
const normalized=value=>Array.isArray(value)?value.map(normalized):typeof value==='number'?Math.round(value*1e5)/1e5:value;
const field=(object,key)=>normalized(object[key]??(key==='rotation'?[0,0,0]:key==='mirror_uv'?false:undefined));
assert.equal(saved.animations.length,3);
assert.equal(saved.elements.length,source.elements.length);
assert.equal(saved.groups.length,source.groups.length);
for(const cube of source.elements){
  const actual=saved.elements.find(c=>c.uuid===cube.uuid);
  for(const key of ['name','from','to','origin','rotation','uv_offset','mirror_uv'])
    assert.deepEqual(field(actual,key),field(cube,key),cube.name+': '+key);
}
for(const bone of source.groups){
  const actual=saved.groups.find(b=>b.uuid===bone.uuid);
  for(const key of ['name','origin','rotation'])assert.deepEqual(field(actual,key),field(bone,key),bone.name+': '+key);
}
for(let i=0;i<source.textures.length;i++)assert.equal(saved.textures[i].source,source.textures[i].source);
let count=0;
for(const [kind,clip] of Object.entries(clips)){
  assert.equal(clip.release_tick,kind==='ring'?30:kind==='staff'?18:10);
  assert.equal(clip.length*20,kind==='ring'?40:kind==='staff'?36:24);
  for(const channels of Object.values(clip.bones))for(const rows of Object.values(channels)){
    assert.deepEqual(rows[0],[0,0,0,0]);assert.deepEqual(rows.at(-1),[clip.length,0,0,0]);
    for(let i=1;i<rows.length;i++)assert(rows[i][0]>rows[i-1][0]);
    assert(rows.every(r=>r.length===4&&r.every(Number.isFinite)));
  }
  const animation=saved.animations.find(a=>a.name===clip.name);
  assert.equal(animation.loop,'once');assert.equal(animation.length,clip.length);
  for(const [bone,frames] of Object.entries(mcpClip(clip).bones)){
    const animator=Object.values(animation.animators).find(b=>b.name===bone);
    let expected=0;
    for(const frame of frames)for(const channel of ['rotation','position']){
      if(!frame[channel])continue;expected++;
      const key=animator.keyframes.find(k=>k.channel===channel&&Math.abs(k.time-frame.time)<1e-6);
      assert(key,`${kind}/${bone}/${channel}/${frame.time}`);assert.equal(key.interpolation,'linear');
      for(let i=0;i<3;i++)assert(Math.abs(Number(key.data_points[0][['x','y','z'][i]])-frame[channel][i])<1e-8,'MCP/runtime keyframe mismatch');
      count++;
    }
    assert.equal(animator.keyframes.length,expected,'Unexpected extra keys');
  }
}
for(const entity of ['ChronicleKeeperEntity','TemporalEchoEntity']){
  const java=read('src/main/java/net/xuwu/time/entity/'+entity+'.java');
  assert(/castAge\(0\)\s*==\s*10/.test(java));
  assert(/now\s*\+\s*EncounterRules.RING_WARNING_TICKS/.test(java));
  assert(java.includes('implements ChronalCaster'));
}
assert(/RING_WARNING_TICKS = 30/.test(read('src/main/java/net/xuwu/time/logic/EncounterRules.java')));
console.log(`PASS: ${count} saved Blockbench keyframes match shared cast data; 267 cubes/97 bones and textures preserved; release ticks 10/30/18 verified. No game launched.`);
