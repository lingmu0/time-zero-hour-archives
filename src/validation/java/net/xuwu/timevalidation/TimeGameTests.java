package net.xuwu.timevalidation;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;
import net.xuwu.time.block.*;
import net.xuwu.time.entity.*;
import net.xuwu.time.logic.*;
import net.xuwu.time.registry.TimeContent;
import net.xuwu.time.research.*;
import net.xuwu.time.world.*;

/** Integration tests execute registered blocks/entities/recipes in a real Minecraft server. */
@GameTestHolder("time_validation")
@PrefixGameTestTemplate(false)
public final class TimeGameTests {
    private static int site;
    // Validation-only access to server NBT. Production never sends the answer to a player.
    public static int[] anchorOrder(ChronicleKeeperEntity boss) {
        CompoundTag tag=new CompoundTag();boss.save(tag);return tag.getIntArray("AnchorOrder");
    }
    public static void unlockShield(ChronicleKeeperEntity boss,ServerPlayer player) {
        CompoundTag tag=new CompoundTag();boss.save(tag);
        int[] order=tag.getIntArray("AnchorOrder");
        for(int i=tag.getInt("Anchors");i<order.length&&boss.shielded();i++)boss.activateAnchor(player,order[i]);
    }
    public static BlockPos build(ServerLevel level, int variant) {
        BlockPos origin = new BlockPos(512 + site++ * 96, 80, 512);
        buildAt(level, origin, variant, origin.getY() + (variant == 0 ? 0 : 32));
        return origin;
    }
    public static void buildAt(ServerLevel level, BlockPos origin, int variant, int surface) {
        TimeRuinPiece piece = new TimeRuinPiece(origin, variant, surface);
        var box = piece.getBoundingBox();
        // Reverse chunk order catches accidental dependence on controller-first generation.
        for (int cx = box.maxX() >> 4; cx >= box.minX() >> 4; cx--) {
            for (int cz = box.maxZ() >> 4; cz >= box.minZ() >> 4; cz--) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.random,
                    new BoundingBox(cx * 16, level.getMinBuildHeight(), cz * 16, cx * 16 + 15, level.getMaxBuildHeight() - 1, cz * 16 + 15),
                    new ChunkPos(cx, cz), origin);
            }
        }
    }
    private static void release(ServerLevel level, BlockPos origin, int variant) {
        var box = new TimeRuinPiece(origin, variant, origin.getY() + 32).getBoundingBox();
        for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++)
            for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) level.setChunkForced(x, z, false);
    }
    private static FakePlayer player(ServerLevel level, BlockPos at) {
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TimeTest"));
        player.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
        player.setGameMode(GameType.SURVIVAL);
        level.addNewPlayer(player);
        return player;
    }
    private static PuzzleControllerBlockEntity controller(ServerLevel l, BlockPos o, int z) {
        return (PuzzleControllerBlockEntity)Objects.requireNonNull(l.getBlockEntity(o.offset(12,0,z)));
    }
    private static void press(PuzzleControllerBlockEntity c, ServerPlayer p, int... indices) {
        for (int i : indices) c.interact(p, InteractionHand.MAIN_HAND, i, c.getBlockPos(), false);
    }
    private static void stand(ServerPlayer p, BlockPos o, int z) { p.moveTo(o.getX()+12.5,o.getY()+1,o.getZ()+z+.5,0,0); }
    private static int count(ServerPlayer p, Item item) { return p.getInventory().countItem(item); }
    private static CompoundTag saved(BlockEntity be) { return be.saveWithFullMetadata(be.getLevel().registryAccess()); }
    private static <T extends BlockEntity> T reload(ServerLevel l, T be) {
        CompoundTag nbt = saved(be);
        BlockPos pos = be.getBlockPos();
        var state = be.getBlockState();
        l.removeBlockEntity(pos);
        @SuppressWarnings("unchecked") T fresh = (T)BlockEntity.loadStatic(pos,state,nbt,l.registryAccess());
        l.setBlockEntity(fresh);
        return fresh;
    }

    @GameTest(template="empty", timeoutTicks=200)
    public static void observatory_chain_and_persistence(GameTestHelper h) {
        ServerLevel l=h.getLevel(); BlockPos o=build(l,0); FakePlayer p=player(l,o.offset(12,1,8));
        var day=controller(l,o,10); var shadow=controller(l,o,34); var frozen=controller(l,o,58);
        h.assertTrue(day.kind()==PuzzleKind.DAY_SEQUENCE,"First room kind");
        var sign=(SignBlockEntity)l.getBlockEntity(o.offset(5,2,3));
        h.assertTrue(sign.isWaxed() && sign.getFrontText().getMessage(1,false).getString().contains("时序"),"Worldgen sign text");
        // Calling the loader on an unattached sign exactly matches ProtoChunk lifecycle.
        var unattached=new SignBlockEntity(sign.getBlockPos(),sign.getBlockState());
        unattached.loadWithComponents(saved(sign),l.registryAccess());
        h.assertTrue(unattached.getLevel()==null && unattached.isWaxed(),"Unattached sign survives NBT initialization");
        h.assertTrue(l.getBlockEntity(o.offset(3,1,3)) instanceof BrushableBlockEntity,"Archaeology sample survives room decoration");
        stand(p,o,34); press(shadow,p,0,1,1,1,3,3,PuzzleNodeBlockEntity.CHECK);
        h.assertTrue(!shadow.solved(),"Cannot skip locked predecessor");
        stand(p,o,10); press(day,p,0,1); day=reload(l,day); press(day,p,2,3);
        h.assertTrue(day.solved(),"Day sequence survives block entity reload");
        h.assertTrue(l.getBlockState(o.offset(12,1,20)).isAir(),"Door physically opens");
        h.assertTrue(ChronicleWorldData.get(l).completed(day.getBlockPos()),"Cross-chunk completed flag");
        stand(p,o,34); press(shadow,p,0,1,1,1,3,3,PuzzleNodeBlockEntity.CHECK);
        h.assertTrue(shadow.solved(),"Dials solve after prerequisite");
        stand(p,o,58); p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIRT)); press(frozen,p,0);
        h.assertTrue(!frozen.solved(),"Reject invalid sample");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TimeContent.TEMPORAL_DUST.get(),2)); press(frozen,p,0);
        h.assertTrue(frozen.solved() && p.getMainHandItem().getCount()==1,"Consume exactly one sample");
        h.assertTrue(count(p,TimeContent.PAST_SEAL.get())==1 && count(p,TimeContent.PAST_RECORD.get())==1,"Research rewards");
        frozen=reload(l,frozen); frozen.claim(p);
        h.assertTrue(count(p,TimeContent.PAST_SEAL.get())==1,"No duplicate rewards after reload");
        p.discard(); release(l,o,0); h.succeed();
    }

    @GameTest(template="empty", timeoutTicks=400)
    public static void archive_rhythm_and_guardian(GameTestHelper h) {
        ServerLevel l=h.getLevel(); BlockPos o=build(l,1); FakePlayer p=player(l,o.offset(12,1,10));
        var order=controller(l,o,10); press(order,p,2,0,4,1,5,3);
        h.assertTrue(!l.getBlockState(o.offset(12,32,-6)).isAir(),"Safe surface landing beside archive ladder");
        h.assertTrue(order.solved(),"Six records sorted");
        var mirror=controller(l,o,34); stand(p,o,34); press(mirror,p,1,2,5);
        h.assertTrue(mirror.solved(),"Mirror path solved");
        var bells=controller(l,o,58); stand(p,o,58); press(bells,p,0);
        // Verify restart cancels partial rhythms, without claiming them as solved.
        var loaded=reload(l,bells);
        h.assertTrue(saved(loaded).getInt("Progress")==0,"Bell rhythm resets across reload");
        press(loaded,p,0);
        h.runAfterDelay(40,()->press(loaded,p,1));
        h.runAfterDelay(120,()->{
            press(loaded,p,2); h.assertTrue(loaded.solved(),"Actual server tick bell timing");
            stand(p,o,82); var guard=controller(l,o,82); press(guard,p,PuzzleNodeBlockEntity.START);
            h.assertTrue(guard.encounterId()!=null,"Guardian spawned");
            var entity=(ArchiveScribeEntity)l.getEntity(guard.encounterId());
            h.assertTrue(entity!=null && entity.isAlive(),"Guardian registered and alive");
            entity.hurt(l.damageSources().genericKill(),10000);
            h.assertTrue(guard.solved(),"Guardian death opens reward state");
            h.assertTrue(count(p,TimeContent.PRESENT_SEAL.get())==1,"Guardian reward once");
            guard.claim(p); h.assertTrue(count(p,TimeContent.PRESENT_SEAL.get())==1,"Guardian claim idempotent");
            p.discard(); release(l,o,1); h.succeed();
        });
    }

    @GameTest(template="empty", timeoutTicks=1200)
    public static void research_real_ticks_and_output_backpressure(GameTestHelper h) {
        var l=h.getLevel(); BlockPos at=h.absolutePos(new BlockPos(2,2,2));
        l.setBlockAndUpdate(at,TimeContent.RESEARCH_DESK.get().defaultBlockState());
        var desk=(ResearchDeskBlockEntity)l.getBlockEntity(at);
        desk.setItem(0,TimeContent.PAST_RECORD.toStack()); desk.setItem(1,new ItemStack(TimeContent.TEMPORAL_DUST.get(),2)); desk.setItem(2,new ItemStack(Items.BOOK,2));
        h.runAfterDelay(210,()->{
            h.assertTrue(desk.data.get(0)>0 && desk.data.get(0)<400,"Research ticks naturally on server");
            int progress=desk.data.get(0); var fresh=reload(l,desk);
            h.assertTrue(fresh.data.get(0)==progress,"Research progress persists");
        });
        h.runAfterDelay(410,()->{
            var fresh=(ResearchDeskBlockEntity)l.getBlockEntity(at);
            h.assertTrue(fresh.getItem(3).is(TimeContent.PAST_CONCLUSION.get()),"First research produces conclusion");
            h.assertTrue(fresh.getItem(0).getCount()==1 && fresh.getItem(1).getCount()==1 && fresh.getItem(2).getCount()==1,"Correct input consumption");
            fresh.setItem(3,new ItemStack(Items.COBBLESTONE,64));
        });
        h.runAfterDelay(830,()->{
            var fresh=(ResearchDeskBlockEntity)l.getBlockEntity(at);
            h.assertTrue(fresh.getItem(1).getCount()==1,"Blocked output never consumes catalyst");
            fresh.setItem(3,ItemStack.EMPTY);
            fresh.setItem(0,TimeContent.PRESENT_RECORD.toStack());fresh.setItem(1,new ItemStack(Items.AMETHYST_SHARD));fresh.setItem(2,TimeContent.PAST_CONCLUSION.toStack());
            // Remaining recipes use the real registered recipe manager and workstation implementation.
            for(int t=0;t<600;t++) ResearchDeskBlockEntity.tick(l,at,fresh.getBlockState(),fresh);
            h.assertTrue(fresh.getItem(3).is(TimeContent.PRESENT_CONCLUSION.get()) && fresh.getItem(2).is(TimeContent.PAST_CONCLUSION.get()),"Present research preserves prior conclusion");
            fresh.setItem(3,ItemStack.EMPTY);fresh.setItem(0,TimeContent.FUTURE_RECORD.toStack());fresh.setItem(1,TimeContent.CHRONICLE_PAGE.toStack());fresh.setItem(2,TimeContent.EPOCH_CORE.toStack());
            for(int t=0;t<800;t++) ResearchDeskBlockEntity.tick(l,at,fresh.getBlockState(),fresh);
            h.assertTrue(fresh.getItem(3).is(TimeContent.FINAL_CONCLUSION.get()) && fresh.getItem(2).is(TimeContent.EPOCH_CORE.get()),"Final research preserves core");
            h.succeed();
        });
    }

    @GameTest(template="empty", timeoutTicks=300)
    public static void boss_shields_damage_gates_and_rewards(GameTestHelper h) {
        ServerLevel l=h.getLevel(); BlockPos o=build(l,2); FakePlayer p=player(l,o.offset(12,1,10));
        var seals=controller(l,o,10);
        Item[] keys={TimeContent.PAST_SEAL.get(),TimeContent.PRESENT_SEAL.get(),TimeContent.BLANK_SEAL.get()};
        for(int i=0;i<3;i++){p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(keys[i]));press(seals,p,i);h.assertTrue(p.getMainHandItem().getCount()==1,"Seal is scanned, never consumed");}
        h.assertTrue(seals.solved(),"Three seals unlock arena");
        stand(p,o,40); var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);press(arena,p,PuzzleNodeBlockEntity.START);
        h.assertTrue(arena.encounterId()==null,"Missing key rejects start");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TimeContent.ZERO_KEY.get(),2));press(arena,p,PuzzleNodeBlockEntity.START);
        h.assertTrue(p.getMainHandItem().getCount()==1,"Start charges exactly one key");
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(arena.encounterId()) instanceof ChronicleKeeperEntity,"Boss becomes queryable after chunk tracking starts")).thenExecute(()->{
        var boss=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());
        h.assertTrue(boss!=null && boss.shielded(),"Boss starts shielded");
        float max=boss.getMaxHealth();boss.hurt(l.damageSources().playerAttack(p),10000);
        h.assertTrue(boss.getHealth()==max,"Shield rejects damage");
        press(arena,p,(anchorOrder(boss)[0]+1)%4);h.assertTrue(boss.shielded(),"Wrong anchor order keeps shield");
        unlockShield(boss,p);h.assertTrue(!boss.shielded(),"Four random anchors unlock shield");
        // Huge actual damage exercises the NeoForge post-armor event cap, not just pure math.
        float[] floor={.8f,.6f,.35f,.15f};
        for(int phase=0;phase<4;phase++) {
            unlockShield(boss,p);
            if(phase==2){p.moveTo(o.getX()+2.5,o.getY()+1,o.getZ()+32.5,0,0);}
            boss.invulnerableTime=0;
            boss.hurt(phase%2==0?l.damageSources().playerAttack(p):l.damageSources().fellOutOfWorld(),1000000);
            h.assertTrue(Math.abs(boss.getHealth()-max*floor[phase])<.01,"Cannot skip phase "+phase+" through armor");
            boss.invulnerableTime=0;boss.hurt(l.damageSources().fellOutOfWorld(),1000000);
            h.assertTrue(Math.abs(boss.getHealth()-max*floor[phase])<.01,"Repeated same-tick bypass-tag hits cannot skip transition");
            boss.tick(); h.assertTrue(boss.phase()==phase+1,"Phase transition "+phase);
            h.assertTrue(boss.shielded(),"Each new phase raises an invincible shield");
            float shieldHp=boss.getHealth();boss.invulnerableTime=0;boss.hurt(l.damageSources().fellOutOfWorld(),1000000);
            h.assertTrue(boss.getHealth()==shieldHp,"Phase shield blocks bypass-tag burst");
        }
        h.assertTrue(boss.shielded()&&l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.mode()==TemporalEchoEntity.PARADOX).isEmpty(),"Final shield precedes false body");
        unlockShield(boss,p);h.assertTrue(!boss.shielded(),"Final random sequence breaks shield");
        boss.invulnerableTime=0;boss.hurt(l.damageSources().playerAttack(p),10000);
        h.assertTrue(arena.solved() && !boss.isAlive(),"Final boss defeated");
        h.assertTrue(l.getBlockState(o.offset(12,1,64)).isAir(),"Vault door opens");
        h.assertTrue(count(p,TimeContent.EPOCH_CORE.get())==1,"Boss core awarded once");
        reload(l,arena).claim(p);h.assertTrue(count(p,TimeContent.EPOCH_CORE.get())==1,"Boss reward persists across reload");
        p.discard();release(l,o,2);h.succeed();
        });
    }

    @GameTest(template="empty", timeoutTicks=600)
    public static void boss_memory_echoes_control_rewind(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(12,1,40));
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        UUID encounter=arena.encounterId();h.assertTrue(encounter!=null,"Controller successfully reserves memory encounter: "+saved(arena));
        ChronicleKeeperEntity[] boss=new ChronicleKeeperEntity[1];
        long started=l.getGameTime();
        // Forced chunks become entity-ticking asynchronously. Anchor delays to the
        // first observed memory window; the response window is now ten seconds.
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(encounter) instanceof ChronicleKeeperEntity,"Spawned boss becomes visible after chunk tracking starts"))
        .thenExecute(()->{boss[0]=(ChronicleKeeperEntity)l.getEntity(encounter);boss[0].setHealth(boss[0].getMaxHealth()*.72f);boss[0].tick();unlockShield(boss[0],p);})
        .thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.ownedBy(encounter) && e.mode()==TemporalEchoEntity.MEMORY).size()==3,"Past phase creates three memory echoes"))
        .thenExecute(()->com.mojang.logging.LogUtils.getLogger().info("TIME_MEMORY_WINDOW worldTicks={} bossTicks={}",l.getGameTime()-started,boss[0].tickCount))
        .thenIdle(150).thenExecute(()->{
            var echoes=l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.isAlive() && e.ownedBy(encounter) && e.mode()==TemporalEchoEntity.MEMORY);
            h.assertTrue(echoes.size()==3,"Memories survive the former seven-second expiry until rewind resolution");
            h.assertTrue(Math.abs(boss[0].getHealth()-boss[0].getMaxHealth()*.72f)<.01,"Rewind does not resolve before ten seconds");
            echoes.stream().limit(2).forEach(e->e.hurt(l.damageSources().playerAttack(p),100));
            h.assertTrue(echoes.stream().filter(TemporalEchoEntity::isAlive).count()==1,"Leave exactly one memory to trigger rewind");
        }).thenIdle(60).thenExecute(()->{
            h.assertTrue(Math.abs(boss[0].getHealth()-boss[0].getMaxHealth()*.8f)<.01,"Surviving echoes restore recorded time");
            boss[0].setHealth(boss[0].getMaxHealth()*.7f);
        }).thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.ownedBy(encounter) && e.mode()==TemporalEchoEntity.MEMORY).size()==3,"Second memory window opens"))
        .thenExecute(()->{
            var echoes=l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.ownedBy(encounter) && e.mode()==TemporalEchoEntity.MEMORY);
            h.assertTrue(echoes.size()==3,"Second memory window opens");
            echoes.forEach(e->e.hurt(l.damageSources().playerAttack(p),100));
        }).thenIdle(210).thenExecute(()->{
            h.assertTrue(Math.abs(boss[0].getHealth()-boss[0].getMaxHealth()*.7f)<.01,"Destroying echoes prevents boss healing");
            boss[0].discard();p.discard();release(l,o,2);
        }).thenSucceed();
    }

    @GameTest(template="empty", timeoutTicks=120)
    public static void boss_quadrant_and_paradox_defenses(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(24,1,56));
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(arena.encounterId()) instanceof ChronicleKeeperEntity,"Boss becomes queryable after chunk tracking starts")).thenExecute(()->{
        var boss=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());boss.setHealth(boss.getMaxHealth()*.5f);boss.tick();unlockShield(boss,p);
        float before=boss.getHealth();boss.hurt(l.damageSources().playerAttack(p),20);
        h.assertTrue(boss.getHealth()==before,"Attacks outside present quadrant are rejected");
        h.runAfterDelay(45,()->{
            h.assertTrue(p.hasEffect(TimeContent.STASIS),"Unsafe quadrant applies temporal stasis");
            p.moveTo(o.getX()+2.5,o.getY()+1,o.getZ()+32.5,0,0);boss.invulnerableTime=0;boss.hurt(l.damageSources().playerAttack(p),20);
            h.assertTrue(boss.getHealth()<before,"Safe quadrant allows damage");
            boss.setHealth(boss.getMaxHealth()*.15f);boss.tick();unlockShield(boss,p);
            var echoes=l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.mode()==TemporalEchoEntity.PARADOX);
            h.assertTrue(echoes.size()==1,"Final phase creates one false body");
            var fake=echoes.getFirst();float hp=fake.getHealth();fake.hurt(l.damageSources().playerAttack(p),100);
            h.assertTrue(fake.getHealth()==hp && p.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),"False body resists damage and applies strain");
            h.assertTrue(Math.abs(fake.getBbWidth()-boss.getBbWidth())<.01,"False body matches boss collision width");
            boss.hurt(l.damageSources().genericKill(),10000);p.discard();release(l,o,2);h.succeed();
        });
        });
    }

    @GameTest(template="empty", timeoutTicks=120)
    public static void boss_and_menu_reload_contracts(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(12,1,40));
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(arena.encounterId()) instanceof ChronicleKeeperEntity,"Boss becomes queryable before saving NBT")).thenExecute(()->{
        var original=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());int[] sequence=anchorOrder(original);
        press(arena,p,sequence[0],sequence[1]);
        var boss=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());CompoundTag nbt=new CompoundTag();boss.save(nbt);boss.discard();
        var restored=(ChronicleKeeperEntity)net.minecraft.world.entity.EntityType.loadEntityRecursive(nbt,l,e->e);
        h.assertTrue(restored!=null && l.addFreshEntity(restored),"Boss reloads with original UUID");
        h.assertTrue(restored.shielded()&&Arrays.equals(sequence,anchorOrder(restored)),"Partial shield and secret order persist");press(arena,p,sequence[2],sequence[3]);
        h.assertTrue(!restored.shielded(),"Anchor order continues after reload");
        var genericMenu=new ResearchMenu(1,p.getInventory(),(net.minecraft.network.RegistryFriendlyByteBuf)null);
        h.assertTrue(genericMenu.slots.size()==40,"Generic menu open tolerates absent block payload");
        for(var slot:genericMenu.slots)h.assertTrue(slot.x>=8 && slot.x+16<=168 && slot.y>=16 && slot.y+16<=158,"All hitboxes fit vanilla 176x166 panel");
        h.assertTrue(genericMenu.getSlot(4).x==8 && genericMenu.getSlot(12).x==152 && genericMenu.getSlot(31).y==142,"Inventory follows vanilla grid spacing");
        h.assertTrue(!genericMenu.getSlot(3).mayPlace(TimeContent.PAST_RECORD.toStack()),"Output remains extraction-only");
        p.getInventory().clearContent();genericMenu.getSlot(3).set(TimeContent.PAST_CONCLUSION.toStack());
        h.assertTrue(genericMenu.quickMoveStack(p,3).is(TimeContent.PAST_CONCLUSION.get()),"Shift-click extracts research output");
        h.assertTrue(genericMenu.getSlot(3).getItem().isEmpty() && count(p,TimeContent.PAST_CONCLUSION.get())==1,"Output transfer neither loses nor duplicates items");
        genericMenu.getSlot(4).set(TimeContent.PAST_RECORD.toStack());genericMenu.quickMoveStack(p,4);
        h.assertTrue(genericMenu.getSlot(0).getItem().is(TimeContent.PAST_RECORD.get()) && genericMenu.getSlot(4).getItem().isEmpty(),"Shift-click inputs retain their slot indices");
        restored.hurt(l.damageSources().genericKill(),10000);p.discard();release(l,o,2);h.succeed();
        });
    }

    @GameTest(template="empty", timeoutTicks=350)
    public static void boss_facing_cast_and_ring_sync(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(12,1,32));p.setInvulnerable(true);
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        ChronicleKeeperEntity[] boss=new ChronicleKeeperEntity[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(arena.encounterId()) instanceof ChronicleKeeperEntity,"Spawn becomes tracked"))
        .thenExecute(()->{boss[0]=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());
            h.assertTrue(boss[0].hasArenaVisuals() && boss[0].visualArena().equals(arena.arena()),"Client visual bounds match authoritative arena");
            h.assertTrue(boss[0].arenaCenter().equals(Vec3.atBottomCenterOf(arena.spawn())),"Quadrants retain fixed compass origin");
            boss[0].hurt(l.damageSources().fellOutOfWorld(),1000000);
            h.assertTrue(boss[0].getHealth()==boss[0].getMaxHealth(),"Bypass-tag damage cannot penetrate initial shield");
        }).thenIdle(35).thenExecute(()->{
            var delta=p.position().subtract(boss[0].position());
            float expected=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(boss[0].yBodyRot-expected))<8,"Orbiting torso faces the player");
        }).thenWaitUntil(()->h.assertTrue(boss[0].castKind()==ChronicleKeeperEntity.CAST_BOLT && boss[0].castAge(0)<10,"Bolt has a synchronized windup before release"))
        .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(ChronalBoltEntity.class,arena.arena(),e->e.getOwner()==boss[0]).isEmpty(),"Windup actually releases a projectile"))
        .thenWaitUntil(()->h.assertTrue(boss[0].ringImpactTime()>l.getGameTime()+10,"Ring warning arrives before impact"))
        .thenExecute(()->h.assertTrue(boss[0].castKind()==ChronicleKeeperEntity.CAST_RING,"Ring has matching cast animation"))
        .thenIdle(42).thenExecute(()->{
            h.assertTrue(l.getGameTime()>boss[0].ringImpactTime()+10,"Ring visual expires after impact");
            boss[0].discard();p.discard();release(l,o,2);
        }).thenSucceed();
    }

    @GameTest(template="empty", timeoutTicks=600)
    public static void boss_active_paradox_and_swap(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(12,1,32));p.setInvulnerable(true);
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        ChronicleKeeperEntity[] boss=new ChronicleKeeperEntity[1];TemporalEchoEntity[] echo=new TemporalEchoEntity[1];Vec3[] initial=new Vec3[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(l.getEntity(arena.encounterId()) instanceof ChronicleKeeperEntity,"Boss tracked"))
        .thenExecute(()->{boss[0]=(ChronicleKeeperEntity)l.getEntity(arena.encounterId());boss[0].setHealth(boss[0].getMaxHealth()*.15f);boss[0].tick();h.assertTrue(boss[0].shielded(),"Final shield raised");unlockShield(boss[0],p);})
        .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.ownedBy(boss[0].getUUID())&&e.mode()==TemporalEchoEntity.PARADOX).isEmpty(),"Paradox tracked"))
        .thenExecute(()->{
            echo[0]=l.getEntitiesOfClass(TemporalEchoEntity.class,arena.arena(),e->e.ownedBy(boss[0].getUUID())&&e.mode()==TemporalEchoEntity.PARADOX).getFirst();initial[0]=echo[0].position();
            h.assertTrue(!boss[0].shielded(),"Final shield was solved before fake spawn");
            CompoundTag old=new CompoundTag();boss[0].save(old);old.putBoolean("Shield",true);
            var restored=(ChronicleKeeperEntity)net.minecraft.world.entity.EntityType.loadEntityRecursive(old,l,e->e);
            h.assertTrue(restored!=null&&restored.shielded(),"Final shield is never stripped on reload");
        }).thenWaitUntil(()->h.assertTrue(echo[0].castKind()==ChronicleKeeperEntity.CAST_BOLT&&echo[0].castAge(0)<10,"Fake has synced bolt windup"))
        .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(ChronalBoltEntity.class,arena.arena(),e->e.getOwner()==echo[0]).isEmpty(),"Fake releases real projectile"))
        .thenWaitUntil(()->h.assertTrue(echo[0].ringImpactTime()>l.getGameTime()+10,"Fake ring is telegraphed"))
        .thenExecute(()->{
            h.assertTrue(echo[0].castKind()==ChronicleKeeperEntity.CAST_RING&&echo[0].hasArenaVisuals(),"Fake ring has animation and floor bounds");
            h.assertTrue(echo[0].position().distanceTo(initial[0])>1,"Fake actually moves in ticking server");
            h.assertTrue(echo[0].visualArena().equals(boss[0].visualArena()),"Both renderers share arena floor");
        }).thenWaitUntil(()->h.assertTrue(boss[0].swapping()&&l.getGameTime()-boss[0].swapStartedAt()<10,"Periodic swap obscuration begins"))
        .thenExecute(()->{
            h.assertTrue(boss[0].swapStartedAt()==echo[0].swapStartedAt(),"Identical synced swap timestamp");
            Vec3 a=boss[0].position(),b=echo[0].position();long marker=boss[0].swapStartedAt();
            boss[0].resolveParadoxSwap(echo[0],.9f);
            h.assertTrue(boss[0].position().equals(a)&&echo[0].position().equals(b),"Decoy event does not exchange positions");
            boss[0].resolveParadoxSwap(echo[0],.1f);
            h.assertTrue(boss[0].position().distanceTo(b)<.001&&echo[0].position().distanceTo(a)<.001,"True event exchanges both positions");
            h.assertTrue(boss[0].swapStartedAt()==marker&&echo[0].swapStartedAt()==marker,"Outcome does not change client effect metadata");
            h.assertTrue(boss[0].ringImpactTime()<0&&echo[0].ringImpactTime()<0,"Swap cancels stale hit zones");
        }).thenIdle(28).thenExecute(()->{
            h.assertTrue(!boss[0].swapping()&&!echo[0].swapping(),"Swap effect expires for both");
            boss[0].hurt(l.damageSources().genericKill(),10000);p.discard();release(l,o,2);
        }).thenSucceed();
    }

    @GameTest(template="empty", timeoutTicks=700)
    public static void wipe_retry_does_not_charge_second_key(GameTestHelper h) {
        var l=h.getLevel();var o=build(l,2);var p=player(l,o.offset(12,1,40));
        ChronicleWorldData.get(l).mark(o.offset(12,0,10));var arena=controller(l,o,44);
        p.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());press(arena,p,PuzzleNodeBlockEntity.START);
        var id=arena.encounterId(); h.assertTrue(id!=null && p.getMainHandItem().isEmpty(),"First entry is charged");
        p.discard();
        h.runAfterDelay(240,()->h.assertTrue(arena.encounterId()==null,"Empty arena wipes after grace period"));
        h.runAfterDelay(560,()->{
            var retry=player(l,o.offset(12,1,40));press(arena,retry,PuzzleNodeBlockEntity.START);
            h.assertTrue(arena.encounterId()!=null && !arena.encounterId().equals(id),"Wipe can retry without another key");
            Entity fresh=l.getEntity(arena.encounterId()); if(fresh!=null)fresh.discard(); retry.discard();release(l,o,2);h.succeed();
        });
    }
}
