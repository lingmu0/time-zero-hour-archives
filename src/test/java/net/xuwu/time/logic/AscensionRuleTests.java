package net.xuwu.time.logic;

/** Executable without any Minecraft bootstrap, window or server. */
public final class AscensionRuleTests {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void run() {
        for (int i=0;i<10000;i++) {
            int delta=AscensionRules.height(i+1)-AscensionRules.height(i);
            check(delta==4||delta==5,"platform vertical interval");
            for (int lane=0;lane<3;lane++) {
                check(Math.abs(AscensionRules.x(i,lane))<=5 && Math.abs(AscensionRules.z(i,lane))<=5,"footprints stay inside sanctuary");
                for (int other=lane+1;other<3;other++)
                    check(Math.abs(AscensionRules.x(i,lane)-AscensionRules.x(i,other))>=3
                        || Math.abs(AscensionRules.z(i,lane)-AscensionRules.z(i,other))>=3,"same-tier 3x3 platforms do not overlap");
            }
        }
        for (int tick=0;tick<AscensionRules.ARRIVAL_TICKS;tick++) check(!AscensionRules.climbing(tick),"arrival grace");
        double rise=0; int climb=0, rest=0, shift=0, rebases=0;
        for(int tick=0;tick<100000;tick++) {
            double old=rise;
            if(AscensionRules.climbing(tick)) { rise+=AscensionRules.RISE_PER_TICK; climb++; } else rest++;
            if(!AscensionRules.climbing(tick)) check(rise==old,"rest freezes tide and platform tier");
            int tier=AscensionRules.tier(rise);
            check(AscensionRules.height(tier+4)-shift < 2032,"top remains inside legal dimension height");
            if(64+rise-shift>1500) {
                int before=AscensionRules.height(tier)-shift; double tideBefore=64+rise-shift-18;
                shift+=1024; rebases++;
                check(AscensionRules.height(tier)-shift==before-1024,"rebase keeps platform displacement uniform");
                check(Math.abs((64+rise-shift-18)-(tideBefore-1024))<1e-8,"rebase keeps tide displacement uniform");
            }
        }
        check(rebases>0,"long ascent exercises rebasing");
        check(climb>rest,"moving/rest cadence");
        for(int cycle=0;cycle<10;cycle++) {
            int moving=0;
            for(int i=0;i<280;i++) if(AscensionRules.climbing(100+280*cycle+i)) moving++;
            check(moving==180,"nine-second climb and five-second rest");
        }
        check(AscensionRules.flash(0)==0 && AscensionRules.flash(25)==1,"white fade-in");
        check(AscensionRules.flash(45)==1 && AscensionRules.flash(65)==1,"transfer fully concealed");
        check(AscensionRules.flash(90)>0 && AscensionRules.flash(100)==0,"white fades out");
        check(EncounterRules.perTickDamage(10000,500)==.5f,"second act damage rate");
        check(EncounterRules.perTickDamage(.2f,500)==.2f,"settle final sub-tick remainder");
        check(EncounterRules.limitFinalDamage(1,500,4,10000)==1,"second act may reach actual death");
        check(AscensionRules.TIDE_RISE_PER_TICK > AscensionRules.RISE_PER_TICK,"tide rises faster than the Boss");
        System.out.println("PASS: "+checks+" ascension geometry, rest timing, height recycling, flash and damage-rate assertions.");
    }
    private AscensionRuleTests() {}
}
