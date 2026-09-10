package net.xuwu.time.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.*;
import net.xuwu.time.logic.PuzzleKind;
import net.xuwu.time.registry.TimeContent;

/** One shared, reusable challenge room in a dedicated void dimension; no overworld edits. */
public final class ChallengeArena {
    public static final ResourceKey<Level> DIMENSION=ResourceKey.create(Registries.DIMENSION,TimeMod.id("challenge"));
    public static final BlockPos CONTROLLER=new BlockPos(0,64,0);
    public static final BlockPos ENTRY=new BlockPos(0,65,-15);
    public static PuzzleControllerBlockEntity prepare(ServerLevel level){
        if(!level.dimension().equals(DIMENSION))return null;
        for(int x=-2;x<=1;x++)for(int z=-2;z<=1;z++)level.getChunk(x,z);
        if(level.getBlockEntity(CONTROLLER) instanceof PuzzleControllerBlockEntity be)
            return be.kind()==PuzzleKind.BOSS_ARENA?be:null;
        // Never overwrite a damaged/customized room. All writes are bounded to a pristine box.
        for(BlockPos at:BlockPos.betweenClosed(-20,63,-20,20,76,20))if(!level.isEmptyBlock(at))return null;
        var shell=TimeContent.CHRONAL_STONE.get().defaultBlockState();
        Block[] accents={Blocks.BLUE_TERRACOTTA,Blocks.CYAN_TERRACOTTA,Blocks.PURPLE_TERRACOTTA,Blocks.ORANGE_TERRACOTTA};
        for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++){
            level.setBlock(new BlockPos(x,63,z),shell,2);
            int q=(x>=0?1:0)+(z>=0?2:0);
            level.setBlock(new BlockPos(x,64,z),(x%4==0||z%4==0?accents[q]:Blocks.DEEPSLATE_TILES).defaultBlockState(),2);
            level.setBlock(new BlockPos(x,76,z),shell,2);
            if(Math.abs(x)==20||Math.abs(z)==20)for(int y=65;y<76;y++)level.setBlock(new BlockPos(x,y,z),shell,2);
        }
        for(int x=-16;x<=16;x+=8)for(int z=-16;z<=16;z+=8){
            level.setBlock(new BlockPos(x,64,z),Blocks.SEA_LANTERN.defaultBlockState(),2);
            level.setBlock(new BlockPos(x,75,z),Blocks.SEA_LANTERN.defaultBlockState(),2);
        }
        var nodes=List.of(new BlockPos(-12,65,-12),new BlockPos(12,65,-12),new BlockPos(-12,65,12),new BlockPos(12,65,12),new BlockPos(0,65,0));
        for(int i=0;i<nodes.size();i++){
            var at=nodes.get(i);level.setBlock(at,TimeContent.NODE.get().defaultBlockState(),2);
            if(level.getBlockEntity(at) instanceof PuzzleNodeBlockEntity node)node.configure(CONTROLLER,i);
        }
        level.setBlock(CONTROLLER,TimeContent.CONTROLLER.get().defaultBlockState(),2);
        if(!(level.getBlockEntity(CONTROLLER) instanceof PuzzleControllerBlockEntity be))return null;
        be.configure(PuzzleKind.BOSS_ARENA,new BlockPos(-19,64,-19),new BlockPos(19,72,19),new BlockPos(0,67,0),null,List.of(),nodes);
        return be;
    }
    private ChallengeArena(){}
}
