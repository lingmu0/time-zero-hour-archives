package net.xuwu.time.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from model_source/chronal_bolt.bbmodel. */
public final class ChronalBoltGeometry {
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var part0 = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,24.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part1 = part0.addOrReplaceChild("dial", CubeListBuilder.create()
            .texOffs(32,0).mirror(false).addBox(-0.20000f,-2.80000f,-1.30000f,0.40000f,2.90000f,0.25000f, new CubeDeformation(0.00000f))
            .texOffs(16,0).mirror(false).addBox(-0.15000f,-0.20000f,-1.35000f,2.05000f,0.40000f,0.30000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part2 = part1.addOrReplaceChild("clock_rim_0", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.30000f,-0.35000f,-1.05000f,0.60000f,1.05000f,0.25000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-3.50000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part3 = part1.addOrReplaceChild("clock_rim_1", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(1.75000f,-3.03109f,0.00000f,0.00000f,0.00000f,0.52360f));
        var part4 = part1.addOrReplaceChild("clock_rim_2", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(3.03109f,-1.75000f,0.00000f,0.00000f,0.00000f,1.04720f));
        var part5 = part1.addOrReplaceChild("clock_rim_3", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.30000f,-0.35000f,-1.05000f,0.60000f,1.05000f,0.25000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(3.50000f,0.00000f,0.00000f,0.00000f,0.00000f,1.57080f));
        var part6 = part1.addOrReplaceChild("clock_rim_4", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(3.03109f,1.75000f,0.00000f,0.00000f,0.00000f,2.09440f));
        var part7 = part1.addOrReplaceChild("clock_rim_5", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(1.75000f,3.03109f,0.00000f,0.00000f,0.00000f,2.61799f));
        var part8 = part1.addOrReplaceChild("clock_rim_6", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.30000f,-0.35000f,-1.05000f,0.60000f,1.05000f,0.25000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,3.50000f,0.00000f,0.00000f,0.00000f,3.14159f));
        var part9 = part1.addOrReplaceChild("clock_rim_7", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-1.75000f,3.03109f,0.00000f,0.00000f,0.00000f,3.66519f));
        var part10 = part1.addOrReplaceChild("clock_rim_8", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-3.03109f,1.75000f,0.00000f,0.00000f,0.00000f,4.18879f));
        var part11 = part1.addOrReplaceChild("clock_rim_9", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.30000f,-0.35000f,-1.05000f,0.60000f,1.05000f,0.25000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-3.50000f,0.00000f,0.00000f,0.00000f,0.00000f,4.71239f));
        var part12 = part1.addOrReplaceChild("clock_rim_10", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-3.03109f,-1.75000f,0.00000f,0.00000f,0.00000f,5.23599f));
        var part13 = part1.addOrReplaceChild("clock_rim_11", CubeListBuilder.create()
            .texOffs(16,0).mirror(false).addBox(-0.65000f,-0.95000f,-0.80000f,1.30000f,1.90000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-1.75000f,-3.03109f,0.00000f,0.00000f,0.00000f,5.75959f));
        var part14 = part0.addOrReplaceChild("core", CubeListBuilder.create()
            .texOffs(48,0).mirror(false).addBox(-1.40000f,-1.40000f,-1.50000f,2.80000f,2.80000f,3.00000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.80000f,-0.80000f,1.50000f,1.60000f,1.60000f,4.00000f, new CubeDeformation(0.00000f))
            .texOffs(48,0).mirror(false).addBox(-0.35000f,-0.35000f,5.50000f,0.70000f,0.70000f,2.50000f, new CubeDeformation(0.00000f))
            .texOffs(48,0).mirror(false).addBox(-0.55000f,-0.55000f,-5.50000f,1.10000f,1.10000f,4.00000f, new CubeDeformation(0.00000f))
            .texOffs(32,0).mirror(false).addBox(-0.25000f,-0.25000f,-8.00000f,0.50000f,0.50000f,2.50000f, new CubeDeformation(0.00000f))
            .texOffs(16,0).mirror(false).addBox(-2.25000f,-0.35000f,2.00000f,0.50000f,0.70000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(16,0).mirror(false).addBox(-0.35000f,1.75000f,2.00000f,0.70000f,0.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(16,0).mirror(false).addBox(1.75000f,-0.35000f,2.00000f,0.50000f,0.70000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(16,0).mirror(false).addBox(-0.35000f,-2.25000f,2.00000f,0.70000f,0.50000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        return LayerDefinition.create(mesh, 64, 32);
    }
    private ChronalBoltGeometry() {}
}
