import fs from 'node:fs';
import assert from 'node:assert/strict';
import {applyArmPose,worldBoxes} from './keeper-pose.mjs';
import {clips,mcpClip} from './cast-clip-utils.mjs';
const read=p=>fs.readFileSync(new URL('../'+p,import.meta.url),'utf8');
const model=JSON.parse(read('model_source/chronicle_keeper_sovereign.bbmodel'));
const authored=JSON.parse(read('model_source/keeper-casts.json')).staff;
assert.equal(authored.interpolation,'monotone');
assert(!authored.bones.staff_hand.position,'Strike should not translate wrist sideways to avoid clipping');
let maxOffset=0;
for(let age=0;age<=200;age+=5){
  function center(t){const box=worldBoxes(applyArmPose(structuredClone(model),'staff',t,age)).find(c=>c.name==='staff_clock_core');return [0,1,2].map(k=>box.points.reduce((s,p)=>s+p[k],0)/8/16);}
  const high=center(13),hit=center(18);maxOffset=Math.max(maxOffset,Math.abs(hit[0]));
  assert(high[1]>4.3,'Staff head must be above the crown before the smash');
  assert(Math.abs(hit[0])<.025&&hit[2]<-1.8&&hit[1]<.75,'Impact must be centered, forward and below the raised staff');
}
const staff=mcpClip(clips.staff);
for(const frames of Object.values(staff.bones))for(let i=1;i<frames.length;i++){
  const a=frames[i-1],b=frames[i],dt=b.time-a.time;
  for(const channel of ['rotation','position'])if(a[channel]&&b[channel])for(let k=0;k<3;k++){
    assert(Number.isFinite(b[channel][k]));
    assert(Math.abs(b[channel][k]-a[channel][k])/dt<(channel==='rotation'?1600:50),'Discontinuous cast channel');
  }
}
const bolt=JSON.parse(read('model_source/chronal_bolt.bbmodel'));
assert.equal(bolt.elements.length,27);
assert(bolt.elements.every(c=>c.to.every((v,i)=>v>c.from[i])),'All projectile parts must have volume');
const renderer=read('src/main/java/net/xuwu/time/client/ChronalBoltRenderer.java');
assert(renderer.includes('rotationTo')&&renderer.includes('root.render')&&!renderer.includes('cameraOrientation'),'Projectile must use directional solid geometry');
const entity=read('src/main/java/net/xuwu/time/entity/ChronalBoltEntity.java');
assert(!entity.includes('AMETHYST_SHARD')&&!entity.includes('ItemSupplier'));
assert(entity.includes('SPEED = 1.05')&&entity.includes('getHitResultOnMoveVector'),'Fast projectile retains swept collision');
const dimension=JSON.parse(read('src/main/resources/data/time/dimension/challenge.json'));
assert.equal(dimension.generator.type,'minecraft:flat');assert.equal(dimension.generator.settings.biome,'minecraft:the_void');
assert.deepEqual(dimension.generator.settings.structure_overrides,[]);
const arena=read('src/main/java/net/xuwu/time/world/ChallengeArena.java');
assert(arena.includes('if(!level.dimension().equals(DIMENSION))return null'));
assert(arena.indexOf('if(!level.isEmptyBlock(at))return null')<arena.indexOf('level.setBlock('),'Never replace an occupied arena volume');
const item=read('src/main/java/net/xuwu/time/item/ChallengeSigilItem.java');
for(const key of ['Player.PERSISTED_NBT_TAG','safePosition','player.isShiftKeyDown()','Difficulty.PEACEFUL','player.fallDistance=0','startChallenge'])assert(item.includes(key),key);
assert(!item.includes('TimeProgress.award')&&!item.includes('ChronicleWorldData'),'Teleport must not award prerequisite progress');
const controller=read('src/main/java/net/xuwu/time/block/PuzzleControllerBlockEntity.java');
assert(controller.includes('!freeChallenge && !paid'),'Challenge item cannot be consumed as a key');
console.log('PASS: 41 centered strike poses (max side offset '+maxOffset.toFixed(4)+' blocks), continuous baked channels, 27 solid projectile cubes, fast swept collision and challenge access wiring. Static/offline checks only.');
