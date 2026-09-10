package net.xuwu.time.logic;

public enum PuzzleKind {
    DAY_SEQUENCE, SHADOW_DIALS, FROZEN_RECORDS,
    ARCHIVE_ORDER, MIRROR_PATH, DELAY_BELLS, ARCHIVE_GUARDIAN,
    PHASE_SEALS, BOSS_ARENA;

    public static PuzzleKind safe(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : DAY_SEQUENCE;
    }

    public String key() { return name().toLowerCase(java.util.Locale.ROOT); }
}
