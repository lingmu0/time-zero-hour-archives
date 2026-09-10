package net.xuwu.time.entity;

import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.xuwu.time.logic.EncounterRules;

public final class TimeCombatEvents {
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ChronicleKeeperEntity boss && !event.getSource().is(DamageTypes.GENERIC_KILL)) {
            event.setNewDamage(EncounterRules.limitFinalDamage(boss.getHealth(), boss.getMaxHealth(), boss.phase(), event.getNewDamage()));
        }
    }
    private TimeCombatEvents() {}
}
