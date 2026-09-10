import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {clips,mcpClip} from './cast-clip-utils.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const model=JSON.parse(fs.readFileSync(path.join(root,'model_source/chronicle_keeper_sovereign.bbmodel'),'utf8'));
const bones=new Map(model.groups.map(g=>[g.uuid,g])),cube=model.elements.find(c=>c.name==='staff_clock_core');
let chain;
function visit(node,parents=[]){if(typeof node==='string'){if(node===cube.uuid)chain=parents;return;}for(const child of node.children)visit(child,[...parents,bones.get(node.uuid)]);}
model.outliner.forEach(n=>visit(n));if(!chain)throw Error('Staff clock socket missing');
for(const [key,className,method] of [['bolt','KeeperStaffSocket','release'],['staff','KeeperSlamSocket','impact']]){
const clip=mcpClip(clips[key]),release=clips[key].release_tick/20,f=v=>Number(v).toFixed(8)+'f';
let code='package net.xuwu.time.entity;\n\nimport net.minecraft.util.Mth;\nimport net.minecraft.world.entity.Mob;\nimport net.minecraft.world.phys.Vec3;\nimport org.joml.Matrix4f;\nimport org.joml.Vector3f;\n\n/** Generated from actual staff bones at '+key+' release; server-safe. */\npublic final class '+className+' {\n    public static Vec3 '+method+'(Mob entity) {\n        float idle=.045f+Mth.sin(entity.tickCount*.04f)*.035f;\n        Matrix4f m=new Matrix4f().rotateY((180-entity.yBodyRot)*Mth.DEG_TO_RAD).scale(-1,-1,1).translate(0,-1.501f,0);\n';
let parent=[0,0,0];
for(const [i,b] of chain.entries()){
  const frame=clip.bones[b.name]?.find(f=>Math.abs(f.time-release)<1e-6),rotation=b.rotation.map((v,k)=>v+(frame?.rotation?.[k]??0));
  const p=b.origin.map((v,k)=>v-parent[k]+(frame?.position?.[k]??0));if(i===0)p[1]-=24;
  const pos=[p[0]/16,-p[1]/16,p[2]/16];
  const rot=[-rotation[0]*Math.PI/180,rotation[1]*Math.PI/180,-rotation[2]*Math.PI/180];
  const extra=b.name==='staff_arm'?'-idle':b.name==='staff_hand'?'+idle':'';
  code+='        m.translate('+pos.map(f).join(',')+');\n';
  if(i===0)code+='        m.translate(0,Mth.sin(entity.tickCount*.045f)*.65f/16,0);\n';
  code+='        m.rotateZYX('+f(rot[2])+','+f(rot[1])+','+f(rot[0])+extra+');\n';
  parent=b.origin;
}
const point=cube.from.map((v,k)=>(v+cube.to[k])/2-parent[k]);point[1]*=-1;
code+='        Vector3f socket=m.transformPosition(new Vector3f('+point.map(v=>f(v/16)).join(',')+'));\n        return entity.position().add(socket.x,socket.y,socket.z);\n    }\n    private '+className+'() {}\n}\n';
const out=path.join(root,'src/main/java/net/xuwu/time/entity/'+className+'.java');
if(process.argv.includes('--check')){if(fs.readFileSync(out,'utf8')!==code)throw Error('Staff socket stale');console.log('PASS: '+key+' server socket mirrors staff bones at tick '+clips[key].release_tick);}
else{fs.writeFileSync(out,code);console.log(out);}
}
