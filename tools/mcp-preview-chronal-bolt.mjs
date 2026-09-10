import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {connect} from './time-blockbench-mcp.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const mcp=await connect(),file=path.join(root,'model_source/chronal_bolt.bbmodel');
const model=JSON.parse(fs.readFileSync(file,'utf8'));
console.log(JSON.stringify(await mcp.tool('create_project',{name:'chronal_bolt',format:'modded_entity'})));
const literal=JSON.stringify(model).replaceAll('/',String.raw`\u002f`);
console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{if(Outliner.elements.length)throw Error("Refusing to overwrite existing model");Codecs.project.parse('+literal+',"");Project.name="chronal_bolt";return {cubes:Cube.all.length,bones:Group.all.length};})()'})));
console.log(JSON.stringify(await mcp.tool('get_project_info')));
for(const [name,position] of [['three-quarter',[22,15,26]],['side',[28,8,2]]]){
  await mcp.tool('set_camera_angle',{position,target:[0,0,0],projection:'perspective'});
  const shot=await mcp.tool('capture_screenshot'),item=shot.content.find(c=>c.type==='image');
  if(!item)throw Error('Missing MCP model preview');
  const out=path.join(root,'build/model-preview/mcp-bolt-'+name+'.png');fs.writeFileSync(out,Buffer.from(item.data,'base64'));console.log(out);
}
console.log(JSON.stringify(await mcp.tool('export_model',{codec_id:'project',path:file,max_content_length:0})));
console.log(JSON.stringify(await mcp.tool('risky_eval',{code:'(()=>{Project.save_path='+JSON.stringify(file)+';const id=Project.uuid;setTimeout(()=>{if(Project&&Project.uuid===id)BarItems.save_project.trigger();},100);return "Native bolt save requested";})()'})));
