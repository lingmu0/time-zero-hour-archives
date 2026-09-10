package net.xuwu.time.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Synced presentation contract shared by the real and false chroniclers. */
public interface ChronalCaster {
    Mob caster();
    long ringImpactTime();
    int castKind();
    float castAge(float partial);
    default int castDuration() { return castKind() == 2 ? 40 : castKind() == 3 ? StaffStrike.DURATION_TICKS : 24; }
    boolean hasArenaVisuals();
    AABB visualArena();
    net.xuwu.time.logic.ArenaPattern ringPattern();
    long swapStartedAt();
    default boolean swapping() {
        return swapStartedAt() >= 0 && caster().level().getGameTime() - swapStartedAt() < 24;
    }
}
