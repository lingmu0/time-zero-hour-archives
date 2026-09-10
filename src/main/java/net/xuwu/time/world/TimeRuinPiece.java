package net.xuwu.time.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.*;
import net.xuwu.time.logic.PuzzleKind;
import net.xuwu.time.registry.TimeContent;

/** Chunk-clipped construction. Controllers and nodes are configured only in their owning chunk. */
public final class TimeRuinPiece extends StructurePiece {
    private final BlockPos origin;
    private final int variant, surface;
    public TimeRuinPiece(BlockPos origin, int variant, int surface) {
        super(TimeContent.RUIN_PIECE.get(), 0, new BoundingBox(origin.getX() - 10, origin.getY() - 3, origin.getZ() - 12,
            origin.getX() + 34, Math.max(origin.getY() + 13, surface + 7), origin.getZ() + (variant == 1 ? 96 : variant == 2 ? 84 : 72)));
        this.origin = origin; this.variant = variant; this.surface = surface;
    }
    public TimeRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(TimeContent.RUIN_PIECE.get(), tag);
        origin = BlockPos.of(tag.getLong("Origin")); variant = Math.max(0, Math.min(2, tag.getInt("Variant"))); surface = tag.getInt("Surface");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Origin", origin.asLong()); tag.putInt("Variant", variant); tag.putInt("Surface", surface);
    }
    @Override public BlockPos getLocatorPosition() { return new BlockPos(origin.getX() + 12, surface + 1, origin.getZ() - 7); }
    private BlockPos p(int x, int y, int z) { return origin.offset(x, y, z); }

    private void block(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
        BlockPos target = p(x, y, z);
        if (clip.isInside(target) && boundingBox.isInside(target)) level.setBlock(target, state, 2);
    }
    private void fill(WorldGenLevel level, BoundingBox clip, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int ax = Math.max(x1, clip.minX() - origin.getX()), bx = Math.min(x2, clip.maxX() - origin.getX());
        int az = Math.max(z1, clip.minZ() - origin.getZ()), bz = Math.min(z2, clip.maxZ() - origin.getZ());
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) for (int y = y1; y <= y2; y++) block(level, clip, x, y, z, state);
    }
    private void room(WorldGenLevel level, BoundingBox clip, int x, int z, int width, int depth, int height) {
        var shell = TimeContent.CHRONAL_STONE.get().defaultBlockState();
        var trim = (variant == 0 ? Blocks.CUT_COPPER : Blocks.CHISELED_DEEPSLATE).defaultBlockState();
        fill(level, clip, x, -2, z, x + width - 1, height, z + depth - 1, shell);
        fill(level, clip, x + 1, 1, z + 1, x + width - 2, height - 1, z + depth - 2, Blocks.AIR.defaultBlockState());
        for (int xx = x + 1; xx < x + width - 1; xx++) for (int zz = z + 1; zz < z + depth - 1; zz++) {
            block(level, clip, xx, 0, zz, ((xx + zz) % 4 == 0 ? Blocks.POLISHED_DEEPSLATE : Blocks.DEEPSLATE_TILES).defaultBlockState());
        }
        // Floor inlays keep clues and arena quadrants legible without night vision.
        for (int xx = x + 5; xx < x + width - 2; xx += 8) for (int zz = z + 5; zz < z + depth - 2; zz += 8) {
            block(level, clip, xx, 0, zz, Blocks.SEA_LANTERN.defaultBlockState());
            block(level, clip, xx, height - 1, zz, Blocks.SEA_LANTERN.defaultBlockState());
        }
        for (int xx : new int[]{x + 2, x + width - 3}) for (int zz = z + 2; zz < z + depth - 2; zz += 6) {
            fill(level, clip, xx, 1, zz, xx, height - 1, zz, trim);
            block(level, clip, xx, height - 2, zz + 1, Blocks.SEA_LANTERN.defaultBlockState());
        }
        for (int yy : new int[]{0, height}) {
            fill(level, clip, x, yy, z, x + width - 1, yy, z, trim);
            fill(level, clip, x, yy, z + depth - 1, x + width - 1, yy, z + depth - 1, trim);
        }
        // Entrances and exits have three-block clearance.
        fill(level, clip, 11, 1, z, 13, 3, z, Blocks.AIR.defaultBlockState());
        fill(level, clip, 11, 1, z + depth - 1, 13, 3, z + depth - 1, Blocks.AIR.defaultBlockState());
        if (variant == 0) {
            // Raised copper cornices and four observation turrets break up the roof silhouette.
            for (int xx : new int[]{x + 1, x + width - 2}) for (int zz : new int[]{z + 1, z + depth - 2}) {
                fill(level, clip, xx, height + 1, zz, xx, height + 2, zz, Blocks.CUT_COPPER.defaultBlockState());
                block(level, clip, xx, height + 3, zz, Blocks.SEA_LANTERN.defaultBlockState());
            }
            for (int zz = z + 3; zz < z + depth - 1; zz += 6) for (int xx : new int[]{x, x + width - 1})
                fill(level, clip, xx, 1, zz, xx, height, zz, Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState());
            fill(level, clip, 9, 4, z, 15, 5, z, Blocks.CUT_COPPER.defaultBlockState());
        }
    }
    private void corridor(WorldGenLevel level, BoundingBox clip, int startZ, int endZ) {
        fill(level, clip, 9, -1, startZ, 15, 5, endZ, TimeContent.CHRONAL_STONE.get().defaultBlockState());
        fill(level, clip, 10, 1, startZ, 14, 4, endZ, Blocks.AIR.defaultBlockState());
        for (int z = startZ; z <= endZ; z += 2) block(level, clip, 12, 4, z, Blocks.SEA_LANTERN.defaultBlockState());
    }
    private void sign(WorldGenLevel level, BoundingBox clip, int x, int y, int z, String line1, String line2) {
        BlockPos at = p(x, y, z);
        block(level, clip, x, y, z, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8));
        if (clip.isInside(at) && level.getBlockEntity(at) instanceof SignBlockEntity be) {
            CompoundTag data = new CompoundTag();
            data.putString("Text1", Component.Serializer.toJson(Component.literal(line1)));
            data.putString("Text2", Component.Serializer.toJson(Component.literal(line2)));
            data.putString("Text3", "\"\""); data.putString("Text4", "\"\"");
            be.load(data); be.setChanged();
        }
    }
    private void node(WorldGenLevel level, BoundingBox clip, BlockPos controller, int x, int y, int z, int index) {
        BlockPos at = p(x, y, z);
        block(level, clip, x, y, z, TimeContent.NODE.get().defaultBlockState());
        if (clip.isInside(at) && level.getBlockEntity(at) instanceof PuzzleNodeBlockEntity be) be.configure(controller, index);
    }
    private void puzzle(WorldGenLevel level, BoundingBox clip, PuzzleKind kind, int z, int depth, int x, int width, BlockPos previous, int[][] inputs, boolean gate) {
        BlockPos controller = p(12, 0, z + depth / 2);
        List<BlockPos> nodes = new ArrayList<>(), gates = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            int[] input = inputs[i];
            node(level, clip, controller, input[0], input[1], z + input[2], i);
            nodes.add(p(input[0], input[1], z + input[2]));
            if (input[1] > 0) sign(level, clip, input[0], input[1] + 1, z + input[2], "刻印 " + (i + 1), "潜行右键阅读");
        }
        node(level, clip, controller, 5, 1, z + 3, PuzzleNodeBlockEntity.HINT);
        sign(level, clip, 5, 2, z + 3, "时序档案", "右键读取线索");
        node(level, clip, controller, x + width - 5, 1, z + depth - 4, PuzzleNodeBlockEntity.CLAIM);
        sign(level, clip, x + width - 5, 2, z + depth - 4, "研究成果", "完成后领取");
        node(level, clip, controller, x + 4, 1, z + depth - 4, PuzzleNodeBlockEntity.RESET);
        sign(level, clip, x + 4, 2, z + depth - 4, "重新校准", "重置未完成谜题");
        if (kind == PuzzleKind.SHADOW_DIALS) {
            node(level, clip, controller, 12, 1, z + 15, PuzzleNodeBlockEntity.CHECK);
            sign(level, clip, 12, 2, z + 15, "检查指针", "右键提交");
        }
        if (kind == PuzzleKind.ARCHIVE_GUARDIAN || kind == PuzzleKind.BOSS_ARENA) {
            node(level, clip, controller, 12, 1, z + 4, PuzzleNodeBlockEntity.START);
            sign(level, clip, 12, 2, z + 4, kind == PuzzleKind.BOSS_ARENA ? "零刻核心" : "编年核心", kind == PuzzleKind.BOSS_ARENA ? "持钥匙启动" : "右键启动");
        }
        if (gate) for (int xx = 11; xx <= 13; xx++) for (int yy = 1; yy <= 3; yy++) {
            BlockPos at = p(xx, yy, z + depth - 1); gates.add(at);
            block(level, clip, xx, yy, z + depth - 1, TimeContent.TEMPORAL_BARRIER.get().defaultBlockState());
        }
        block(level, clip, 12, 0, z + depth / 2, TimeContent.CONTROLLER.get().defaultBlockState());
        if (clip.isInside(controller) && level.getBlockEntity(controller) instanceof PuzzleControllerBlockEntity be) {
            be.configure(kind, p(x + 1, 0, z + 1), p(x + width - 2, 8, z + depth - 2),
                p(12, kind == PuzzleKind.BOSS_ARENA ? 3 : 1, z + depth / 2), previous, gates, nodes);
        }
    }

    @Override public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pos) {
        if (variant == 2) buildClockroom(level, clip);
        else {
            PuzzleKind[] kinds = variant == 0
                ? new PuzzleKind[]{PuzzleKind.DAY_SEQUENCE, PuzzleKind.SHADOW_DIALS, PuzzleKind.FROZEN_RECORDS}
                : new PuzzleKind[]{PuzzleKind.ARCHIVE_ORDER, PuzzleKind.MIRROR_PATH, PuzzleKind.DELAY_BELLS, PuzzleKind.ARCHIVE_GUARDIAN};
            BlockPos previous = null;
            for (int i = 0; i < kinds.length; i++) {
                int z = i * 24;
                room(level, clip, 0, z, 25, 21, 9);
                if (i + 1 < kinds.length) corridor(level, clip, z + 21, z + 23);
                int[][] inputs = switch (kinds[i]) {
                    case DAY_SEQUENCE, SHADOW_DIALS -> new int[][]{{6,1,9},{10,1,9},{14,1,9},{18,1,9}};
                    case FROZEN_RECORDS -> new int[][]{{12,1,10}};
                    case ARCHIVE_ORDER -> new int[][]{{6,1,7},{12,1,7},{18,1,7},{6,1,13},{12,1,13},{18,1,13}};
                    case MIRROR_PATH -> new int[][]{{9,0,6},{15,0,6},{9,0,10},{15,0,10},{9,0,14},{15,0,14}};
                    case DELAY_BELLS -> new int[][]{{6,1,10},{12,1,10},{18,1,10}};
                    default -> new int[0][];
                };
                decorate(level, clip, kinds[i], z);
                puzzle(level, clip, kinds[i], z, 21, 0, 25, previous, inputs, i + 1 < kinds.length);
                previous = p(12, 0, z + 10);
            }
            if (variant == 0) {
                // Guaranteed archaeology specimens before the sealed research room.
                for (int x : new int[]{3, 7, 19, 21}) {
                    block(level, clip, x, 1, 3, Blocks.SUSPICIOUS_SAND.defaultBlockState());
                    BlockPos at = p(x, 1, 3);
                    if (clip.isInside(at) && level.getBlockEntity(at) instanceof BrushableBlockEntity be)
                        be.setLootTable(TimeMod.id("archaeology/temporal_dust"), at.asLong());
                }
            }
            chest(level, clip, 3, 1, (kinds.length - 1) * 24 + 17, variant == 0 ? "observatory" : "archive");
        }
        entrance(level, clip);
    }

    private void decorate(WorldGenLevel level, BoundingBox clip, PuzzleKind kind, int z) {
        switch (kind) {
            case DAY_SEQUENCE -> {
                Block[] colors = {Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA};
                for (int i = 0; i < 4; i++) fill(level, clip, 5 + i * 4, 4, z + 18, 7 + i * 4, 6, z + 18, colors[i].defaultBlockState());
                // Broken copper observation roof with a visible central lens.
                fill(level, clip, 9, 9, z + 7, 15, 9, z + 13, Blocks.TINTED_GLASS.defaultBlockState());
                block(level, clip, 12, 10, z + 10, Blocks.AMETHYST_BLOCK.defaultBlockState());
                for (int mark = 0; mark < 12; mark++) {
                    double angle = mark * Math.PI / 6;
                    int xx = 12 + (int)Math.round(Math.sin(angle) * 8), zz = z + 10 + (int)Math.round(Math.cos(angle) * 8);
                    block(level, clip, xx, 10, zz, Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState());
                    if (mark % 3 == 0) block(level, clip, xx, 11, zz, Blocks.SEA_LANTERN.defaultBlockState());
                }
            }
            case SHADOW_DIALS -> {
                for (int x = 4; x <= 20; x += 4) {
                    fill(level, clip, x, 4, z + 18, x, 7, z + 18, Blocks.GOLD_BLOCK.defaultBlockState());
                    block(level, clip, x + 1, 6, z + 18, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                }
            }
            case FROZEN_RECORDS, ARCHIVE_ORDER -> {
                for (int zz = z + 5; zz <= z + 15; zz += 5) {
                    fill(level, clip, 1, 1, zz, 3, 4, zz + 1, Blocks.BOOKSHELF.defaultBlockState());
                    fill(level, clip, 21, 1, zz, 23, 4, zz + 1, Blocks.BOOKSHELF.defaultBlockState());
                }
            }
            case MIRROR_PATH -> {
                int[] correct = {1, 0, 1};
                for (int row = 0; row < 3; row++) {
                    int zz = z + 4 + row * 4;
                    fill(level, clip, 3, 1, zz, 21, 3, zz, TimeContent.CHRONAL_STONE.get().defaultBlockState());
                    for (int lane = 0; lane < 2; lane++) {
                        int xx = lane == 0 ? 9 : 15;
                        fill(level, clip, xx - 1, 1, zz, xx + 1, 3, zz, Blocks.AIR.defaultBlockState());
                        block(level, clip, xx, 4, zz, (lane == correct[row] ? Blocks.CYAN_GLAZED_TERRACOTTA : Blocks.MAGENTA_GLAZED_TERRACOTTA).defaultBlockState());
                    }
                }
            }
            case DELAY_BELLS -> {
                for (int x : new int[]{6,12,18}) {
                    fill(level, clip, x - 1, 5, z + 10, x + 1, 5, z + 10, Blocks.CUT_COPPER.defaultBlockState());
                    block(level, clip, x, 4, z + 10, Blocks.AMETHYST_BLOCK.defaultBlockState());
                }
            }
            case ARCHIVE_GUARDIAN -> {
                fill(level, clip, 8, 0, z + 6, 16, 0, z + 14, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                for (int xx : new int[]{5,19}) for (int zz : new int[]{z + 6,z + 14}) {
                    fill(level, clip, xx, 1, zz, xx, 5, zz, Blocks.BOOKSHELF.defaultBlockState());
                    block(level, clip, xx, 6, zz, Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
            default -> {}
        }
    }

    private void buildClockroom(WorldGenLevel level, BoundingBox clip) {
        room(level, clip, 0, 0, 25, 21, 9);
        puzzle(level, clip, PuzzleKind.PHASE_SEALS, 0, 21, 0, 25, null, new int[][]{{6,1,10},{12,1,10},{18,1,10}}, true);
        corridor(level, clip, 21, 23);
        room(level, clip, -8, 24, 41, 41, 11);
        Block[] colors = {Blocks.BLUE_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA};
        for (int x = -6; x <= 30; x++) for (int z = 26; z <= 62; z++) {
            int q = (x >= 12 ? 1 : 0) + (z >= 44 ? 2 : 0);
            if (x % 4 == 0 || z % 4 == 0) block(level, clip, x, 0, z, colors[q].defaultBlockState());
        }
        for (int x : new int[]{-3,27}) for (int z : new int[]{29,59}) {
            fill(level, clip, x, 1, z, x, 8, z, Blocks.CUT_COPPER.defaultBlockState());
            block(level, clip, x, 9, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        puzzle(level, clip, PuzzleKind.BOSS_ARENA, 24, 41, -8, 41, p(12,0,10),
            new int[][]{{0,1,8},{24,1,8},{0,1,32},{24,1,32},{12,1,20}}, true);
        corridor(level, clip, 65, 67);
        room(level, clip, 4, 68, 17, 13, 7);
        chest(level, clip, 12, 1, 77, "clockroom");
        block(level, clip, 8, 1, 75, TimeContent.RESEARCH_DESK.get().defaultBlockState());
        sign(level, clip, 12, 1, 73, "零刻档案", "时间仍在前行");
    }

    private void entrance(WorldGenLevel level, BoundingBox clip) {
        int top = surface - origin.getY();
        corridor(level, clip, -4, -1);
        if (variant != 0) {
            // Clear a small surface porch and provide a landing beside the ladder. A player
            // entering the doorway must never step straight into an 80-100 block fall.
            fill(level, clip, 8, top + 1, -11, 16, top + 6, -1, Blocks.AIR.defaultBlockState());
            fill(level, clip, 8, top, -3, 16, top, -1, Blocks.CUT_COPPER.defaultBlockState());
            fill(level, clip, 9, 0, -10, 15, top + 4, -4, TimeContent.CHRONAL_STONE.get().defaultBlockState());
            fill(level, clip, 10, 1, -9, 14, top + 3, -5, Blocks.AIR.defaultBlockState());
            fill(level, clip, 11, top, -9, 14, top, -5, Blocks.CUT_COPPER.defaultBlockState());
            block(level, clip, 12, top, -4, Blocks.CUT_COPPER.defaultBlockState());
            fill(level, clip, 11, 1, -4, 13, 3, -4, Blocks.AIR.defaultBlockState());
            fill(level, clip, 11, top + 1, -4, 13, top + 3, -4, Blocks.AIR.defaultBlockState());
            for (int y = 1; y <= top + 3; y++) {
                block(level, clip, 10, y, -7, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
                if (y % 8 == 0) block(level, clip, 14, y, -7, Blocks.SEA_LANTERN.defaultBlockState());
            }
            fill(level, clip, 8, top + 4, -11, 16, top + 4, -3, Blocks.CUT_COPPER.defaultBlockState());
            block(level, clip, 12, top + 5, -7, Blocks.SEA_LANTERN.defaultBlockState());
            sign(level, clip, 12, top + 1, -3, variant == 1 ? "逆时档案馆" : "零点钟室", "竖井通往地下");
        } else {
            fill(level, clip, 8, 0, -11, 16, 0, -4, Blocks.CUT_SANDSTONE.defaultBlockState());
            fill(level, clip, 8, 1, -11, 16, 6, -5, Blocks.AIR.defaultBlockState());
            for (int x : new int[]{8,16}) fill(level, clip, x, 1, -8, x, 5, -8, Blocks.CUT_COPPER.defaultBlockState());
            fill(level, clip, 8, 6, -8, 16, 6, -8, Blocks.CUT_COPPER.defaultBlockState());
            sign(level, clip, 12, 1, -7, "残刻观测台", "带上刷子与时钟");
        }
    }
    private void chest(WorldGenLevel level, BoundingBox clip, int x, int y, int z, String loot) {
        BlockPos at = p(x,y,z);
        block(level, clip, x,y,z, Blocks.CHEST.defaultBlockState());
        if (clip.isInside(at) && level.getBlockEntity(at) instanceof ChestBlockEntity chest) {
            chest.setLootTable(TimeMod.id("chests/" + loot), at.asLong());
        }
    }
}
