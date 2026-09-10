import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {png, write} from './asset-utils.mjs';

// Initial construction only. Never overwrite an artist-edited Blockbench project.
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const target = process.argv[2] ? path.resolve(process.argv[2]) : path.join(root, 'model_source/chronicle_keeper_sovereign.bbmodel');
if (fs.existsSync(target)) throw new Error('Refusing to overwrite existing model: ' + target);
const elements = [], materials = new Map();
const id = name => {
  const h = crypto.createHash('md5').update('time:sovereign:' + name).digest('hex');
  return `${h.slice(0,8)}-${h.slice(8,12)}-${h.slice(12,16)}-${h.slice(16,20)}-${h.slice(20)}`;
};
function bone(name, parent, origin = [0,0,0], rotation = [0,0,0]) {
  const g = {name, uuid:id(name), origin, rotation, export:true, visibility:true, children:[]};
  if (parent) parent.children.push(g);
  return g;
}
function box(g, name, from, size, material = 'gold') {
  const uuid = id(name);
  if (materials.has(uuid)) throw new Error('Duplicate cube: ' + name);
  const cube = {name, type:'cube', uuid, from, to:from.map((v,i)=>v+size[i]), origin:[...g.origin], rotation:[0,0,0], box_uv:true,
    uv_offset:[0,0], autouv:0, shade:true, visibility:true, faces:{}};
  materials.set(uuid, material); elements.push(cube); g.children.push(uuid); return cube;
}
const rig = bone('root');
const torso = bone('torso', rig);
box(torso,'mechanical_ribcage',[-6,24,-3],[12,16,7],'dark');
box(torso,'waist_corset',[-4,21,-3],[8,5,6],'bronze');
box(torso,'ivory_breastplate',[-6,30,-4],[12,10,2],'ivory');
box(torso,'waist_front_belt',[-6,23,-4.5],[12,2,2],'gold');
box(torso,'spine_casing',[-2,24,4],[4,18,2],'gold');
for(let i=0;i<5;i++)box(torso,'spine_rivet_'+i,[-.75,26+i*3,6],[1.5,1,1],'glow');
for(const s of [-1,1]) {
  box(torso,'breast_border_'+s,[s<0?-6.5:5.5,28,-5],[1,12,2],'gold');
  box(torso,'collar_flange_'+s,[s<0?-7:2,39,-3],[5,2,7],'gold');
  const collar=bone('collar_blade_'+s,torso,[s*5,40,1],[0,0,-s*24]);
  box(collar,'raised_collar_'+s,[s*5-1,39,1],[2,7,5],'ivory');
  for(let i=0;i<3;i++)box(torso,'side_rib_'+s+'_'+i,[s<0?-6:4,26+i*3,-4.6],[2,1,1],'gold');
}
box(torso,'neck_joint',[-2,40,-1.5],[4,4,4],'bronze');

const head = bone('head',rig,[0,44,0]);
box(head,'obsidian_helm',[-4.5,43,-3],[9,8,7],'dark');
box(head,'ivory_mask',[-3.5,43.5,-4.2],[7,6.5,1.5],'ivory');
box(head,'brow_plate',[-4,49,-4.7],[8,1.5,2],'gold');
box(head,'mask_center_ridge',[-.5,44,-4.9],[1,5,1],'gold');
box(head,'jaw_guard',[-2.5,42.5,-4],[5,1.5,1.5],'gold');
for(const s of [-1,1]) {
  box(head,'eye_socket_'+s,[s<0?-3:-0,46.5,-4.85],[3,1.5,.7],'dark');
  box(head,'eye_light_'+s,[s<0?-2.5:1,46.75,-5.05],[1.5,.7,.4],'glow');
  box(head,'temple_guard_'+s,[s<0?-5:4,43,-2],[1,8,5],'gold');
  box(head,'cheek_inlay_'+s,[s<0?-3.5:2.5,44,-4.9],[1,1.5,.4],'bronze');
}
const crown = bone('crown',head,[0,50,0]);
box(crown,'crown_band',[-5,50,-3.5],[10,1.5,7.5],'gold');
for(let i=-2;i<=2;i++) {
  const h=4.5-Math.abs(i)*.9;
  const spike=bone('crown_ray_'+(i+2),crown,[i*2,51,0],[0,0,-i*9]);
  box(spike,'crown_prong_'+(i+2),[i*2-.55,51,-2.7],[1.1,h,1.4],'gold');
  box(spike,'crown_tip_'+(i+2),[i*2-.4,51+h-.8,-2.85],[.8,.8,1.6],i===0?'glow':'ivory');
}

const core = bone('hourglass',rig,[0,33,-5]);
box(core,'core_recess',[-4,27,-5],[8,12,1],'dark');
box(core,'glass_top_rail',[-4.5,38,-6.5],[9,1.5,2],'gold');
box(core,'glass_bottom_rail',[-4.5,26,-6.5],[9,1.5,2],'gold');
for(const s of [-1,1])box(core,'glass_pillar_'+s,[s<0?-4.5:3.5,27,-6.3],[1,11,1.2],'bronze');
for(let i=0;i<4;i++) {
  const w=6-i*1.3;
  box(core,'upper_glass_step_'+i,[-w/2,36.5-i*1.2,-6],[w,1.2,1.3],i===3?'warmglow':'glow');
  box(core,'lower_glass_step_'+i,[-w/2,27.5+i*1.2,-6],[w,1.2,1.3],i<2?'warmglow':'glow');
}
box(core,'falling_sand',[-.3,31,-6.7],[.6,3,1],'warmglow');

const arms = bone('arms',rig);
for(const s of [-1,1]) {
  const arm=bone(s<0?'staff_arm':'book_arm',arms,[s*8,38,0],[0,0,s*7]);
  box(arm,'upper_arm_'+s,[s*9-2,28,-2],[4,10,5],'dark');
  box(arm,'elbow_joint_'+s,[s*10-2,26,-2.5],[4,3,5],'bronze');
  const shoulder=bone('pauldron_'+s,arm,[s*8,38,0],[0,0,-s*15]);
  for(let i=0;i<3;i++) {
    const x=s<0?-10-i*2.2:6+i*2.2;
    box(shoulder,'shoulder_lamella_'+s+'_'+i,[x,37-i*1.5,-4],[6,2.5,9],i===0?'gold':'ivory');
    box(shoulder,'shoulder_edge_'+s+'_'+i,[x,36.7-i*1.5,-4.3],[6,.8,1],'gold');
  }
  box(shoulder,'shoulder_cabochon_'+s,[s*10-1,39.5,-1.5],[2,1,3],'glow');
  const forearm=bone('forearm_'+s,arm,[s*10,27,0],[-7,0,s*5]);
  box(forearm,'gauntlet_shell_'+s,[s*10-2.5,19,-3],[5,8,6],'ivory');
  box(forearm,'gauntlet_cuff_'+s,[s*10-3,25,-3.5],[6,2,7],'gold');
  box(forearm,'gauntlet_rail_'+s,[s*10-.5,20,-3.6],[1,5,1],'gold');
  box(forearm,'gauntlet_light_'+s,[s*10-.3,21,-3.85],[.6,3,.5],'glow');
  box(forearm,'palm_'+s,[s*10-1.8,16,-2.5],[3.6,3,4],'bronze');
  for(let f=0;f<3;f++)box(forearm,'finger_'+s+'_'+f,[s*10-1.5+f*1.1,14.5,-2.8],[.8,2,1.4],'gold');
}

const mantle = bone('mantle',rig,[0,25,3]);
for(let i=-2;i<=2;i++) {
  const panel=bone('mantle_panel_'+(i+2),mantle,[i*4,25,4],[i===0?6:9,0,-i*5]);
  const len=20-Math.abs(i)*2;
  box(panel,'cape_panel_'+(i+2),[i*4-2.4,25-len,4],[4.8,len,2],'teal');
  box(panel,'cape_border_'+(i+2),[i*4-2.4,25-len,3.8],[.7,len,2.4],'gold');
  box(panel,'cape_hem_'+(i+2),[i*4-2.4,25-len,3.8],[4.8,1,2.4],'gold');
  box(panel,'cape_inlay_'+(i+2),[i*4-.4,28-len,6.05],[.8,len-6,.3],'bronze');
}
for(const s of [-1,1]) {
  const skirt=bone('front_tasset_'+s,rig,[s*4,23,-3],[0,0,-s*8]);
  box(skirt,'tasset_core_'+s,[s*4-2.5,8,-4],[5,15,2],'ivory');
  box(skirt,'tasset_trim_'+s,[s*4-2.7,8,-4.3],[.7,15,2.6],'gold');
  box(skirt,'tasset_hem_'+s,[s*4-2.5,8,-4.3],[5,1.3,2.6],'gold');
  for(let i=0;i<3;i++)box(skirt,'tasset_rune_'+s+'_'+i,[s*4-.8,12+i*3,-4.45],[1.6,.7,.4],i===1?'glow':'gold');
  box(rig,'boot_'+s,[s*3-1.6,2,-2],[3.2,6,5],'dark');
  box(rig,'gilded_toe_'+s,[s*3-1.7,2,-3],[3.4,2.5,2],'gold');
}
const pendulum = bone('pendulum',rig,[0,25,-6]);
box(pendulum,'pendulum_chain',[-.4,12,-6.2],[.8,13,.8],'gold');
const bob=bone('pendulum_bob',pendulum,[0,11,-6],[0,0,45]);
box(bob,'pendulum_setting',[-2,9,-6.7],[4,4,1.6],'gold');
box(bob,'pendulum_jewel',[-1.2,9.8,-6.9],[2.4,2.4,2],'glow');

const dial = bone('dial',rig,[0,35,7]);
for(let i=0;i<24;i++) {
  const segment=bone('outer_ring_'+i,dial,[0,35,7],[0,0,-i*15]);
  box(segment,'outer_arc_'+i,[-2.5,53,6],[5,1.5,2],i%2===0?'gold':'bronze');
  box(segment,'outer_marker_'+i,[-.65,50.8,5.7],[1.3,i%6===0?3.5:2,1.4],i%6===0?'glow':'ivory');
}
const innerHalo=bone('inner_halo',rig,[0,35,8],[0,18,0]);
for(let i=0;i<12;i++) {
  const segment=bone('inner_ring_'+i,innerHalo,[0,35,8],[0,0,-i*30]);
  box(segment,'inner_arc_'+i,[-3.6,48,7.5],[7.2,1,1.3],'bronze');
  box(segment,'inner_glyph_'+i,[-.55,46.7,7],[1.1,2,1],'gold');
}
const hand=bone('halo_hand',dial,[0,35,7]);
box(hand,'halo_long_hand',[-.35,35,6.3],[.7,17,1],'gold');
box(hand,'halo_short_hand',[-.6,29,6.2],[1.2,6,1.2],'ivory');
box(dial,'halo_axle',[-2,33,6],[4,4,3],'gold');

const staff=bone('chronostaff',rig,[-16,23,0],[0,0,-5]);
box(staff,'staff_shaft',[-16.65,3,-.65],[1.3,45,1.3],'bronze');
box(staff,'staff_grip',[-17,18,-1],[2,8,2],'dark');
for(const y of [3,17,26,43,47])box(staff,'staff_ferrule_'+y,[-17.2,y,-1.2],[2.4,1.2,2.4],'gold');
const staffHead=bone('staff_clock',staff,[-16,49,0]);
for(let i=0;i<8;i++) {
  const cog=bone('staff_cog_'+i,staffHead,[-16,49,0],[0,0,-i*45]);
  box(cog,'staff_cog_rim_'+i,[-17.3,53,-1],[2.6,1.2,2],'gold');
  box(cog,'staff_cog_tick_'+i,[-16.4,51.5,-1.2],[.8,1.5,1],i%2===0?'glow':'ivory');
}
box(staffHead,'staff_clock_core',[-17,48,-1],[2,2,2],'glow');
box(staffHead,'staff_clock_pointer',[-16.35,49,-1.5],[.7,3,1],'ivory');

const book=bone('floating_book',rig,[15,23,-4],[-20,0,-12]);
box(book,'book_spine',[14.4,20,-5],[1.2,7,2.6],'gold');
for(const s of [-1,1]) {
  const leaf=bone('book_leaf_'+s,book,[15,23,-4],[0,-s*18,0]);
  box(leaf,'book_cover_'+s,[s<0?9:15,19.5,-4],[6,8,1],'teal');
  box(leaf,'book_pages_'+s,[s<0?9.4:15,20,-5],[5.6,7,1],'ivory');
  for(let line=0;line<4;line++)box(leaf,'book_ink_'+s+'_'+line,[s<0?10.1:15.7,21+line*1.25,-5.15],[3.5,.25,.2],line===0?'glow':'bronze');
  box(leaf,'book_corner_'+s,[s<0?9:20,26.3,-5.3],[1,1,1.7],'gold');
}
for(let i=0;i<3;i++) {
  const page=bone('orbit_page_'+i,rig,[12+i*3,30+i*2,-2+i*3],[i*10,20,-15+i*20]);
  box(page,'loose_page_'+i,[10+i*3,28+i*2,-2+i*3],[3.5,4.5,.3],'ivory');
  box(page,'loose_page_seal_'+i,[11+i*3,29+i*2,-2.2+i*3],[1.5,.5,.3],'gold');
}

const colors={dark:[30,39,47],bronze:[113,77,45],gold:[211,161,74],ivory:[218,216,191],teal:[31,86,90],glow:[110,244,232],warmglow:[255,209,117]};
const size=512, atlas=new Uint8Array(size*size*4), emissive=new Uint8Array(size*size*4);
let u=2,v=2,rowHeight=0;
for(const cube of elements) {
  const [w,h,d]=cube.to.map((n,i)=>n-cube.from[i]);
  const width=Math.ceil(2*(w+d))+2,height=Math.ceil(h+d)+2;
  if(u+width>=size){u=2;v+=rowHeight+2;rowHeight=0;}
  if(v+height>=size)throw new Error('Texture atlas overflow');
  cube.uv_offset=[u,v];
  const faces={up:[u+d,v,u+d+w,v+d],down:[u+d+w,v,u+d+2*w,v+d],
    west:[u,v+d,u+d,v+d+h],north:[u+d,v+d,u+d+w,v+d+h],east:[u+d+w,v+d,u+2*d+w,v+d+h],south:[u+2*d+w,v+d,u+2*d+2*w,v+d+h]};
  cube.faces=Object.fromEntries(Object.entries(faces).map(([name,uv])=>[name,{uv,texture:0}]));
  const material=materials.get(cube.uuid),color=colors[material];
  const lit=material==='glow'||material==='warmglow';
  for(let y=v;y<v+height;y++)for(let x=u;x<u+width;x++) {
    let shade=((x*11+y*7)%5)-2;
    const face=Object.values(faces).find(r=>x>=r[0]&&x<r[2]&&y>=r[1]&&y<r[3]);
    if(face){
      const [a,b,c,e]=face;
      if(x-a<1||y-b<1)shade+=material==='dark'?14:18;
      else if(c-x<=1||e-y<=1)shade-=24;
      else if(material==='teal' && (x+y)%7===0)shade+=9;
      else if(material==='ivory' && y%5===0)shade-=3;
      else if(material==='gold' && (x+y)%13===0)shade-=12;
    }
    const rgba=[...color.map(c=>Math.max(0,Math.min(255,c+shade))),255],offset=(y*size+x)*4;
    atlas.set(rgba,offset);if(lit)emissive.set(rgba,offset);
  }
  u+=width+2;rowHeight=Math.max(rowHeight,height);
}
const texture=(name,data,index)=>({uuid:id(name),name,folder:'entity',namespace:'time',id:String(index),width:size,height:size,uv_width:size,uv_height:size,mode:'bitmap',source:'data:image/png;base64,'+png(size,size,data).toString('base64')});
const model={meta:{format_version:'4.10',model_format:'modded_entity',box_uv:true},name:'Chronicle Keeper - Clockwork Sovereign',model_identifier:'chronicle_keeper_sovereign',modded_entity_version:'1.17',visible_box:[6,6,3],resolution:{width:size,height:size},elements,outliner:[rig],textures:[texture('chronicle_keeper.png',atlas,0),texture('chronicle_keeper_emissive.png',emissive,1)]};
write(target,JSON.stringify(model,null,2)+'\n');
console.log(`Created ${elements.length} cuboids, atlas used ${v+rowHeight}/${size} rows: ${target}`);
