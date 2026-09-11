package net.xuwu.time.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.xuwu.time.api.TimeProgress;
import net.xuwu.time.registry.TimeContent;

public final class ChronalBoltEntity extends Projectile {
    public static final double SPEED = 1.05;
    public static void fireFrom(LivingEntity owner, Vec3 origin, Vec3 target, float damage, double speed) {
        if (owner.level().isClientSide) return;
        var bolt = TimeContent.CHRONAL_BOLT.get().create(owner.level());
        if (bolt == null) return;
        bolt.setOwner(owner); bolt.damage = damage;
        bolt.setPos(origin.x, origin.y, origin.z);
        bolt.setDeltaMovement(target.subtract(origin).normalize().scale(speed));
        owner.level().addFreshEntity(bolt);
    }
    private int life = 100;
    private float damage = 6;
    public ChronalBoltEntity(EntityType<? extends ChronalBoltEntity> type, Level level) { super(type, level); }
    public static void fire(LivingEntity owner, Vec3 target, float damage) {
        fireVolley(owner, target, damage, 1);
    }
    /** One aimed bolt plus phase-added bolts in a compact fan. */
    public static void fireVolley(LivingEntity owner, Vec3 target, float damage, int requestedCount) {
        if (owner.level().isClientSide) return;
        int count = Math.max(1, Math.min(5, requestedCount));
        Vec3 origin = owner instanceof Mob mob && (owner instanceof ChronicleKeeperEntity
            || owner instanceof TemporalEchoEntity echo && echo.mode() == TemporalEchoEntity.PARADOX)
            ? KeeperStaffSocket.release(mob) : owner.getEyePosition().add(0,-.3,0);
        Vec3 forward = target.subtract(origin);
        if (forward.lengthSqr() < 1.0e-7) forward = owner.getLookAngle();
        forward = forward.normalize();
        Vec3 reference = Math.abs(forward.y) > .9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = forward.cross(reference).normalize();
        Vec3 vertical = side.cross(forward).normalize();
        if (owner.level() instanceof net.minecraft.server.level.ServerLevel server)
            server.sendParticles(ParticleTypes.END_ROD,origin.x,origin.y,origin.z,8 + count * 2,.08,.08,.08,.06);
        for (int index = 0; index < count; index++) {
            double sideAngle = 0, verticalAngle = 0;
            if (index > 0) {
                double angle = (index - 1) * (Math.PI * 2.0 / Math.max(1, count - 1));
                double spread = Math.toRadians(2.75 + count * .75);
                sideAngle = Math.cos(angle) * spread;
                verticalAngle = Math.sin(angle) * spread;
            }
            Vec3 launch = forward.add(side.scale(sideAngle)).add(vertical.scale(verticalAngle)).normalize();
            var bolt = TimeContent.CHRONAL_BOLT.get().create(owner.level());
            if (bolt == null) continue;
            bolt.setOwner(owner); bolt.damage = damage;
            bolt.setPos(origin.x, origin.y, origin.z);
            bolt.setDeltaMovement(launch.scale(SPEED));
            owner.level().addFreshEntity(bolt);
        }
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (--life <= 0) { discard(); return; }
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                if (hit instanceof EntityHitResult entityHit) {
                    Entity target = entityHit.getEntity();
                    if (target.hurt(damageSources().indirectMagic(this, getOwner()), damage) && target instanceof ServerPlayer p) TimeProgress.strain(p, 1);
                }
                discard(); return;
            }
        } else level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
        Vec3 velocity = getDeltaMovement();
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
        updateRotation();
    }
    @Override protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof TemporalEchoEntity) && !(entity instanceof ChronicleKeeperEntity) && !(entity instanceof ArchiveScribeEntity);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("Life", life); tag.putFloat("Damage", damage);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); life = Math.min(100, tag.getInt("Life")); damage = tag.getFloat("Damage");
    }
}
