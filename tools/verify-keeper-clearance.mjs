import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {applyArmPose,worldBoxes,intersects} from './keeper-pose.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const source=JSON.parse(fs.readFileSync(path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel'),'utf8'));
const java=fs.readFileSync(path.join(root,'src/main/java/net/xuwu/time/client/KeeperModel.java'),'utf8');
// Keyframes are shared; export-keeper-casts.mjs --check verifies the generated table.
for(const expression of [
  'KeeperCastAnimations.apply(castBones, boss.castKind(), t)',
  'staffHand.xRot -= (staffArm.xRot - staffArm.getInitialPose().xRot)',
  'bookHand.xRot -= (bookArm.xRot - bookArm.getInitialPose().xRot)',
  'ghostTail.zRot += Mth.sin(age * .12f)','ghostTip.zRot += Mth.sin(age * .12f - 1.4f)'
])assert(java.includes(expression),'Offline pose mirror is stale: '+expression);
let poses=0,pairs=0,minHoverGap=Infinity,minUp=1,allowedStrikeOverlaps=0;const hits=[];
const bodyName=/^(upper_arm_|elbow_joint_|gauntlet_|shoulder_|mechanical_ribcage|ivory_breastplate|collar_|raised_collar_|obsidian_helm|ivory_mask)/;
for(const age of [0,20,60,100])for(const kind of ['idle','bolt','ring','staff']){
  for(let t=0;t<=(kind==='idle'?0:kind==='ring'?40:kind==='staff'?36:24);t+=.5){
    const posed=applyArmPose(structuredClone(source),kind,t,age);
    for(const name of ['forearm_-1','forearm_1']){
      const angle=posed.groups.find(g=>g.name===name).rotation[0];
      assert(angle>=(kind==='staff'&&name==='forearm_-1'?10:65)&&angle<=90,'Elbow outside natural range: '+angle);
    }
    const boxes=worldBoxes(posed),props=boxes.filter(b=>b.chain.includes('chronostaff')||b.chain.includes('held_book'));
    const body=boxes.filter(b=>bodyName.test(b.name));
    for(const prop of props)for(const arm of body){pairs++;if(intersects(prop,arm)){
      if(kind==='staff'&&prop.chain.includes('chronostaff'))allowedStrikeOverlaps++;
      else if(hits.length<25)hits.push({kind,t,age,prop:prop.name,body:arm.name});
    }}
    const tome=props.filter(b=>b.chain.includes('held_book'));
    const palm=boxes.filter(b=>b.chain.includes('book_hand')&&!b.chain.includes('held_book'));
    const gap=Math.min(...tome.flatMap(b=>b.points.map(p=>p[1])))-Math.max(...palm.flatMap(b=>b.points.map(p=>p[1])));
    assert(gap>1,'Hovering book touches open palm');minHoverGap=Math.min(minHoverGap,gap);
    const spine=tome.find(b=>b.name==='book_spine'),normal=spine.points[0].map((v,i)=>v-spine.points[1][i]);
    const up=normal[1]/Math.hypot(...normal);assert(up>Math.cos(18*Math.PI/180),'Book no longer lies nearly flat');minUp=Math.min(minUp,up);
    poses++;
  }
}
if(hits.length){console.error(JSON.stringify(hits,null,2));throw new Error('Prop/arm intersections in sampled poses');}
console.log('PASS: '+poses+' sampled poses; '+pairs+' cuboid checks; '+allowedStrikeOverlaps+' permitted staff-strike overlaps (natural motion prioritized). Offline geometry probe, not in-game testing.');
console.log('PASS: floating book has at least '+minHoverGap.toFixed(2)+' model pixels of palm clearance; maximum sampled tilt '+(Math.acos(minUp)*180/Math.PI).toFixed(2)+' degrees.');
