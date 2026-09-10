package net.xuwu.time;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.xuwu.time.registry.TimeContent;

@Mod(TimeMod.ID)
public final class TimeMod {
    public static final String ID = "time";

    public TimeMod(IEventBus bus, ModContainer container) {
        TimeContent.register(bus);
        container.registerConfig(ModConfig.Type.COMMON, TimeConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(TimeCommands::register);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, net.xuwu.time.entity.TimeCombatEvents::onDamage);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
