package net.xuwu.time.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xuwu.time.TimeMod;

/** Persist only our return record across death; never change a player's bed/respawn point. */
@Mod.EventBusSubscriber(modid = TimeMod.ID)
public final class AscensionRecovery {
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        var previous = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!previous.contains(AscensionFight.RETURN)) return;
        var current = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        current.put(AscensionFight.RETURN, previous.getCompound(AscensionFight.RETURN).copy());
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, current);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AscensionFight.returnPlayer(player);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || !player.isAlive()
                || !player.level().dimension().equals(AscensionFight.DIMENSION)) return;
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(AscensionFight.RETURN)) return;
        var saved = persisted.getCompound(AscensionFight.RETURN);
        var boss = saved.hasUUID("Boss") ? player.serverLevel().getEntity(saved.getUUID("Boss")) : null;
        if (boss instanceof ChronicleKeeperEntity keeper && keeper.isAlive()) { saved.remove("MissingTicks"); return; }
        int missing = saved.getInt("MissingTicks") + 1;
        saved.putInt("MissingTicks", missing);
        // Give the server time to load the encounter entity after a restart/login.
        if (missing >= 100) AscensionFight.returnPlayer(player);
    }
    private AscensionRecovery() {}
}
