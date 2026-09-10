package net.xuwu.time;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xuwu.time.registry.TimeContent;

@Mod(TimeMod.ID)
public final class TimeMod {
    public static final String ID = "time";

    public TimeMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TimeContent.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TimeConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(TimeCommands::register);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, net.xuwu.time.entity.TimeCombatEvents::onDamage);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
}
