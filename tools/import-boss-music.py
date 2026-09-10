"""Convert a user-supplied local MP3 into the Boss Vorbis stream.

Keeps the complete composition and its natural ending; not a beat-matched remix.
Only gain reduction and 10 ms anti-click edge fades are applied.
"""
from pathlib import Path
import argparse
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'build/audio-tools'))
import numpy as np
import soundfile as sf

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('source', type=Path)
args = parser.parse_args()
source = args.source.resolve(strict=True)
data, rate = sf.read(source, dtype='float32', always_2d=True)
assert rate == 44100 and data.shape[1] == 2, 'Expected this supplied 44.1 kHz stereo file'
assert np.isfinite(data).all() and len(data) > 10 * rate
source_peak = float(np.max(np.abs(data)))
gain = min(1.0, 10 ** (-3.5 / 20) / source_peak)
data *= gain
fade = round(rate * .01)
data[:fade] *= np.linspace(0, 1, fade, dtype=np.float32)[:, None]
data[-fade:] *= np.linspace(1, 0, fade, dtype=np.float32)[:, None]
asset = ROOT / 'src/main/resources/assets/time/sounds/music/chronicler_battle.ogg'
temporary = ROOT / 'build/audio/chronicler_battle_import.ogg'
temporary.parent.mkdir(parents=True, exist_ok=True)
with sf.SoundFile(temporary, mode='w', samplerate=rate, channels=2, format='OGG', subtype='VORBIS') as out:
    for offset in range(0, len(data), rate):
        out.write(data[offset:offset + rate])
decoded, decoded_rate = sf.read(temporary, dtype='float32', always_2d=True)
assert decoded_rate == rate and decoded.shape == data.shape and np.isfinite(decoded).all()
assert float(np.max(np.abs(decoded))) < 10 ** (-1 / 20), 'Encoded peak exceeds headroom'
assert float(np.max(np.abs(decoded[0] - decoded[-1]))) < .04
backup = ROOT / 'build/audio-source/chronicler_battle-before-0.1.8.ogg'
backup.parent.mkdir(parents=True, exist_ok=True)
if asset.exists() and not backup.exists():
    import shutil
    shutil.copy2(asset, backup)
temporary.replace(asset)
manifest = {
    'title': '漆黒の代償 / 漆黑的代价',
    'asset': asset.relative_to(ROOT).as_posix(),
    'source_filename': source.name,
    'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
    'source_reference': 'https://www.youtube.com/watch?v=ZrgrrObNZFo',
    'source_reference_verified': False,
    'license_status': 'unverified-user-supplied',
    'author': 'Not verified',
    'sha256': hashlib.sha256(asset.read_bytes()).hexdigest(),
    'sample_rate': rate, 'channels': 2, 'frames': len(data),
    'duration_seconds': len(data) / rate,
    'source_peak_dbfs': float(20 * np.log10(source_peak)),
    'gain_db': float(20 * np.log10(gain)),
    'edge_fade_seconds': .01,
    'processing': 'Complete track; gain reduction; 10 ms edge fades; Ogg Vorbis encoding. Natural ending retained, not a beat-matched seamless loop.'
}
(ROOT / 'music_source/boss-track.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(manifest, ensure_ascii=True))
