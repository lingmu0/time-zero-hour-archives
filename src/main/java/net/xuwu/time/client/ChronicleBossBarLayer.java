package net.xuwu.time.client;

import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.logic.EncounterRules;

/** Adds separators after the vanilla Boss bar has rendered, at exact per-bar coordinates. */
@EventBusSubscriber(modid = TimeMod.ID, value = Dist.CLIENT)
public final class ChronicleBossBarLayer {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static boolean keeperBarSeen;
    private static int keeperBarX;
    private static int keeperBarY;

    @SubscribeEvent
    public static void capture(CustomizeGuiOverlayEvent.BossEventProgress event) {
        ChronicleKeeperEntity boss = findEncounter(Minecraft.getInstance());
        if (boss != null && !boss.ascended() && isKeeperBar(event.getBossEvent())) {
            keeperBarSeen = true;
            keeperBarX = event.getX();
            keeperBarY = event.getY();
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.BOSS_OVERLAY.equals(event.getName()) || !keeperBarSeen) return;
        ChronicleKeeperEntity boss = findEncounter(Minecraft.getInstance());
        if (boss != null && !boss.ascended()) {
            GuiGraphics graphics = event.getGuiGraphics();
            for (int boundary = 0; boundary < 4; boundary++) {
                int x = keeperBarX + Math.round(BAR_WIDTH * EncounterRules.phaseBoundary(boundary));
                graphics.fill(x, keeperBarY, x + 1, keeperBarY + BAR_HEIGHT, 0xFF555555);
            }
        }
        keeperBarSeen = false;
    }

    private static boolean isKeeperBar(BossEvent event) {
        String keeperName = Component.translatable("entity.time.chronicle_keeper").getString();
        return event.getName().getString().startsWith(keeperName);
    }

    private static ChronicleKeeperEntity findEncounter(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()) return null;
        return minecraft.level.getEntitiesOfClass(ChronicleKeeperEntity.class, minecraft.player.getBoundingBox().inflate(64),
                boss -> boss.isAlive() && !boss.isRemoved() && boss.hasArenaVisuals() && boss.visualArena().contains(minecraft.player.position()))
            .stream().min(Comparator.comparingDouble(boss -> boss.distanceToSqr(minecraft.player))).orElse(null);
    }

    private ChronicleBossBarLayer() {}
}
