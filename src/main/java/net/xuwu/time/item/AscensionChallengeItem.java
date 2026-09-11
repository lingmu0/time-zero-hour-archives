package net.xuwu.time.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.xuwu.time.entity.AscensionFight;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.registry.TimeContent;

/** A non-progression practice entry that starts only the second act. */
public final class AscensionChallengeItem extends Item {
    public AscensionChallengeItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!(user instanceof ServerPlayer player)) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.isPassenger() || player.isSpectator()) return fail(player, stack, "challenge_unavailable");
        player.getCooldowns().addCooldown(this, 40);
        if (level.dimension().equals(AscensionFight.DIMENSION)) {
            if (player.isShiftKeyDown() && AscensionFight.returnPlayer(player)) return InteractionResultHolder.success(stack);
            return fail(player, stack, "ascension_already_in_fight");
        }
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(AscensionFight.RETURN, Tag.TAG_COMPOUND))
            return fail(player, stack, "ascension_already_in_fight");
        if (!level.getEntitiesOfClass(ChronicleKeeperEntity.class, player.getBoundingBox().inflate(48),
                boss -> boss.isAlive() && !boss.isRemoved()).isEmpty())
            return fail(player, stack, "ascension_challenge_nearby");
        var boss = TimeContent.CHRONICLE_KEEPER.get().create(level);
        if (boss == null) return fail(player, stack, "challenge_missing");
        boss.bindDirect(player.blockPosition());
        boss.moveTo(player.getX(), player.getY() + 2, player.getZ(), player.getYRot(), 0);
        boss.setPersistenceRequired();
        if (!level.addFreshEntity(boss) || !boss.beginDirect(player)) {
            boss.discard();
            return fail(player, stack, "challenge_missing");
        }
        player.sendSystemMessage(Component.translatable("message.time.ascension_challenge_started"));
        return InteractionResultHolder.success(stack);
    }
    private static InteractionResultHolder<ItemStack> fail(ServerPlayer player, ItemStack stack, String key) {
        player.displayClientMessage(Component.translatable("message.time." + key), true);
        return InteractionResultHolder.fail(stack);
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("message.time.ascension_challenge_hint"));
    }
}
