import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
import {clips} from './cast-clip-utils.mjs';
const f=v=>Number(v).toFixed(6)+'f';
let java='package net.xuwu.time.client;\n\nimport java.util.Map;\nimport net.minecraft.client.model.geom.ModelPart;\n\n/** Generated from model_source/keeper-casts.json; same keyframes authored through Blockbench MCP. */\npublic final class KeeperCastAnimations {\n';
java+='    private record Track(String bone, boolean rotation, float[][] frames) {}\n';
for(const [key,clip] of Object.entries(clips)){
  const tracks=[];
  for(const [bone,channels] of Object.entries(clip.bones))for(const [channel,frames] of Object.entries(channels)){
    const rotation=channel==='rotation';
    tracks.push('new Track('+JSON.stringify(bone)+', '+rotation+', new float[][]{'+frames.map(([t,x,y,z])=>{
      const values=rotation?[-x*Math.PI/180,y*Math.PI/180,-z*Math.PI/180]:[x,-y,z];
      return '{'+[t*20,...values].map(f).join(',')+'}';
    }).join(',')+'})');
  }
  java+='    private static final Track[] '+key.toUpperCase()+' = {\n        '+tracks.join(',\n        ')+'\n    };\n';
}
java+='    public static final String[] BONES = {'+[...new Set(Object.values(clips).flatMap(c=>Object.keys(c.bones)))].map(v=>JSON.stringify(v)).join(',')+'};\n';
java+=`    public static void apply(Map<String, ModelPart> bones, int kind, float ticks) {
        Track[] tracks = kind == 1 ? BOLT : kind == 2 ? RING : kind == 3 ? STAFF : null;
        if (tracks == null || ticks < 0 || ticks >= (kind == 2 ? 40 : kind == 3 ? 36 : 24)) return;
        for (Track track : tracks) {
            float[][] frames = track.frames();
            int i = 0;
            while (i + 1 < frames.length - 1 && ticks > frames[i + 1][0]) i++;
            float[] a = frames[i], b = frames[i + 1];
            float u = Math.max(0, Math.min(1, (ticks - a[0]) / (b[0] - a[0])));
            float x = a[1] + (b[1] - a[1]) * u, y = a[2] + (b[2] - a[2]) * u, z = a[3] + (b[3] - a[3]) * u;
            ModelPart bone = bones.get(track.bone());
            if (track.rotation()) { bone.xRot += x; bone.yRot += y; bone.zRot += z; }
            else { bone.x += x; bone.y += y; bone.z += z; }
        }
    }
    private KeeperCastAnimations() {}
}
`;
const file=path.join(root,'src/main/java/net/xuwu/time/client/KeeperCastAnimations.java');
if(process.argv.includes('--check')){
  if(fs.readFileSync(file,'utf8')!==java)throw Error('Runtime cast keyframes stale');
  console.log('PASS: Java cast player matches the MCP clip source.');
}else{fs.writeFileSync(file,java);console.log('Generated runtime cast player:',file);}
