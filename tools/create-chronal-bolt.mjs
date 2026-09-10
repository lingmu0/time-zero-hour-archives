import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {write,png,pixels} from './asset-utils.mjs';
import {exportGeometry} from './export-bbmodel.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const file=path.join(root,'model_source/chronal_bolt.bbmodel');
if(fs.existsSync(file))throw Error('Model already exists; edit the existing project instead.');
const uuid=name=>{const s=crypto.createHash('md5').update('time:bolt:'+name).digest('hex');return `${s.slice(0,8)}-${s.slice(8,12)}-${s.slice(12,16)}-${s.slice(16,20)}-${s.slice(20)}`;};
const palette=[[21,43,50],[204,155,63],[248,234,189],[101,237,224]];
const atlas=png(64,32,pixels(64,32,(x,y)=>{const c=palette[Math.floor(x/16)],v=(x%16===0||y===0)?15:((x*3+y*7)%5)-2;return [...c.map(c=>Math.max(0,Math.min(255,c+v))),255];}));
const groups=[],elements=[];
function bone(name,origin=[0,0,0],rotation=[0,0,0]){const b={uuid:uuid(name),name,origin,rotation,visibility:true,export:true};groups.push(b);return {uuid:b.uuid,children:[]};}
function cube(b,name,from,to,tile){const id=uuid(name),u=tile*16;elements.push({uuid:id,name,type:'cube',from,to,origin:[0,0,0],rotation:[0,0,0],uv_offset:[u,0],box_uv:true,autouv:0,visibility:true,export:true,faces:Object.fromEntries(['north','south','east','west','up','down'].map(n=>[n,{uv:[u,0,u+4,4],texture:0}]))});b.children.push(id);}
const rig=bone('root'),dial=bone('dial'),core=bone('core');rig.children.push(dial,core);
for(let i=0;i<12;i++){const a=i*Math.PI/6,x=Math.sin(a)*3.5,y=Math.cos(a)*3.5,b=bone('clock_rim_'+i,[x,y,0],[0,0,-i*30]);dial.children.push(b);cube(b,'brass_rim_'+i,[x-.65,y-.95,-.8],[x+.65,y+.95,.8],1);if(i%3===0)cube(b,'ivory_mark_'+i,[x-.3,y-.7,-1.05],[x+.3,y+.35,-.8],2);}
cube(core,'soul_core',[-1.4,-1.4,-1.5],[1.4,1.4,1.5],3);
cube(core,'lance_body',[-.8,-.8,1.5],[.8,.8,5.5],2);
cube(core,'lance_tip',[-.35,-.35,5.5],[.35,.35,8],3);
cube(core,'spectral_tail',[-.55,-.55,-5.5],[.55,.55,-1.5],3);
cube(core,'tail_tip',[-.25,-.25,-8],[.25,.25,-5.5],2);
cube(dial,'minute_hand',[-.2,-.1,-1.3],[.2,2.8,-1.05],2);
cube(dial,'hour_hand',[-.15,-.2,-1.35],[1.9,.2,-1.05],1);
for(const side of [-1,1]){cube(core,'fin_x_'+side,[side*2-.25,-.35,2],[side*2+.25,.35,4],1);cube(core,'fin_y_'+side,[-.35,side*2-.25,2],[.35,side*2+.25,4],1);}
const model={meta:{format_version:'4.10',model_format:'modded_entity',box_uv:true},name:'chronal_bolt',model_identifier:'chronal_bolt',modded_entity_version:'1.17',resolution:{width:64,height:32},visible_box:[2,2,0],groups,elements,outliner:[rig],textures:[{uuid:uuid('atlas'),name:'chronal_bolt.png',folder:'entity',namespace:'time',id:'0',width:64,height:32,uv_width:64,uv_height:32,mode:'bitmap',source:'data:image/png;base64,'+atlas.toString('base64')}]};
write(file,JSON.stringify(model,null,2)+'\n');
exportGeometry(file,path.join(root,'src/main/java/net/xuwu/time/client/ChronalBoltGeometry.java'),false,{textureName:'chronal_bolt',requiredBones:['root','dial','core']});
// A small deterministic inventory symbol, not the projectile's rendered geometry.
write(path.join(root,'src/main/resources/assets/time/textures/item/challenge_sigil.png'),png(32,32,pixels(32,32,(x,y)=>{const dx=x-15.5,dy=y-15.5,r=Math.hypot(dx,dy);if(r>14)return [0,0,0,0];if(r>11.5)return [...palette[1],255];if((Math.abs(dx)<1&&dy<4)||(Math.abs(dy)<1&&dx>-1&&dx<8))return [...palette[2],255];return [...(r<4?palette[3]:palette[0]),255];})));
