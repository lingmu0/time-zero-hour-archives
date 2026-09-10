package net.xuwu.time.entity;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.xuwu.time.logic.EncounterRules;

public final class TimeCombatEvents {
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ChronicleKeeperEntity boss && !event.getSource().is(DamageTypes.GENERIC_KILL)) {
            event.setAmount(EncounterRules.limitFinalDamage(boss.getHealth(), boss.getMaxHealth(), boss.phase(), event.getAmount()));
        }
    }
    private TimeCombatEvents() {}
}
