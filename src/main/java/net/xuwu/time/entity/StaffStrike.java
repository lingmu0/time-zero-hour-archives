package net.xuwu.time.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** Overhead lift and downward slam; only the locked nearby target can be hit. */
public final class StaffStrike {
    public static final int IMPACT_TICKS = 18, DURATION_TICKS = 36, COOLDOWN_TICKS = 50;
    public static boolean inReach(Mob caster, ServerPlayer target) {
        return target.position().subtract(caster.position()).horizontalDistanceSqr() <= 2.8 * 2.8
            && Math.abs(target.getY() - caster.getY()) <= 2.5 && caster.hasLineOfSight(target);
    }
    public static void hit(Mob caster, ServerPlayer target, float damage) {
        if (!(caster.level() instanceof ServerLevel server)) return;
        Vec3 facing = Vec3.directionFromRotation(0, caster.getYRot());
        Vec3 tip = KeeperSlamSocket.impact(caster);
        double floor = caster instanceof ChronalCaster c ? c.visualArena().minY + 1.05 : caster.getY();
        server.sendParticles(ParticleTypes.POOF,tip.x,floor+.12,tip.z,20,.65,.08,.65,.1);
        server.sendParticles(ParticleTypes.CRIT,tip.x,floor+.2,tip.z,24,.45,.12,.45,.18);
        server.playSound(null,tip.x,floor,tip.z,SoundEvents.ANVIL_LAND,SoundSource.HOSTILE,.65f,1.4f);
        Vec3 toward = target.position().subtract(caster.position());
        if (!inReach(caster,target) || facing.dot(toward.normalize()) < .35) return;
        if (target.hurt(caster.damageSources().mobAttack(caster), damage)) {
            target.knockback(.9, caster.getX()-target.getX(), caster.getZ()-target.getZ());
            server.sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+1,target.getZ(),14,.3,.5,.3,.12);
        }
    }
    private StaffStrike() {}
}
