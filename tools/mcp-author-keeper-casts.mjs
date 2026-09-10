import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {connect} from './time-blockbench-mcp.mjs';
import {clips,mcpClip} from './cast-clip-utils.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..'),mcp=await connect();
const model=JSON.parse(fs.readFileSync(path.join(root,process.argv[2]==='load-casts'?'model_source/chronicle_keeper_casts.bbmodel':'model_source/chronicle_keeper_sovereign.bbmodel'),'utf8'));
if(process.argv[2]==='load'||process.argv[2]==='load-casts'){
  const literal=JSON.stringify(model).replaceAll('/',String.raw`\u002f`);
  console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{if(Outliner.elements.length)throw Error("Refusing to replace an existing project");Codecs.project.parse('+literal+',"");Project.name="Chronicle Keeper Casts";return {bones:Group.all.length,cubes:Cube.all.length};})()'})));
  console.log(JSON.stringify(await mcp.tool('get_project_info')));
}else if(process.argv[2]==='create'){
  for(const clip of Object.values(clips))console.log(JSON.stringify(await mcp.tool('create_animation',mcpClip(clip))));
}else if(process.argv[2]==='replace-staff'){
  console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{if(Project.uuid!=="801c25ee-d462-5ec2-9c82-c906de1c9991")throw Error("Unexpected project");const a=Animation.all.find(a=>a.name==="animation.time.staff_strike");if(!a)throw Error("Missing staff animation");a.remove();return "Replacing only staff strike";})()'})));
  const data=mcpClip(clips.staff);data.name=data.name.replace(/^animation\./,'');
  console.log(JSON.stringify(await mcp.tool('create_animation',data)));
}else if(process.argv[2]==='update'||process.argv[2]==='update-staff'){
  // MCP 1.6.1 manage_keyframes calls set('values', array), which does not update
  // Blockbench 5 data_points.x/y/z. Update axes explicitly using its public setter.
  const data=(process.argv[2]==='update-staff'?[clips.staff]:Object.values(clips)).map(mcpClip);
  console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{const clips='+JSON.stringify(data)+';let count=0;for(const clip of clips){const a=Animation.all.find(a=>a.name===clip.name);if(!a)throw Error("Missing cast animation");for(const [bone,frames] of Object.entries(clip.bones)){const b=Object.values(a.animators).find(b=>b.name===bone);if(!b)throw Error("Missing bone");for(const f of frames)for(const channel of ["rotation","position"]){if(!f[channel])continue;const k=b[channel].find(k=>Math.abs(k.time-f.time)<0.001);if(!k)throw Error("Missing keyframe");for(let i=0;i<3;i++)k.set(["x","y","z"][i],f[channel][i]);k.interpolation="linear";count++;}}}Animator.preview();return {updated:count};})()'})));
}else if(process.argv[2]==='capture'){
  const kind=process.argv[3],time=Number(process.argv[4]);
  console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{Animation.all.find(a=>a.name==='+JSON.stringify(clips[kind].name)+').select();Modes.options.animate.select();return Animation.selected.name;})()'})));
  console.log(JSON.stringify(await mcp.tool('animation_timeline',{action:'set_time',time})));
  await mcp.tool('set_camera_angle',{position:kind==='staff'?[-110,75,-150]:[-78,54,-110],target:kind==='staff'?[0,36,0]:[0,28,0],projection:'perspective'});
  const shot=await mcp.tool('capture_screenshot'),item=shot.content.find(c=>c.type==='image');
  if(!item)throw Error('MCP screenshot missing');
  const file=path.join(root,'build/model-preview/mcp-'+kind+'-'+String(time).replace('.','_')+'.png');
  fs.writeFileSync(file,Buffer.from(item.data,'base64'));console.log(file);
}else if(process.argv[2]==='formats'){
  console.log(JSON.stringify(await mcp.tool('list_export_formats',{only_current_format:false})));
}else if(process.argv[2]==='save'){
  console.log(JSON.stringify(await mcp.tool('animation_timeline',{action:'stop'})));
  console.log(JSON.stringify(await mcp.tool('export_model',{codec_id:'project',path:path.join(root,'model_source/chronicle_keeper_casts.bbmodel'),max_content_length:0})));
  console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{Project.save_path='+JSON.stringify(path.join(root,'model_source/chronicle_keeper_casts.bbmodel'))+';const id=Project.uuid;setTimeout(()=>{if(Project&&Project.uuid===id)BarItems.save_project.trigger();},100);return {name:Project.name,native_save_requested:true,animations:Animation.all.map(a=>({name:a.name,length:a.length}))};})()'})));
}else throw Error('Use load/create/update/capture/formats/save');
