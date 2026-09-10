package net.xuwu.time.logic;

import java.util.*;

/** Pure JVM checks: no Minecraft bootstrap, no worlds, no OpenGL. */
public final class EncounterPatternTests {
    private static int checks;
    private static void check(boolean condition,String why){checks++;if(!condition)throw new AssertionError(why);}
    public static void run(){
        Set<String> orders=new HashSet<>();
        var seeds=new Random(123456789);
        for(long seed=0;seed<512;seed++){
            var lock=new AnchorSequence();lock.randomize(seeds.nextLong());int[] order=lock.order();orders.add(Arrays.toString(order));
            check(Arrays.equals(Arrays.stream(order).sorted().toArray(),new int[]{0,1,2,3}),"permutation");
            check(!lock.press((order[0]+1)%4)&&lock.progress()==0,"wrong first phase");
            check(lock.press(order[0])&&lock.mask()==1<<order[0],"light actual first phase, not fixed order");
            var restored=new AnchorSequence();check(restored.restore(lock.order(),lock.progress()),"save valid secret");
            check(restored.mask()==lock.mask(),"reload lit mask");
            for(int i=1;i<4;i++)check(restored.press(order[i]),"resume sequence");
            check(restored.progress()==4&&restored.mask()==15,"four phases unlock");
            check(!restored.press(order[0])&&restored.progress()==4,"completed lock cannot retrigger");
            check(!lock.press(order[0])&&lock.mask()==0&&Arrays.equals(lock.order(),order),"mistake resets prefix without reshuffling");
            check(!lock.restore(new int[]{0,0,2,3},2),"reject corrupt duplicate order");
            lock.randomize(seed+700);check(lock.progress()==0&&lock.mask()==0,"next phase resets all lights");
        }
        check(orders.size()==24,"all 24 orders reachable");
        var ring=new ArenaPattern(0,1,-18,-18,18,18,0,0);
        for(double r:new double[]{0,3.99,10.01,12.99,13})check(!ring.danger(r,0),"radial safe gap "+r);
        for(double r:new double[]{4,6,10,13.01,17.99})check(ring.danger(r,0),"radial danger "+r);
        check(!ring.danger(18,0)&&!ring.danger(-19,0),"clip outside arena");
        var random=new Random(917);
        for(int kind=0;kind<3;kind++)for(int seed=0;seed<24;seed++){
            var p=new ArenaPattern(kind,seed,101,203,138,240,110.5,217.25);
            var same=new ArenaPattern(kind,seed,101,203,138,240,110.5,217.25);
            check(p.polygons().equals(same.polygons()),"client/server deterministic geometry");
            if(kind==2){
                check(p.circles().size()==ArenaPattern.CIRCLE_COUNT,"twenty dense circles");
                check(p.circles().getFirst().x()==110.5&&p.circles().getFirst().z()==217.25&&p.danger(110.5,217.25),"one fixed target-foot circle");
                for(int a=0;a<p.circles().size();a++)for(int b=0;b<a;b++)
                    check(Math.hypot(p.circles().get(a).x()-p.circles().get(b).x(),p.circles().get(a).z()-p.circles().get(b).z())>ArenaPattern.CIRCLE_MIN_SEPARATION,
                        "dense circles keep their minimum center separation");
            }
            int safe=0,danger=0;
            for(var polygon:p.polygons())for(var v:polygon)check(v.x()>=p.minX-1e-8&&v.x()<=p.maxX+1e-8&&v.z()>=p.minZ-1e-8&&v.z()<=p.maxZ+1e-8,"warning clipped to arena");
            for(int i=0;i<800;i++){
                double x=101+random.nextDouble()*37,z=203+random.nextDouble()*37;
                boolean hit=p.danger(x,z);if(hit)danger++;else safe++;
                check(hit==same.danger(x,z),"synced hit mask");
                // 128-segment circles differ from analytic boundaries by < .005 blocks.
                boolean near=false;
                if(kind==0)for(double r:new double[]{4,10,13})near|=Math.abs(Math.hypot(x-p.cx,z-p.cz)-r)<.006;
                if(kind==2)for(var c:p.circles())near|=Math.abs(Math.hypot(x-c.x(),z-c.z())-c.radius())<.006;
                if(!near)check(hit==p.polygons().stream().anyMatch(poly->inside(poly,x,z)),"warning matches hit geometry");
            }
            check(safe>40&&danger>20,"all patterns leave meaningful safe and danger regions");
        }
        System.out.println("PASS: "+checks+" randomized shield, persistence, fixed-target, arena clipping and warning/hit assertions.");
    }
    private static boolean inside(List<ArenaPattern.Point> polygon,double x,double z){
        boolean in=false;
        for(int i=0,j=polygon.size()-1;i<polygon.size();j=i++){
            var a=polygon.get(i);var b=polygon.get(j);
            if((a.z()>z)!=(b.z()>z)&&x<(b.x()-a.x())*(z-a.z())/(b.z()-a.z())+a.x())in=!in;
        }
        return in;
    }
}
