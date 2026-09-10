package net.xuwu.time.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraftforge.common.MinecraftForge;
import net.xuwu.time.TimeMod;

public final class TimeProgress {
    public static void award(ServerPlayer player, String path) {
        var advancement = player.server.getAdvancements().getAdvancement(TimeMod.id(path));
        if (advancement != null) {
            for (String criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }
    public static void strain(ServerPlayer player, int age) {
        if (player.isCreative() || player.isSpectator()) return;
        if (MinecraftForge.EVENT_BUS.post(new TemporalStrainEvent(player, age))) return;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        // Optional compatibility uses only existing keys. Opt-in: the pack controls equipped-organ checks.
        var data = player.getPersistentData();
        if (net.xuwu.time.TimeConfig.AGE_BRIDGE.get() && data.contains("timeCount") && data.contains("timeCountMax")) {
            data.putInt("timeCount", Math.min(data.getInt("timeCountMax"), data.getInt("timeCount") + age));
        }
    }
    private TimeProgress() {}
}
