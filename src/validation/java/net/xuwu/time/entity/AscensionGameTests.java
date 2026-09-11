package net.xuwu.time.entity;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;
import net.xuwu.time.registry.TimeContent;

/** Compiled by normal verification; run only when the user requests game/server testing. */
@GameTestHolder("time_validation")
@PrefixGameTestTemplate(false)
public final class AscensionGameTests {
    @GameTest(template="empty", timeoutTicks=100)
    public static void platform_only_catches_descending_players(GameTestHelper h) {
        var level=h.getLevel(); BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        var state=TimeContent.SANCTUM_PLATFORM.get().defaultBlockState();
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"PlatformTest"));
        player.moveTo(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5,0,0); player.setDeltaMovement(0,-.2,0);
        h.assertTrue(!state.getCollisionShape(level,pos,CollisionContext.of(player)).isEmpty(),"descending player can land");
        player.setDeltaMovement(0,.4,0);
        h.assertTrue(state.getCollisionShape(level,pos,CollisionContext.of(player)).isEmpty(),"ascending player passes through");
        var boss=TimeContent.CHRONICLE_KEEPER.get().create(level);
        var bolt=TimeContent.CHRONAL_BOLT.get().create(level);
        h.assertTrue(state.getCollisionShape(level,pos,CollisionContext.of(boss)).isEmpty(),"boss passes through");
        h.assertTrue(state.getCollisionShape(level,pos,CollisionContext.of(bolt)).isEmpty(),"projectile passes through");
        h.assertTrue(state.getShape(level,pos,CollisionContext.of(player)).isEmpty(),"melee targeting is unobstructed");
        h.assertTrue(state.getVisualShape(level,pos,CollisionContext.of(player)).isEmpty(),"camera has no opaque clipping face");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=100)
    public static void first_bar_transitions_second_bar_dies(GameTestHelper h) {
        var level=h.getLevel(); var boss=TimeContent.CHRONICLE_KEEPER.get().create(level);
        boss.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));
        CompoundTag first=new CompoundTag(); boss.addAdditionalSaveData(first);
        first.putInt("Phase",4); first.putBoolean("Shield",false); boss.readAdditionalSaveData(first);
        boss.setHealth(.5f); boss.queueDamage(10000); boss.tick();
        h.assertTrue(boss.isAlive()&&boss.transitioning(),"first bar depletion begins transition, not death");
        float frozen=boss.getHealth(); boss.queueDamage(10000); boss.tick();
        h.assertTrue(boss.getHealth()==frozen,"transition rejects further queued hits");
        var second=TimeContent.CHRONICLE_KEEPER.get().create(level);
        second.prepareAscensionHealth();
        CompoundTag state=new CompoundTag(); second.addAdditionalSaveData(state);
        CompoundTag act=state.getCompound("Ascension");act.putInt("Mode",2);state.put("Ascension",act);
        second.readAdditionalSaveData(state);
        float max=second.getMaxHealth();second.queueDamage(max);second.tick();
        h.assertTrue(Math.abs(second.getHealth()-(max-max*.001f))<.001f,"second bar also drains by one thousandth per tick");
        CompoundTag reload=new CompoundTag();second.addAdditionalSaveData(reload);
        var restored=TimeContent.CHRONICLE_KEEPER.get().create(level);restored.readAdditionalSaveData(reload);
        h.assertTrue(restored.ascended()&&restored.getHealth()==second.getHealth(),"second act and health survive NBT reload");
        restored.setHealth(.1f);restored.queueDamage(10000);restored.tick();
        h.assertTrue(!restored.isAlive()&&!restored.transitioning(),"second bar depletion finally kills");
        boss.discard();second.discard();restored.discard();h.succeed();
    }
}
