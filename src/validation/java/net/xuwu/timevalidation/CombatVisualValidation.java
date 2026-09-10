package net.xuwu.timevalidation;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.xuwu.time.block.*;
import net.xuwu.time.client.*;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.entity.TemporalEchoEntity;
import net.xuwu.time.entity.ChronalEffects;
import net.xuwu.time.registry.TimeContent;
import net.xuwu.time.world.ChronicleWorldData;

/** Opt-in real GPU test: each screenshot waits on live synchronized attack state. */
@EventBusSubscriber(modid="time_validation", value=Dist.CLIENT)
public final class CombatVisualValidation {
    private static final BlockPos ORIGIN = new BlockPos(0,100,0);
    private static boolean started, prepared, finished;
    private static CompletableFuture<Void> work;
    private static int scene, ticks, bossId=-1, settle;
    private static String failure;
    private static volatile long debtHitAt=-1;
    private static volatile float debtDamage;
    @SubscribeEvent public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!Boolean.getBoolean("time.validation.combat") || event.getNewDamage()<=0) return;
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer && event.getSource().getDirectEntity() instanceof ChronicleKeeperEntity boss
                && boss.phase()==3 && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)) {
            debtDamage=event.getNewDamage();debtHitAt=boss.level().getGameTime();
        }
    }
    private static final JsonArray captures = new JsonArray();
    private static final String[] SHOTS = {"01-shield-north","02-shield-east","03-bolt-windup","04-bolt-release",
        "05-ring-warning","06-ring-impact","07-ring-cleared","08-present-no-zone-overlay",
        "09-stasis-fx-fixture","10-future-hit-fx-fixture","11-future-debt-hit","12-fake-bolt-windup","13-fake-ring-warning","14-paradox-swap"};

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("time.validation.combat") || finished) return;
        var mc=Minecraft.getInstance();
        if (!started && mc.getOverlay()==null && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
            mc.options.onboardAccessibility=false;mc.options.save();mc.setScreen(new TitleScreen());
        }
        if (!started && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            started=true; mc.options.pauseOnLostFocus=false; mc.options.hideGui=true;
            mc.options.renderDistance().set(6);mc.options.simulationDistance().set(5);
            mc.getTutorial().setStep(TutorialSteps.NONE);
            var rules=new GameRules();
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
            rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
            rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
            mc.createWorldOpenFlows().createFreshLevel("TimeCombat-"+System.currentTimeMillis(),
                new LevelSettings("Time combat validation",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                new WorldOptions(20260909L,true,false),WorldPresets::createNormalWorldDimensions,mc.screen);
            return;
        }
        if (mc.level==null || mc.player==null || mc.getSingleplayerServer()==null) {
            if(prepared && ++ticks>1000) { failure="Disconnected before combat verification completed"; finish(mc); }
            return;
        }
        if (!prepared) {
            prepared=true;
            work=CompletableFuture.runAsync(()->prepare(mc),mc.getSingleplayerServer());
            return;
        }
        if(work!=null && !work.isDone()) return;
        if(work!=null && work.isCompletedExceptionally()) {
            try {work.join();}catch(Exception e){failure=e.toString();}
            finish(mc);return;
        }
        if (++ticks>800) { failure="Timed out waiting for combat scene "+scene; finish(mc); return; }
        if(scene>=SHOTS.length) { if(++settle>=25) finish(mc); return; }
        var entity=mc.level.getEntity(bossId);
        if(!(entity instanceof ChronicleKeeperEntity boss)) {
            work=CompletableFuture.runAsync(()->{
                var level=mc.getSingleplayerServer().overworld();
                var arena=(PuzzleControllerBlockEntity)level.getBlockEntity(ORIGIN.offset(12,0,44));
                var found=arena.encounterId()==null?null:level.getEntity(arena.encounterId());
                if(found!=null)bossId=found.getId();
            },mc.getSingleplayerServer()); return;
        }
        double delta=boss.ringImpactTime()-mc.level.getGameTime();
        Vec3 toPlayer=mc.player.position().subtract(boss.position());
        float expected=(float)Math.toDegrees(Math.atan2(-toPlayer.x,toPlayer.z));
        float yawError=Math.abs(net.minecraft.util.Mth.wrapDegrees(boss.yBodyRot-expected));
        var echoes=mc.level.getEntitiesOfClass(TemporalEchoEntity.class,boss.visualArena(),e->e.mode()==TemporalEchoEntity.PARADOX);
        var fake=echoes.isEmpty()?null:echoes.getFirst();
        boolean ready=switch(scene) {
            case 0,1 -> ticks>35 && boss.shielded() && yawError<8;
            case 2 -> !boss.shielded() && boss.castKind()==ChronicleKeeperEntity.CAST_BOLT && boss.castAge(0)>=6 && boss.castAge(0)<10;
            case 3 -> boss.castKind()==ChronicleKeeperEntity.CAST_BOLT && boss.castAge(0)>=11 && boss.castAge(0)<17;
            case 4 -> delta>=6 && delta<=12 && KeeperTelegraph.visible(boss);
            case 5 -> delta<=0 && delta>=-4 && KeeperTelegraph.visible(boss);
            case 6 -> boss.ringImpactTime()>=0 && delta<-14 && !KeeperTelegraph.visible(boss);
            case 7 -> ticks>35 && boss.phase()==2 && !KeeperTelegraph.visible(boss);
            case 8,9 -> ticks==3;
            case 10 -> debtHitAt>=0&&mc.level.getGameTime()>=debtHitAt+2;
            case 11 -> fake!=null&&!boss.shielded()&&fake.castKind()==1&&fake.castAge(0)>=6&&fake.castAge(0)<10;
            case 12 -> fake!=null&&KeeperTelegraph.visible(fake)&&fake.ringImpactTime()-mc.level.getGameTime()>5&&fake.ringImpactTime()-mc.level.getGameTime()<15;
            case 13 -> fake!=null&&boss.swapping()&&fake.swapping()&&mc.level.getGameTime()-boss.swapStartedAt()>=5;
            default -> false;
        };
        if(!ready)return;
        JsonObject shot=new JsonObject();shot.addProperty("file",SHOTS[scene]+".png");
        shot.addProperty("phase",boss.phase());shot.addProperty("shield",boss.shielded());
        shot.addProperty("cast",boss.castKind());shot.addProperty("cast_age",boss.castAge(0));
        shot.addProperty("ring_remaining",delta);shot.addProperty("facing_error_degrees",yawError);
        shot.addProperty("ring_inner",net.xuwu.time.logic.EncounterRules.RING_INNER);shot.addProperty("ring_outer",net.xuwu.time.logic.EncounterRules.RING_OUTER);
        if(scene==10){shot.addProperty("actual_debt_damage",debtDamage);shot.addProperty("debt_hit_age",mc.level.getGameTime()-debtHitAt);}
        if(fake!=null){shot.addProperty("fake_cast",fake.castKind());shot.addProperty("fake_cast_age",fake.castAge(0));shot.addProperty("fake_ring_remaining",fake.ringImpactTime()-mc.level.getGameTime());shot.addProperty("swap_active",boss.swapping()&&fake.swapping());}
        captures.add(shot);
        Screenshot.grab(mc.gameDirectory,SHOTS[scene]+".png",mc.getMainRenderTarget(),m->LogUtils.getLogger().info("TIME_COMBAT_SCREENSHOT {}",m.getString()));
        scene++;ticks=0;
        if(scene==1 || scene==2 || scene==7 || scene==8 || scene==9 || scene==10 || scene==11) {
            if(scene==8)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            if(scene==11)mc.options.setCameraType(CameraType.FIRST_PERSON);
            work=CompletableFuture.runAsync(()->{
                var level=mc.getSingleplayerServer().overworld();
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var serverBoss=(ChronicleKeeperEntity)level.getEntity(bossId);
                if(scene==1) camera(player,new Vec3(28.5,107.2,44.5),new Vec3(12.5,102.5,44.5));
                if(scene==2) {
                    var arena=(PuzzleControllerBlockEntity)level.getBlockEntity(ORIGIN.offset(12,0,44));
                    TimeGameTests.unlockShield(serverBoss,player);
                    // Elevated oblique view shows the full attack footprint and animated staff.
                    camera(player,new Vec3(26.5,107.5,30.5),new Vec3(12.5,102,44.5));
                }
                if(scene==7)serverBoss.setHealth(serverBoss.getMaxHealth()*.5f);
                // Deterministic VFX fixtures exercise the production effect helpers, not damage assertions.
                if(scene==8){camera(player,new Vec3(12.5,104,34.5),new Vec3(12.5,103,44.5));ChronalEffects.stasis(player);}
                if(scene==9)ChronalEffects.futureHit(player);
                if(scene==10){serverBoss.setHealth(serverBoss.getMaxHealth()*.25f);player.setInvulnerable(false);player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);player.setHealth(200);}
                if(scene==11){player.setInvulnerable(true);serverBoss.setHealth(serverBoss.getMaxHealth()*.15f);camera(player,new Vec3(26.5,107.5,30.5),new Vec3(12.5,103,44.5));}
                if(scene==7||scene==10||scene==11){serverBoss.tick();TimeGameTests.unlockShield(serverBoss,player);}
            },mc.getSingleplayerServer());
        }
    }
    private static void prepare(Minecraft mc) {
        var server=mc.getSingleplayerServer();var level=server.overworld();
        TimeGameTests.buildAt(level,ORIGIN,2,132);level.setDayTime(18000);
        var player=server.getPlayerList().getPlayer(mc.player.getUUID());
        player.setGameMode(GameType.SURVIVAL);player.setInvulnerable(true);
        player.getAbilities().mayfly=true;player.getAbilities().flying=true;player.onUpdateAbilities();
        camera(player,new Vec3(12.5,107.2,28.5),new Vec3(12.5,103,44.5));
        ChronicleWorldData.get(level).mark(ORIGIN.offset(12,0,10));
        var arena=(PuzzleControllerBlockEntity)level.getBlockEntity(ORIGIN.offset(12,0,44));
        player.setItemInHand(InteractionHand.MAIN_HAND,TimeContent.ZERO_KEY.toStack());
        arena.interact(player,InteractionHand.MAIN_HAND,PuzzleNodeBlockEntity.START,arena.getBlockPos(),false);
    }
    private static void camera(net.minecraft.server.level.ServerPlayer p, Vec3 eye, Vec3 target) {
        Vec3 d=target.subtract(eye);
        float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        float pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z)));
        p.teleportTo(p.serverLevel(),eye.x,eye.y-1.62,eye.z,Set.of(),yaw,pitch);
        p.fallDistance=0;
    }
    private static void finish(Minecraft mc) {
        finished=true;JsonObject report=new JsonObject();
        report.addProperty("generated_at",java.time.Instant.now().toString());
        report.addProperty("status",failure==null?"passed":"failed");report.addProperty("version","0.1.16");
        report.add("captures",captures);if(failure!=null)report.addProperty("error",failure);
        try {Path p=Path.of("../build/reports/combat-visual.json");Files.createDirectories(p.getParent());Files.writeString(p,new GsonBuilder().setPrettyPrinting().create().toJson(report));}
        catch(Exception e){LogUtils.getLogger().error("Combat report failed",e);}
        LogUtils.getLogger().info("TIME_COMBAT_COMPLETE {}",failure==null?"passed":failure);mc.stop();
    }
}
