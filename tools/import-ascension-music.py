"""Convert the user-supplied second-act MP3 into a headroom-safe Ogg stream."""
from pathlib import Path
import argparse
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "build/audio-tools"))
import numpy as np
import soundfile as sf

parser = argparse.ArgumentParser()
parser.add_argument("source", type=Path)
args = parser.parse_args()
source = args.source.resolve(strict=True)
info = sf.info(str(source))
assert info.samplerate == 44100 and info.channels == 2, "Expected 44.1 kHz stereo audio"
assert info.frames > 10 * info.samplerate, "Track is unexpectedly short"

data, rate = sf.read(source, dtype="float32", always_2d=True)
assert np.isfinite(data).all()
peak = float(np.max(np.abs(data)))
gain = min(1.0, 10 ** (-3.5 / 20) / max(peak, 1.0e-9))
data *= gain
fade = round(rate * .01)
data[:fade] *= np.linspace(0, 1, fade, dtype=np.float32)[:, None]
data[-fade:] *= np.linspace(1, 0, fade, dtype=np.float32)[:, None]

asset = ROOT / "src/main/resources/assets/time/sounds/music/ascension_battle.ogg"
temporary = ROOT / "build/audio/ascension_battle_import.ogg"
temporary.parent.mkdir(parents=True, exist_ok=True)
with sf.SoundFile(temporary, mode="w", samplerate=rate, channels=2, format="OGG", subtype="VORBIS") as out:
    for offset in range(0, len(data), rate):
        out.write(data[offset:offset + rate])
decoded, decoded_rate = sf.read(temporary, dtype="float32", always_2d=True)
assert decoded_rate == rate and decoded.shape == data.shape and np.isfinite(decoded).all()
assert float(np.max(np.abs(decoded))) < 10 ** (-1 / 20), "Encoded peak exceeds headroom"
temporary.replace(asset)

manifest = {
    "title": "拯救的代价 / The Cost of Salvation",
    "asset": asset.relative_to(ROOT).as_posix(),
    "source_filename": source.name,
    "source_sha256": hashlib.sha256(source.read_bytes()).hexdigest(),
    "license_status": "unverified-user-supplied",
    "author": "Not verified",
    "sha256": hashlib.sha256(asset.read_bytes()).hexdigest(),
    "sample_rate": rate,
    "channels": 2,
    "frames": len(data),
    "duration_seconds": len(data) / rate,
    "source_peak_dbfs": float(20 * np.log10(max(peak, 1.0e-9))),
    "gain_db": float(20 * np.log10(max(gain, 1.0e-9))),
    "edge_fade_seconds": .01,
    "processing": "Complete track; gain reduction; 10 ms edge fades; Ogg Vorbis encoding."
}
(ROOT / "music_source/ascension-track.json").write_text(
    json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
)
print(json.dumps(manifest, ensure_ascii=True))
