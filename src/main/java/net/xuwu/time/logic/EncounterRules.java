package net.xuwu.time.logic;

/** Single source of truth for phase thresholds and party scaling. */
public final class EncounterRules {
    public static final double RING_INNER = 4, RING_OUTER = 10, RING_FAR = 13;
    /** Non-kill damage is settled gradually: at most 0.1% of max health per server tick. */
    public static final float DAMAGE_TICK_FRACTION = .001f;
    public static final int SWAP_INTERVAL = 200;
    /** Past-phase rewind warning/resolve cadence: 200 server ticks = 10 seconds. */
    public static final int PAST_REWIND_INTERVAL_TICKS = 200;
    public static final int PAST_REWIND_RESPONSE_TICKS = 200;
    /** Phase 0 fires one bolt; each completed phase adds one, capped at five. */
    public static int boltCount(int phase) { return Math.max(1, Math.min(5, phase + 1)); }
    public static boolean shouldSwap(float roll) { return roll < .5f; }
    public static final int RING_WARNING_TICKS = 30;
    public static boolean inRing(double dx, double dz) {
        double d = dx * dx + dz * dz;
        return d >= RING_INNER * RING_INNER && d <= RING_OUTER * RING_OUTER || d > RING_FAR * RING_FAR;
    }
    public static int phase(float fraction) {
        return fraction > .80f ? 0 : fraction > .60f ? 1 : fraction > .35f ? 2 : fraction > .15f ? 3 : 4;
    }
    public static double partyScale(int players) { return 1 + .35 * (Math.max(1, Math.min(4, players)) - 1); }
    public static int quadrant(double dx, double dz) { return (dx >= 0 ? 1 : 0) + (dz >= 0 ? 2 : 0); }
    /** Counter-clockwise compass order: NW -> SW -> SE -> NE. */
    public static int safeQuadrant(int cycle) {
        return new int[]{0, 2, 3, 1}[Math.floorMod(cycle, 4)];
    }
    public static float phaseBoundary(int boundary) {
        return switch (Math.max(0, Math.min(3, boundary))) {
            case 0 -> .80f;
            case 1 -> .60f;
            case 2 -> .35f;
            default -> .15f;
        };
    }
    public static float phaseDamageRemaining(float health, float maxHealth, int phase) {
        if (Float.isNaN(health) || Float.isNaN(maxHealth) || maxHealth <= 0) return 0;
        float[] floors = {.80f, .60f, .35f, .15f, 0};
        return Math.max(0, health - maxHealth * floors[Math.max(0, Math.min(4, phase))]);
    }
    public static float perTickDamage(float pendingDamage, float maxHealth) {
        if (Float.isNaN(pendingDamage) || pendingDamage <= 0 || Float.isNaN(maxHealth) || maxHealth <= 0) return 0;
        return Math.min(pendingDamage, maxHealth * DAMAGE_TICK_FRACTION);
    }
    public static float limitFinalDamage(float health, float maxHealth, int phase, float damage) {
        if (Float.isNaN(damage)) return 0;
        return Math.min(Math.max(0, damage), phaseDamageRemaining(health, maxHealth, phase));
    }
    private EncounterRules() {}
}
