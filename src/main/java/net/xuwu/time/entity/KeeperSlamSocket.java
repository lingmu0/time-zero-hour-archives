package net.xuwu.time.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Generated from actual staff bones at staff release; server-safe. */
public final class KeeperSlamSocket {
    public static Vec3 impact(Mob entity) {
        float idle=.045f+Mth.sin(entity.tickCount*.04f)*.035f;
        Matrix4f m=new Matrix4f().rotateY((180-entity.yBodyRot)*Mth.DEG_TO_RAD).scale(-1,-1,1).translate(0,-1.501f,0);
        m.translate(0.00000000f,1.68750000f,-0.12500000f);
        m.translate(0,Mth.sin(entity.tickCount*.045f)*.65f/16,0);
        m.rotateZYX(0.00000000f,0.00000000f,-0.13962634f);
        m.translate(0.00000000f,0.00000000f,0.00000000f);
        m.rotateZYX(0.00000000f,0.00000000f,0.00000000f);
        m.translate(-0.50000000f,-2.37500000f,0.00000000f);
        m.rotateZYX(-0.17453293f,0.00000000f,-0.22689280f-idle);
        m.translate(-0.12500000f,0.68750000f,0.00000000f);
        m.rotateZYX(-0.17453293f,0.00000000f,-0.29670597f);
        m.translate(0.00000000f,0.53125000f,-0.06250000f);
        m.rotateZYX(0.00000000f,0.00000000f,2.61799388f+idle);
        m.translate(0.00000000f,0.06250000f,-0.08125000f);
        m.rotateZYX(0.00000000f,0.00000000f,0.00000000f);
        m.translate(0.00000000f,-1.68750000f,0.00000000f);
        m.rotateZYX(0.00000000f,0.00000000f,0.00000000f);
        Vector3f socket=m.transformPosition(new Vector3f(0.00000000f,0.00000000f,0.00000000f));
        return entity.position().add(socket.x,socket.y,socket.z);
    }
    private KeeperSlamSocket() {}
}
