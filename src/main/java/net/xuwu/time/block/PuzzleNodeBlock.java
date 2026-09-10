package net.xuwu.time.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class PuzzleNodeBlock extends BaseEntityBlock {
    public static final MapCodec<PuzzleNodeBlock> CODEC = simpleCodec(PuzzleNodeBlock::new);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final IntegerProperty TURN = IntegerProperty.create("turn", 0, 3);
    public PuzzleNodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(TURN, 0));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT, TURN); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PuzzleNodeBlockEntity(pos, state); }
    private void interact(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof PuzzleNodeBlockEntity node) node.interact(server, hand);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        interact(level, pos, player, hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(level, pos, player, InteractionHand.MAIN_HAND);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof PuzzleNodeBlockEntity node) node.step(player);
        super.stepOn(level, pos, state, entity);
    }
}
