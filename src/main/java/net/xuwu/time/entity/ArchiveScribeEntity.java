package net.xuwu.time.entity;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.xuwu.time.block.PuzzleControllerBlockEntity;

public final class ArchiveScribeEntity extends Monster {
    private BlockPos controller, home;
    private Vec3 lastPosition;
    private boolean rewound;
    private int idle, clock;
    public ArchiveScribeEntity(EntityType<? extends ArchiveScribeEntity> type, Level level) { super(type, level); xpReward = 0; }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 220).add(Attributes.ATTACK_DAMAGE, 7)
            .add(Attributes.ARMOR, 6).add(Attributes.MOVEMENT_SPEED, .24).add(Attributes.FOLLOW_RANGE, 32);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this)); goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 24));
    }
    public void bind(BlockPos controller, BlockPos home) { this.controller = controller; this.home = home; }
    private PuzzleControllerBlockEntity puzzle() {
        return controller != null && level().hasChunkAt(controller) && level().getBlockEntity(controller) instanceof PuzzleControllerBlockEntity p ? p : null;
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        var puzzle = puzzle();
        if (puzzle == null) {
            if (controller != null && ++idle > 200) discard();
            return;
        }
        List<ServerPlayer> players = puzzle.players();
        if (!getUUID().equals(puzzle.encounterId())) { discard(); return; }
        if (players.isEmpty()) {
            if (++idle >= 200) { puzzle.abandonEncounter(getUUID()); discard(); }
            return;
        }
        idle = 0; clock++;
        var target = players.stream().filter(p -> !p.isCreative()).min(java.util.Comparator.comparingDouble(p -> p.distanceToSqr(this))).orElse(null);
        if (target != null) {
            getLookControl().setLookAt(target, 30, 30);
            if (clock % 20 == 0 && target.distanceToSqr(this) > 36) getNavigation().moveTo(target, .8);
            if (clock % 45 == 0) ChronalBoltEntity.fire(this, target.getEyePosition(), 7);
            if (clock % 160 == 0) {
                for (var player : players) if (!player.isCreative() && player.distanceToSqr(this) < 36)
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                server.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1, getZ(), 60, 4, 1, 4, .1);
            }
        }
        if (clock % 40 == 0) lastPosition = position();
        if (!rewound && getHealth() <= getMaxHealth() * .5f) {
            rewound = true;
            if (lastPosition != null) teleportTo(lastPosition.x, lastPosition.y, lastPosition.z);
            heal(getMaxHealth() * .15f);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1, getZ(), 60, 1, 1, 1, .1);
        }
        if (!puzzle.arena().contains(position()) && home != null) teleportTo(home.getX() + .5, home.getY(), home.getZ() + .5);
    }
    @Override public void die(DamageSource source) {
        if (!level().isClientSide && puzzle() != null) puzzle().encounterDefeated(getUUID());
        super.die(source);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (controller != null) tag.putLong("Controller", controller.asLong());
        if (home != null) tag.putLong("Home", home.asLong());
        tag.putBoolean("Rewound", rewound); tag.putInt("Clock", clock);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null;
        home = tag.contains("Home") ? BlockPos.of(tag.getLong("Home")) : null;
        rewound = tag.getBoolean("Rewound"); clock = tag.getInt("Clock");
    }
}
