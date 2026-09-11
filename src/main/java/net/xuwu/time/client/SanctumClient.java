package net.xuwu.time.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.*;
import net.xuwu.time.logic.AscensionRules;

@EventBusSubscriber(modid = TimeMod.ID, value = Dist.CLIENT)
public final class SanctumClient {
    private static UUID transition;
    private static boolean arrived;
    private static int flashAge = 100;
    @SubscribeEvent public static void dimensions(RegisterDimensionSpecialEffectsEvent event) {
        event.register(TimeMod.id("sanctum"), new SanctumEffects());
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        if (flashAge < 100) flashAge++;
        for (var boss : mc.level.getEntitiesOfClass(ChronicleKeeperEntity.class, mc.player.getBoundingBox().inflate(80))) {
            if (!boss.visualArena().contains(mc.player.position())) continue;
            if (boss.transitioning()) {
                if (!boss.getUUID().equals(transition)) { transition = boss.getUUID(); arrived = false; }
                flashAge = Math.min(45, boss.ascensionView().getInt("Ticks"));
                break;
            }
            if (boss.ascended() && boss.ascensionView().getInt("Ticks") < 100
                    && (!boss.getUUID().equals(transition) || !arrived)) {
                transition = boss.getUUID(); arrived = true; flashAge = 45;
                break;
            }
        }
    }
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive()) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int alpha = (int)(255 * AscensionRules.flash(flashAge + partial));
        if (alpha > 0) event.getGuiGraphics().fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), (alpha << 24) | 0xFFFFFF);
    }
    private static boolean inSanctum() {
        var level = Minecraft.getInstance().level;
        return level != null && level.dimension().equals(AscensionFight.DIMENSION);
    }
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor event) {
        if (!inSanctum() || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        event.setRed(.97f); event.setGreen(.965f); event.setBlue(.94f);
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event) {
        if (!inSanctum() || event.getType() != FogType.NONE) return;
        event.setNearPlaneDistance(20); event.setFarPlaneDistance(80); event.setCanceled(true);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        transition = null; arrived = false; flashAge = 100;
    }
    private SanctumClient() {}
}
