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
import net.xuwu.time.entity.AscensionFight;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.registry.TimeContent;

/** Client-owned playback follows the formal encounter and starts the second-act track only in the sanctum. */
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

        ChronicleKeeperEntity encounter = findEncounter(mc);
        boolean fighting = encounter != null
            && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0
            && mc.options.getSoundSourceVolume(SoundSource.MASTER) > 0;
        // The server marks the transition before the dimension hop. Do not play the
        // sanctum track until this client is actually in the new space.
        boolean inSanctum = mc.level.dimension().equals(AscensionFight.DIMENSION);
        boolean ascension = inSanctum && encounter != null && encounter.ascended();
        var sounds = mc.getSoundManager();
        if (music != null && (music.isStopped() || music.isAscension() != ascension
                || --startupGrace <= 0 && !sounds.isActive(music))) {
            music.stopImmediately(); sounds.stop(music); music = null;
        }
        if (music == null && fighting) {
            mc.getMusicManager().stopPlaying();
            music = new BossMusicSound(ascension ? TimeContent.ASCENSION_BOSS_MUSIC.get() : TimeContent.BOSS_MUSIC.get(), ascension);
            startupGrace = 40;
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
