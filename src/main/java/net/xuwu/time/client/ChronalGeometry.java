package net.xuwu.time.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from model_source/archive_scribe.bbmodel. */
public final class ChronalGeometry {
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var part0 = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,24.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part1 = part0.addOrReplaceChild("robe", CubeListBuilder.create()
            .texOffs(0,0).mirror(false).addBox(-6.00000f,-35.00000f,-4.00000f,12.00000f,25.00000f,8.00000f, new CubeDeformation(0.00000f))
            .texOffs(0,0).mirror(false).addBox(-9.00000f,-15.00000f,-6.00000f,18.00000f,10.00000f,12.00000f, new CubeDeformation(0.00000f))
            .texOffs(64,0).mirror(false).addBox(-6.00000f,-43.00000f,-5.00000f,12.00000f,10.00000f,10.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part2 = part0.addOrReplaceChild("mask", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-4.00000f,-41.00000f,-5.50000f,8.00000f,6.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(192,0).mirror(false).addBox(-3.00000f,-39.00000f,-6.10000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(192,0).mirror(false).addBox(1.00000f,-39.00000f,-6.10000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(-5.00000f,-47.00000f,-2.00000f,2.00000f,5.00000f,3.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(3.00000f,-47.00000f,-2.00000f,2.00000f,5.00000f,3.00000f, new CubeDeformation(0.00000f))
            .texOffs(192,0).mirror(false).addBox(-0.75000f,-49.00000f,-2.00000f,1.50000f,7.00000f,3.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part3 = part0.addOrReplaceChild("arms", CubeListBuilder.create()
            .texOffs(64,0).mirror(false).addBox(-12.00000f,-32.00000f,-4.00000f,6.00000f,17.00000f,8.00000f, new CubeDeformation(0.00000f))
            .texOffs(64,0).mirror(false).addBox(6.00000f,-32.00000f,-4.00000f,6.00000f,17.00000f,8.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(-14.00000f,-34.00000f,-5.00000f,8.00000f,3.00000f,10.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(6.00000f,-34.00000f,-5.00000f,8.00000f,3.00000f,10.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(-12.50000f,-17.00000f,-4.50000f,7.00000f,2.00000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(5.50000f,-17.00000f,-4.50000f,7.00000f,2.00000f,9.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part4 = part0.addOrReplaceChild("spine", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-35.00000f,4.00000f,2.00000f,27.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part5 = part0.addOrReplaceChild("dial", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-1.00000f,-1.00000f,2.00000f,2.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(128,0).mirror(false).addBox(0.00000f,-0.50000f,-1.60000f,6.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-28.00000f,-7.00000f,0.00000f,0.00000f,0.00000f));
        var part6 = part5.addOrReplaceChild("tick_0", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,11.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part7 = part5.addOrReplaceChild("tick_1", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(5.50000f,9.52628f,0.00000f,0.00000f,0.00000f,-0.52360f));
        var part8 = part5.addOrReplaceChild("tick_2", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(9.52628f,5.50000f,0.00000f,0.00000f,0.00000f,-1.04720f));
        var part9 = part5.addOrReplaceChild("tick_3", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(11.00000f,0.00000f,0.00000f,0.00000f,0.00000f,-1.57080f));
        var part10 = part5.addOrReplaceChild("tick_4", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(9.52628f,-5.50000f,0.00000f,0.00000f,0.00000f,-2.09440f));
        var part11 = part5.addOrReplaceChild("tick_5", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(5.50000f,-9.52628f,0.00000f,0.00000f,0.00000f,-2.61799f));
        var part12 = part5.addOrReplaceChild("tick_6", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-11.00000f,0.00000f,0.00000f,0.00000f,-3.14159f));
        var part13 = part5.addOrReplaceChild("tick_7", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-5.50000f,-9.52628f,0.00000f,0.00000f,0.00000f,-3.66519f));
        var part14 = part5.addOrReplaceChild("tick_8", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-9.52628f,-5.50000f,0.00000f,0.00000f,0.00000f,-4.18879f));
        var part15 = part5.addOrReplaceChild("tick_9", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-11.00000f,0.00000f,0.00000f,0.00000f,0.00000f,-4.71239f));
        var part16 = part5.addOrReplaceChild("tick_10", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-9.52628f,5.50000f,0.00000f,0.00000f,0.00000f,-5.23599f));
        var part17 = part5.addOrReplaceChild("tick_11", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-1.00000f,-2.00000f,-1.00000f,2.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-5.50000f,9.52628f,0.00000f,0.00000f,0.00000f,-5.75959f));
        var part18 = part5.addOrReplaceChild("hand", CubeListBuilder.create()
            .texOffs(192,0).mirror(false).addBox(-0.50000f,-10.00000f,-1.50000f,1.00000f,10.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part19 = part0.addOrReplaceChild("pendulum", CubeListBuilder.create()
            .texOffs(128,0).mirror(false).addBox(-0.50000f,0.00000f,-0.50000f,1.00000f,13.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(192,0).mirror(false).addBox(-2.00000f,12.00000f,-1.00000f,4.00000f,4.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-23.00000f,-8.00000f,0.00000f,0.00000f,0.00000f));
        return LayerDefinition.create(mesh, 256, 256);
    }
    private ChronalGeometry() {}
}
