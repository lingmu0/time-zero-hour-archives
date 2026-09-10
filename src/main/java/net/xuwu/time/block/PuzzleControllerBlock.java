package net.xuwu.time.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.xuwu.time.registry.TimeContent;

public final class PuzzleControllerBlock extends BaseEntityBlock {
    public static final MapCodec<PuzzleControllerBlock> CODEC = simpleCodec(PuzzleControllerBlock::new);
    public PuzzleControllerBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PuzzleControllerBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TimeContent.CONTROLLER_BE.get(), PuzzleControllerBlockEntity::tick);
    }
}
