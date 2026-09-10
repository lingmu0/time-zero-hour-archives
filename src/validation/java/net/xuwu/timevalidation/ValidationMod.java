package net.xuwu.timevalidation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

/** Separate source set and mod: never packaged into the released content jar. */
@Mod("time_validation")
public final class ValidationMod {
    public ValidationMod(IEventBus bus) {
        if (Boolean.getBoolean("neoforge.gameTestServer"))
            net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(new ValidationReporter());
        bus.addListener((RegisterGameTestsEvent event) -> event.register(TimeGameTests.class));
    }
}
