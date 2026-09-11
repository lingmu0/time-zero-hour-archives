package net.xuwu.time.logic;

/** Deterministic geometry and timing for the endless second act. No movement buffs. */
public final class AscensionRules {
    public static final int ARRIVAL_TICKS = 100, CLIMB_TICKS = 180, REST_TICKS = 100;
    /** Shared default upward speed for the Boss, platform tiers and black tide. */
    public static final double RISE_PER_TICK = .075;
    /** Kept as a named assertion/default for callers that describe the tide separately. */
    public static final double TIDE_RISE_PER_TICK = RISE_PER_TICK;
    private static final double PLATFORM_SYNC_DISTANCE = .225;
    public static final int BOLT_INTERVAL_TICKS = 40, REST_BOLT_INTERVAL_TICKS = 30;

    /**
     * Keeps platform creation/removal checks proportional to vertical travel.
     * The old .045 speed checked every five ticks, so the same distance at .075
     * is checked every three ticks.
     */
    public static int platformSyncInterval(double speed) {
        double safeSpeed = Double.isFinite(speed) ? Math.max(.01, speed) : RISE_PER_TICK;
        return Math.max(1, (int)Math.ceil(PLATFORM_SYNC_DISTANCE / safeSpeed));
    }

    public static boolean climbing(int tick) {
        return tick >= ARRIVAL_TICKS && (tick - ARRIVAL_TICKS) % (CLIMB_TICKS + REST_TICKS) < CLIMB_TICKS;
    }
    public static int height(int tier) { return 64 + tier * 4 + Math.floorDiv(tier, 2); }
    public static int tier(double rise) { return Math.max(0, (int)Math.floor(rise / 4.5)); }
    public static int x(int tier, int lane) {
        return (int)Math.round(Math.cos(tier * .82 + lane * Math.PI * 2 / 3) * 5);
    }
    public static int z(int tier, int lane) {
        return (int)Math.round(Math.sin(tier * .82 + lane * Math.PI * 2 / 3) * 5);
    }
    public static float flash(float age) {
        return age < 0 || age >= 100 ? 0 : Math.max(0, Math.min(1, Math.min(age / 25, (100 - age) / 35)));
    }
    private AscensionRules() {}
}
