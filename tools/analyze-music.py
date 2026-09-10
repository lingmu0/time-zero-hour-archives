"""Decode the two shipped tracks and create reports plus a boss audition WAV.

Does not rewrite either shipped Ogg. Requires numpy and soundfile.
"""
from pathlib import Path
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'build/audio-tools'))
import numpy as np
import soundfile as sf

audio_dir = ROOT / 'build/audio'
audio_dir.mkdir(parents=True, exist_ok=True)
manifest = json.loads((ROOT / 'music_source/boss-track.json').read_text(encoding='utf-8'))
for name, frames, expected_hash, report_name in [
    ('zero_hour', 80 * 44100, '5eb9734f39103ba47b4ccb19a88dc6ef9d6ac570a5269aa04d311611af6423b7', 'zero-hour-analysis.json'),
    ('chronicler_battle', manifest['frames'], manifest['sha256'], 'boss-music-analysis.json'),
]:
    asset = ROOT / f'src/main/resources/assets/time/sounds/music/{name}.ogg'
    digest = hashlib.sha256(asset.read_bytes()).hexdigest()
    assert digest == expected_hash, f'{name}: original asset changed'
    data, rate = sf.read(asset, dtype='float32', always_2d=True)
    assert data.shape == (frames, 2) and rate == 44100
    assert np.isfinite(data).all()
    report = {
        'asset': str(asset.relative_to(ROOT)), 'sha256': digest,
        'sample_rate': rate, 'channels': 2, 'frames': len(data), 'duration_seconds': len(data) / rate,
        'peak_dbfs': float(20 * np.log10(np.max(np.abs(data)))),
        'rms_dbfs': float(20 * np.log10(np.sqrt(np.mean(data.astype(np.float64) ** 2)))),
        'loop_seam_delta': float(np.max(np.abs(data[0] - data[-1]))),
    }
    assert report['peak_dbfs'] < 0 and -25 < report['rms_dbfs'] < -10
    assert report['loop_seam_delta'] < .04
    (audio_dir / report_name).write_text(json.dumps(report, indent=2), encoding='utf-8')
    if name == 'chronicler_battle':
        sf.write(audio_dir / 'chronicler_battle_preview.wav', data[30 * rate:50 * rate], rate, subtype='PCM_16')
    print(json.dumps(report))
