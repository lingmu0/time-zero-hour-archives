package net.xuwu.time.item;

import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.xuwu.time.TimeMod;
import net.xuwu.time.TimeConfig;

public final class LocatorItem extends Item {
    private final String target;
    public LocatorItem(Properties properties, String target) { super(properties); this.target = target; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (!server.dimension().equals(Level.OVERWORLD)) {
                player.displayClientMessage(Component.translatable("message.time.overworld_only"), true);
                return InteractionResultHolder.fail(stack);
            }
            var tracker = stack.get(DataComponents.LODESTONE_TRACKER);
            net.minecraft.core.BlockPos pos = tracker == null || tracker.target().isEmpty() ? null : tracker.target().get().pos();
            if (pos == null || player.isShiftKeyDown()) {
                player.getCooldowns().addCooldown(this, 200);
                pos = server.findNearestMapStructure(TagKey.create(Registries.STRUCTURE, TimeMod.id(target)), player.blockPosition(), TimeConfig.LOCATOR_RADIUS.get(), false);
            }
            if (pos != null && pos.getY() == 0) {
                // Vanilla locate points at a start chunk (Y=0). Resolve the actual surface
                // entrance, including old cached probes from 0.1.0, before guiding players.
                var structure = server.registryAccess().registryOrThrow(Registries.STRUCTURE).get(TimeMod.id(target));
                if (structure != null) {
                    var start = server.getChunkAt(pos).getStartForStructure(structure);
                    if (start != null && start.isValid()) {
                        pos = start.getPieces().stream().filter(p -> p instanceof net.xuwu.time.world.TimeRuinPiece)
                            .map(net.minecraft.world.level.levelgen.structure.StructurePiece::getLocatorPosition).findFirst().orElse(pos);
                    }
                }
            }
            if (pos != null) stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(java.util.Optional.of(GlobalPos.of(server.dimension(), pos)), false));
            player.sendSystemMessage(pos == null ? Component.translatable("message.time.not_found")
                : Component.translatable("message.time.located", Component.translatable("structure.time." + target), pos.getX(), pos.getY(), pos.getZ()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("message.time.probe_hint"));
    }
}
