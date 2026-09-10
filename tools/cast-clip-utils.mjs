import fs from 'node:fs';
import {fileURLToPath} from 'node:url';
export const clips=JSON.parse(fs.readFileSync(fileURLToPath(new URL('../model_source/keeper-casts.json',import.meta.url)),'utf8'));
// Bake shape-preserving cubic curves at half-tick intervals for identical BB/Java playback.
// Interior slopes keep the swing continuous; extrema ease without curve overshoot.
function curved(rows,t){
  let i=0;while(i+1<rows.length-1&&t>rows[i+1][0])i++;
  const a=rows[i],b=rows[i+1],h=b[0]-a[0],u=Math.max(0,Math.min(1,(t-a[0])/h));
  const slope=(j,k)=>{
    if(j===0||j===rows.length-1)return 0;
    const ha=rows[j][0]-rows[j-1][0],hb=rows[j+1][0]-rows[j][0];
    const da=(rows[j][k]-rows[j-1][k])/ha,db=(rows[j+1][k]-rows[j][k])/hb;
    return da*db<=0?0:3*(ha+hb)/((2*hb+ha)/da+(hb+2*ha)/db);
  };
  return [1,2,3].map(k=>(2*u**3-3*u*u+1)*a[k]+(u**3-2*u*u+u)*h*slope(i,k)+(-2*u**3+3*u*u)*b[k]+(u**3-u*u)*h*slope(i+1,k));
}
for(const clip of Object.values(clips))if(clip.interpolation==='monotone'){
  const times=[...new Set([0,clip.length,...Object.values(clip.bones).flatMap(c=>Object.values(c).flatMap(r=>r.map(f=>f[0]))),
    ...Array.from({length:Math.round(clip.length*40)+1},(_,i)=>i/40)])].sort((a,b)=>a-b);
  for(const channels of Object.values(clip.bones))for(const [key,rows] of Object.entries(channels))
    channels[key]=times.map(t=>[t,...curved(rows,t).map(v=>Math.round(v*1e8)/1e8)]);
}
export function sample(frames,time){
  if(!frames)return [0,0,0];
  let i=0;while(i+1<frames.length-1&&time>frames[i+1][0])i++;
  const a=frames[i],b=frames[i+1],u=Math.max(0,Math.min(1,(time-a[0])/(b[0]-a[0])));
  return [1,2,3].map(k=>a[k]+(b[k]-a[k])*u);
}
export function mcpClip(clip){
  const bones={};
  for(const [bone,channels] of Object.entries(clip.bones)){
    const times=[...new Set(Object.values(channels).flatMap(rows=>rows.map(row=>row[0])))].sort((a,b)=>a-b);
    bones[bone]=times.map(time=>({time,...Object.fromEntries(Object.entries(channels).map(([channel,rows])=>[channel,sample(rows,time)]))}));
  }
  for(const [wrist,arm,elbow] of [['staff_hand','staff_arm','forearm_-1'],['book_hand','book_arm','forearm_1']]){
    const names=[wrist,arm,elbow],times=[...new Set([...names.flatMap(n=>clip.bones[n]?.rotation?.map(r=>r[0])??[]),...(clip.bones[wrist]?.position?.map(r=>r[0])??[])])].sort((a,b)=>a-b);
    if(!times.length)continue;
    bones[wrist]=times.map(time=>{
      const rotation=sample(clip.bones[wrist]?.rotation,time);
      rotation[0]-=sample(clip.bones[arm]?.rotation,time)[0]+sample(clip.bones[elbow]?.rotation,time)[0];
      return {time,rotation,...(clip.bones[wrist]?.position?{position:sample(clip.bones[wrist].position,time)}:{})};
    });
  }
  return {name:clip.name,loop:false,animation_length:clip.length,bones};
}
