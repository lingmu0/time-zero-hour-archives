package net.xuwu.time.logic;

import java.util.*;

/** Immutable arena-space warning and hit geometry, independent of caster movement. */
public final class ArenaPattern {
    /** Dense random-circle variant: one target-foot circle plus nineteen seeded circles. */
    public static final int CIRCLE_COUNT=20;
    public static final double CIRCLE_MIN_SEPARATION=4.5;
    public record Point(double x,double z){}
    public record Circle(double x,double z,double radius){}
    public final int kind;
    public final double minX,minZ,maxX,maxZ,cx,cz;
    private final boolean stripesX;
    private final double stripeOffset;
    private final List<Circle> circles=new ArrayList<>();
    private List<List<Point>> polygons;
    public ArenaPattern(int kind,long seed,double minX,double minZ,double maxX,double maxZ,double targetX,double targetZ){
        this.kind=Math.floorMod(kind,3);this.minX=minX;this.minZ=minZ;this.maxX=maxX;this.maxZ=maxZ;
        cx=(minX+maxX)*.5;cz=(minZ+maxZ)*.5;
        var random=new Random(seed);stripesX=random.nextBoolean();stripeOffset=random.nextDouble()*6;
        circles.add(new Circle(Math.max(minX,Math.min(maxX,targetX)),Math.max(minZ,Math.min(maxZ,targetZ)),2.5));
        for(int attempt=0;attempt<640&&circles.size()<CIRCLE_COUNT;attempt++){
            double x=minX+random.nextDouble()*(maxX-minX),z=minZ+random.nextDouble()*(maxZ-minZ);
            if(circles.stream().allMatch(c->Math.hypot(c.x-x,c.z-z)>CIRCLE_MIN_SEPARATION))circles.add(new Circle(x,z,2.5));
        }
    }
    public List<Circle> circles(){return List.copyOf(circles);}
    public boolean danger(double x,double z){
        if(x<minX||x>=maxX||z<minZ||z>=maxZ)return false;
        if(kind==0)return EncounterRules.inRing(x-cx,z-cz);
        if(kind==1){double p=(stripesX?x-cx:z-cz)+stripeOffset;return p-Math.floor(p/6)*6<3;}
        // Called repeatedly by telegraph edge rendering; avoid streams/square roots per edge.
        for(var c:circles){double dx=x-c.x,dz=z-c.z;if(dx*dx+dz*dz<=c.radius*c.radius)return true;}
        return false;
    }
    /** Shapes clipped to the arena. Circles use 128 segments. */
    public List<List<Point>> polygons(){
        if(polygons!=null)return polygons;
        var result=new ArrayList<List<Point>>();
        if(kind==0){annulus(result,EncounterRules.RING_INNER,EncounterRules.RING_OUTER,cx,cz);annulus(result,EncounterRules.RING_FAR,Math.hypot(maxX-minX,maxZ-minZ),cx,cz);}
        else if(kind==1){
            double start=(stripesX?minX-cx:minZ-cz)+stripeOffset,end=(stripesX?maxX-cx:maxZ-cz)+stripeOffset;
            for(double p=Math.floor(start/6)*6;p<end;p+=6){
                double a=p-stripeOffset+(stripesX?cx:cz),b=a+3;
                add(result,stripesX?List.of(new Point(a,minZ),new Point(b,minZ),new Point(b,maxZ),new Point(a,maxZ))
                    :List.of(new Point(minX,a),new Point(maxX,a),new Point(maxX,b),new Point(minX,b)));
            }
        }else for(var c:circles)annulus(result,0,c.radius,c.x,c.z);
        polygons=List.copyOf(result);return polygons;
    }
    private void annulus(List<List<Point>> result,double inner,double outer,double x,double z){
        for(int i=0;i<128;i++){
            double a=i*Math.PI/64,b=(i+1)*Math.PI/64;
            add(result,List.of(new Point(x+Math.cos(a)*inner,z+Math.sin(a)*inner),new Point(x+Math.cos(a)*outer,z+Math.sin(a)*outer),
                new Point(x+Math.cos(b)*outer,z+Math.sin(b)*outer),new Point(x+Math.cos(b)*inner,z+Math.sin(b)*inner)));
        }
    }
    private void add(List<List<Point>> result,List<Point> input){
        List<Point> points=input;
        for(int edge=0;edge<4;edge++){
            var out=new ArrayList<Point>();
            for(int i=0;i<points.size();i++){
                var a=points.get(i);var b=points.get((i+1)%points.size());double da=distance(a,edge),db=distance(b,edge);
                if(da>=0)out.add(a);
                if((da>=0)!=(db>=0)){double t=da/(da-db);out.add(new Point(a.x+(b.x-a.x)*t,a.z+(b.z-a.z)*t));}
            }
            points=out;
        }
        if(points.size()>=3)result.add(List.copyOf(points));
    }
    private double distance(Point p,int edge){return switch(edge){case 0->p.x-minX;case 1->maxX-p.x;case 2->p.z-minZ;default->maxZ-p.z;};}
}
