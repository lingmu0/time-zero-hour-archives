package net.xuwu.time.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;

/** Event-only world effects: no permanent safe/unsafe zone overlay. */
public final class ChronalEffects {
    public static void stasis(Entity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        for (int layer = 0; layer < 3; layer++) for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX()+Math.cos(angle)*.75,
                target.getY()+.15+layer*.7, target.getZ()+Math.sin(angle)*.75, 1, 0, 0, 0, 0);
        }
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, .65f, .55f);
    }
    public static void futureHit(Entity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY()+1, target.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY()+1, target.getZ(), 36, .65, .9, .65, .13);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY()+1, target.getZ(), 24, .5, .7, .5, .25);
        level.playSound(null, target.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, .9f, 1.45f);
    }
    public static void swap(Entity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY()+1.6, target.getZ(), 100, 1.1, 1.7, 1.1, .1);
        level.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY()+1.6, target.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, .7f, .65f);
    }
    private ChronalEffects() {}
}
