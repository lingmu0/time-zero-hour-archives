package net.xuwu.time.client;

import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.logic.EncounterRules;

/** Adds subtle vanilla-style separators at the four phase boundaries. */
@Mod.EventBusSubscriber(modid = TimeMod.ID, value = Dist.CLIENT)
public final class ChronicleBossBarOverlay {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_Y = 12;
    private static final int BAR_HEIGHT = 5;

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.BOSS_EVENT_PROGRESS.type()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (findEncounter(minecraft) == null) return;
        int left = event.getGuiGraphics().guiWidth() / 2 - 91;
        for (int boundary = 0; boundary < 4; boundary++) {
            int x = left + Math.round(BAR_WIDTH * EncounterRules.phaseBoundary(boundary));
            event.getGuiGraphics().fill(x, BAR_Y, x + 1, BAR_Y + BAR_HEIGHT, 0xFF555555);
        }
    }

    private static ChronicleKeeperEntity findEncounter(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()) return null;
        return minecraft.level.getEntitiesOfClass(ChronicleKeeperEntity.class, minecraft.player.getBoundingBox().inflate(64),
                boss -> boss.isAlive() && !boss.isRemoved() && boss.hasArenaVisuals() && boss.visualArena().contains(minecraft.player.position()))
            .stream().min(Comparator.comparingDouble(boss -> boss.distanceToSqr(minecraft.player))).orElse(null);
    }

    private ChronicleBossBarOverlay() {}
}
