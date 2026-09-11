package net.xuwu.time.item;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.xuwu.time.world.ChallengeArena;

/** Convenience challenge access, without awarding prerequisite advancements or consuming quest items. */
public final class ChallengeSigilItem extends Item {
    private static final String RETURN="TimeChallengeReturn";
    public ChallengeSigilItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player user,InteractionHand hand){
        ItemStack stack=user.getItemInHand(hand);
        if(!(user instanceof ServerPlayer player))return InteractionResultHolder.sidedSuccess(stack,true);
        if(player.isPassenger()||player.isSpectator())return fail(player,stack,"challenge_unavailable");
        player.getCooldowns().addCooldown(this,40);
        if (level.dimension().equals(net.xuwu.time.entity.AscensionFight.DIMENSION)) {
            if (player.isShiftKeyDown() && net.xuwu.time.entity.AscensionFight.returnPlayer(player)) return InteractionResultHolder.success(stack);
            return fail(player,stack,"challenge_unavailable");
        }
        if(player.isShiftKeyDown())return leave(player,stack);
        ServerLevel arena=player.server.getLevel(ChallengeArena.DIMENSION);
        if(arena==null)return fail(player,stack,"challenge_missing");
        if(arena.getDifficulty()==Difficulty.PEACEFUL)return fail(player,stack,"peaceful");
        var controller=ChallengeArena.prepare(arena);
        if(controller==null)return fail(player,stack,"challenge_damaged");
        Vec3 entry=safePosition(arena,player,ChallengeArena.ENTRY);
        if(entry==null)return fail(player,stack,"challenge_no_landing");
        if(!level.dimension().equals(ChallengeArena.DIMENSION)){
            CompoundTag saved=new CompoundTag();saved.putString("Dimension",level.dimension().location().toString());
            saved.putLong("Position",player.blockPosition().asLong());saved.putFloat("Yaw",player.getYRot());saved.putFloat("Pitch",player.getXRot());
            CompoundTag persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            persisted.put(RETURN,saved);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        }
        teleport(player,arena,entry,0,0);
        if(controller.startChallenge(player))player.sendSystemMessage(Component.translatable("message.time.challenge_entered"));
        return InteractionResultHolder.success(stack);
    }
    private InteractionResultHolder<ItemStack> leave(ServerPlayer player,ItemStack stack){
        CompoundTag persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if(!player.level().dimension().equals(ChallengeArena.DIMENSION)||!persisted.contains(RETURN,Tag.TAG_COMPOUND))
            return fail(player,stack,"challenge_no_return");
        CompoundTag saved=persisted.getCompound(RETURN);ResourceLocation id=ResourceLocation.tryParse(saved.getString("Dimension"));
        ServerLevel destination=id==null?null:player.server.getLevel(ResourceKey.create(Registries.DIMENSION,id));
        if(destination==null)return fail(player,stack,"challenge_no_landing");
        Vec3 at=safePosition(destination,player,BlockPos.of(saved.getLong("Position")));
        if(at==null)return fail(player,stack,"challenge_no_landing");
        teleport(player,destination,at,saved.getFloat("Yaw"),saved.getFloat("Pitch"));
        persisted.remove(RETURN);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        player.sendSystemMessage(Component.translatable("message.time.challenge_returned"));return InteractionResultHolder.success(stack);
    }
    private static void teleport(ServerPlayer player,ServerLevel level,Vec3 at,float yaw,float pitch){
        player.teleportTo(level,at.x,at.y,at.z,yaw,pitch);player.setDeltaMovement(Vec3.ZERO);player.fallDistance=0;
    }
    private static Vec3 safePosition(ServerLevel level,ServerPlayer player,BlockPos origin){
        for(int radius=0;radius<=4;radius++)for(int dy=0;dy<=5;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
            BlockPos at=origin.offset(dx,dy,dz);
            if(at.getY()<=level.getMinBuildHeight()||at.getY()+2>=level.getMaxBuildHeight()||!level.getWorldBorder().isWithinBounds(at))continue;
            level.getChunkAt(at);Vec3 point=Vec3.atBottomCenterOf(at);
            if(!level.getBlockState(at.below()).isFaceSturdy(level,at.below(),Direction.UP))continue;
            if(!level.getFluidState(at).isEmpty()||!level.getFluidState(at.above()).isEmpty())continue;
            if(level.noCollision(player,player.getBoundingBox().move(point.subtract(player.position()))))return point;
        }
        return null;
    }
    private static InteractionResultHolder<ItemStack> fail(ServerPlayer player,ItemStack stack,String key){
        player.displayClientMessage(Component.translatable("message.time."+key),true);return InteractionResultHolder.fail(stack);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("message.time.challenge_hint"));
    }
}
