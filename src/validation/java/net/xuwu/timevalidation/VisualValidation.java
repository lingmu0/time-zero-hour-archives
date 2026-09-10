package net.xuwu.timevalidation;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.*;
import net.xuwu.time.registry.TimeContent;
import net.xuwu.time.research.ResearchDeskBlockEntity;
import net.xuwu.time.world.ChronicleWorldData;

/** Reproducible GPU smoke test, enabled ONLY by runValidationClient. */
@EventBusSubscriber(modid="time_validation", value=Dist.CLIENT)
public final class VisualValidation {
    private static boolean started, prepared, finished;
    private static int ticks, scene=-1;
    private static CompletableFuture<Void> work;
    private static volatile String failure;
    private static boolean modelBindingsVerified;
    private static final int[] displayEntities=new int[3];
    private static final List<UUID> displayUuids=new ArrayList<>();
    private static final BlockPos[] origins=new BlockPos[3];
    private static final BlockPos[] entrances=new BlockPos[3];
    private static final JsonArray checks=new JsonArray();
    private static final String[] names={"observatory","archive","clockroom"};
    private static final String[] shots={"01-observatory-exterior","02-observatory-puzzle","03-archive-entrance","04-archive-mirror","05-clockroom-entrance","06-boss-arena","07-research-interface","08-model-comparison-day","09-model-comparison-night"};

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("time.validation.visual") || finished)return;
        Minecraft mc=Minecraft.getInstance();
        if(!started && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
            started=true; mc.options.pauseOnLostFocus=false;mc.getTutorial().setStep(TutorialSteps.NONE);
            var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
            rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
            mc.createWorldOpenFlows().createFreshLevel("TimeValidation-"+System.currentTimeMillis(),
                new LevelSettings("Time runtime validation",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                new WorldOptions(20260909L,true,false),WorldPresets::createNormalWorldDimensions,mc.screen);
            return;
        }
        if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null){
            if(prepared && ++ticks>1200){failure="Client disconnected before visual validation finished";finish(mc);}
            return;
        }
        if(!prepared){
            prepared=true;
            work=CompletableFuture.runAsync(()->prepare(mc),mc.getSingleplayerServer());
            return;
        }
        if(work!=null && !work.isDone())return;
        if(work!=null && work.isCompletedExceptionally()){
            try{work.join();}catch(Exception ex){failure=ex.toString();LogUtils.getLogger().error("TIME_VISUAL_FAILED",ex);}
            finish(mc);return;
        }
        if(scene<0){scene=0;ticks=0;setScene(mc,scene);return;}
        if(++ticks==100){
            if(scene==6 && !(mc.screen instanceof net.xuwu.time.client.ResearchScreen)){
                failure="Research screen did not open";finish(mc);return;
            }
            if(scene>=7){
                for(int i=0;i<displayEntities.length;i++){
                    var entity=mc.level.getEntity(displayEntities[i]);
                    if(entity==null){failure="Model display entity was not tracked";finish(mc);return;}
                    var renderer=mc.getEntityRenderDispatcher().getRenderer(entity);
                    var expected=i==0?net.xuwu.time.client.ChronalRenderer.TEXTURE:net.xuwu.time.client.KeeperRenderer.TEXTURE;
                    if(!renderer.getTextureLocation(entity).equals(expected)){
                        failure="Wrong scribe/keeper/paradox renderer texture";finish(mc);return;
                    }
                }
                modelBindingsVerified=true;
            }
            Screenshot.grab(mc.gameDirectory,shots[scene]+".png",mc.getMainRenderTarget(),message->LogUtils.getLogger().info("TIME_SCREENSHOT {}",message.getString()));
        }
        if(ticks>=150){
            if(++scene>=shots.length){finish(mc);return;}
            ticks=0;setScene(mc,scene);
        }
    }

    private static void prepare(Minecraft mc){
        ServerLevel level=mc.getSingleplayerServer().overworld();level.setDayTime(6000);
        var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for(int i=0;i<3;i++){
            var structure=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,TimeMod.id(names[i])));
            var located=level.getChunkSource().getGenerator().findNearestMapStructure(level,HolderSet.direct(structure),BlockPos.ZERO,80,false);
            if(located==null)throw new IllegalStateException("Natural structure not found: "+names[i]);
            // Vanilla locate returns the start chunk at Y=0, not a position in a piece.
            var start=level.getChunkAt(located.getFirst()).getStartForStructure(structure.value());
            if(!start.isValid())throw new IllegalStateException("Natural structure has no valid start: "+names[i]);
            BlockPos entrance=start.getPieces().getFirst().getLocatorPosition();
            var box=start.getPieces().getFirst().getBoundingBox();
            for(int x=box.minX()>>4;x<=box.maxX()>>4;x++) for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++){
                level.getChunk(x,z);level.setChunkForced(x,z,true);
            }
            BlockPos origin=new BlockPos(box.minX()+10,box.minY()+3,box.minZ()+12);
            if(!(level.getBlockEntity(origin.offset(5,2,3)) instanceof SignBlockEntity sign) || !sign.isWaxed())
                throw new IllegalStateException("Natural sign lost in ProtoChunk: "+names[i]);
            if(!(level.getBlockEntity(origin.offset(12,0,10)) instanceof PuzzleControllerBlockEntity))
                throw new IllegalStateException("Natural controller missing: "+names[i]);
            origins[i]=origin;entrances[i]=entrance;
            var tester=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            Item probe=switch(i){case 0->TimeContent.TIME_PROBE.get();case 1->TimeContent.ARCHIVE_PROBE.get();default->TimeContent.ZERO_PROBE.get();};
            ItemStack probeStack=new ItemStack(probe);
            probeStack.set(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER,
                new net.minecraft.world.item.component.LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),located.getFirst())),false));
            tester.setItemInHand(InteractionHand.MAIN_HAND,probeStack);probe.use(level,tester,InteractionHand.MAIN_HAND);
            var resolved=probeStack.get(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER).target().orElseThrow().pos();
            if(!resolved.equals(entrance))throw new IllegalStateException("Probe failed to resolve actual entrance: "+names[i]);
            tester.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            JsonObject result=new JsonObject();result.addProperty("structure",names[i]);result.addProperty("origin",origin.toShortString());
            result.addProperty("entrance",entrance.toShortString());result.addProperty("natural_generation",true);
            result.addProperty("probe_resolves_surface_entrance",true);checks.add(result);
            LogUtils.getLogger().info("TIME_NATURAL_OK {} origin={} entrance={}",names[i],origin,entrance);
        }
    }
    private static void setScene(Minecraft mc,int index){
        mc.options.hideGui=index>=7;
        work=CompletableFuture.runAsync(()->{
            var server=mc.getSingleplayerServer();var level=server.overworld();var player=server.getPlayerList().getPlayer(mc.player.getUUID());
            if(player==null)throw new IllegalStateException("Real client player missing");
            player.closeContainer();player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
            Vec3 camera,target;
            if(index==0){camera=Vec3.atCenterOf(origins[0].offset(-8,16,-19));target=Vec3.atCenterOf(origins[0].offset(12,4,17));}
            else if(index==1){camera=Vec3.atCenterOf(origins[0].offset(12,3,3));target=Vec3.atCenterOf(origins[0].offset(12,3,14));}
            else if(index==2){camera=Vec3.atCenterOf(entrances[1].offset(12,6,13));target=Vec3.atCenterOf(entrances[1].above(2));}
            else if(index==3){camera=Vec3.atCenterOf(origins[1].offset(9,3,26));target=Vec3.atCenterOf(origins[1].offset(9,2,39));}
            else if(index==4){camera=Vec3.atCenterOf(entrances[2].offset(12,6,13));target=Vec3.atCenterOf(entrances[2].above(2));}
            else if(index==5){
                camera=Vec3.atCenterOf(origins[2].offset(12,4,34));target=Vec3.atCenterOf(origins[2].offset(12,4,44));
                ChronicleWorldData.get(level).mark(origins[2].offset(12,0,10));
            }else if(index==6){
                BlockPos deskAt=origins[0].offset(12,1,5);level.setBlockAndUpdate(deskAt,TimeContent.RESEARCH_DESK.get().defaultBlockState());
                var desk=(ResearchDeskBlockEntity)level.getBlockEntity(deskAt);
                desk.setItem(0,TimeContent.PAST_RECORD.toStack());desk.setItem(1,TimeContent.TEMPORAL_DUST.toStack());desk.setItem(2,new ItemStack(Items.BOOK));
                camera=Vec3.atCenterOf(origins[0].offset(12,1,3));target=Vec3.atCenterOf(deskAt);
            }else{
                BlockPos studio=new BlockPos(96,150,96);
                for(UUID uuid:displayUuids){var old=level.getEntity(uuid);if(old!=null)old.discard();}displayUuids.clear();
                for(int x=-11;x<=11;x++)for(int z=-4;z<=4;z++){
                    BlockPos at=studio.offset(x,0,z);level.getChunkAt(at);level.setChunkForced(at.getX()>>4,at.getZ()>>4,true);
                    level.setBlockAndUpdate(at,net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    if(Math.abs(x)%6==0 && Math.abs(z)<=1)level.setBlockAndUpdate(at,net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ.defaultBlockState());
                }
                var scribe=TimeContent.ARCHIVE_SCRIBE.get().create(level);
                var keeper=TimeContent.CHRONICLE_KEEPER.get().create(level);
                var fake=TimeContent.TEMPORAL_ECHO.get().create(level);
                // An absent controller holds the display boss still for <200 ticks;
                // actual arena combat/animation is still exercised in scene 06.
                keeper.bind(studio.below(10),studio.above(),1);
                fake.configure(net.xuwu.time.entity.TemporalEchoEntity.PARADOX,keeper.getUUID(),600);
                net.minecraft.world.entity.Mob[] actors={scribe,keeper,fake};
                for(int i=0;i<actors.length;i++){
                    var actor=actors[i];actor.moveTo(studio.getX()+(i-1)*6+.5,151,96.5,180,0);
                    actor.yBodyRot=180;actor.yHeadRot=180;actor.setNoAi(true);actor.setNoGravity(true);actor.setPersistenceRequired();
                    actor.setCustomName(net.minecraft.network.chat.Component.literal(i==0?"失序抄写员":i==1?"终末编年者":"编年者假身"));actor.setCustomNameVisible(true);
                    if(!level.addFreshEntity(actor))throw new IllegalStateException("Display entity spawn failed");
                    displayEntities[i]=actor.getId();displayUuids.add(actor.getUUID());
                }
                level.setDayTime(index==7?6000:18000);
                camera=new Vec3(96.5,154.1,83);target=new Vec3(96.5,152.7,96.5);
            }
            Vec3 direction=target.subtract(camera);float yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
            float pitch=(float)-Math.toDegrees(Math.atan2(direction.y,Math.sqrt(direction.x*direction.x+direction.z*direction.z)));
            player.teleportTo(level,camera.x,camera.y-1.62,camera.z,Set.of(),yaw,pitch);
            player.fallDistance=0;
            if(index==5){
                var controller=(PuzzleControllerBlockEntity)level.getBlockEntity(origins[2].offset(12,0,44));
                controller.interact(player,InteractionHand.MAIN_HAND,PuzzleNodeBlockEntity.START,origins[2].offset(12,1,28),false);
            }
            if(index==6){
                BlockPos at=origins[0].offset(12,1,5);
                level.getBlockState(at).useWithoutItem(level,player,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(at),Direction.NORTH,at,false));
            }
            LogUtils.getLogger().info("TIME_VISUAL_SCENE {}",shots[index]);
        },mc.getSingleplayerServer());
    }
    private static void finish(Minecraft mc){
        finished=true;JsonObject report=new JsonObject();report.addProperty("seed",20260909L);report.add("natural_structures",checks);
        report.addProperty("status",failure==null?"passed":"failed");report.addProperty("generated_at",java.time.Instant.now().toString());
        report.addProperty("model_bindings_verified",modelBindingsVerified);report.addProperty("screenshot_count",shots.length);
        report.addProperty("world_directory",mc.getSingleplayerServer()==null?"":mc.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toString());
        if(failure!=null)report.addProperty("error",failure);
        try{Path at=Path.of("../build/reports/visual-runtime.json");Files.createDirectories(at.getParent());Files.writeString(at,new GsonBuilder().setPrettyPrinting().create().toJson(report));}
        catch(Exception e){LogUtils.getLogger().error("Visual report write failed",e);}
        LogUtils.getLogger().info("TIME_VISUAL_COMPLETE {}",failure==null?"passed":failure);
        mc.stop();
    }
}
