package net.xuwu.time.block;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xuwu.time.registry.TimeContent;

public final class PuzzleNodeBlockEntity extends BlockEntity {
    public static final int HINT = -1, CLAIM = -2, CHECK = -3, START = -4, RESET = -5;
    private BlockPos controller = BlockPos.ZERO;
    private int index;
    private boolean configured;
    public PuzzleNodeBlockEntity(BlockPos pos, BlockState state) { super(TimeContent.NODE_BE.get(), pos, state); }
    public void configure(BlockPos controller, int index) {
        if (configured) return;
        this.controller = controller.immutable(); this.index = index; configured = true; setChanged();
    }
    public void interact(ServerPlayer player, InteractionHand hand) {
        if (level != null && level.hasChunkAt(controller) && level.getBlockEntity(controller) instanceof PuzzleControllerBlockEntity puzzle) {
            puzzle.interact(player, hand, index, getBlockPos(), player.isShiftKeyDown());
        }
    }
    public void step(ServerPlayer player) {
        if (index < 0) return;
        if (level != null && level.hasChunkAt(controller) && level.getBlockEntity(controller) instanceof PuzzleControllerBlockEntity puzzle) {
            puzzle.step(player, index, getBlockPos());
        }
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Controller", controller.asLong()); tag.putInt("Index", index); tag.putBoolean("Configured", configured);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = BlockPos.of(tag.getLong("Controller")); index = tag.getInt("Index"); configured = tag.getBoolean("Configured");
    }
}
