package net.xuwu.time.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Cancel to replace the default short slow/weakness penalty with pack-specific behavior. */
@Cancelable
public final class TemporalStrainEvent extends Event {
    private final ServerPlayer player;
    private final int age;
    public TemporalStrainEvent(ServerPlayer player, int age) { this.player = player; this.age = age; }
    public ServerPlayer getPlayer() { return player; }
    public int getAge() { return age; }
}
