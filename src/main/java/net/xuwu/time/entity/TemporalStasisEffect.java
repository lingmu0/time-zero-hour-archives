package net.xuwu.time.entity;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TemporalStasisEffect extends MobEffect {
    // Forge 1.20.1 parses the legacy string overload as a UUID. Keep these
    // stable and unique so the same effect instance can be refreshed safely.
    private static final String MOVEMENT_MODIFIER_ID = "6a9b5e3e-7b2b-4a2d-9f08-51e6c2d3b780";
    private static final String ATTACK_MODIFIER_ID = "2e7c4f4a-1f3b-4d29-8e62-9a0c5f7b1d44";

    public TemporalStasisEffect() {
        super(MobEffectCategory.HARMFUL, 0x68B9C9);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_MODIFIER_ID, -.6, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_MODIFIER_ID, -.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
