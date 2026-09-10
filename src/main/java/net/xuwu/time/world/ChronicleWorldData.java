package net.xuwu.time.world;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Completed room flags remain available when an earlier room's chunk is unloaded. */
public final class ChronicleWorldData extends SavedData {
    private final Set<Long> completed = new HashSet<>();
    public static ChronicleWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ChronicleWorldData::load, ChronicleWorldData::new, "time_rooms");
    }
    public boolean completed(BlockPos pos) { return completed.contains(pos.asLong()); }
    public void mark(BlockPos pos) { if (completed.add(pos.asLong())) setDirty(); }
    public void clear(BlockPos pos) { if (completed.remove(pos.asLong())) setDirty(); }
    private static ChronicleWorldData load(CompoundTag tag) {
        var result = new ChronicleWorldData();
        for (long pos : tag.getLongArray("Completed")) result.completed.add(pos);
        return result;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("Completed", completed.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }
}
