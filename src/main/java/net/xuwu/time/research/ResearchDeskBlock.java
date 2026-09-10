package net.xuwu.time.research;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xuwu.time.registry.TimeContent;

public final class ResearchDeskBlock extends BaseEntityBlock {
    public static final MapCodec<ResearchDeskBlock> CODEC = simpleCodec(ResearchDeskBlock::new);
    public ResearchDeskBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ResearchDeskBlockEntity(pos, state); }
    private void open(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof ResearchDeskBlockEntity desk) server.openMenu(desk, pos);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player); return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof ResearchDeskBlockEntity desk) {
            Containers.dropContents(level, pos, desk);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TimeContent.RESEARCH_BE.get(), ResearchDeskBlockEntity::tick);
    }
}
