package net.xuwu.time.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persisted site allocation keeps concurrent encounters and old saves apart. */
public final class SanctumSites extends SavedData {
    private int next;
    public static int allocate(ServerLevel level) {
        var data = level.getDataStorage().computeIfAbsent(SanctumSites::load, SanctumSites::new, "time_sanctum_sites");
        int site = data.next++;
        data.setDirty();
        return site;
    }
    private static SanctumSites load(CompoundTag tag) {
        var data = new SanctumSites(); data.next = Math.max(0, tag.getInt("Next")); return data;
    }
    @Override public CompoundTag save(CompoundTag tag) { tag.putInt("Next", next); return tag; }
}
