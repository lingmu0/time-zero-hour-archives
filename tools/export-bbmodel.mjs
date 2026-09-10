import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath,pathToFileURL} from 'node:url';
import {write} from './asset-utils.mjs';

export function exportGeometry(input, output, check = false, options = {}) {
  const model=JSON.parse(fs.readFileSync(input,'utf8'));
  if(!model.meta?.box_uv)throw new Error('The Java exporter requires Box UV, matching the provided model.');
  const elements=new Map(model.elements.map(e=>[e.uuid,e]));
  // Blockbench 5 stores bone properties in groups[], leaving UUID references in outliner.
  const groups=new Map((model.groups??[]).map(g=>[g.uuid,g]));
  const resolve=g=>({...groups.get(g.uuid),...g});
  const names=new Set(),lines=[];
  const className=path.basename(output,'.java');
  if(!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(className))throw new Error('Invalid Java class name');
  const f=v=>Number(v).toFixed(5)+'f';
  let serial=0;
  function emit(group,parent,origin,isRoot=false) {
    group=resolve(group);
    if(!group.name)throw new Error('Unresolved Blockbench bone '+group.uuid);
    if(group.visibility===false || group.export===false)return;
    if(names.has(group.name))throw new Error('Duplicate bone name: '+group.name);
    names.add(group.name);
    const variable='part'+serial++,pivot=group.origin??[0,0,0],rot=group.rotation??[0,0,0];
    const children=(group.children??[]).map(e=>typeof e==='string'?elements.get(e):e).filter(Boolean);
    const cubes=children.filter(e=>e.type==='cube'&&e.visibility!==false&&e.export!==false);
    if(cubes.some(c=>(c.rotation??[]).some(v=>v!==0)))throw new Error('Move per-cube rotations into a bone before exporting: '+group.name);
    let builder='CubeListBuilder.create()';
    for(const cube of cubes) {
      const from=cube.from,to=cube.to,uv=cube.uv_offset??[0,0];
      builder+='\n            .texOffs('+uv[0]+','+uv[1]+').mirror('+!!cube.mirror_uv+').addBox('+[from[0]-pivot[0],pivot[1]-to[1],from[2]-pivot[2],to[0]-from[0],to[1]-from[1],to[2]-from[2]].map(f).join(',')+', new CubeDeformation('+f(cube.inflate??0)+'))';
    }
    const pose=[pivot[0]-origin[0],isRoot?24-pivot[1]:origin[1]-pivot[1],pivot[2]-origin[2],-rot[0]*Math.PI/180,rot[1]*Math.PI/180,-rot[2]*Math.PI/180].map(f).join(',');
    lines.push('        var '+variable+' = '+parent+'.addOrReplaceChild('+JSON.stringify(group.name)+', '+builder+', PartPose.offsetAndRotation('+pose+'));');
    for(const child of children.filter(e=>e.type!=='cube'))emit(child,variable,pivot);
  }
  for(const group of model.outliner) {
    if(typeof group==='string')throw new Error('Place all elements under the root bone.');
    emit(group,'mesh.getRoot()',[0,0,0],true);
  }
  for(const name of options.requiredBones??['root','dial','pendulum','arms'])if(!names.has(name))throw new Error('Required animation bone missing: '+name);
  const java='package net.xuwu.time.client;\n\nimport net.minecraft.client.model.geom.PartPose;\nimport net.minecraft.client.model.geom.builders.*;\n\n/** Generated from model_source/'+path.basename(input)+'. */\npublic final class '+className+' {\n    public static LayerDefinition createLayer() {\n        MeshDefinition mesh = new MeshDefinition();\n'+lines.join('\n')+'\n        return LayerDefinition.create(mesh, '+model.resolution.width+', '+model.resolution.height+');\n    }\n    private '+className+'() {}\n}\n';
  const source=model.textures?.[0]?.source;
  if(!source?.startsWith('data:image/png;base64,'))throw new Error('Embed the primary PNG texture when saving the Blockbench project.');
  const texture=Buffer.from(source.slice(source.indexOf(',')+1),'base64');
  const project=path.resolve(path.dirname(output),'../../../../../../..');
  const textureFile=path.join(project,'src/main/resources/assets/time/textures/entity/'+(options.textureName??'chronicle_keeper')+'.png');
  if(check) {
    if(fs.readFileSync(output,'utf8')!==java)throw new Error('Generated Java geometry is stale; run the Blockbench exporter.');
    if(!fs.readFileSync(textureFile).equals(texture))throw new Error('The runtime texture differs from the Blockbench atlas.');
  } else { write(output,java);write(textureFile,texture); }
  if(options.emissive) {
    const glow=model.textures.find(t=>t.name===options.emissive+'.png')?.source;
    if(!glow?.startsWith('data:image/png;base64,'))throw new Error('Missing embedded emissive mask');
    const glowBytes=Buffer.from(glow.slice(glow.indexOf(',')+1),'base64');
    const glowFile=path.join(project,'src/main/resources/assets/time/textures/entity/'+options.emissive+'.png');
    if(check) { if(!fs.readFileSync(glowFile).equals(glowBytes))throw new Error('Emissive mask is stale'); }
    else write(glowFile,glowBytes);
  }
  console.log((check?'Verified ':'Exported ')+model.elements.length+' cubes in '+names.size+' bones '+(check?'against ':'to ')+output);
}
export const modelExports = [
  {key:'scribe', source:'archive_scribe.bbmodel', java:'ChronalGeometry.java', textureName:'archive_scribe'},
  {key:'keeper', source:'chronicle_keeper_sovereign.bbmodel', java:'KeeperGeometry.java', textureName:'chronicle_keeper', emissive:'chronicle_keeper_emissive'},
  {key:'bolt', source:'chronal_bolt.bbmodel', java:'ChronalBoltGeometry.java', textureName:'chronal_bolt', requiredBones:['root','dial','core']}
];
if(process.argv[1]&&pathToFileURL(path.resolve(process.argv[1])).href===import.meta.url){
  const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
  const selected=process.argv[2]?modelExports.filter(m=>m.key===process.argv[2]):modelExports;
  if(selected.length===0)throw new Error('Usage: node tools/export-bbmodel.mjs [scribe|keeper]');
  for(const model of selected)exportGeometry(path.join(root,'model_source',model.source),path.join(root,'src/main/java/net/xuwu/time/client',model.java),false,model);
}
