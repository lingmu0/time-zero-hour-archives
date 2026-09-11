package net.xuwu.time.entity;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.xuwu.time.TimeConfig;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.PuzzleControllerBlockEntity;
import net.xuwu.time.logic.AscensionRules;
import net.xuwu.time.registry.TimeContent;
import net.xuwu.time.world.SanctumSites;

/** Server-owned second act. Its NBT travels with the same boss UUID between dimensions. */
public final class AscensionFight {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, TimeMod.id("sanctum"));
    public static final String RETURN = "TimeSanctumReturn";
    private final ChronicleKeeperEntity boss;
    private int mode, ticks, emptyTicks, shift, siteX, siteZ;
    private double rise, tideRise;
    private String sourceDimension = "minecraft:overworld";
    private BlockPos sourceController;
    private final Set<UUID> party = new HashSet<>();
    private final Set<BlockPos> platforms = new HashSet<>();
    private final Map<UUID, Long> rescueUntil = new HashMap<>();
    private final List<Portal> portals = new ArrayList<>();
    private record Portal(Vec3 origin, Vec3 target, long due) {}

    public AscensionFight(ChronicleKeeperEntity boss) { this.boss = boss; }
    public int mode() { return mode; }
    public double tide() { return 46 + tideRise - shift; }
    public AABB bounds() { return new AABB(siteX - 24, 0, siteZ - 24, siteX + 24, 2032, siteZ + 24); }

    public void begin(BlockPos controller, List<ServerPlayer> players) {
        sourceDimension = boss.level().dimension().location().toString();
        sourceController = controller;
        party.clear(); players.forEach(p -> party.add(p.getUUID()));
        mode = 1; ticks = 0; boss.setDeltaMovement(Vec3.ZERO);
        sync();
        ((ServerLevel)boss.level()).playSound(null, boss.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 3, .5f);
    }

    public void tick() {
        if (!(boss.level() instanceof ServerLevel level)) return;
        if (mode == 1) {
            boss.setDeltaMovement(Vec3.ZERO);
            level.sendParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY() + 2, boss.getZ(), 12, 2, 3, 2, .04);
            if (++ticks >= 45) transfer(level);
            else sync();
            return;
        }
        if (mode != 2) return;
        List<ServerPlayer> players = participants(level);
        if (players.isEmpty()) {
            boss.setDeltaMovement(Vec3.ZERO);
            if (++emptyTicks >= 200) { finish(false); boss.discard(); }
            return;
        }
        emptyTicks = 0;
        double ascentSpeed = TimeConfig.SECOND_PHASE_TIDE_SPEED.get();
        if (!Double.isFinite(ascentSpeed) || ascentSpeed <= 0) ascentSpeed = AscensionRules.RISE_PER_TICK;
        boolean moving = AscensionRules.climbing(ticks++);
        if (moving) {
            // One live speed drives all three moving parts of the arena.
            rise += ascentSpeed;
            tideRise += ascentSpeed;
        }
        if (64 + rise - shift > 1500) rebase(level, players);
        int platformSyncInterval = AscensionRules.platformSyncInterval(ascentSpeed);
        if ((moving && ticks % platformSyncInterval == 0) || platforms.isEmpty()) reconcilePlatforms(level);
        double y = 72 + rise - shift;
        // Descend to a reachable firing height during rests; no endless upward drift then.
        double angle = rise * .07;
        Vec3 goal = new Vec3(siteX + Math.cos(angle) * 3, y, siteZ + Math.sin(angle) * 3);
        double tracking = Math.max(.13, Math.min(.25, .13 + ascentSpeed * 1.2));
        boss.setDeltaMovement(goal.subtract(boss.position()).scale(tracking));
        if (!moving && boss.position().distanceToSqr(goal) < .0025) boss.setDeltaMovement(Vec3.ZERO);
        ServerPlayer target = players.get(Math.floorMod(ticks / 100, players.size()));
        boss.faceAscensionTarget(target);
        for (ServerPlayer player : players) {
            player.fallDistance = 0;
            if (player.getY() < tide() + .5 || Math.abs(player.getX() - siteX) > 23 || Math.abs(player.getZ() - siteZ) > 23) rescue(level, player);
        }
        int boltInterval = moving ? TimeConfig.SECOND_PHASE_BOLT_INTERVAL.get()
            : Math.max(20, TimeConfig.SECOND_PHASE_BOLT_INTERVAL.get() - 10);
        if (ticks > AscensionRules.ARRIVAL_TICKS && ticks % boltInterval == 0) {
            boss.castAscensionBolt(target);
            int count = Math.min(12, TimeConfig.SECOND_PHASE_BOLT_COUNT.get()
                + (boss.getHealth() < boss.getMaxHealth() * .5f ? 2 : 0));
            for (ServerPlayer player : players) if (!player.isCreative()) {
                Vec3 aim = player.getEyePosition();
                double start = boss.getRandom().nextDouble() * Math.PI * 2;
                for (int i = 0; i < count; i++) {
                    double a = start + i * Math.PI * 2 / count;
                    double dy = switch (i % 4) { case 0 -> 8; case 1 -> 0; case 2 -> -4; default -> 4; };
                    portals.add(new Portal(aim.add(Math.cos(a) * 12, dy, Math.sin(a) * 12), aim, level.getGameTime() + 24));
                }
            }
        }
        for (Iterator<Portal> it = portals.iterator(); it.hasNext();) {
            Portal portal = it.next();
            if (level.getGameTime() >= portal.due) {
                ChronalBoltEntity.fireFrom(boss, portal.origin, portal.target, (float)boss.getAttributeValue(Attributes.ATTACK_DAMAGE), .85);
                it.remove();
            } else if (ticks % 4 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, portal.origin.x, portal.origin.y, portal.origin.z, 6, .3, .3, .3, .005);
            }
        }
        if (ticks % 2 == 0) sync();
    }

    private void transfer(ServerLevel from) {
        ServerLevel destination = from.getServer().getLevel(DIMENSION);
        if (destination == null) {
            for (var player : participants(from)) player.sendSystemMessage(Component.translatable("message.time.sanctum_missing"));
            finish(false); boss.discard(); return;
        }
        List<ServerPlayer> players = participants(from);
        if (players.isEmpty()) { finish(false); boss.discard(); return; }
        int site = SanctumSites.allocate(destination);
        siteX = (site % 2048) * 256; siteZ = (site / 2048) * 256;
        mode = 2; ticks = 0; rise = 0; tideRise = 0; shift = 0;
        reconcilePlatforms(destination);
        boss.prepareAscensionHealth(); sync();
        var moved = boss.changeDimension(new DimensionTransition(destination, new Vec3(siteX, 72, siteZ), Vec3.ZERO, boss.getYRot(), 0, DimensionTransition.DO_NOTHING));
        if (!(moved instanceof ChronicleKeeperEntity keeper)) {
            cleanupPlatforms(destination); mode = 1;
            for (var player : players) player.sendSystemMessage(Component.translatable("message.time.sanctum_missing"));
            finish(false); boss.discard(); return;
        }
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            CompoundTag data = new CompoundTag();
            data.putString("Dimension", from.dimension().location().toString());
            data.putDouble("X", player.getX()); data.putDouble("Y", player.getY()); data.putDouble("Z", player.getZ());
            data.putUUID("Boss", keeper.getUUID());
            var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            persisted.put(RETURN, data); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            BlockPos landing = platformCenter(0, i % 3);
            player.teleportTo(destination, landing.getX() + .5, landing.getY() + 1.05, landing.getZ() + .5, 0, -20);
            player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
            player.sendSystemMessage(Component.translatable("message.time.sanctum_enter"));
        }
    }

    private List<ServerPlayer> participants(ServerLevel level) {
        return level.players().stream().filter(p -> party.contains(p.getUUID()) && p.isAlive() && !p.isSpectator()).toList();
    }
    private BlockPos platformCenter(int tier, int lane) {
        return new BlockPos(siteX + AscensionRules.x(tier, lane), AscensionRules.height(tier) - shift, siteZ + AscensionRules.z(tier, lane));
    }
    private void reconcilePlatforms(ServerLevel level) {
        Set<BlockPos> wanted = new HashSet<>();
        int tier = AscensionRules.tier(rise);
        for (int i = Math.max(0, tier - 3); i <= tier + 4; i++) for (int lane = 0; lane < 3; lane++) {
            BlockPos center = platformCenter(i, lane);
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) wanted.add(center.offset(x, 0, z));
            if (center.getY() < tide() + 5 && AscensionRules.climbing(ticks))
                level.sendParticles(ParticleTypes.END_ROD, center.getX() + .5, center.getY() + 1.1, center.getZ() + .5, 3, 1.3, .1, 1.3, .02);
        }
        for (BlockPos pos : platforms) if (!wanted.contains(pos) && level.getBlockState(pos).is(TimeContent.SANCTUM_PLATFORM.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 2, .2, .2, .2, .02);
        }
        for (BlockPos pos : wanted) if (level.isEmptyBlock(pos))
            level.setBlock(pos, TimeContent.SANCTUM_PLATFORM.get().defaultBlockState(), 2);
        platforms.clear(); platforms.addAll(wanted);
    }
    private void rescue(ServerLevel level, ServerPlayer player) {
        if (level.getGameTime() < rescueUntil.getOrDefault(player.getUUID(), 0L)) return;
        int tier = AscensionRules.tier(rise);
        BlockPos nearest = null; double distance = Double.MAX_VALUE;
        for (int i = Math.max(0, tier - 3); i <= tier + 4; i++) for (int lane = 0; lane < 3; lane++) {
            BlockPos p = platformCenter(i, lane);
            double d = Vec3.atCenterOf(p).distanceToSqr(player.position());
            if (p.getY() > tide() + 3 && d < distance) { nearest = p; distance = d; }
        }
        if (nearest == null) return;
        // Move first so an armorless player's death/loot also occurs on a recoverable platform.
        player.teleportTo(level, nearest.getX() + .5, nearest.getY() + 1.05, nearest.getZ() + .5, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
        if (!player.isCreative()) player.hurt(level.damageSources().magic(), Math.max(12, player.getMaxHealth() * .6f));
        rescueUntil.put(player.getUUID(), level.getGameTime() + 40);
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + .5, player.getZ(), 25, .6, .7, .6, .02);
        level.playSound(null, nearest, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 1, .5f);
    }
    private void rebase(ServerLevel level, List<ServerPlayer> players) {
        // Recycle physical height without ever introducing a ceiling into the ascent.
        cleanupPlatforms(level); shift += 1024; portals.clear(); reconcilePlatforms(level);
        for (ServerPlayer player : players) {
            Vec3 velocity = player.getDeltaMovement();
            player.teleportTo(level, player.getX(), player.getY() - 1024, player.getZ(), player.getYRot(), player.getXRot());
            player.setDeltaMovement(velocity); player.fallDistance = 0;
        }
        for (var bolt : level.getEntitiesOfClass(ChronalBoltEntity.class, bounds(), b -> b.getOwner() == boss)) bolt.discard();
        boss.teleportTo(boss.getX(), boss.getY() - 1024, boss.getZ());
    }
    private void cleanupPlatforms(ServerLevel level) {
        for (BlockPos pos : platforms) if (level.getBlockState(pos).is(TimeContent.SANCTUM_PLATFORM.get())) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        platforms.clear();
    }

    public void finish(boolean victory) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        for (UUID id : party) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.isAlive()) returnPlayer(player);
        }
        ResourceLocation id = ResourceLocation.tryParse(sourceDimension);
        ServerLevel origin = id == null ? null : level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (origin != null && sourceController != null) {
            origin.getChunkAt(sourceController);
            if (origin.getBlockEntity(sourceController) instanceof PuzzleControllerBlockEntity controller) {
                if (victory) controller.encounterDefeated(boss.getUUID()); else controller.abandonEncounter(boss.getUUID());
            }
        }
        if (mode == 2) cleanupPlatforms(level);
        portals.clear();
    }
    public static boolean returnPlayer(ServerPlayer player) {
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(RETURN)) return false;
        CompoundTag saved = persisted.getCompound(RETURN);
        ResourceLocation id = ResourceLocation.tryParse(saved.getString("Dimension"));
        ServerLevel destination = id == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (destination == null) destination = player.server.overworld();
        Vec3 landing = new Vec3(saved.getDouble("X"), saved.getDouble("Y"), saved.getDouble("Z"));
        destination.getChunkAt(BlockPos.containing(landing));
        for (int dy = 0; dy < 8 && !destination.noCollision(player, player.getBoundingBox().move(landing.subtract(player.position()))); dy++) landing = landing.add(0, 1, 0);
        player.teleportTo(destination, landing.x, landing.y, landing.z, player.getYRot(), 0);
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
        persisted.remove(RETURN); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }
    private void sync() {
        CompoundTag view = new CompoundTag();
        view.putInt("Mode", mode); view.putInt("Ticks", ticks);
        view.putDouble("Tide", tide()); view.putInt("X", siteX); view.putInt("Z", siteZ);
        view.putBoolean("Moving", mode == 2 && AscensionRules.climbing(ticks));
        ListTag gates = new ListTag();
        for (Portal portal : portals) {
            CompoundTag p = new CompoundTag(); p.putDouble("X", portal.origin.x); p.putDouble("Y", portal.origin.y); p.putDouble("Z", portal.origin.z);
            p.putDouble("TX", portal.target.x); p.putDouble("TY", portal.target.y); p.putDouble("TZ", portal.target.z); p.putLong("Due", portal.due); gates.add(p);
        }
        view.put("Portals", gates);
        boss.syncAscension(view, mode == 2 ? bounds() : boss.getBoundingBox().inflate(32));
    }
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mode", mode); tag.putInt("Ticks", ticks); tag.putDouble("Rise", rise);
        tag.putInt("Shift", shift); tag.putInt("X", siteX); tag.putInt("Z", siteZ); tag.putDouble("TideRise", tideRise); tag.putString("Source", sourceDimension);
        if (sourceController != null) tag.putLong("Controller", sourceController.asLong());
        ListTag members = new ListTag(); for (UUID id : party) members.add(StringTag.valueOf(id.toString())); tag.put("Party", members);
        tag.putLongArray("Platforms", platforms.stream().mapToLong(BlockPos::asLong).toArray());
        return tag;
    }
    public void load(CompoundTag tag) {
        mode = Math.max(0, Math.min(2, tag.getInt("Mode"))); ticks = Math.max(0, tag.getInt("Ticks"));
        rise = Math.max(0, tag.getDouble("Rise")); if (!Double.isFinite(rise)) rise = 0;
        shift = tag.getInt("Shift"); siteX = tag.getInt("X"); siteZ = tag.getInt("Z");
        tideRise = Math.max(0, tag.getDouble("TideRise")); if (!Double.isFinite(tideRise)) tideRise = rise;
        sourceDimension = tag.getString("Source"); sourceController = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null;
        party.clear(); for (Tag member : tag.getList("Party", Tag.TAG_STRING)) {
            try { party.add(UUID.fromString(member.getAsString())); } catch (IllegalArgumentException ignored) { }
        }
        platforms.clear(); for (long pos : tag.getLongArray("Platforms")) platforms.add(BlockPos.of(pos));
        portals.clear(); sync();
    }
}
