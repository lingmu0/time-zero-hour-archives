package net.xuwu.time.entity;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.xuwu.time.TimeConfig;
import net.xuwu.time.api.TimeProgress;
import net.xuwu.time.block.PuzzleControllerBlockEntity;
import net.xuwu.time.logic.EncounterRules;
import net.xuwu.time.registry.TimeContent;

public final class ChronicleKeeperEntity extends Monster implements ChronalCaster {
    private static final EntityDataAccessor<Long> SWAP_AT = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.LONG);
    double hoverAngle;
    private int swapClock;
    private boolean swapResolved;
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHIELD = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SAFE = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> ARENA_MIN = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> ARENA_MAX = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> ORIGIN = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Long> RING_AT = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> CAST_AT = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> CAST = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.INT);
    public static final int CAST_BOLT = 1, CAST_RING = 2, CAST_STAFF = 3;
    private static final EntityDataAccessor<CompoundTag> RING_PATTERN = SynchedEntityData.defineId(ChronicleKeeperEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final RingAttacks ringCache = new RingAttacks();
    private final net.xuwu.time.logic.AnchorSequence anchors = new net.xuwu.time.logic.AnchorSequence();
    private int lastPattern = -1, nextStaffTick, nextRingTick = 120;
    private UUID castTarget;
    private boolean ringResolved, administrativeDamage;
    private int damageDepth;
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.time.chronicle_keeper"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private BlockPos controller, home;
    private int clock, phaseTicks, emptyTicks, rewindTicks;
    private boolean restoreCleanup;
    private Vec3 recordedPosition;
    private final Map<UUID, Vec3> recordedPlayers = new HashMap<>();
    private final Map<UUID, Debt> debts = new HashMap<>();
    private record Debt(int due, int quadrant) {}

    public ChronicleKeeperEntity(EntityType<? extends ChronicleKeeperEntity> type, Level level) {
        super(type, level); setNoGravity(true); xpReward = 0;
        setCustomName(Component.translatable("entity.time.chronicle_keeper"));
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 900).add(Attributes.ATTACK_DAMAGE, 10)
            .add(Attributes.ARMOR, 10).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.MOVEMENT_SPEED, .20).add(Attributes.FOLLOW_RANGE, 48);
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 0); entityData.define(SHIELD, true); entityData.define(SAFE, 0);
        entityData.define(ARENA_MIN, BlockPos.ZERO); entityData.define(ARENA_MAX, BlockPos.ZERO); entityData.define(ORIGIN, BlockPos.ZERO);
        entityData.define(RING_AT, -1L); entityData.define(CAST_AT, -1L); entityData.define(CAST, 0); entityData.define(SWAP_AT, -1L);
        entityData.define(RING_PATTERN, new CompoundTag());
    }
    @Override protected void registerGoals() { /* Encounter owns target selection. */ }
    @Override protected float tickHeadTurn(float movementYaw, float limbAmount) {
        yBodyRot = getYRot();
        yHeadRot = getYRot(); // The whole rig tracks the target; never leave a stale head yaw.
        return limbAmount;
    }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        // Vanilla teleport packets still request three interpolation ticks. Do not slide
        // the body or its obscuration cylinder across the arena during a paradox swap.
        boolean snap = level().isClientSide && phase() == 4 && (swapping() || distanceToSqr(x, y, z) > 4);
        super.lerpTo(x, y, z, yaw, pitch, snap ? 0 : steps, snap || teleport);
        if (snap) {
            setPos(x, y, z); setYRot(yaw); setXRot(pitch);
            setDeltaMovement(Vec3.ZERO); setOldPosAndRot();
        }
    }
    public void bind(BlockPos controller, BlockPos home, int players) {
        this.controller = controller.immutable(); this.home = home.immutable();
        anchors.randomize(random.nextLong());
        syncArena();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(TimeConfig.BOSS_HEALTH.get() * EncounterRules.partyScale(players));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(TimeConfig.BOSS_DAMAGE.get()); setHealth(getMaxHealth());
    }
    public int phase() { return entityData.get(PHASE); }
    @Override public Mob caster() { return this; }
    @Override public long swapStartedAt() { return entityData.get(SWAP_AT); }
    public boolean shielded() { return entityData.get(SHIELD); }
    public int safeQuadrant() { return entityData.get(SAFE); }
    public Vec3 arenaCenter() { return Vec3.atBottomCenterOf(entityData.get(ORIGIN)); }
    public AABB visualArena() { return new AABB(Vec3.atLowerCornerOf(entityData.get(ARENA_MIN)), Vec3.atLowerCornerOf(entityData.get(ARENA_MAX).offset(1,1,1))); }
    public boolean hasArenaVisuals() { return !entityData.get(ARENA_MIN).equals(entityData.get(ARENA_MAX)); }
    public long ringImpactTime() { return entityData.get(RING_AT); }
    @Override public net.xuwu.time.logic.ArenaPattern ringPattern() { return ringCache.decode(entityData.get(RING_PATTERN), visualArena()); }
    public int castKind() { return entityData.get(CAST); }
    public float castAge(float partial) { return entityData.get(CAST_AT) < 0 ? 1000 : level().getGameTime() - entityData.get(CAST_AT) + partial; }
    private void syncArena() {
        if (home == null) return;
        entityData.set(ORIGIN, home);
        if (puzzle() != null) {
            var box = puzzle().arena();
            entityData.set(ARENA_MIN, BlockPos.containing(box.minX, box.minY, box.minZ));
            entityData.set(ARENA_MAX, BlockPos.containing(box.maxX - 1, box.maxY - 1, box.maxZ - 1));
        } else if (controller == null) {
            entityData.set(ARENA_MIN, home.offset(-17,-1,-17));
            entityData.set(ARENA_MAX, home.offset(17,8,17));
        }
    }
    private void faceTarget(ServerPlayer target) {
        Vec3 delta = target.getEyePosition().subtract(getEyePosition());
        float yaw = (float)(Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90;
        setYRot(Mth.approachDegrees(getYRot(), yaw, 12));
        yBodyRot = getYRot(); yHeadRot = getYRot();
        setXRot(Mth.clamp((float)(-Mth.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)) * Mth.RAD_TO_DEG), -25, 25));
    }
    private void beginCast(int kind, ServerPlayer target) {
        entityData.set(CAST, kind); entityData.set(CAST_AT, level().getGameTime());
        castTarget = target == null ? null : target.getUUID();
    }
    private void clearCombatVisuals() {
        entityData.set(RING_AT, -1L); entityData.set(CAST, 0); castTarget = null;
    }
    private PuzzleControllerBlockEntity puzzle() {
        return controller != null && level().hasChunkAt(controller) && level().getBlockEntity(controller) instanceof PuzzleControllerBlockEntity p ? p : null;
    }
    private AABB bounds() { return puzzle() != null ? puzzle().arena() : hasArenaVisuals() ? visualArena() : getBoundingBox().inflate(22); }
    private List<ServerPlayer> players() {
        return level() instanceof ServerLevel server ? server.getEntitiesOfClass(ServerPlayer.class, bounds(), p -> p.isAlive() && !p.isSpectator()) : List.of();
    }
    private Vec3 center() { return home != null ? Vec3.atBottomCenterOf(home) : position(); }
    private int quadrant(Player player) { return EncounterRules.quadrant(player.getX() - center().x, player.getZ() - center().z); }
    private void announce(String key, Object... args) { for (var player : players()) player.sendSystemMessage(Component.translatable("message.time." + key, args)); }

    public void activateAnchor(ServerPlayer player, int index) {
        if (level().isClientSide || !bounds().contains(player.position())) return;
        if (shielded() && index >= 0 && index < 4) {
            if (!anchors.press(index)) {
                TimeProgress.strain(player, 5); announce("anchor_wrong");
                if (puzzle() != null) puzzle().showAnchors(0, false);
                return;
            }
            if (anchors.progress() == 4) {
                entityData.set(SHIELD, false); phaseTicks = 0; announce("shield_open");
                if (phase() == 4) {
                    spawnEcho(TemporalEchoEntity.PARADOX, center().add(7, 0, 4), 72000);
                    announce("paradox_hint");
                }
            } else announce("anchor_progress", anchors.progress(), 4);
            if (puzzle() != null) puzzle().showAnchors(anchors.mask(), false);
            level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1, .6f + anchors.progress() * .2f);
        }
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        // Only administrative /kill bypasses the encounter; modded bypass-tag hits do not.
        if (source.is(DamageTypes.GENERIC_KILL)) {
            boolean previous = administrativeDamage; administrativeDamage = true;
            try { return super.hurt(source, amount); } finally { administrativeDamage = previous; }
        }
        if (Float.isNaN(amount) || amount <= 0) return false;
        if (level().isClientSide) return false;
        if (shielded()) {
            if (source.getEntity() instanceof ServerPlayer player)
                player.displayClientMessage(Component.translatable("message.time.shield_order_hint"), true);
            return false;
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!bounds().contains(player.position())) return false;
            if (phase() == 2 && quadrant(player) != safeQuadrant()) {
                player.displayClientMessage(Component.translatable("message.time.attack_from_present"), true); return false;
            }
        }
        // The final cap is applied by TimeCombatEvents after armor reduction.
        if (EncounterRules.limitFinalDamage(getHealth(), getMaxHealth(), phase(), amount) <= 0) return false;
        damageDepth++;
        try { return super.hurt(source, amount); } finally { damageDepth--; }
    }

    @Override public void setHealth(float health) {
        // Last line of defense, after armor and damage-event modifiers. Preserve NBT/debug writes.
        if (damageDepth > 0 && !administrativeDamage && (Float.isNaN(health) || health < getHealth())) {
            float before = getHealth();
            health = Float.isNaN(health) ? before : before - EncounterRules.limitFinalDamage(before, getMaxHealth(), phase(), before - health);
        }
        super.setHealth(health);
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (home == null) { home = blockPosition(); syncArena(); }
        if (controller != null && !hasArenaVisuals()) syncArena();
        if (controller == null) entityData.set(SHIELD, false);
        bossBar.setProgress(Math.max(0, Math.min(1, getHealth() / getMaxHealth())));
        if (controller != null && puzzle() == null) {
            if (++emptyTicks >= 200) { cleanupEchoes(); bossBar.removeAllPlayers(); discard(); }
            return;
        }
        var allPlayers = players();
        if (puzzle() != null && !getUUID().equals(puzzle().encounterId())) {
            cleanupEchoes(); bossBar.removeAllPlayers(); discard(); return;
        }
        if (allPlayers.isEmpty()) {
            clearCombatVisuals();
            if (++emptyTicks >= 200 && controller != null) {
                if (puzzle() != null) puzzle().abandonEncounter(getUUID());
                cleanupEchoes(); bossBar.removeAllPlayers(); discard();
            }
            return;
        }
        emptyTicks = 0; clock++; if (!shielded()) phaseTicks++;
        if (shielded() && clock % 10 == 0) server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5, getZ(), 20, 1.2, 1.5, 1.2, .02);
        if (restoreCleanup) {
            cleanupEchoes(); restoreCleanup = false;
            if (puzzle() != null) puzzle().showAnchors(anchors.mask(), false);
            if (phase() == 4 && !shielded()) spawnEcho(TemporalEchoEntity.PARADOX, center().add(7, 0, 4), 72000);
        }
        int wanted = EncounterRules.phase(getHealth() / getMaxHealth());
        if (wanted > phase()) enterPhase(wanted);
        if (!bounds().deflate(1).contains(position())) teleportTo(center().x, center().y, center().z);
        hoverAngle += .012;
        double radius = phase() == 4 ? 7 : 3;
        Vec3 hoverTarget = center().add(Math.sin(hoverAngle) * radius, Math.sin(clock * .025) * .65, Math.cos(hoverAngle) * radius);
        setDeltaMovement(hoverTarget.subtract(position()).scale(.055));
        var targets = allPlayers.stream().filter(p -> !p.isCreative()).toList();
        if (phase() == 4 && !shielded() && !targets.isEmpty()) tickParadox(server);
        if (swapping()) { setDeltaMovement(Vec3.ZERO); faceTarget(allPlayers.get(0)); return; }
        if (targets.isEmpty()) {
            faceTarget(allPlayers.get(0)); // Creative inspection still has correct facing.
            clearCombatVisuals(); // Never resume an expired strike after a target changes mode.
        }
        if (!targets.isEmpty()) {
            var target = targets.get(Math.floorMod(clock / 80, targets.size()));
            if (castTarget != null && server.getEntity(castTarget) instanceof ServerPlayer locked && targets.contains(locked)) target = locked;
            faceTarget(target);
            int interval = phase() == 4 ? 30 : 55;
            long now = level().getGameTime();
            boolean ringCharging = ringImpactTime() >= now;
            if (castAge(0) >= castDuration()) { entityData.set(CAST, 0); castTarget = null; }
            if (castKind() == 0 && !ringCharging && clock < nextRingTick && clock >= nextStaffTick && StaffStrike.inReach(this, target)) {
                beginCast(CAST_STAFF, target); nextStaffTick = clock + StaffStrike.COOLDOWN_TICKS;
                level().playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, .65f, .6f);
            }
            if (clock % interval == interval - 10 && clock < nextRingTick && !ringCharging && castKind() == 0) beginCast(CAST_BOLT, target);
            if (castKind() == CAST_STAFF) {
                setDeltaMovement(Vec3.ZERO);
                if (castAge(0) == StaffStrike.IMPACT_TICKS && target.getUUID().equals(castTarget)) StaffStrike.hit(this, target, (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f);
            }
            if (castKind() == CAST_BOLT && castAge(0) == 10) {
                ChronalBoltEntity.fireVolley(this, target.getEyePosition(), (float)getAttributeValue(Attributes.ATTACK_DAMAGE), EncounterRules.boltCount(phase()));
                level().playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, .7f, 1.3f);
            }
            if (clock >= nextRingTick && castKind() == 0) {
                nextRingTick = clock + 120;
                var pattern = RingAttacks.create(this, target, lastPattern); lastPattern = pattern.getInt("Kind");
                entityData.set(RING_PATTERN, pattern);
                entityData.set(RING_AT, now + EncounterRules.RING_WARNING_TICKS); ringResolved = false;
                beginCast(CAST_RING, null);
                level().playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1, .6f);
            }
            if (ringImpactTime() >= 0 && now >= ringImpactTime() && !ringResolved) {
                ringResolved = true;
                RingAttacks.resolve(this, targets, (float)getAttributeValue(Attributes.ATTACK_DAMAGE));
                level().playSound(null, blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, .7f, 1.5f);
            }
            if (castAge(0) >= castDuration()) { entityData.set(CAST, 0); castTarget = null; }
        }
        if (shielded()) return; // Phase clocks and phase-specific hazards start after calibration.
        switch (phase()) {
            case 1 -> tickPast(server);
            case 2 -> tickPresent(server, targets);
            case 3 -> tickFuture(server, targets);
            case 4 -> {
                if (phaseTicks % 30 == 0) server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2, getZ(), 6, .5, .5, .5, .01);
            }
            default -> {}
        }
    }

    private void enterPhase(int next) {
        entityData.set(PHASE, next); phaseTicks = 0; rewindTicks = 0; debts.clear(); recordedPlayers.clear();
        cleanupEchoes(); entityData.set(SHIELD, controller != null); clearCombatVisuals();
        anchors.randomize(random.nextLong());
        if (puzzle() != null) puzzle().showAnchors(0, false);
        nextRingTick = clock + 60;
        swapClock = 0; entityData.set(SWAP_AT, -1L);
        bossBar.setName(Component.translatable("entity.time.chronicle_keeper").append(" · ").append(Component.translatable("phase.time." + next)));
        announce("phase", Component.translatable("phase.time." + next));
        if (next == 2) entityData.set(SAFE, 0);
        if (next == 4 && !shielded()) {
            spawnEcho(TemporalEchoEntity.PARADOX, center().add(7, 0, 4), 72000);
            announce("paradox_hint");
        }
    }

    private void tickPast(ServerLevel server) {
        // Resolve before starting the next warning so a ten-second response window
        // can end on the same tick as the next ten-second cadence.
        if (rewindTicks > 0 && --rewindTicks == 0) {
            boolean remains = server.getEntitiesOfClass(TemporalEchoEntity.class, bounds(), e -> e.isAlive() && e.ownedBy(getUUID()) && e.mode() == TemporalEchoEntity.MEMORY).size() > 0;
            if (remains) {
                setHealth(Math.min(getMaxHealth() * .8f, getHealth() + getMaxHealth() * .08f));
                if (recordedPosition != null) teleportTo(recordedPosition.x, recordedPosition.y, recordedPosition.z);
                for (var player : players()) {
                    Vec3 target = recordedPlayers.get(player.getUUID());
                    if (target != null && bounds().contains(target) && server.noCollision(player, player.getBoundingBox().move(target.subtract(player.position())))) {
                        player.teleportTo(target.x, target.y, target.z); player.fallDistance = 0; TimeProgress.strain(player, 2);
                    }
                }
                announce("rewind_failed");
            } else announce("rewind_broken");
            cleanupEchoes();
        }
        if (phaseTicks % EncounterRules.PAST_REWIND_INTERVAL_TICKS == 1) {
            recordedPosition = position(); recordedPlayers.clear();
            for (var player : players()) recordedPlayers.put(player.getUUID(), player.position());
            rewindTicks = EncounterRules.PAST_REWIND_RESPONSE_TICKS;
            int echoCount = Math.max(1, TimeConfig.ECHO_COUNT.get());
            for (int index = 0; index < echoCount; index++) {
                double angle = -Math.PI / 2 + index * (Math.PI * 2 / echoCount);
                spawnEcho(TemporalEchoEntity.MEMORY,
                    center().add(Math.cos(angle) * 8, -1, Math.sin(angle) * 8),
                    EncounterRules.PAST_REWIND_RESPONSE_TICKS);
            }
            announce("rewind_warning");
        }
    }

    private void tickPresent(ServerLevel server, List<ServerPlayer> targets) {
        int cycle = phaseTicks / 120;
        if (phaseTicks % 120 == 1) {
            entityData.set(SAFE, EncounterRules.safeQuadrant(cycle));
            announce("present_quadrant", Component.translatable("present_phase.time." + safeQuadrant()));
        }
        if (phaseTicks % 20 == 0) {
            if (phaseTicks % 120 > 30) for (var player : targets) if (quadrant(player) != safeQuadrant()) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(TimeContent.STASIS.get(), 30, 0));
                ChronalEffects.stasis(player);
            }
        }
    }

    private Vec3 quadrantCenter(int quadrant) {
        return center().add((quadrant & 1) == 0 ? -9 : 9, 0, (quadrant & 2) == 0 ? -9 : 9);
    }
    private void tickFuture(ServerLevel server, List<ServerPlayer> targets) {
        if (phaseTicks % 120 == 1) for (var player : targets) {
            int q = random.nextInt(4);
            debts.put(player.getUUID(), new Debt(clock + 80, q));
            player.sendSystemMessage(Component.translatable("message.time.debt_warning", Component.translatable("present_phase.time." + q)));
        }
        var iterator = debts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var player = server.getServer().getPlayerList().getPlayer(entry.getKey());
            if (clock < entry.getValue().due) continue;
            if (player != null && player.level() == server && bounds().contains(player.position())) {
                if (quadrant(player) != entry.getValue().quadrant) {
                    if (player.hurt(damageSources().indirectMagic(this, this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f)) ChronalEffects.futureHit(player);
                    TimeProgress.strain(player, 3);
                } else server.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 10, .5, .5, .5, .01);
            }
            iterator.remove();
        }
    }

    private void spawnEcho(int mode, Vec3 position, int life) {
        var echo = TimeContent.TEMPORAL_ECHO.get().create(level());
        if (echo == null) return;
        echo.configure(mode, getUUID(), life); echo.moveTo(position.x, position.y, position.z, 0, 0);
        if (mode == TemporalEchoEntity.PARADOX) echo.bindCaster(this);
        level().addFreshEntity(echo);
    }
    private void tickParadox(ServerLevel server) {
        var echoes = server.getEntitiesOfClass(TemporalEchoEntity.class, bounds().inflate(4), e -> e.isAlive() && e.mode() == TemporalEchoEntity.PARADOX && e.ownedBy(getUUID()));
        if (echoes.isEmpty()) return;
        var echo = echoes.get(0);
        if (swapping()) {
            if (!swapResolved && server.getGameTime() - swapStartedAt() >= 12) {
                swapResolved = true;
                resolveParadoxSwap(echo, random.nextFloat());
            }
        } else if (++swapClock >= EncounterRules.SWAP_INTERVAL && ringImpactTime()+10 < server.getGameTime()
                && echo.ringImpactTime()+10 < server.getGameTime() && castAge(0)>=10 && echo.castAge(0)>=10) {
            swapClock = 0; swapResolved = false; clearCombatVisuals();
            entityData.set(SWAP_AT, server.getGameTime()); echo.beginSwap(server.getGameTime());
            ChronalEffects.swap(this); ChronalEffects.swap(echo);
        }
    }
    /** Server-only random decision; both outcomes deliberately emit the exact same effects. */
    public void resolveParadoxSwap(TemporalEchoEntity echo, float roll) {
        if (!(level() instanceof ServerLevel server) || phase() != 4 || !swapping() || !echo.ownedBy(getUUID())) return;
        Vec3 a = position(), b = echo.position();
        if (EncounterRules.shouldSwap(roll) && bounds().deflate(1).contains(a) && bounds().deflate(1).contains(b)
                && !server.getBlockCollisions(this, getBoundingBox().move(b.subtract(a))).iterator().hasNext()
                && !server.getBlockCollisions(echo, echo.getBoundingBox().move(a.subtract(b))).iterator().hasNext()) {
            teleportTo(b.x,b.y,b.z); echo.teleportTo(a.x,a.y,a.z);
            double angle = hoverAngle; hoverAngle = echo.hoverAngle; echo.hoverAngle = angle;
        }
        setDeltaMovement(Vec3.ZERO); echo.setDeltaMovement(Vec3.ZERO);
        ChronalEffects.swap(this); ChronalEffects.swap(echo);
    }
    private void cleanupEchoes() {
        if (level() instanceof ServerLevel server) for (var echo : server.getEntitiesOfClass(TemporalEchoEntity.class, bounds().inflate(4), e -> e.ownedBy(getUUID()))) echo.discard();
    }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossBar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossBar.removePlayer(player); }
    @Override public void die(DamageSource source) {
        if (!level().isClientSide) {
            cleanupEchoes(); bossBar.removeAllPlayers();
            if (puzzle() != null) puzzle().encounterDefeated(getUUID());
        }
        super.die(source);
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (controller != null) tag.putLong("Controller", controller.asLong());
        if (home != null) tag.putLong("Home", home.asLong());
        tag.putInt("Phase", phase()); tag.putBoolean("Shield", shielded()); tag.putInt("Safe", safeQuadrant());
        tag.putInt("Clock", clock); tag.putInt("PhaseTicks", phaseTicks); tag.putInt("Anchors", anchors.progress());
        tag.putIntArray("AnchorOrder", anchors.order()); tag.putInt("LastPattern", lastPattern);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null;
        home = tag.contains("Home") ? BlockPos.of(tag.getLong("Home")) : null;
        entityData.set(PHASE, Math.max(0, Math.min(4, tag.getInt("Phase")))); entityData.set(SHIELD, tag.getBoolean("Shield")); entityData.set(SAFE, Math.floorMod(tag.getInt("Safe"), 4));
        if (!anchors.restore(tag.getIntArray("AnchorOrder"), tag.getInt("Anchors"))) anchors.randomize(random.nextLong());
        if (shielded() && anchors.progress() == 4) anchors.restore(anchors.order(), 0);
        lastPattern = tag.contains("LastPattern") ? Math.floorMod(tag.getInt("LastPattern"), 3) : -1;
        entityData.set(SWAP_AT, -1L); swapClock = 0;
        clock = tag.getInt("Clock"); phaseTicks = tag.getInt("PhaseTicks"); nextStaffTick = clock + 20; nextRingTick = clock + 60;
        restoreCleanup = true; rewindTicks = 0; debts.clear(); setNoGravity(true);
        clearCombatVisuals(); syncArena();
    }
}
