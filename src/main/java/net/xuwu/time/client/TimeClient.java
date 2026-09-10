package net.xuwu.time.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.xuwu.time.TimeMod;
import net.xuwu.time.registry.TimeContent;

@EventBusSubscriber(modid = TimeMod.ID, value = Dist.CLIENT)
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
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent event) { event.register(TimeContent.RESEARCH_MENU.get(), ResearchScreen::new); }
    private TimeClient() {}
}
