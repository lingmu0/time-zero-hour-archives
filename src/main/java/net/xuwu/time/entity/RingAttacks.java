package net.xuwu.time.entity;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.AABB;
import net.xuwu.time.logic.ArenaPattern;

public final class RingAttacks {
    private CompoundTag last;
    private AABB arena;
    private ArenaPattern cached;
    public ArenaPattern decode(CompoundTag tag,AABB bounds){
        if(cached==null||!tag.equals(last)||!bounds.equals(arena)){
            last=tag.copy();arena=bounds;
            cached=new ArenaPattern(tag.getInt("Kind"),tag.getLong("Seed"),bounds.minX,bounds.minZ,bounds.maxX,bounds.maxZ,tag.getDouble("TargetX"),tag.getDouble("TargetZ"));
        }
        return cached;
    }
    public static CompoundTag create(ChronalCaster caster,ServerPlayer target,int previous){
        var random=caster.caster().getRandom();var tag=new CompoundTag();
        tag.putInt("Kind",previous<0?random.nextInt(3):(previous+1+random.nextInt(2))%3);
        tag.putLong("Seed",random.nextLong());tag.putDouble("TargetX",target.getX());tag.putDouble("TargetZ",target.getZ());
        return tag;
    }
    public static void resolve(ChronalCaster caster,List<ServerPlayer> targets,float damage){
        var entity=caster.caster();if(!(entity.level() instanceof ServerLevel server))return;
        var pattern=caster.ringPattern();var arena=caster.visualArena();
        for(var player:targets)if(arena.contains(player.position())&&pattern.danger(player.getX(),player.getZ()))player.hurt(entity.damageSources().mobAttack(entity),damage);
        // One bounded burst at release, distributed throughout the authoritative hit mask.
        for(double x=arena.minX+.5;x<arena.maxX;x+=1.5)for(double z=arena.minZ+.5;z<arena.maxZ;z+=1.5)
            if(pattern.danger(x,z))server.sendParticles(ParticleTypes.ENCHANT,x,arena.minY+1.15,z,5,.15,.3,.15,.6);
    }
}
