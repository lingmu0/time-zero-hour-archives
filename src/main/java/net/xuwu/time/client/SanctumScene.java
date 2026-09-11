package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import org.joml.Matrix4f;

/** Cosmetic architecture and a tide surface sharing the server's exact hazard height. */
public final class SanctumScene {
    public static void render(ChronicleKeeperEntity boss, float partial, PoseStack pose, MultiBufferSource buffers) {
        if (!boss.ascended() || !boss.isAlive()) return;
        var data = boss.ascensionView();
        double bx = Mth.lerp(partial,boss.xOld,boss.getX()), by = Mth.lerp(partial,boss.yOld,boss.getY()), bz = Mth.lerp(partial,boss.zOld,boss.getZ());
        double x = data.getInt("X"), z = data.getInt("Z"), tide = data.getDouble("Tide");
        double now = boss.level().getGameTime() + partial;
        pose.pushPose(); pose.translate(-bx, -by, -bz);
        Matrix4f matrix = pose.last().pose();
        VertexConsumer v = buffers.getBuffer(RenderType.debugQuads());
        // Opaque, slightly undulating black liquid. Peak displacement stays below the rescue margin.
        for (int dx = -90; dx < 90; dx += 6) for (int dz = -90; dz < 90; dz += 6) {
            quad(v,matrix,new Vec3(x+dx,wave(tide,dx,dz,now),z+dz),
                new Vec3(x+dx,wave(tide,dx,dz+6,now),z+dz+6),
                new Vec3(x+dx+6,wave(tide,dx+6,dz+6,now),z+dz+6),
                new Vec3(x+dx+6,wave(tide,dx+6,dz,now),z+dz), 0xFF090A10);
        }
        for (int i = 0; i < 9; i++) ring(v,matrix,new Vec3(x,tide+.14,z),new Vec3(1,0,0),new Vec3(0,0,1),
                2+i*5+(now*.025)%5,.035,0xFF25242E);
        double cameraY = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y;
        // Segmented columns fade into the white void above and below the camera; no visible ceiling.
        double base = Math.floor(cameraY / 16) * 16;
        for (int segment = -5; segment <= 5; segment++) {
            double y = base + segment * 16;
            float fade = (float)Math.max(0,1-Math.abs(y+8-cameraY)/88);
            int white = ((int)(fade*230)<<24)|0xFFFFFF, gold = ((int)(fade*230)<<24)|0xD8BB69;
            for (int i = 0; i < 8; i++) {
                double a = i*Math.PI/4, px = x+Math.cos(a)*30, pz = z+Math.sin(a)*30;
                box(v,matrix,px-.6,y,pz-.6,px+.6,y+16,pz+.6,white);
                box(v,matrix,px-.66,y+1,pz-.66,px+.66,y+1.18,pz+.66,gold);
            }
            ring(v,matrix,new Vec3(x,y,z),new Vec3(1,0,0),new Vec3(0,0,1),29,.12,gold);
            ring(v,matrix,new Vec3(x,y+.3,z),new Vec3(1,0,0),new Vec3(0,0,1),31,.04,white);
        }
        ring(v,matrix,new Vec3(bx,by+3.7,bz),new Vec3(1,0,0),new Vec3(0,0,1),2.1,.055,0xFFF3DC94);
        for (var value : data.getList("Portals", Tag.TAG_COMPOUND)) {
            var gate = (net.minecraft.nbt.CompoundTag)value;
            Vec3 origin = new Vec3(gate.getDouble("X"),gate.getDouble("Y"),gate.getDouble("Z"));
            Vec3 target = new Vec3(gate.getDouble("TX"),gate.getDouble("TY"),gate.getDouble("TZ"));
            Vec3 forward = target.subtract(origin).normalize();
            Vec3 u = forward.cross(Math.abs(forward.y)>.9?new Vec3(1,0,0):new Vec3(0,1,0)).normalize(), w = forward.cross(u);
            double charge = Mth.clamp(1-(gate.getLong("Due")-now)/24,0,1);
            ring(v,matrix,origin,u,w,.45+charge*.3,.09,0xFFFFE2A0);
            ring(v,matrix,origin,u,w,1.05-charge*.3,.025,0xFFFFFFFF);
        }
        pose.popPose();
    }
    private static double wave(double tide,double x,double z,double now) { return tide+Math.sin(x*.25+now*.06)*Math.cos(z*.23+now*.035)*.1; }
    private static void ring(VertexConsumer v,Matrix4f m,Vec3 c,Vec3 u,Vec3 w,double radius,double width,int color) {
        for(int i=0;i<64;i++) {
            double a=i*Math.PI/32,b=(i+1)*Math.PI/32;
            Vec3 d=u.scale(Math.cos(a)).add(w.scale(Math.sin(a))),e=u.scale(Math.cos(b)).add(w.scale(Math.sin(b)));
            quad(v,m,c.add(d.scale(radius)),c.add(e.scale(radius)),c.add(e.scale(radius+width)),c.add(d.scale(radius+width)),color);
        }
    }
    private static void box(VertexConsumer v,Matrix4f m,double x,double y,double z,double X,double Y,double Z,int color) {
        Vec3 a=new Vec3(x,y,z),b=new Vec3(X,y,z),c=new Vec3(X,y,Z),d=new Vec3(x,y,Z);
        Vec3 A=new Vec3(x,Y,z),B=new Vec3(X,Y,z),C=new Vec3(X,Y,Z),D=new Vec3(x,Y,Z);
        quad(v,m,a,b,B,A,color);quad(v,m,b,c,C,B,color);quad(v,m,c,d,D,C,color);
        quad(v,m,d,a,A,D,color);quad(v,m,A,B,C,D,color);quad(v,m,d,c,b,a,color);
    }
    private static void quad(VertexConsumer v,Matrix4f m,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color) {
        vertex(v,m,a,color);vertex(v,m,b,color);vertex(v,m,c,color);vertex(v,m,d,color);
    }
    private static void vertex(VertexConsumer v,Matrix4f m,Vec3 p,int color) { v.addVertex(m,(float)p.x,(float)p.y,(float)p.z).setColor(color); }
    private SanctumScene() {}
}
