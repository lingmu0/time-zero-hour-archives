import fs from 'node:fs';
import assert from 'node:assert/strict';
import {applyArmPose,worldBoxes} from './keeper-pose.mjs';
const read=p=>fs.readFileSync(new URL('../'+p,import.meta.url),'utf8');
const source=JSON.parse(read('model_source/chronicle_keeper_sovereign.bbmodel'));
const identity=()=>[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
const multiply=(a,b)=>Array.from({length:16},(_,k)=>{let v=0;for(let i=0;i<4;i++)v+=a[(k%4)+i*4]*b[Math.floor(k/4)*4+i];return v;});
const rotation=(axis,a)=>{const m=identity(),c=Math.cos(a),s=Math.sin(a);const i=(axis+1)%3,j=(axis+2)%3;m[i+i*4]=c;m[j+j*4]=c;m[j+i*4]=s;m[i+j*4]=-s;return m;};
let checks=0;
for(const [kind,tick,filename] of [['bolt',10,'KeeperStaffSocket'],['staff',18,'KeeperSlamSocket']]){
const java=read('src/main/java/net/xuwu/time/entity/'+filename+'.java');
for(const age of [0,1,20,60,100,250,600])for(const yaw of [0,30,90,150,180,270,359]){
  let m=multiply(rotation(1,(180-yaw)*Math.PI/180),[-1,0,0,0,0,-1,0,0,0,0,1,0,0,0,0,1]);
  let translation=identity();translation[13]=-1.501;m=multiply(m,translation);
  const idle=.045+Math.sin(age*.04)*.035;
  for(const match of java.matchAll(/m\.(translate|rotateZYX)\(([^;]+)\);/g)){
    const expression=match[2].replaceAll('entity.tickCount','age').replaceAll('Mth.sin','Math.sin').replace(/(\d)f\b/g,'$1');
    const values=Function('age','idle','return ['+expression+'];')(age,idle);
    if(match[1]==='translate'){const t=identity();for(let i=0;i<3;i++)t[12+i]=values[i];m=multiply(m,t);}
    else for(let axis=2;axis>=0;axis--)m=multiply(m,rotation(axis,values[2-axis]));
  }
  const cube=worldBoxes(applyArmPose(structuredClone(source),kind,tick,age)).find(c=>c.name==='staff_clock_core');
  const p=[0,1,2].map(i=>cube.points.reduce((n,v)=>n+v[i],0)/8/16),angle=yaw*Math.PI/180;
  const expected=[p[0]*Math.cos(angle)+p[2]*Math.sin(angle),p[1]+.001-Math.sin(age*.045)*.65/16,p[0]*Math.sin(angle)-p[2]*Math.cos(angle)];
  assert(Math.hypot(...expected.map((v,i)=>v-m[12+i]))<.00001,'Server muzzle diverges from posed staff clock');checks++;
}
}
assert(read('src/main/java/net/xuwu/time/entity/ChronalBoltEntity.java').includes('KeeperStaffSocket.release(mob)'));
console.log('PASS: '+checks+' staff-tip socket comparisons across ages and headings; generated server transform matches posed model at release.');
