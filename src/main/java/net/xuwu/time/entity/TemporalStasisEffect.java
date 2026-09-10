package net.xuwu.time.entity;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.xuwu.time.TimeMod;

public final class TemporalStasisEffect extends MobEffect {
    public TemporalStasisEffect() {
        super(MobEffectCategory.HARMFUL, 0x68B9C9);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, TimeMod.id("stasis_movement"), -.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, TimeMod.id("stasis_attack"), -.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
