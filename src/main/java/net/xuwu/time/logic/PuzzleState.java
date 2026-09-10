package net.xuwu.time.logic;

import java.util.Arrays;

/** Pure server-authoritative logic. Timestamps use level game time, never wall time. */
public final class PuzzleState {
    public enum Result { PROGRESS, SOLVED, WRONG, COOLDOWN, ALREADY_SOLVED }
    private int progress;
    private final int[] dials = new int[4];
    private long cooldownUntil;
    private long lastBell = -1;
    private boolean solved;

    public Result press(int value, int[] expected, long now) {
        if (solved) return Result.ALREADY_SOLVED;
        if (now < cooldownUntil) return Result.COOLDOWN;
        if (progress >= expected.length || value != expected[progress]) return fail(now);
        if (++progress == expected.length) { solved = true; return Result.SOLVED; }
        return Result.PROGRESS;
    }

    public Result bell(int value, long now) {
        if (solved) return Result.ALREADY_SOLVED;
        if (now < cooldownUntil) return Result.COOLDOWN;
        int[] order = {0, 1, 2};
        if (progress > 0) {
            long expectedDelay = progress == 1 ? 40 : 80;
            if (Math.abs((now - lastBell) - expectedDelay) > 12) return fail(now);
        }
        Result result = press(value, order, now);
        if (result == Result.PROGRESS) lastBell = now;
        return result;
    }

    public void expireBell(long now) {
        if (!solved && progress > 0 && lastBell >= 0 && now - lastBell > (progress == 1 ? 52 : 92)) {
            fail(now);
        }
    }

    public Result rotate(int dial, long now) {
        if (solved) return Result.ALREADY_SOLVED;
        if (now < cooldownUntil) return Result.COOLDOWN;
        if (dial < 0 || dial >= dials.length) return Result.WRONG;
        dials[dial] = (dials[dial] + 1) % 4;
        return Result.PROGRESS;
    }

    public Result checkDials(int[] expected, long now) {
        if (solved) return Result.ALREADY_SOLVED;
        if (now < cooldownUntil) return Result.COOLDOWN;
        if (Arrays.equals(dials, expected)) { solved = true; return Result.SOLVED; }
        return fail(now);
    }

    public Result fail(long now) {
        progress = 0;
        lastBell = -1;
        cooldownUntil = now + 40;
        return Result.WRONG;
    }

    public void solve() { solved = true; }
    public void reset(long now) {
        solved = false; progress = 0; Arrays.fill(dials, 0);
        lastBell = -1; cooldownUntil = now;
    }

    public int progress() { return progress; }
    public int dial(int i) { return dials[i]; }
    public int[] dials() { return dials.clone(); }
    public boolean solved() { return solved; }
    public long cooldownUntil() { return cooldownUntil; }

    public void restore(int progress, int[] values, boolean solved, long cooldownUntil) {
        this.progress = Math.max(0, Math.min(5, progress));
        for (int i = 0; i < 4; i++) dials[i] = i < values.length ? Math.floorMod(values[i], 4) : 0;
        this.solved = solved;
        this.cooldownUntil = Math.max(0, cooldownUntil);
        this.lastBell = -1;
    }

    /** Timed sequences cannot carry an incomplete rhythm across unload/restart. */
    public void clearRhythm() { if (!solved) progress = 0; lastBell = -1; }
}
