package net.xuwu.time.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.xuwu.time.registry.TimeContent;

/** One listener-relative streaming loop; never spatialized or attached to a body. */
final class BossMusicSound extends AbstractTickableSoundInstance {
    private static final float LEVEL = .7f;
    private final boolean ascension;
    private boolean fighting = true;

    BossMusicSound(SoundEvent track, boolean ascension) {
        super(track, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.ascension = ascension;
        looping = true; delay = 0; relative = true;
        attenuation = SoundInstance.Attenuation.NONE;
        volume = 0; pitch = 1;
    }

    void setFighting(boolean fighting) { this.fighting = fighting; }
    boolean isAscension() { return ascension; }
    void stopImmediately() { stop(); }
    @Override public boolean canStartSilent() { return true; }
    @Override public void tick() {
        volume = Mth.approach(volume, fighting ? LEVEL : 0, LEVEL / (fighting ? 40 : 30));
        if (!fighting && volume <= 0) stop();
    }
}
