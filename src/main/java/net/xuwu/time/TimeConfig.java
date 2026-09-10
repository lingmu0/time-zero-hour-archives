package net.xuwu.time;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TimeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BOSS_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_DAMAGE;
    public static final ModConfigSpec.IntValue ECHO_COUNT;
    public static final ModConfigSpec.DoubleValue ECHO_HEALTH;
    public static final ModConfigSpec.IntValue RETRY_DELAY;
    public static final ModConfigSpec.IntValue LOCATOR_RADIUS;
    public static final ModConfigSpec.BooleanValue AGE_BRIDGE;
    static {
        var b = new ModConfigSpec.Builder();
        BOSS_HEALTH = b.comment("Chronicle Keeper base health before party scaling.").defineInRange("bossHealth", 900.0, 50, 1000000);
        BOSS_DAMAGE = b.comment("Chronicle Keeper attack damage before armor and pack scaling.").defineInRange("bossDamage", 10.0, 1, 1000);
        ECHO_COUNT = b.comment("Number of past memory echoes spawned per rewind.").defineInRange("echoCount", 3, 1, 12);
        ECHO_HEALTH = b.comment("Health assigned to every temporal echo.").defineInRange("echoHealth", 18.0, 1, 1000);
        RETRY_DELAY = b.comment("Seconds after a wipe before a free retry.").defineInRange("retryDelaySeconds", 15, 1, 300);
        LOCATOR_RADIUS = b.comment("Structure search radius in chunks; bounded to avoid unbounded searches.").defineInRange("locatorRadius", 80, 16, 160);
        AGE_BRIDGE = b.comment("Opt-in bridge; the pack must check that a time organ is equipped.").define("soulForgeAgeBridge", false);
        SPEC = b.build();
    }
    private TimeConfig() {}
}
