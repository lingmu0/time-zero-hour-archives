package net.xuwu.time.entity;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TemporalStasisEffect extends MobEffect {
    public TemporalStasisEffect() {
        super(MobEffectCategory.HARMFUL, 0x68B9C9);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "time.stasis_movement", -.6, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "time.stasis_attack", -.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
