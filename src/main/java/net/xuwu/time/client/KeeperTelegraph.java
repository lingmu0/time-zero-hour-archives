package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.entity.ChronalCaster;
import net.xuwu.time.logic.EncounterRules;
import org.joml.Matrix4f;

/** Transient attack geometry only. Never paints permanent safe/unsafe quadrants. */
public final class KeeperTelegraph {
    public static boolean visible(ChronalCaster boss) {
        long impact = boss.ringImpactTime(), now = boss.caster().level().getGameTime();
        return boss.caster().isAlive() && boss.hasArenaVisuals() && impact >= 0 && now <= impact + 10;
    }
    public static void render(ChronalCaster boss, float partial, PoseStack pose, MultiBufferSource buffers) {
        renderSwap(boss,partial,pose,buffers);
        if (!visible(boss)) return;
        var entity = boss.caster(); var arena = boss.visualArena();
        double now = entity.level().getGameTime() + partial;
        double remaining = boss.ringImpactTime() - now;
        float charge = Mth.clamp(1 - (float)remaining / EncounterRules.RING_WARNING_TICKS, 0, 1);
        boolean impact = remaining <= 0;
        float fade = impact ? Mth.clamp(1 + (float)remaining / 10, 0, 1) : 1;
        pose.pushPose();
        // World-space pattern, frozen at cast start; caster motion never moves the warning.
        pose.translate(-Mth.lerp(partial,entity.xOld,entity.getX()), arena.minY + 1.035 - Mth.lerp(partial, entity.yOld, entity.getY()), -Mth.lerp(partial,entity.zOld,entity.getZ()));
        Matrix4f matrix = pose.last().pose();
        var buffer = buffers.getBuffer(RenderType.debugQuads());
        int fill = color((impact ? .65f : .24f + charge * .14f) * fade, 255, impact ? 170 : 54, 30);
        int edge = color(.95f * fade, 255, impact ? 230 : 118, 45);
        var pattern = boss.ringPattern();
        for (var polygon : pattern.polygons()) {
            var first=polygon.getFirst();
            for(int i=1;i<polygon.size()-1;i++) {
                var a=polygon.get(i);var b=polygon.get(i+1);
                vertex(buffer,matrix,first.x(),0,first.z(),fill);vertex(buffer,matrix,a.x(),0,a.z(),fill);
                vertex(buffer,matrix,b.x(),0,b.z(),fill);vertex(buffer,matrix,b.x(),0,b.z(),fill);
            }
            for(int i=0;i<polygon.size();i++) {
                var a=polygon.get(i);var b=polygon.get((i+1)%polygon.size());
                double dx=b.x()-a.x(),dz=b.z()-a.z(),length=Math.hypot(dx,dz);
                if(length<.001)continue;
                double nx=-dz/length*.045,nz=dx/length*.045,x=(a.x()+b.x())*.5,z=(a.z()+b.z())*.5;
                if(pattern.danger(x+nx*2,z+nz*2)==pattern.danger(x-nx*2,z-nz*2))continue;
                vertex(buffer,matrix,a.x()+nx,.015,a.z()+nz,edge);vertex(buffer,matrix,b.x()+nx,.015,b.z()+nz,edge);
                vertex(buffer,matrix,b.x()-nx,.015,b.z()-nz,edge);vertex(buffer,matrix,a.x()-nx,.015,a.z()-nz,edge);
                if(impact){
                    vertex(buffer,matrix,a.x(),0,a.z(),edge);vertex(buffer,matrix,b.x(),0,b.z(),edge);
                    vertex(buffer,matrix,b.x(),.8*fade,b.z(),color(.05f*fade,255,220,120));
                    vertex(buffer,matrix,a.x(),.8*fade,a.z(),color(.05f*fade,255,220,120));
                }
            }
        }
        pose.popPose();
    }
    private static void renderSwap(ChronalCaster caster, float partial, PoseStack pose, MultiBufferSource buffers) {
        if (!caster.swapping()) return;
        float age=caster.caster().level().getGameTime()-caster.swapStartedAt()+partial;
        float fade=Math.min(1,Math.min(age/4,(24-age)/4));
        var v=buffers.getBuffer(RenderType.debugQuads()); var m=pose.last().pose();
        for (int i=0;i<48;i++) {
            double a=i*Math.PI/24, b=(i+1)*Math.PI/24, r=1.65;
            int c=color(.92f*fade,105+(i%3)*30,70,205);
            vertex(v,m,Math.cos(a)*r,-.15,Math.sin(a)*r,c); vertex(v,m,Math.cos(b)*r,-.15,Math.sin(b)*r,c);
            vertex(v,m,Math.cos(b)*r,4,Math.sin(b)*r,c); vertex(v,m,Math.cos(a)*r,4,Math.sin(a)*r,c);
        }
        for(int i=0;i<5;i++) annulus(v,m,1.6,1.8,(i*.8+age*.13)%4,color(fade,190,205,255));
    }
    private static int color(float a, int r, int g, int b) { return ((int)(Mth.clamp(a,0,1)*255)<<24) | r<<16 | g<<8 | b; }
    private static void annulus(VertexConsumer v, Matrix4f m, double inner, double outer, double y, int c) {
        for(int i=0;i<96;i++) segment(v,m,inner,outer,i*Math.PI/48,(i+1)*Math.PI/48,y,c);
    }
    private static void segment(VertexConsumer v, Matrix4f m, double inner, double outer, double a, double b, double y, int c) {
        vertex(v,m,Math.cos(a)*inner,y,Math.sin(a)*inner,c);
        vertex(v,m,Math.cos(a)*outer,y,Math.sin(a)*outer,c);
        vertex(v,m,Math.cos(b)*outer,y,Math.sin(b)*outer,c);
        vertex(v,m,Math.cos(b)*inner,y,Math.sin(b)*inner,c);
    }
    private static void vertex(VertexConsumer v, Matrix4f m, double x, double y, double z, int c) { v.addVertex(m,(float)x,(float)y,(float)z).setColor(c); }
    private KeeperTelegraph() {}
}
