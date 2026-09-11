package net.xuwu.time.logic;

import java.util.Arrays;

/** Replays critical state transitions without launching Minecraft or a game server. */
public final class StateTests {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        EncounterPatternTests.run();
        AscensionRuleTests.run();
        var state = new PuzzleState();
        int[] day = {0,1,2,3};
        check(state.press(0, day, 100) == PuzzleState.Result.PROGRESS, "first observation");
        check(state.press(2, day, 101) == PuzzleState.Result.WRONG, "reject wrong chronology");
        check(state.progress() == 0, "wrong order discards partial sequence");
        check(state.press(0, day, 140) == PuzzleState.Result.COOLDOWN, "cooldown cannot be bypassed");
        for (int i = 0; i < 4; i++) state.press(i, day, 141 + i);
        check(state.solved(), "correct sequence solves");
        check(state.press(3, day, 200) == PuzzleState.Result.ALREADY_SOLVED, "solved state cannot be retriggered");
        state.reset(250);
        check(!state.solved() && state.progress() == 0, "explicit reset");
        state.restore(2, new int[]{1,3,0,2}, false, 0);
        check(state.press(2, day, 260) == PuzzleState.Result.PROGRESS, "resume persisted sequence");
        check(state.press(3, day, 261) == PuzzleState.Result.SOLVED, "resume completion");
        var dials = new PuzzleState();
        for (int index = 0; index < 4; index++) for (int turn = 0; turn < new int[]{1,3,0,2}[index]; turn++) dials.rotate(index, 0);
        check(dials.checkDials(new int[]{1,3,0,2}, 1) == PuzzleState.Result.SOLVED, "dial calibration");
        int[] copy = dials.dials(); copy[0] = 99;
        check(dials.dial(0) == 1, "external array cannot mutate dials");
        dials.reset(2); for (int i = 0; i < 4; i++) dials.rotate(0, 3);
        check(dials.dial(0) == 0, "dial wraps at four positions");
        var rhythm = new PuzzleState();
        rhythm.bell(0, 100); rhythm.bell(1, 140);
        check(rhythm.bell(2, 220) == PuzzleState.Result.SOLVED, "2-second then 4-second rhythm");
        rhythm.reset(300); rhythm.bell(0, 300);
        check(rhythm.bell(1, 327) == PuzzleState.Result.WRONG, "early bell rejected");
        rhythm.reset(400); rhythm.bell(0, 400); rhythm.bell(1, 452);
        check(rhythm.bell(2, 520) == PuzzleState.Result.SOLVED, "inclusive timing tolerance");
        rhythm.reset(600); rhythm.bell(0, 600); rhythm.expireBell(653);
        check(rhythm.progress() == 0 && !rhythm.solved(), "timed-out sequence resets");
        rhythm.restore(2, new int[4], false, 0); rhythm.clearRhythm();
        check(rhythm.progress() == 0, "restart cannot resume an expired rhythm");
        var order = new PuzzleState();
        for (int i : new int[]{2,0,4,1,5,3}) order.press(i, new int[]{2,0,4,1,5,3}, 0);
        check(order.solved(), "archive causal order");
        var mirror = new PuzzleState();
        for (int i : new int[]{1,2,5}) mirror.press(i, new int[]{1,2,5}, 0);
        check(mirror.solved(), "three physical mirror gates");
        check(EncounterRules.phase(1) == 0, "initial boss phase");
        check(EncounterRules.phase(.8f) == 1 && EncounterRules.phase(.6f) == 2, "upper phase boundaries");
        check(EncounterRules.phase(.35f) == 3 && EncounterRules.phase(.15f) == 4, "lower phase boundaries");
        check(EncounterRules.boltCount(0) == 1 && EncounterRules.boltCount(1) == 2
            && EncounterRules.boltCount(2) == 3 && EncounterRules.boltCount(3) == 4
            && EncounterRules.boltCount(4) == 5, "one additional bolt per phase");
        check(EncounterRules.PAST_REWIND_INTERVAL_TICKS == 200
            && EncounterRules.PAST_REWIND_RESPONSE_TICKS == 200, "past rewind uses a ten-second window and cadence");
        check(EncounterRules.partyScale(1) == 1 && Math.abs(EncounterRules.partyScale(5) - 2.05) < .0001, "party scaling caps at four");
        check(EncounterRules.quadrant(-1,-1) == 0 && EncounterRules.quadrant(1,1) == 3, "arena compass orientation");
        check(EncounterRules.limitFinalDamage(900,900,0,100000) == 180, "burst damage cannot skip first mechanic");
        check(EncounterRules.limitFinalDamage(725,900,0,9) == 5, "post-armor damage reaches exact threshold");
        check(EncounterRules.limitFinalDamage(720,900,0,30) == 0, "phase boundary waits for transition");
        check(EncounterRules.limitFinalDamage(10,900,4,100) == 10, "final phase remains killable");
        check(EncounterRules.limitFinalDamage(900,900,0,-1) == 0, "negative damage ignored");
        check(EncounterRules.limitFinalDamage(900,900,0,Float.NaN) == 0, "invalid damage never poisons health");
        check(Math.abs(EncounterRules.perTickDamage(100, 900) - .9f) < .0001f, "queued damage settles at one-thousandth max health per tick");
        check(Math.abs(EncounterRules.perTickDamage(.4f, 900) - .4f) < .0001f, "queued damage below the tick cap settles completely");
        check(EncounterRules.phaseDamageRemaining(900, 900, 0) == 180, "queued damage cannot cross a phase boundary");
        check(EncounterRules.safeQuadrant(0) == 0 && EncounterRules.safeQuadrant(1) == 2
            && EncounterRules.safeQuadrant(2) == 3 && EncounterRules.safeQuadrant(3) == 1
            && EncounterRules.safeQuadrant(4) == 0, "counter-clockwise safe quadrant order");
        check(!EncounterRules.inRing(0,0) && EncounterRules.inRing(4,0), "center safe and danger starts at four");
        check(EncounterRules.inRing(6,0) && EncounterRules.inRing(0,-6), "ring danger follows horizontal radius");
        check(EncounterRules.inRing(8,0) && EncounterRules.inRing(10,0) && !EncounterRules.inRing(12,0) && EncounterRules.inRing(14,0), "ring plus far danger leaves a ten-to-thirteen gap");
        check(EncounterRules.shouldSwap(0) && EncounterRules.shouldSwap(.499f) && !EncounterRules.shouldSwap(.5f) && !EncounterRules.shouldSwap(.999f), "half of random swap outcomes exchange positions");
        check(EncounterRules.limitFinalDamage(900,900,0,Float.POSITIVE_INFINITY)==180, "infinite burst cannot skip a phase");
        System.out.println("PASS: " + checks + " chronology, persistence, timing and encounter assertions.");
    }
}
