package net.xuwu.time.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** One-way player footing; ray casts, mobs and projectiles see empty space. */
public final class SanctumPlatformBlock extends Block {
    public SanctumPlatformBlock(Properties properties) { super(properties); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context instanceof EntityCollisionContext entity && entity.getEntity() instanceof Player player
            && player.getDeltaMovement().y <= 0 && context.isAbove(Shapes.block(), pos, false)
            ? Shapes.block() : Shapes.empty();
    }
    @Override public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        if (entity instanceof Player) entity.fallDistance = 0;
        else super.fallOn(level, state, pos, entity, distance);
    }
}
