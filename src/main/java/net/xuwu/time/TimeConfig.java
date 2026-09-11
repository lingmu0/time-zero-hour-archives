package net.xuwu.time;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TimeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BOSS_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_SECOND_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_DAMAGE;
    public static final ModConfigSpec.IntValue SECOND_PHASE_BOLT_COUNT;
    public static final ModConfigSpec.IntValue SECOND_PHASE_BOLT_INTERVAL;
    public static final ModConfigSpec.DoubleValue SECOND_PHASE_TIDE_SPEED;
    public static final ModConfigSpec.IntValue ECHO_COUNT;
    public static final ModConfigSpec.DoubleValue ECHO_HEALTH;
    public static final ModConfigSpec.IntValue RETRY_DELAY;
    public static final ModConfigSpec.IntValue LOCATOR_RADIUS;
    public static final ModConfigSpec.BooleanValue AGE_BRIDGE;
    static {
        var b = new ModConfigSpec.Builder();
        BOSS_HEALTH = b.comment("Chronicle Keeper base health before party scaling.").defineInRange("bossHealth", 900.0, 50, 1000000);
        BOSS_SECOND_HEALTH = b.comment("Second act (sanctum ascent) health; independent of first-act health and party scaling.").defineInRange("bossSecondPhaseHealth", 500.0, 1, 1000000);
        BOSS_DAMAGE = b.comment("Chronicle Keeper attack damage before armor and pack scaling.").defineInRange("bossDamage", 10.0, 1, 1000);
        SECOND_PHASE_BOLT_COUNT = b.comment("Base chronal bolt count per second-act volley; two extra bolts are added below half health.").defineInRange("secondPhaseBoltCount", 4, 1, 12);
        SECOND_PHASE_BOLT_INTERVAL = b.comment("Moving-state second-act bolt interval in ticks; the rest interval is ten ticks faster.").defineInRange("secondPhaseBoltInterval", 40, 20, 200);
        SECOND_PHASE_TIDE_SPEED = b.comment("Black-tide rise per moving tick; the tide pauses during rests.").defineInRange("secondPhaseTideSpeed", .075, .01, 1.0);
        ECHO_COUNT = b.comment("Number of past memory echoes spawned per rewind.").defineInRange("echoCount", 3, 1, 12);
        ECHO_HEALTH = b.comment("Health assigned to every temporal echo.").defineInRange("echoHealth", 18.0, 1, 1000);
        RETRY_DELAY = b.comment("Seconds after a wipe before a free retry.").defineInRange("retryDelaySeconds", 15, 1, 300);
        LOCATOR_RADIUS = b.comment("Structure search radius in chunks; bounded to avoid unbounded searches.").defineInRange("locatorRadius", 80, 16, 160);
        AGE_BRIDGE = b.comment("Opt-in bridge; the pack must check that a time organ is equipped.").define("soulForgeAgeBridge", false);
        SPEC = b.build();
    }
    private TimeConfig() {}
}
