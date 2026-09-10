package net.xuwu.time.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xuwu.time.TimeMod;
import net.xuwu.time.registry.TimeContent;

@Mod.EventBusSubscriber(modid = TimeMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TimeClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TimeContent.CHRONICLE_KEEPER.get(), KeeperRenderer::new);
        event.registerEntityRenderer(TimeContent.ARCHIVE_SCRIBE.get(), context -> new ChronalRenderer<>(context, .82f));
        event.registerEntityRenderer(TimeContent.TEMPORAL_ECHO.get(), TemporalEchoRenderer::new);
        event.registerEntityRenderer(TimeContent.CHRONAL_BOLT.get(), ChronalBoltRenderer::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChronalModel.LAYER, ChronalModel::createLayer);
        event.registerLayerDefinition(KeeperModel.LAYER, KeeperGeometry::createLayer);
        event.registerLayerDefinition(ChronalBoltRenderer.LAYER, ChronalBoltGeometry::createLayer);
    }
    @SubscribeEvent public static void screens(FMLClientSetupEvent event) { event.enqueueWork(() -> MenuScreens.register(TimeContent.RESEARCH_MENU.get(), ResearchScreen::new)); }
    private TimeClient() {}
}
