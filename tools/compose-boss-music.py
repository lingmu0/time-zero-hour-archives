"""Original 0.1.6 cue, now the Zero-Hour music disc (NOT the current boss BGM).

Requires numpy and soundfile (local encoder can live in build/audio-tools).
Creates a periodic stereo Ogg/Vorbis asset and an uncompressed audition excerpt.
"""
from pathlib import Path
import hashlib
import json
import sys
import wave

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'build/audio-tools'))
import numpy as np
import soundfile as sf

RATE, BPM, BARS = 44100, 96, 32
BEAT = 60 / BPM
FRAMES = round(BARS * 4 * BEAT * RATE)
mix = np.zeros((FRAMES, 2), dtype=np.float64)
rng = np.random.default_rng(20260910)
TAU = 2 * np.pi


def frequency(midi):
    return 440 * 2 ** ((midi - 69) / 12)


def add(signal, beat, gain, pan=0):
    # Wrap release/reverb tails across the loop boundary, not a fade-to-silence seam.
    start = round(beat * BEAT * RATE) % FRAMES
    stereo = signal[:, None] * gain * np.array([np.sqrt((1-pan)/2), np.sqrt((1+pan)/2)])
    end = min(len(signal), FRAMES-start)
    mix[start:start+end] += stereo[:end]
    if end < len(signal):
        mix[:len(signal)-end] += stereo[end:]


def note(midi, beat, beats, gain, voice='pluck', pan=0):
    length = beats * BEAT
    release = .4 if voice == 'strings' else .7 if voice == 'bell' else .16
    t = np.arange(round((length+release)*RATE)) / RATE
    f = frequency(midi)
    if voice == 'strings':
        wave_sum = np.zeros_like(t)
        for detune in [-.0018, .0015]:
            phase = TAU*f*(1+detune)*t + .012*np.sin(TAU*4.7*t)
            for harmonic in range(1, 7):
                wave_sum += np.sin(harmonic*phase) / harmonic**1.65
        signal = wave_sum*.4
        envelope = np.minimum(t/.18, 1)*np.minimum(np.maximum((length+release-t)/release, 0), 1)
    elif voice == 'bell':
        signal = np.sin(TAU*f*t)*np.exp(-t/1.1) + .28*np.sin(TAU*f*2.756*t)*np.exp(-t/.22) + .08*np.sin(TAU*f*5.4*t)*np.exp(-t/.08)
        envelope = (1-np.exp(-t/.003))*np.minimum(np.maximum((length+release-t)/.12,0),1)
    elif voice == 'organ':
        phase = TAU*f*t + .025*np.sin(TAU*5*t)
        signal = (np.sin(phase)+.32*np.sin(2*phase)+.16*np.sin(3*phase)+.045*np.sin(4*phase))*.7
        envelope = np.minimum(t/.035,1)*np.minimum(np.maximum((length+release-t)/.18,0),1)
    elif voice == 'bass':
        signal = (np.sin(TAU*f*t)+.25*np.sin(TAU*f*2*t)+.12*np.sin(TAU*f*3*t))*.8
        envelope = (1-np.exp(-t/.012))*np.exp(-t/1.2)*np.minimum(np.maximum((length+release-t)/.15,0),1)
    else:
        signal = np.sin(TAU*f*t)+.28*np.sin(TAU*f*2*t)*np.exp(-t/.1)+.12*np.sin(TAU*f*3*t)*np.exp(-t/.06)
        envelope = (1-np.exp(-t/.006))*np.exp(-t/.23)*np.minimum(np.maximum((length+release-t)/.08,0),1)
    add(signal*envelope,beat,gain,pan)


def drum(beat, gain, kind='kick', pan=0):
    duration = {'kick': .8, 'snare': .23, 'hat': .055, 'tick': .045}[kind]
    t = np.arange(round(duration*RATE))/RATE
    noise = rng.uniform(-1,1,len(t))
    if kind == 'kick':
        phase = TAU*(48*t + 36*.028*(1-np.exp(-t/.028)))
        signal = np.sin(phase)*np.exp(-t*6)+.15*noise*np.exp(-t*90)
    elif kind == 'snare':
        high = noise-np.roll(noise,1)
        signal = .35*high*np.exp(-t*23)+.3*np.sin(TAU*175*t)*np.exp(-t*35)
    elif kind == 'hat':
        signal = (noise-np.roll(noise,1))*.35*np.exp(-t*80)
    else:
        signal = (np.sin(TAU*1150*t)+.5*np.sin(TAU*1730*t))*np.exp(-t*160)
    signal *= np.minimum(t/.002,1)*np.minimum((duration-t)/.006,1)
    add(signal,beat,gain,pan)


# D minor: an eight-bar harmonic arc; C# in the dominant pulls back to D.
roots = [38, 38, 34, 34, 43, 41, 45, 45]
chords = [[62,65,69],[62,65,69],[58,62,65],[58,62,65],
          [55,58,62],[57,60,65],[57,61,64],[57,61,64]]
motif = [
    [(0,74,1), (1.5,77,.5), (2,76,1), (3,69,.75)],
    [(0,72,1.5), (2,69,.5), (3,65,.75)],
    [(0,70,1), (1.5,74,.5), (2,77,1.5)],
    [(0,76,1), (1,74,1), (2.5,70,1)],
    [(0,74,1.5), (2,70,.5), (3,67,.75)],
    [(0,72,1), (1.5,69,.5), (2,65,1.5)],
    [(0,73,1), (1.5,76,.5), (2,69,1)],
    [(0,70,.75), (1,69,.75), (2,73,1.5)],
]

for bar in range(BARS):
    section, local, start = bar//8, bar%8, bar*4
    root, chord = roots[local], chords[local]
    intensity = [.72, .9, 1, .86][section]
    for n, pitch in enumerate(chord):
        note(pitch-12,start,3.8,.085*intensity,'strings',(n-1)*.48)
    for offset in [0, 1.5, 2.5, 3.5]:
        note(root,start+offset,.65,.24*intensity,'bass')
    arp = [0,1,2,1,0,2,1,2] if section!=2 else [0,2,1,2,0,1,2,1]
    for index, tone in enumerate(arp):
        note(chord[tone],start+index*.5,.35,.13*intensity,'pluck',-.32 if index%2 else .32)
    # Deliberate space at the start of each arc keeps the lead from masking cues.
    for offset, pitch, duration in motif[local]:
        if section == 0 and local in [0,1]:
            note(pitch,start+offset,duration,.11,'bell',.25)
        else:
            note(pitch,start+offset,duration,.13*intensity,'organ',-.15)
            if section == 2:
                note(pitch+12,start+offset,duration,.045,'bell',.4)
    if local in [0,2,4,6]:
        note(chord[0]+12,start,1.4,.07,'bell',-.55)
    for offset in [0,2]:
        drum(start+offset,.28*intensity)
    if section in [1,2]:
        drum(start+3.5,.17*intensity)
    for offset in [1,3]:
        drum(start+offset,.10*intensity,'snare',.1)
    for beat in range(4):
        drum(start+beat,.042,'tick',-.65 if beat%2==0 else .65)
        if section != 0 or local > 3:
            drum(start+beat+.5,.028*intensity,'hat',.35)
    if local == 7:
        for offset in [3,3.25,3.5,3.75]:
            drum(start+offset,.055,'snare',-.2+offset*.08)

# A sparse, circular stereo room/delay keeps note tails intact when Ogg loops.
dry = mix.copy()
for seconds, gain in [(.083,.13),(.137,.1),(.233,.075),(.379,.06),(.593,.045),(.911,.025)]:
    mix += np.roll(dry[:, ::-1],round(seconds*RATE),axis=0)*gain
mix -= mix.mean(axis=0)
mix = np.tanh(mix*1.15)
mix *= min(.72/np.max(np.abs(mix)),.13/np.sqrt(np.mean(mix**2)))

asset = ROOT/'src/main/resources/assets/time/sounds/music/zero_hour.ogg'
asset.parent.mkdir(parents=True,exist_ok=True)
print('Encoding 80-second original score...',flush=True)
with sf.SoundFile(asset,mode='w',samplerate=RATE,channels=2,format='OGG',subtype='VORBIS') as out:
    for offset in range(0,FRAMES,RATE):
        out.write(mix[offset:offset+RATE].astype(np.float32))
print('Decoding and checking the loop...',flush=True)
decoded, rate = sf.read(asset,always_2d=True)
assert rate == RATE and decoded.shape == (FRAMES,2)
assert np.isfinite(decoded).all() and np.max(np.abs(decoded)) < .9
assert np.max(np.abs(decoded[-1]-decoded[0])) < .04, 'Audible loop seam'
preview = ROOT/'build/audio/zero_hour_preview.wav'
preview.parent.mkdir(parents=True,exist_ok=True)
with wave.open(str(preview),'wb') as out:
    out.setnchannels(2);out.setsampwidth(2);out.setframerate(RATE)
    excerpt = decoded[round(20*RATE):round(40*RATE)].copy()
    fade=round(.08*RATE);excerpt[:fade]*=np.linspace(0,1,fade)[:,None];excerpt[-fade:]*=np.linspace(1,0,fade)[:,None]
    out.writeframes((excerpt*32767).astype('<i2').tobytes())
report = {'title':'零点钟决 / The Zero-Hour Reckoning','bpm':BPM,'bars':BARS,'duration_seconds':FRAMES/RATE,
          'sample_rate':RATE,'channels':2,'codec':'Ogg Vorbis','original_synthesis':True,
          'peak_dbfs':float(20*np.log10(np.max(np.abs(decoded)))),'rms_dbfs':float(20*np.log10(np.sqrt(np.mean(decoded**2)))),
          'loop_seam_delta':float(np.max(np.abs(decoded[-1]-decoded[0]))),
          'sha256':hashlib.sha256(asset.read_bytes()).hexdigest(),'bytes':asset.stat().st_size}
(preview.parent/'zero-hour-analysis.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
