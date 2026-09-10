package net.xuwu.time.client;

import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.ChronicleKeeperEntity;

/** Client-owned playback follows the formal encounter, not phase changes or false bodies. */
@Mod.EventBusSubscriber(modid = TimeMod.ID, value = Dist.CLIENT)
public final class BossMusicManager {
    private static ClientLevel level;
    private static BossMusicSound music;
    private static int startupGrace;

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (level != mc.level) { clear(); level = mc.level; }
        if (mc.level == null || mc.player == null) { clear(); return; }
        if (mc.isPaused()) return;

        boolean fighting = findEncounter(mc) != null
            && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0
            && mc.options.getSoundSourceVolume(SoundSource.MASTER) > 0;
        var sounds = mc.getSoundManager();
        if (music != null && (music.isStopped() || --startupGrace <= 0 && !sounds.isActive(music))) {
            music.stopImmediately(); sounds.stop(music); music = null;
        }
        if (music == null && fighting) {
            mc.getMusicManager().stopPlaying();
            music = new BossMusicSound(); startupGrace = 40;
            sounds.play(music);
        }
        // Forge 1.20.1 has no SelectMusicEvent. Keep ambient music suppressed while
        // the encounter is active; the custom sound remains on the normal MUSIC bus.
        if (fighting) mc.getMusicManager().stopPlaying();
        if (music != null) music.setFighting(fighting);
    }

    private static ChronicleKeeperEntity findEncounter(Minecraft mc) {
        if (!mc.player.isAlive() || mc.player.isSpectator()) return null;
        return mc.level.getEntitiesOfClass(ChronicleKeeperEntity.class, mc.player.getBoundingBox().inflate(64),
            boss -> boss.isAlive() && !boss.isRemoved() && boss.hasArenaVisuals() && boss.visualArena().contains(mc.player.position()))
            .stream().min(Comparator.comparingDouble(boss -> boss.distanceToSqr(mc.player))).orElse(null);
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); level = null; }

    private static void clear() {
        if (music != null) {
            music.stopImmediately(); Minecraft.getInstance().getSoundManager().stop(music); music = null;
        }
        startupGrace = 0;
    }
    private BossMusicManager() {}
}
