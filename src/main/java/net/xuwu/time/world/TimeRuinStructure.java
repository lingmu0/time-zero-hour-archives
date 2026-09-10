package net.xuwu.time.world;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.xuwu.time.registry.TimeContent;

public final class TimeRuinStructure extends Structure {
    public static final MapCodec<TimeRuinStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(settingsCodec(instance), Codec.intRange(0, 2).fieldOf("variant").forGetter(s -> s.variant))
            .apply(instance, TimeRuinStructure::new));
    private final int variant;
    public TimeRuinStructure(StructureSettings settings, int variant) { super(settings); this.variant = variant; }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX() - 12, z = context.chunkPos().getMiddleBlockZ();
        int surface = context.chunkGenerator().getFirstOccupiedHeight(x + 12, z - 7,
            Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (surface < context.chunkGenerator().getSeaLevel() - 2 || surface > context.heightAccessor().getMaxBuildHeight() - 16) return Optional.empty();
        int y = variant == 0 ? surface : variant == 1 ? -12 : -42;
        y = Math.max(context.heightAccessor().getMinBuildHeight() + 6, Math.min(y, surface - (variant == 0 ? 0 : 14)));
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(new BlockPos(x + 12, surface, z - 7),
            builder -> builder.addPiece(new TimeRuinPiece(origin, variant, surface))));
    }
    @Override public StructureType<?> type() { return TimeContent.RUIN_TYPE.get(); }
}
