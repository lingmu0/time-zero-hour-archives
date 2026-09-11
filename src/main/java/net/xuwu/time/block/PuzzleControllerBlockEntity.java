package net.xuwu.time.block;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.xuwu.time.*;
import net.xuwu.time.api.*;
import net.xuwu.time.entity.*;
import net.xuwu.time.logic.*;
import net.xuwu.time.registry.TimeContent;
import net.xuwu.time.world.ChronicleWorldData;

public final class PuzzleControllerBlockEntity extends BlockEntity {
    private PuzzleKind kind = PuzzleKind.DAY_SEQUENCE;
    private final PuzzleState state = new PuzzleState();
    private final Set<UUID> claimed = new HashSet<>();
    private final List<BlockPos> gates = new ArrayList<>();
    private final List<BlockPos> nodes = new ArrayList<>();
    private BlockPos previous;
    private BlockPos min = BlockPos.ZERO, max = BlockPos.ZERO, spawn = BlockPos.ZERO;
    private boolean configured, paid;
    private UUID encounter;
    private long retryAt, demoAt = -1;
    private int missingTicks;
    private final Map<UUID, Long> footCooldown = new HashMap<>();

    public PuzzleControllerBlockEntity(BlockPos pos, BlockState blockState) { super(TimeContent.CONTROLLER_BE.get(), pos, blockState); }
    public void configure(PuzzleKind kind, BlockPos min, BlockPos max, BlockPos spawn, BlockPos previous, List<BlockPos> gates, List<BlockPos> nodes) {
        if (configured) return;
        this.kind = kind; this.min = min; this.max = max; this.spawn = spawn; this.previous = previous;
        this.gates.addAll(gates); this.nodes.addAll(nodes); configured = true; setChanged();
    }
    public PuzzleKind kind() { return kind; }
    public boolean solved() { return state.solved(); }
    public BlockPos spawn() { return spawn; }
    public AABB arena() { return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1); }
    public UUID encounterId() { return encounter; }
    public List<ServerPlayer> players() {
        if (!(level instanceof ServerLevel server)) return List.of();
        return server.getEntitiesOfClass(ServerPlayer.class, arena(), p -> p.isAlive() && !p.isSpectator());
    }
    private boolean ready(ServerLevel server) {
        return previous == null || ChronicleWorldData.get(server).completed(previous);
    }
    private void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.time." + key, args), true);
    }

    public void interact(ServerPlayer player, InteractionHand hand, int index, BlockPos node, boolean inspect) {
        if (!(level instanceof ServerLevel server) || !configured || !arena().inflate(3).contains(player.position())) return;
        if (index == PuzzleNodeBlockEntity.HINT || inspect) {
            player.sendSystemMessage(Component.translatable("clue.time." + kind.key()));
            if (index >= 0) player.sendSystemMessage(Component.translatable("node.time." + kind.key() + "." + index));
            if (kind == PuzzleKind.DELAY_BELLS && index == PuzzleNodeBlockEntity.HINT) demoAt = server.getGameTime();
            return;
        }
        if (!ready(server)) { message(player, "previous_room"); return; }
        if (state.solved()) {
            if (kind == PuzzleKind.BOSS_ARENA && index == PuzzleNodeBlockEntity.START && player.getItemInHand(hand).is(TimeContent.ZERO_KEY.get())) {
                startEncounter(player, hand);
                if (encounter != null) {
                    state.reset(server.getGameTime()); claimed.clear();
                    ChronicleWorldData.get(server).clear(getBlockPos()); refreshNodes(); setChanged();
                }
            } else claim(player);
            return;
        }
        if (index == PuzzleNodeBlockEntity.RESET) {
            if (encounter == null) { state.reset(server.getGameTime()); refreshNodes(); setChanged(); message(player, "reset"); }
            return;
        }
        if (index == PuzzleNodeBlockEntity.CLAIM) { message(player, "unsolved"); return; }
        if (kind == PuzzleKind.BOSS_ARENA || kind == PuzzleKind.ARCHIVE_GUARDIAN) {
            if (encounter == null) {
                if (index == PuzzleNodeBlockEntity.START) startEncounter(player, hand);
            } else if (kind == PuzzleKind.BOSS_ARENA && server.getEntity(encounter) instanceof ChronicleKeeperEntity boss && index >= 0) {
                boss.activateAnchor(player, index);
            }
            return;
        }
        long now = server.getGameTime();
        PuzzleState.Result result;
        switch (kind) {
            case DAY_SEQUENCE -> result = index >= 0 ? state.press(index, new int[]{0, 1, 2, 3}, now) : PuzzleState.Result.PROGRESS;
            case SHADOW_DIALS -> result = index == PuzzleNodeBlockEntity.CHECK
                ? state.checkDials(new int[]{1, 3, 0, 2}, now)
                : state.rotate(index, now);
            case FROZEN_RECORDS -> {
                if (!player.getItemInHand(hand).is(TagKey.create(Registries.ITEM, TimeMod.id("temporal_samples")))) {
                    message(player, "needs_sample"); return;
                }
                if (!player.isCreative()) player.getItemInHand(hand).shrink(1);
                state.solve(); result = PuzzleState.Result.SOLVED;
            }
            case ARCHIVE_ORDER -> result = index >= 0 ? state.press(index, new int[]{2, 0, 4, 1, 5, 3}, now) : PuzzleState.Result.PROGRESS;
            case MIRROR_PATH -> result = index >= 0 ? state.press(index, new int[]{1, 2, 5}, now) : PuzzleState.Result.PROGRESS;
            case DELAY_BELLS -> result = index >= 0 ? state.bell(index, now) : PuzzleState.Result.PROGRESS;
            case PHASE_SEALS -> {
                Item[] seals = {TimeContent.PAST_SEAL.get(), TimeContent.PRESENT_SEAL.get(), TimeContent.BLANK_SEAL.get()};
                if (index < 0 || index > 2) return;
                if (!player.getItemInHand(hand).is(seals[index])) { message(player, "needs_seal", new ItemStack(seals[index]).getHoverName()); return; }
                // Imprints are scanned, not destroyed. An incorrect order never loses quest tokens.
                result = state.press(index, new int[]{0, 1, 2}, now);
            }
            default -> { return; }
        }
        if (result == PuzzleState.Result.SOLVED) complete(server);
        else if (result == PuzzleState.Result.WRONG) {
            TimeProgress.strain(player, 3); message(player, "wrong");
            if (!player.isCreative()) spawnFailureEcho(server);
            server.playSound(null, node, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, .8f, .6f);
        } else if (result == PuzzleState.Result.COOLDOWN) message(player, "wait");
        else {
            message(player, "progress", state.progress());
            server.playSound(null, node, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1, .7f + Math.max(0, index) * .15f);
        }
        refreshNodes(); setChanged();
    }

    public void step(ServerPlayer player, int index, BlockPos node) {
        if (kind != PuzzleKind.MIRROR_PATH || level == null || state.solved()) return;
        long now = level.getGameTime();
        if (now < footCooldown.getOrDefault(player.getUUID(), 0L)) return;
        // Stepping on an already cleared row must not reset a party's progress.
        if (index / 2 < state.progress()) return;
        footCooldown.put(player.getUUID(), now + 30);
        interact(player, InteractionHand.MAIN_HAND, index, node, false);
    }

    private void startEncounter(ServerPlayer player, InteractionHand hand) {
        startEncounter(player,hand,false);
    }
    /** Only the dedicated challenge controller may bypass quest entry checks. */
    public boolean startChallenge(ServerPlayer player) {
        if(!(level instanceof ServerLevel server)||!configured||kind!=PuzzleKind.BOSS_ARENA
            ||!server.dimension().equals(net.xuwu.time.world.ChallengeArena.DIMENSION)
            ||!getBlockPos().equals(net.xuwu.time.world.ChallengeArena.CONTROLLER)||!arena().contains(player.position()))return false;
        if(encounter!=null){
            if(server.getEntity(encounter) instanceof ChronicleKeeperEntity boss&&boss.isAlive())return true;
            message(player,"retry_wait");return false;
        }
        startEncounter(player,InteractionHand.MAIN_HAND,true);
        if(encounter==null)return false;
        state.reset(server.getGameTime());claimed.clear();showAnchors(0,false);setChanged();return true;
    }
    private void startEncounter(ServerPlayer player, InteractionHand hand, boolean freeChallenge) {
        if (!(level instanceof ServerLevel server) || server.getGameTime() < retryAt) { message(player, "retry_wait"); return; }
        if (server.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) { message(player, "peaceful"); return; }
        if (kind == PuzzleKind.BOSS_ARENA && !freeChallenge && !paid && !player.isCreative() && !player.getItemInHand(hand).is(TimeContent.ZERO_KEY.get())) {
            message(player, "needs_key"); return;
        }
        Mob mob;
        if (kind == PuzzleKind.BOSS_ARENA) {
            var boss = TimeContent.CHRONICLE_KEEPER.get().create(server);
            if (boss == null) return;
            boss.bind(getBlockPos(), spawn, (int)players().stream().filter(p -> !p.isCreative()).count());
            mob = boss;
        } else {
            var scribe = TimeContent.ARCHIVE_SCRIBE.get().create(server);
            if (scribe == null) return;
            scribe.bind(getBlockPos(), spawn); mob = scribe;
        }
        mob.moveTo(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5, 180, 0);
        mob.setPersistenceRequired();
        if (!server.addFreshEntity(mob)) return;
        if (kind == PuzzleKind.BOSS_ARENA && !freeChallenge && !paid && !player.isCreative()) player.getItemInHand(hand).shrink(1);
        paid = true; encounter = mob.getUUID(); missingTicks = 0;
        if (kind == PuzzleKind.BOSS_ARENA) showAnchors(0, false);
        setChanged();
    }

    private void spawnFailureEcho(ServerLevel server) {
        if (server.getEntitiesOfClass(TemporalEchoEntity.class, arena(), e -> e.isAlive() && e.mode() == TemporalEchoEntity.HOSTILE).size() >= 2) return;
        var echo = TimeContent.TEMPORAL_ECHO.get().create(server);
        if (echo == null) return;
        echo.configure(TemporalEchoEntity.HOSTILE, null, 400);
        for (int dx : new int[]{2,-2,4,-4}) {
            echo.moveTo(spawn.getX() + dx + .5, min.getY() + 1, spawn.getZ() + 1.5, 0, 0);
            if (arena().contains(echo.position()) && server.noCollision(echo)) { server.addFreshEntity(echo); return; }
        }
    }

    public void encounterDefeated(UUID id) {
        if (level instanceof ServerLevel server && id.equals(encounter)) {
            encounter = null; paid = false; complete(server);
        }
    }

    public void abandonEncounter(UUID id) {
        if (id.equals(encounter) && level != null) {
            encounter = null; missingTicks = 0;
            retryAt = level.getGameTime() + TimeConfig.RETRY_DELAY.get() * 20L;
            setChanged();
        }
    }

    private void complete(ServerLevel server) {
        state.solve(); ChronicleWorldData.get(server).mark(getBlockPos()); openGates();
        server.sendParticles(ParticleTypes.END_ROD, spawn.getX() + .5, spawn.getY() + 1, spawn.getZ() + .5, 40, 3, 1, 3, .02);
        server.playSound(null, spawn, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1, .75f);
        for (var player : players()) claim(player);
        setChanged();
    }

    public void claim(ServerPlayer player) {
        if (!state.solved() || !claimed.add(player.getUUID())) { message(player, state.solved() ? "claimed" : "unsolved"); return; }
        switch (kind) {
            case FROZEN_RECORDS -> { give(player, TimeContent.PAST_RECORD.toStack()); give(player, TimeContent.PAST_SEAL.toStack()); give(player, TimeContent.CHRONICLE_PAGE.toStack()); }
            case ARCHIVE_GUARDIAN -> { give(player, TimeContent.PRESENT_RECORD.toStack()); give(player, TimeContent.PRESENT_SEAL.toStack()); give(player, TimeContent.CHRONICLE_PAGE.toStack()); }
            case BOSS_ARENA -> { give(player, TimeContent.EPOCH_CORE.toStack()); give(player, TimeContent.FUTURE_RECORD.toStack()); give(player, new ItemStack(TimeContent.CHRONICLE_PAGE.get(), 2)); }
            default -> {}
        }
        player.giveExperiencePoints(kind == PuzzleKind.BOSS_ARENA ? 300 : 20);
        TimeProgress.award(player, "puzzles/" + kind.key());
        NeoForge.EVENT_BUS.post(new PuzzleSolvedEvent(player, getBlockPos(), kind));
        message(player, "solved"); setChanged();
    }
    private static void give(ServerPlayer player, ItemStack stack) { if (!player.getInventory().add(stack)) player.drop(stack, false); }

    private void openGates() {
        if (level == null) return;
        for (var pos : gates) if (level.hasChunkAt(pos) && level.getBlockState(pos).is(TimeContent.TEMPORAL_BARRIER.get())) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    private void refreshNodes() {
        if (level == null) return;
        for (int i = 0; i < nodes.size(); i++) {
            BlockPos pos = nodes.get(i);
            if (!level.hasChunkAt(pos)) continue;
            BlockState block = level.getBlockState(pos);
            if (!block.is(TimeContent.NODE.get())) continue;
            int[] sequence = switch (kind) {
                case ARCHIVE_ORDER -> new int[]{2,0,4,1,5,3};
                case MIRROR_PATH -> new int[]{1,2,5};
                default -> new int[]{0,1,2,3,4,5};
            };
            boolean lit = state.solved();
            for (int step = 0; step < Math.min(state.progress(), sequence.length); step++) if (sequence[step] == i) lit = true;
            if (kind == PuzzleKind.SHADOW_DIALS && i < 4) {
                lit = state.dial(i) == new int[]{1, 3, 0, 2}[i];
                block = block.setValue(PuzzleNodeBlock.TURN, state.dial(i));
            }
            level.setBlockAndUpdate(pos, block.setValue(PuzzleNodeBlock.LIT, lit));
        }
    }

    public void showAnchors(int litMask, boolean central) {
        if (level == null) return;
        for (int i = 0; i < nodes.size(); i++) {
            var pos = nodes.get(i);
            if (!level.hasChunkAt(pos)) continue;
            var block = level.getBlockState(pos);
            if (!block.is(TimeContent.NODE.get())) continue;
            boolean lit = i == 4 && central;
            if (i < 4 && (litMask & (1 << i)) != 0) lit = true;
            level.setBlockAndUpdate(pos, block.setValue(PuzzleNodeBlock.LIT, lit));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, PuzzleControllerBlockEntity be) {
        if (!(level instanceof ServerLevel server) || !be.configured) return;
        long now = server.getGameTime();
        if (be.kind == PuzzleKind.DELAY_BELLS) {
            int before = be.state.progress(); be.state.expireBell(now);
            if (before != be.state.progress()) { be.setChanged(); be.refreshNodes(); }
            if (be.demoAt >= 0) {
                long elapsed = now - be.demoAt;
                if (elapsed == 1 || elapsed == 40 || elapsed == 120) {
                    server.playSound(null, be.spawn, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 2, elapsed < 40 ? .7f : elapsed < 120 ? .9f : 1.1f);
                    for (var player : be.players()) player.displayClientMessage(Component.translatable("message.time.bell_demo", elapsed < 40 ? 1 : elapsed < 120 ? 2 : 3), true);
                }
                if (elapsed > 120) be.demoAt = -1;
            }
        }
        if (now % 20 != 0) return;
        be.footCooldown.entrySet().removeIf(e -> e.getValue() < now);
        if (be.state.solved()) be.openGates();
        if (be.encounter != null) {
            Entity entity = server.getEntity(be.encounter);
            if (entity == null) for (ServerLevel other : server.getServer().getAllLevels()) {
                Entity candidate = other.getEntity(be.encounter);
                if (candidate instanceof ChronicleKeeperEntity && candidate.isAlive()) { entity = candidate; break; }
            }
            if (entity == null) {
                // Wait for adjacent entity chunks to load before considering a missing boss abandoned.
                if (!be.players().isEmpty() && ++be.missingTicks >= 10) be.abandonEncounter(be.encounter);
            } else {
                be.missingTicks = 0;
                if (be.players().stream().noneMatch(p -> !p.isCreative()) || !entity.isAlive()) {
                    be.missingTicks = 0;
                    // The entity handles the 10-second empty-arena timer, allowing creative testing.
                }
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putBoolean("Configured", configured); tag.putInt("Kind", kind.ordinal());
        tag.putBoolean("Solved", state.solved()); tag.putInt("Progress", state.progress()); tag.putIntArray("Dials", state.dials()); tag.putLong("Cooldown", state.cooldownUntil());
        tag.putLong("Min", min.asLong()); tag.putLong("Max", max.asLong()); tag.putLong("Spawn", spawn.asLong());
        if (previous != null) tag.putLong("Previous", previous.asLong());
        tag.putLongArray("Gates", gates.stream().mapToLong(BlockPos::asLong).toArray()); tag.putLongArray("Nodes", nodes.stream().mapToLong(BlockPos::asLong).toArray());
        ListTag rewards = new ListTag(); claimed.forEach(id -> rewards.add(StringTag.valueOf(id.toString()))); tag.put("Claimed", rewards);
        if (encounter != null) tag.putUUID("Encounter", encounter);
        tag.putBoolean("Paid", paid); tag.putLong("RetryAt", retryAt);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        configured = tag.getBoolean("Configured"); kind = PuzzleKind.safe(tag.getInt("Kind"));
        state.restore(tag.getInt("Progress"), tag.getIntArray("Dials"), tag.getBoolean("Solved"), tag.getLong("Cooldown"));
        if (kind == PuzzleKind.DELAY_BELLS) state.clearRhythm();
        min = BlockPos.of(tag.getLong("Min")); max = BlockPos.of(tag.getLong("Max")); spawn = BlockPos.of(tag.getLong("Spawn"));
        previous = tag.contains("Previous") ? BlockPos.of(tag.getLong("Previous")) : null;
        gates.clear(); for (long value : tag.getLongArray("Gates")) gates.add(BlockPos.of(value));
        nodes.clear(); for (long value : tag.getLongArray("Nodes")) nodes.add(BlockPos.of(value));
        claimed.clear(); for (Tag value : tag.getList("Claimed", Tag.TAG_STRING)) {
            try { claimed.add(UUID.fromString(value.getAsString())); } catch (IllegalArgumentException ignored) {}
        }
        encounter = tag.hasUUID("Encounter") ? tag.getUUID("Encounter") : null;
        paid = tag.getBoolean("Paid"); retryAt = tag.getLong("RetryAt");
    }
}
