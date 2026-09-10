package net.xuwu.time.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.*;
import net.xuwu.time.logic.EncounterRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xuwu.time.api.TimeProgress;
import net.xuwu.time.TimeConfig;

public final class TemporalEchoEntity extends Monster implements ChronalCaster {
    private static final EntityDataAccessor<Long> RING_AT = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> CAST_AT = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> SWAP_AT = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> CAST = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> MIN = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> MAX = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.BLOCK_POS);
    private int combatClock;
    private int lastPattern = -1, nextRingTick = 60;
    private static final EntityDataAccessor<CompoundTag> RING_PATTERN = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final RingAttacks ringCache = new RingAttacks();
    private boolean ringResolved;
    private UUID castTarget;
    double hoverAngle;
    public static final int HOSTILE = 0, MEMORY = 1, PARADOX = 2;
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(TemporalEchoEntity.class, EntityDataSerializers.INT);
    private UUID owner;
    private int life = 600;
    private int orphanTicks;
    public TemporalEchoEntity(EntityType<? extends TemporalEchoEntity> type, Level level) { super(type, level); xpReward = 0; }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 18).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.MOVEMENT_SPEED, .27);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(MODE, HOSTILE);
        builder.define(RING_AT,-1L); builder.define(CAST_AT,-1L); builder.define(SWAP_AT,-1L); builder.define(CAST,0);
        builder.define(MIN,BlockPos.ZERO); builder.define(MAX,BlockPos.ZERO);
        builder.define(RING_PATTERN,new CompoundTag());
    }
    @Override public Mob caster() { return this; }
    @Override public long ringImpactTime() { return entityData.get(RING_AT); }
    @Override public net.xuwu.time.logic.ArenaPattern ringPattern() { return ringCache.decode(entityData.get(RING_PATTERN),visualArena()); }
    @Override public long swapStartedAt() { return entityData.get(SWAP_AT); }
    @Override public int castKind() { return entityData.get(CAST); }
    @Override public float castAge(float partial) { return entityData.get(CAST_AT)<0 ? 1000 : level().getGameTime()-entityData.get(CAST_AT)+partial; }
    @Override public boolean hasArenaVisuals() { return mode()==PARADOX && !entityData.get(MIN).equals(entityData.get(MAX)); }
    @Override public AABB visualArena() { return new AABB(Vec3.atLowerCornerOf(entityData.get(MIN)),Vec3.atLowerCornerOf(entityData.get(MAX))); }
    @Override protected float tickHeadTurn(float movementYaw, float limbAmount) {
        if (mode()!=PARADOX) return super.tickHeadTurn(movementYaw,limbAmount);
        yBodyRot=getYRot(); return limbAmount;
    }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        // Match the real body's instantaneous swap, including render-frame history.
        boolean snap = level().isClientSide && mode() == PARADOX && (swapping() || distanceToSqr(x, y, z) > 4);
        super.lerpTo(x, y, z, yaw, pitch, snap ? 0 : steps);
        if (snap) {
            setPos(x, y, z); setYRot(yaw); setXRot(pitch);
            setDeltaMovement(Vec3.ZERO); setOldPosAndRot();
        }
    }
    public void bindCaster(ChronicleKeeperEntity boss) {
        var arena=boss.visualArena();
        entityData.set(MIN,BlockPos.containing(arena.minX,arena.minY,arena.minZ));
        entityData.set(MAX,BlockPos.containing(arena.maxX,arena.maxY,arena.maxZ));
        hoverAngle=Math.atan2(getX()-boss.arenaCenter().x,getZ()-boss.arenaCenter().z);
    }
    public void beginSwap(long now) {
        clearCast(); entityData.set(SWAP_AT,now); setDeltaMovement(Vec3.ZERO);
    }
    private void clearCast() { entityData.set(CAST,0); entityData.set(RING_AT,-1L); castTarget=null; }
    private void beginCast(int kind, ServerPlayer player) {
        entityData.set(CAST,kind); entityData.set(CAST_AT,level().getGameTime()); castTarget=player.getUUID();
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    public void configure(int mode, UUID owner, int life) {
        double previousMax = getMaxHealth();
        boolean wasFull = getHealth() >= previousMax - .001f;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(TimeConfig.ECHO_HEALTH.get());
        if (wasFull) setHealth(getMaxHealth());
        else setHealth(Math.min(getHealth(), getMaxHealth()));
        entityData.set(MODE, mode); this.owner = owner; this.life = life;
        if (mode != HOSTILE) { setNoAi(true); setNoGravity(true); }
        setCustomName(Component.translatable(mode == PARADOX ? "entity.time.chronicle_keeper" : "entity.time.echo." + mode));
        setCustomNameVisible(mode != PARADOX);
        setPersistenceRequired();
    }
    public int mode() { return entityData.get(MODE); }
    @Override protected EntityDimensions getDefaultDimensions(Pose pose) {
        return mode() == PARADOX ? EntityDimensions.scalable(1.2f, 3.2f) : super.getDefaultDimensions(pose);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (MODE.equals(key)) refreshDimensions();
    }
    public boolean ownedBy(UUID id) { return id.equals(owner); }
    @Override public void tick() {
        super.tick();
        // Owned memories must survive until their keeper resolves the rewind.
        // A separate lifetime previously removed them at 7s, before the 10s check.
        // This also covers memories loaded with the old lifetime from a save.
        if (!level().isClientSide && (mode() != MEMORY || owner == null) && --life <= 0) discard();
        if (level() instanceof ServerLevel server && owner != null) {
            Entity source = server.getEntity(owner);
            if (source == null || !source.isAlive()) { if (++orphanTicks > 200) discard(); }
            else orphanTicks = 0;
            if (mode()==PARADOX) {
                if (source instanceof ChronicleKeeperEntity boss && boss.isAlive() && boss.phase()==4 && !boss.shielded()) tickParadox(server,boss);
                else { clearCast(); setDeltaMovement(Vec3.ZERO); }
            }
        }
        if (level() instanceof ServerLevel server && tickCount % 12 == 0) server.sendParticles(
            mode() == PARADOX ? ParticleTypes.SOUL : ParticleTypes.ENCHANT, getX(), getY() + 1, getZ(), 4, .3, .5, .3, .01);
    }
    private void tickParadox(ServerLevel server, ChronicleKeeperEntity boss) {
        if (!hasArenaVisuals()) bindCaster(boss);
        var targets=server.getEntitiesOfClass(ServerPlayer.class,visualArena(), p->p.isAlive()&&!p.isSpectator()&&!p.isCreative());
        if (swapping() || boss.swapping() || targets.isEmpty()) { clearCast(); setDeltaMovement(Vec3.ZERO); return; }
        combatClock++; hoverAngle+=.012;
        Vec3 destination=boss.arenaCenter().add(Math.sin(hoverAngle)*7,Math.sin(tickCount*.025)*.65,Math.cos(hoverAngle)*7);
        // No-AI echoes need an explicit movement step; ordinary memories remain stationary.
        Vec3 velocity=destination.subtract(position()).scale(.055);
        setDeltaMovement(Vec3.ZERO); move(MoverType.SELF,velocity);
        var target=targets.get(Math.floorMod(combatClock/80,targets.size()));
        if (castTarget!=null && server.getEntity(castTarget) instanceof ServerPlayer locked && targets.contains(locked)) target=locked;
        Vec3 delta=target.getEyePosition().subtract(getEyePosition());
        setYRot(Mth.approachDegrees(getYRot(),(float)(Mth.atan2(delta.z,delta.x)*Mth.RAD_TO_DEG)-90,12));
        yBodyRot=getYRot(); yHeadRot=getYRot();
        setXRot(Mth.clamp((float)(-Mth.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))*Mth.RAD_TO_DEG),-25,25));
        long now=server.getGameTime();
        float damage=(float)boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (castAge(0)>=(castKind()==ChronicleKeeperEntity.CAST_RING?40:24)) { entityData.set(CAST,0); castTarget=null; }
        if (combatClock%30==20 && combatClock<nextRingTick && ringImpactTime()<now && castKind()==0) beginCast(ChronicleKeeperEntity.CAST_BOLT,target);
        if (castKind()==ChronicleKeeperEntity.CAST_BOLT && castAge(0)==10) {
            ChronalBoltEntity.fireVolley(this,target.getEyePosition(),damage,EncounterRules.boltCount(4));
            level().playSound(null,blockPosition(),SoundEvents.EVOKER_CAST_SPELL,SoundSource.HOSTILE,.7f,1.3f);
        }
        if (combatClock>=nextRingTick && castKind()==0) {
            nextRingTick=combatClock+120;
            var pattern=RingAttacks.create(this,target,lastPattern);lastPattern=pattern.getInt("Kind");entityData.set(RING_PATTERN,pattern);
            entityData.set(RING_AT,now+EncounterRules.RING_WARNING_TICKS); ringResolved=false;
            beginCast(ChronicleKeeperEntity.CAST_RING,target);
            level().playSound(null,blockPosition(),SoundEvents.BEACON_POWER_SELECT,SoundSource.HOSTILE,1,.6f);
        }
        if (ringImpactTime()>=0 && now>=ringImpactTime() && !ringResolved) {
            ringResolved=true;
            RingAttacks.resolve(this,targets,damage);
            level().playSound(null,blockPosition(),SoundEvents.TRIDENT_THUNDER.value(),SoundSource.HOSTILE,.7f,1.5f);
        }
        if (castAge(0)>=(castKind()==ChronicleKeeperEntity.CAST_RING?40:24)) { entityData.set(CAST,0); castTarget=null; }
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (mode() == PARADOX && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.getEntity() instanceof ServerPlayer player) {
                player.hurt(damageSources().magic(), Math.min(8, amount * .25f)); TimeProgress.strain(player, 2);
                player.displayClientMessage(Component.translatable("message.time.false_chronicler"), true);
            }
            return false;
        }
        return super.hurt(source, amount);
    }
    @Override protected boolean shouldDropLoot() { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("Mode", mode()); tag.putInt("Life", life); if (owner != null) tag.putUUID("Owner", owner);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        configure(tag.getInt("Mode"), owner, Math.max(1, tag.getInt("Life")));
    }
}
