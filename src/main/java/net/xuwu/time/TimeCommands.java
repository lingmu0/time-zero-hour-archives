package net.xuwu.time;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.xuwu.time.block.PuzzleControllerBlockEntity;
import net.xuwu.time.registry.TimeContent;

public final class TimeCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("timearchive").requires(source -> source.hasPermission(2))
            .then(Commands.literal("kit").executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                for (var item : TimeContent.ITEMS.getEntries()) {
                    var stack = new ItemStack(item.get());
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                }
                ctx.getSource().sendSuccess(() -> Component.translatable("message.time.kit"), false);
                return 1;
            }))
            .then(Commands.literal("status").then(Commands.argument("controller", BlockPosArgument.blockPos()).executes(ctx -> {
                var pos = BlockPosArgument.getLoadedBlockPos(ctx, "controller");
                if (ctx.getSource().getLevel().getBlockEntity(pos) instanceof PuzzleControllerBlockEntity puzzle) {
                    ctx.getSource().sendSuccess(() -> Component.literal(puzzle.kind().key() + " solved=" + puzzle.solved() + " encounter=" + puzzle.encounterId()), false);
                    return 1;
                }
                ctx.getSource().sendFailure(Component.translatable("message.time.not_controller")); return 0;
            }))));
    }
    private TimeCommands() {}
}
