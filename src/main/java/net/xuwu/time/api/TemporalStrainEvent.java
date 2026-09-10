package net.xuwu.time.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Cancel to replace the default short slow/weakness penalty with pack-specific behavior. */
public final class TemporalStrainEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final int age;
    public TemporalStrainEvent(ServerPlayer player, int age) { this.player = player; this.age = age; }
    public ServerPlayer getPlayer() { return player; }
    public int getAge() { return age; }
}
