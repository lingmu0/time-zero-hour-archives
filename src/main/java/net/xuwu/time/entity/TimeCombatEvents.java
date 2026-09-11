package net.xuwu.time.entity;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class TimeCombatEvents {
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ChronicleKeeperEntity boss && !event.getSource().is(DamageTypes.GENERIC_KILL)) {
            // This listener is registered at LOWEST, so the amount is the final
            // post-armor value. Queue it instead of applying a burst to health.
            boss.queueDamage(event.getAmount());
            event.setAmount(0);
        }
    }
    private TimeCombatEvents() {}
}
