package net.xuwu.time.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.xuwu.time.logic.PuzzleKind;

/** Fired once per player/reward cycle on the Forge game bus. */
public final class PuzzleSolvedEvent extends Event {
    private final ServerPlayer player;
    private final BlockPos controller;
    private final PuzzleKind kind;
    public PuzzleSolvedEvent(ServerPlayer player, BlockPos controller, PuzzleKind kind) {
        this.player = player; this.controller = controller.immutable(); this.kind = kind;
    }
    public ServerPlayer getPlayer() { return player; }
    public BlockPos getController() { return controller; }
    public PuzzleKind getKind() { return kind; }
}
