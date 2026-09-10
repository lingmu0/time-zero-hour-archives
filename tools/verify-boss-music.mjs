import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const bank=JSON.parse(read('src/main/resources/assets/time/sounds.json'));
function verifyTrack(event, name, frames, expectedHash, reportName) {
const cue=bank[event];
assert(cue&&cue.sounds.length===1,'Exactly one music cue required');
assert.equal(cue.sounds[0].name,'time:music/'+name);
assert.equal(cue.sounds[0].stream,true,'Long BGM must be streamed');
const bytes=fs.readFileSync(path.join(root,'src/main/resources/assets/time/sounds/music/'+name+'.ogg'));
assert(bytes.length>100000,'Audio is missing or truncated');
let offset=0,pages=0,lastGranule=0n;
while(offset<bytes.length){
  assert(offset+27<=bytes.length,'Truncated Ogg header');
  assert.equal(bytes.toString('ascii',offset,offset+4),'OggS','Broken Ogg page');
  assert.equal(bytes[offset+4],0);
  const count=bytes[offset+26];
  assert(offset+27+count<=bytes.length,'Truncated segment table');
  const payload=bytes.subarray(offset+27,offset+27+count).reduce((a,b)=>a+b,0);
  assert(offset+27+count+payload<=bytes.length,'Truncated Ogg payload');
  lastGranule=bytes.readBigUInt64LE(offset+6);
  if(pages===0){
    const head=offset+27+count;
    assert.equal(bytes[head],1);assert.equal(bytes.toString('ascii',head+1,head+7),'vorbis');
    assert.equal(bytes[head+11],2,'Stereo cue required');assert.equal(bytes.readUInt32LE(head+12),44100);
  }
  offset+=27+count+payload;pages++;
}
assert.equal(offset,bytes.length);assert.equal(lastGranule,BigInt(frames),'Wrong track duration');
const report=JSON.parse(read('build/audio/'+reportName));
assert.equal(report.sha256,crypto.createHash('sha256').update(bytes).digest('hex'),'Stale decoded-audio report');
assert.equal(report.sha256,expectedHash,'Original track bytes must stay unchanged');
assert(report.peak_dbfs<-1&&report.rms_dbfs>-25&&report.rms_dbfs<-12,'Invalid levels');
assert(report.loop_seam_delta<.04,'Loop has a sharp sample discontinuity');
console.log('PASS: '+name+' '+(frames/44100)+'s stereo Vorbis; '+pages+' complete Ogg pages; decoded levels/seam/hash.');
}
const manifest=JSON.parse(read('music_source/boss-track.json'));
assert.equal(manifest.license_status,'unverified-user-supplied');
assert.equal(manifest.sample_rate,44100);assert.equal(manifest.channels,2);
verifyTrack('music.chronicle_keeper','chronicler_battle',manifest.frames,
  manifest.sha256,'boss-music-analysis.json');
verifyTrack('music_disc.zero_hour','zero_hour',80*44100,
  '5eb9734f39103ba47b4ccb19a88dc6ef9d6ac570a5269aa04d311611af6423b7','zero-hour-analysis.json');
const content=read('src/main/java/net/xuwu/time/registry/TimeContent.java');
assert(content.includes('SOUNDS.register(bus)')&&content.includes('SOUNDS.register("music.chronicle_keeper"'),'Sound event not wired');
assert(content.includes('SOUNDS.register("music_disc.zero_hour"')&&content.includes('.jukeboxPlayable(ZERO_HOUR_SONG)'),'Disc sound/component not wired');
assert(content.includes('ResourceKey.create(Registries.JUKEBOX_SONG, TimeMod.id("zero_hour"))'),'Wrong jukebox registry key');
const song=JSON.parse(read('src/main/resources/data/time/jukebox_song/zero_hour.json'));
assert.equal(song.sound_event,'time:music_disc.zero_hour');
assert.equal(song.length_in_seconds,80);assert.equal(song.comparator_output,12);
for(const lang of ['zh_cn','en_us']){
  const text=JSON.parse(read('src/main/resources/assets/time/lang/'+lang+'.json'));
  assert(text['item.time.music_disc_zero_hour']&&text[song.description.translate],'Disc translation missing');
}
const recipe=JSON.parse(read('src/main/resources/data/time/recipe/music_disc_zero_hour.json'));
assert.equal(recipe.type,'minecraft:crafting_shapeless');assert.equal(recipe.result.id,'time:music_disc_zero_hour');
assert.equal(recipe.result.count,1);assert.equal(recipe.ingredients.length,4);
const unlock=JSON.parse(read('src/main/resources/data/time/advancement/recipes/misc/music_disc_zero_hour.json'));
assert(unlock.rewards.recipes.includes(recipe.result.id),'Recipe book unlock missing');
const notice=read('src/main/resources/META-INF/THIRD-PARTY-MUSIC.txt');
assert(notice.includes('漆黒の代償')&&notice.includes('unverified-user-supplied')&&!notice.includes('License as published by the author: CC0'),'Supplied music must not inherit the previous track license');
const manager=read('src/main/java/net/xuwu/time/client/BossMusicManager.java');
const instance=read('src/main/java/net/xuwu/time/client/BossMusicSound.java');
assert(manager.includes('Dist.CLIENT')&&manager.includes('boss.hasArenaVisuals()')&&manager.includes('boss.visualArena().contains(mc.player.position())'),'Encounter/client guard missing');
assert(manager.includes('LoggingOut')&&manager.includes('level != mc.level')&&manager.includes('event.overrideMusic(null)'),'Music lifecycle hook missing');
assert(instance.includes('SoundSource.MUSIC')&&instance.includes('looping = true')&&instance.includes('Attenuation.NONE'),'Wrong playback channel/loop/spatialization');
console.log('PASS: separate boss/disc events, jukebox song/component, recipe, translations, supplied-source notice and lifecycle source guards. No game launched.');
