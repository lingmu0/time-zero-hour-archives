import path from 'node:path';
import fs from 'node:fs';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {write,pixels,png,palette,renderPreview} from './asset-utils.mjs';
import {exportGeometry} from './export-bbmodel.mjs';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
if(fs.existsSync(path.join(root,'model_source/archive_scribe.bbmodel')))throw new Error('Legacy initializer disabled after the model split. Use export-bbmodel.mjs; do not overwrite the edited models.');
const resource=p=>path.join(root,'src/main/resources/assets/time',p);
const atlas=png(256,256,pixels(256,256,(x,y)=>{
  const tile=Math.floor(x/64),base=palette[tile],u=x%64,v=y%64;
  const edge=(u===0||v===0||u===63||v===63)?10:0;
  const engraving=(u%16===1&&v>3&&v<60)||(v%16===1&&u>3&&u<60)?-9:0;
  const noise=((x*13+y*7)%5)-2;
  return [...base.map(c=>Math.max(0,Math.min(255,c+edge+engraving+noise))),255];
}));
write(resource('textures/entity/chronicle_keeper.png'),atlas);
for(const lit of [false,true]) {
  const image=png(32,32,pixels(32,32,(x,y)=>{
    const dx=x-15.5,dy=y-15.5,r=Math.hypot(dx,dy);
    const rim=r>12.5&&r<14.5,hand=(Math.abs(dx)<1.3&&y>4&&y<18)||(y>=5&&y<=10&&Math.abs(dx)<(y-4)*.6);
    const tick=(Math.abs(dx)<.8||Math.abs(dy)<.8)&&r>10&&r<12;
    return rim||tick?[204,153,63,255]:hand?(lit?[249,226,130,255]:[115,220,217,255]):[23,45,53,255];
  }));
  write(resource('textures/block/dial_'+(lit?'on':'off')+'.png'),image);
}
write(resource('textures/mob_effect/temporal_stasis.png'),png(18,18,pixels(18,18,(x,y)=>{
  const line=((y===3||y===14)&&x>=4&&x<=13)||((Math.abs(x-8.5)-Math.abs(y-8.5)*.7)<1&&Math.abs(x-8.5)<Math.abs(y-8.5)*.7+1&&y>3&&y<14);
  return line?[110,230,220,255]:[0,0,0,0];
})));
const uuid=name=>{const s=crypto.createHash('md5').update(name).digest('hex');return s.slice(0,8)+'-'+s.slice(8,12)+'-'+s.slice(12,16)+'-'+s.slice(16,20)+'-'+s.slice(20);};
const elements=[];
function bone(name,origin=[0,0,0],rotation=[0,0,0]) {return {name,origin,rotation,uuid:uuid('bone:'+name),export:true,visibility:true,isOpen:true,children:[]};}
function cube(group,name,javaFrom,size,tile=0) {
  const [x,y,z]=javaFrom,[w,h,d]=size,o=group.origin;
  const id=uuid('cube:'+name);
  elements.push({name,type:'cube',uuid:id,from:[o[0]+x,o[1]-y-h,o[2]+z],to:[o[0]+x+w,o[1]-y,o[2]+z+d],origin:[...o],rotation:[0,0,0],uv_offset:[tile*64,0],box_uv:true,autouv:0,shade:true,visibility:true,color:tile,
    faces:Object.fromEntries(['north','south','east','west','up','down'].map(face=>[face,{uv:[tile*64,0,tile*64+16,16],texture:0}]))});
  group.children.push(id);
}
const rig=bone('root'),robe=bone('robe'),mask=bone('mask'),arms=bone('arms'),spine=bone('spine'),dial=bone('dial',[0,28,-7]),pendulum=bone('pendulum',[0,23,-8]);
rig.children.push(robe,mask,arms,spine,dial,pendulum);
cube(robe,'robe_core',[-6,-35,-4],[12,25,8],0);
cube(robe,'flared_hem',[-9,-15,-6],[18,10,12],0);
cube(robe,'bronze_hood',[-6,-43,-5],[12,10,10],1);
cube(mask,'gold_mask',[-4,-41,-5.5],[8,6,1],2);
cube(mask,'left_eye',[-3,-39,-6.1],[2,1,1],3);cube(mask,'right_eye',[1,-39,-6.1],[2,1,1],3);
cube(arms,'left_sleeve',[-12,-32,-4],[6,17,8],1);cube(arms,'right_sleeve',[6,-32,-4],[6,17,8],1);
cube(spine,'gold_spine',[-1,-35,4],[2,27,2],2);
cube(dial,'dial_core',[-1,-1,-1],[2,2,2],2);
for(let i=0;i<12;i++){
  const a=i*Math.PI/6,mark=bone('tick_'+i,[Math.sin(a)*11,28-Math.cos(a)*11,-7],[0,0,i*30]);
  cube(mark,'clock_mark_'+i,[-1,-2,-1],[2,4,2],2);dial.children.push(mark);
}
const hand=bone('hand',[0,28,-7]);cube(hand,'minute_hand',[-.5,-10,-1.5],[1,10,1],3);dial.children.push(hand);
cube(pendulum,'pendulum_chain',[-.5,0,-.5],[1,13,1],2);cube(pendulum,'pendulum_weight',[-2,12,-1],[4,4,2],3);
const model={meta:{format_version:'4.10',model_format:'modded_entity',box_uv:true},name:'Chronicle Keeper',model_identifier:'chronicle_keeper',modded_entity_version:'1.17',visible_box:[4,5,2],resolution:{width:256,height:256},elements,outliner:[rig],textures:[{uuid:uuid('texture:chronicle_keeper'),name:'chronicle_keeper.png',folder:'entity',namespace:'time',id:'0',width:256,height:256,uv_width:256,uv_height:256,mode:'bitmap',source:'data:image/png;base64,'+atlas.toString('base64')}]};
const modelFile=path.join(root,'model_source/chronicle_keeper.bbmodel');
write(modelFile,JSON.stringify(model,null,2)+'\n');
renderPreview(model,path.join(root,'model_source/chronicle_keeper_preview.png'));
exportGeometry(modelFile,path.join(root,'src/main/java/net/xuwu/time/client/ChronalGeometry.java'));
console.log('Generated original pixel atlas, clock faces, effect icon and editable Blockbench rig.');
