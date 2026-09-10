package net.xuwu.time.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from model_source/chronicle_keeper_sovereign.bbmodel. */
public final class KeeperGeometry {
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var part0 = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,24.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part1 = part0.addOrReplaceChild("torso", CubeListBuilder.create()
            .texOffs(2,2).mirror(false).addBox(-6.00000f,-40.00000f,-3.00000f,12.00000f,16.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(44,2).mirror(false).addBox(-4.00000f,-26.00000f,-3.00000f,8.00000f,5.00000f,6.00000f, new CubeDeformation(0.00000f))
            .texOffs(76,2).mirror(false).addBox(-6.00000f,-40.00000f,-4.00000f,12.00000f,10.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(108,2).mirror(false).addBox(-6.00000f,-25.00000f,-4.50000f,12.00000f,2.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(140,2).mirror(false).addBox(-2.00000f,-42.00000f,4.00000f,4.00000f,18.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(156,2).mirror(false).addBox(-0.75000f,-27.00000f,6.00000f,1.50000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(165,2).mirror(false).addBox(-0.75000f,-30.00000f,6.00000f,1.50000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(174,2).mirror(false).addBox(-0.75000f,-33.00000f,6.00000f,1.50000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(183,2).mirror(false).addBox(-0.75000f,-36.00000f,6.00000f,1.50000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(192,2).mirror(false).addBox(-0.75000f,-39.00000f,6.00000f,1.50000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(201,2).mirror(false).addBox(-6.50000f,-40.00000f,-5.00000f,1.00000f,12.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(211,2).mirror(false).addBox(-7.00000f,-41.00000f,-3.00000f,5.00000f,2.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(257,2).mirror(false).addBox(-6.00000f,-27.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(267,2).mirror(false).addBox(-6.00000f,-30.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(277,2).mirror(false).addBox(-6.00000f,-33.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(287,2).mirror(false).addBox(5.50000f,-40.00000f,-5.00000f,1.00000f,12.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(297,2).mirror(false).addBox(2.00000f,-41.00000f,-3.00000f,5.00000f,2.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(343,2).mirror(false).addBox(4.00000f,-27.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(353,2).mirror(false).addBox(4.00000f,-30.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(363,2).mirror(false).addBox(4.00000f,-33.00000f,-4.60000f,2.00000f,1.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(373,2).mirror(false).addBox(-2.00000f,-44.00000f,-1.50000f,4.00000f,4.00000f,4.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part2 = part1.addOrReplaceChild("collar_blade_-1", CubeListBuilder.create()
            .texOffs(239,2).mirror(false).addBox(-1.00000f,-6.00000f,0.00000f,2.00000f,7.00000f,5.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-5.00000f,-40.00000f,1.00000f,0.00000f,0.00000f,-0.41888f));
        var part3 = part1.addOrReplaceChild("collar_blade_1", CubeListBuilder.create()
            .texOffs(325,2).mirror(false).addBox(-1.00000f,-6.00000f,0.00000f,2.00000f,7.00000f,5.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(5.00000f,-40.00000f,1.00000f,0.00000f,0.00000f,0.41888f));
        var part4 = part0.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(393,2).mirror(false).addBox(-4.50000f,-7.00000f,-3.00000f,9.00000f,8.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(429,2).mirror(false).addBox(-3.50000f,-6.00000f,-4.20000f,7.00000f,6.50000f,1.50000f, new CubeDeformation(0.00000f))
            .texOffs(450,2).mirror(false).addBox(-4.00000f,-6.50000f,-4.70000f,8.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(474,2).mirror(false).addBox(-0.50000f,-5.00000f,-4.90000f,1.00000f,5.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(482,2).mirror(false).addBox(-2.50000f,0.00000f,-4.00000f,5.00000f,1.50000f,1.50000f, new CubeDeformation(0.00000f))
            .texOffs(499,2).mirror(false).addBox(-3.00000f,-4.00000f,-4.85000f,3.00000f,1.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(2,29).mirror(false).addBox(-2.50000f,-3.45000f,-5.05000f,1.50000f,0.70000f,0.40000f, new CubeDeformation(0.00000f))
            .texOffs(10,29).mirror(false).addBox(-5.00000f,-7.00000f,-2.00000f,1.00000f,8.00000f,5.00000f, new CubeDeformation(0.00000f))
            .texOffs(26,29).mirror(false).addBox(-3.50000f,-1.50000f,-4.90000f,1.00000f,1.50000f,0.40000f, new CubeDeformation(0.00000f))
            .texOffs(33,29).mirror(false).addBox(0.00000f,-4.00000f,-4.85000f,3.00000f,1.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(45,29).mirror(false).addBox(1.00000f,-3.45000f,-5.05000f,1.50000f,0.70000f,0.40000f, new CubeDeformation(0.00000f))
            .texOffs(53,29).mirror(false).addBox(4.00000f,-7.00000f,-2.00000f,1.00000f,8.00000f,5.00000f, new CubeDeformation(0.00000f))
            .texOffs(69,29).mirror(false).addBox(2.50000f,-1.50000f,-4.90000f,1.00000f,1.50000f,0.40000f, new CubeDeformation(0.00000f))
            .texOffs(109,94).mirror(false).addBox(-0.40000f,-7.30000f,-5.45000f,0.80000f,2.00000f,0.70000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-44.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part5 = part4.addOrReplaceChild("crown", CubeListBuilder.create()
            .texOffs(76,29).mirror(false).addBox(-5.00000f,-1.50000f,-3.50000f,10.00000f,1.50000f,7.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-6.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part6 = part5.addOrReplaceChild("crown_ray_0", CubeListBuilder.create()
            .texOffs(115,29).mirror(false).addBox(-0.55000f,-2.70000f,-2.70000f,1.10000f,2.70000f,1.40000f, new CubeDeformation(0.00000f))
            .texOffs(124,29).mirror(false).addBox(-0.40000f,-2.70000f,-2.85000f,0.80000f,0.80000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-4.00000f,-1.00000f,0.00000f,0.00000f,0.00000f,-0.31416f));
        var part7 = part5.addOrReplaceChild("crown_ray_1", CubeListBuilder.create()
            .texOffs(133,29).mirror(false).addBox(-0.55000f,-3.60000f,-2.70000f,1.10000f,3.60000f,1.40000f, new CubeDeformation(0.00000f))
            .texOffs(142,29).mirror(false).addBox(-0.40000f,-3.60000f,-2.85000f,0.80000f,0.80000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-2.00000f,-1.00000f,0.00000f,0.00000f,0.00000f,-0.15708f));
        var part8 = part5.addOrReplaceChild("crown_ray_2", CubeListBuilder.create()
            .texOffs(151,29).mirror(false).addBox(-0.55000f,-4.50000f,-2.70000f,1.10000f,4.50000f,1.40000f, new CubeDeformation(0.00000f))
            .texOffs(160,29).mirror(false).addBox(-0.40000f,-4.50000f,-2.85000f,0.80000f,0.80000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-1.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part9 = part5.addOrReplaceChild("crown_ray_3", CubeListBuilder.create()
            .texOffs(169,29).mirror(false).addBox(-0.55000f,-3.60000f,-2.70000f,1.10000f,3.60000f,1.40000f, new CubeDeformation(0.00000f))
            .texOffs(178,29).mirror(false).addBox(-0.40000f,-3.60000f,-2.85000f,0.80000f,0.80000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(2.00000f,-1.00000f,0.00000f,0.00000f,0.00000f,0.15708f));
        var part10 = part5.addOrReplaceChild("crown_ray_4", CubeListBuilder.create()
            .texOffs(187,29).mirror(false).addBox(-0.55000f,-2.70000f,-2.70000f,1.10000f,2.70000f,1.40000f, new CubeDeformation(0.00000f))
            .texOffs(197,29).mirror(false).addBox(-0.40000f,-2.70000f,-2.85000f,0.80000f,0.80000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(4.00000f,-1.00000f,0.00000f,0.00000f,0.00000f,0.31416f));
        var part11 = part0.addOrReplaceChild("hourglass", CubeListBuilder.create()
            .texOffs(206,29).mirror(false).addBox(-4.00000f,-6.00000f,0.00000f,8.00000f,12.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(228,29).mirror(false).addBox(-4.50000f,-6.50000f,-1.50000f,9.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(254,29).mirror(false).addBox(-4.50000f,5.50000f,-1.50000f,9.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(280,29).mirror(false).addBox(-4.50000f,-5.00000f,-1.30000f,1.00000f,11.00000f,1.20000f, new CubeDeformation(0.00000f))
            .texOffs(289,29).mirror(false).addBox(3.50000f,-5.00000f,-1.30000f,1.00000f,11.00000f,1.20000f, new CubeDeformation(0.00000f))
            .texOffs(298,29).mirror(false).addBox(-3.00000f,-4.70000f,-1.00000f,6.00000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(317,29).mirror(false).addBox(-3.00000f,4.30000f,-1.00000f,6.00000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(336,29).mirror(false).addBox(-2.35000f,-3.50000f,-1.00000f,4.70000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(352,29).mirror(false).addBox(-2.35000f,3.10000f,-1.00000f,4.70000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(368,29).mirror(false).addBox(-1.70000f,-2.30000f,-1.00000f,3.40000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(382,29).mirror(false).addBox(-1.70000f,1.90000f,-1.00000f,3.40000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(396,29).mirror(false).addBox(-1.05000f,-1.10000f,-1.00000f,2.10000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(407,29).mirror(false).addBox(-1.05000f,0.70000f,-1.00000f,2.10000f,1.20000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(418,29).mirror(false).addBox(-0.30000f,-1.00000f,-1.70000f,0.60000f,3.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-33.00000f,-5.00000f,0.00000f,0.00000f,0.00000f));
        var part12 = part0.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part13 = part12.addOrReplaceChild("staff_arm", CubeListBuilder.create()
            .texOffs(426,29).mirror(false).addBox(-3.00000f,0.00000f,-2.00000f,4.00000f,10.00000f,5.00000f, new CubeDeformation(0.00000f))
            .texOffs(448,29).mirror(false).addBox(-4.00000f,9.00000f,-2.50000f,4.00000f,3.00000f,5.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-8.00000f,-38.00000f,0.00000f,-0.13963f,0.00000f,0.17453f));
        var part14 = part13.addOrReplaceChild("pauldron_-1", CubeListBuilder.create()
            .texOffs(470,29).mirror(false).addBox(-2.00000f,-1.50000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,48).mirror(false).addBox(-2.00000f,0.50000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(20,48).mirror(false).addBox(-4.20000f,0.00000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(54,48).mirror(false).addBox(-4.20000f,2.00000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(72,48).mirror(false).addBox(-6.40000f,1.50000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(106,48).mirror(false).addBox(-6.40000f,3.50000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(124,48).mirror(false).addBox(-3.00000f,-2.50000f,-1.50000f,2.00000f,1.00000f,3.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,-0.26180f));
        var part15 = part13.addOrReplaceChild("forearm_-1", CubeListBuilder.create()
            .texOffs(138,48).mirror(false).addBox(-2.50000f,0.00000f,-3.00000f,5.00000f,6.50000f,6.00000f, new CubeDeformation(0.00000f))
            .texOffs(164,48).mirror(false).addBox(-3.00000f,0.00000f,-3.50000f,6.00000f,2.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(194,48).mirror(false).addBox(-0.50000f,2.00000f,-3.60000f,1.00000f,5.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(202,48).mirror(false).addBox(-0.30000f,3.00000f,-3.85000f,0.60000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-2.00000f,11.00000f,0.00000f,-1.43117f,0.00000f,-0.17453f));
        var part16 = part15.addOrReplaceChild("staff_hand", CubeListBuilder.create()
            .texOffs(209,48).mirror(false).addBox(-1.80000f,-0.50000f,-0.30000f,3.60000f,3.00000f,2.30000f, new CubeDeformation(0.00000f))
            .texOffs(209,48).mirror(false).addBox(-1.30000f,1.70000f,-2.70000f,2.60000f,0.70000f,1.20000f, new CubeDeformation(0.00000f))
            .texOffs(209,48).mirror(false).addBox(-1.30000f,0.80000f,-2.70000f,2.60000f,0.70000f,1.20000f, new CubeDeformation(0.00000f))
            .texOffs(209,48).mirror(false).addBox(-1.30000f,-0.10000f,-2.70000f,2.60000f,0.70000f,1.20000f, new CubeDeformation(0.00000f))
            .texOffs(209,48).mirror(false).addBox(1.00000f,0.00000f,-2.10000f,0.70000f,2.00000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,8.50000f,-1.00000f,1.57080f,0.00000f,0.00000f));
        var part17 = part16.addOrReplaceChild("chronostaff", CubeListBuilder.create()
            .texOffs(187,121).mirror(false).addBox(-0.65000f,-26.00000f,-0.65000f,1.30000f,45.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(197,121).mirror(false).addBox(-1.00000f,-4.00000f,-1.00000f,2.00000f,8.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(209,121).mirror(false).addBox(-1.20000f,17.80000f,-1.20000f,2.40000f,1.20000f,2.40000f, new CubeDeformation(0.00000f))
            .texOffs(223,121).mirror(false).addBox(-1.20000f,3.80000f,-1.20000f,2.40000f,1.20000f,2.40000f, new CubeDeformation(0.00000f))
            .texOffs(237,121).mirror(false).addBox(-1.20000f,-5.20000f,-1.20000f,2.40000f,1.20000f,2.40000f, new CubeDeformation(0.00000f))
            .texOffs(251,121).mirror(false).addBox(-1.20000f,-22.20000f,-1.20000f,2.40000f,1.20000f,2.40000f, new CubeDeformation(0.00000f))
            .texOffs(265,121).mirror(false).addBox(-1.20000f,-26.20000f,-1.20000f,2.40000f,1.20000f,2.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,1.00000f,-1.30000f,0.00000f,0.00000f,0.00000f));
        var part18 = part17.addOrReplaceChild("staff_clock", CubeListBuilder.create()
            .texOffs(455,121).mirror(false).addBox(-1.00000f,-1.00000f,-1.00000f,2.00000f,2.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(467,121).mirror(false).addBox(-0.35000f,-3.00000f,-1.50000f,0.70000f,3.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-27.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part19 = part18.addOrReplaceChild("staff_cog_0", CubeListBuilder.create()
            .texOffs(279,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(293,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part20 = part18.addOrReplaceChild("staff_cog_1", CubeListBuilder.create()
            .texOffs(301,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(315,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.78540f));
        var part21 = part18.addOrReplaceChild("staff_cog_2", CubeListBuilder.create()
            .texOffs(323,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(337,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.57080f));
        var part22 = part18.addOrReplaceChild("staff_cog_3", CubeListBuilder.create()
            .texOffs(345,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(359,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.35619f));
        var part23 = part18.addOrReplaceChild("staff_cog_4", CubeListBuilder.create()
            .texOffs(367,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(381,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.14159f));
        var part24 = part18.addOrReplaceChild("staff_cog_5", CubeListBuilder.create()
            .texOffs(389,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(403,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.92699f));
        var part25 = part18.addOrReplaceChild("staff_cog_6", CubeListBuilder.create()
            .texOffs(411,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(425,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.71239f));
        var part26 = part18.addOrReplaceChild("staff_cog_7", CubeListBuilder.create()
            .texOffs(433,121).mirror(false).addBox(-1.30000f,-5.20000f,-1.00000f,2.60000f,1.20000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(447,121).mirror(false).addBox(-0.40000f,-4.00000f,-1.20000f,0.80000f,1.50000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.49779f));
        var part27 = part12.addOrReplaceChild("book_arm", CubeListBuilder.create()
            .texOffs(256,48).mirror(false).addBox(-1.00000f,0.00000f,-2.00000f,4.00000f,10.00000f,5.00000f, new CubeDeformation(0.00000f))
            .texOffs(278,48).mirror(false).addBox(0.00000f,9.00000f,-2.50000f,4.00000f,3.00000f,5.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(8.00000f,-38.00000f,0.00000f,-0.13963f,0.00000f,-0.17453f));
        var part28 = part27.addOrReplaceChild("pauldron_1", CubeListBuilder.create()
            .texOffs(300,48).mirror(false).addBox(-2.00000f,-1.50000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(334,48).mirror(false).addBox(-2.00000f,0.50000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(352,48).mirror(false).addBox(0.20000f,0.00000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(386,48).mirror(false).addBox(0.20000f,2.00000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(404,48).mirror(false).addBox(2.40000f,1.50000f,-4.00000f,6.00000f,2.50000f,9.00000f, new CubeDeformation(0.00000f))
            .texOffs(438,48).mirror(false).addBox(2.40000f,3.50000f,-4.30000f,6.00000f,0.80000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(456,48).mirror(false).addBox(1.00000f,-2.50000f,-1.50000f,2.00000f,1.00000f,3.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.26180f));
        var part29 = part27.addOrReplaceChild("forearm_1", CubeListBuilder.create()
            .texOffs(470,48).mirror(false).addBox(-2.50000f,0.00000f,-3.00000f,5.00000f,8.00000f,6.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,67).mirror(false).addBox(-3.00000f,0.00000f,-3.50000f,6.00000f,2.00000f,7.00000f, new CubeDeformation(0.00000f))
            .texOffs(32,67).mirror(false).addBox(-0.50000f,2.00000f,-3.60000f,1.00000f,5.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.30000f,3.00000f,-3.85000f,0.60000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(2.00000f,11.00000f,0.00000f,-1.36136f,0.00000f,0.17453f));
        var part30 = part29.addOrReplaceChild("book_hand", CubeListBuilder.create()
            .texOffs(47,67).mirror(false).addBox(-1.80000f,-0.50000f,-0.30000f,3.60000f,3.00000f,2.30000f, new CubeDeformation(0.00000f))
            .texOffs(47,67).mirror(false).addBox(-1.40000f,2.20000f,-0.30000f,0.75000f,2.50000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(47,67).mirror(false).addBox(-0.45000f,2.20000f,-0.30000f,0.75000f,2.50000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(47,67).mirror(false).addBox(0.50000f,2.20000f,-0.30000f,0.75000f,2.50000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(47,67).mirror(false).addBox(-2.30000f,1.10000f,-0.20000f,0.90000f,2.10000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,8.50000f,-1.00000f,-0.07189f,-0.00340f,-0.02399f));
        var part31 = part30.addOrReplaceChild("held_book", CubeListBuilder.create()
            .texOffs(475,121).mirror(false).addBox(-0.60000f,-7.00000f,-1.50000f,1.20000f,7.00000f,2.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,4.50000f,-5.00000f,0.00000f,0.00000f,0.00000f));
        var part32 = part31.addOrReplaceChild("book_leaf_-1", CubeListBuilder.create()
            .texOffs(487,121).mirror(false).addBox(-6.00000f,-4.50000f,0.00000f,6.00000f,8.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,172).mirror(false).addBox(-5.60000f,-4.00000f,-1.00000f,5.60000f,7.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(20,172).mirror(false).addBox(-4.90000f,1.75000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(32,172).mirror(false).addBox(-4.90000f,0.50000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(44,172).mirror(false).addBox(-4.90000f,-0.75000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(56,172).mirror(false).addBox(-4.90000f,-2.00000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(68,172).mirror(false).addBox(-6.00000f,-4.30000f,-1.30000f,1.00000f,1.00000f,1.70000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-3.00000f,-0.50000f,0.00000f,0.31416f,0.00000f));
        var part33 = part31.addOrReplaceChild("book_leaf_1", CubeListBuilder.create()
            .texOffs(78,172).mirror(false).addBox(0.00000f,-4.50000f,0.00000f,6.00000f,8.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(96,172).mirror(false).addBox(0.00000f,-4.00000f,-1.00000f,5.60000f,7.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(114,172).mirror(false).addBox(0.70000f,1.75000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(126,172).mirror(false).addBox(0.70000f,0.50000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(138,172).mirror(false).addBox(0.70000f,-0.75000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(150,172).mirror(false).addBox(0.70000f,-2.00000f,-1.15000f,3.50000f,0.25000f,0.20000f, new CubeDeformation(0.00000f))
            .texOffs(162,172).mirror(false).addBox(5.00000f,-4.30000f,-1.30000f,1.00000f,1.00000f,1.70000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-3.00000f,-0.50000f,0.00000f,-0.31416f,0.00000f));
        var part34 = part0.addOrReplaceChild("mantle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,-25.00000f,3.00000f,0.00000f,0.00000f,0.00000f));
        var part35 = part34.addOrReplaceChild("mantle_panel_0", CubeListBuilder.create()
            .texOffs(94,67).mirror(false).addBox(-2.40000f,0.00000f,0.00000f,4.80000f,6.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(94,67).mirror(false).addBox(-1.50000f,5.00000f,0.00000f,3.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-8.00000f,0.00000f,1.00000f,-0.15708f,0.00000f,-0.17453f));
        var part36 = part34.addOrReplaceChild("mantle_panel_1", CubeListBuilder.create()
            .texOffs(149,67).mirror(false).addBox(-2.40000f,0.00000f,0.00000f,4.80000f,7.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(149,67).mirror(false).addBox(-1.50000f,6.00000f,0.00000f,3.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-4.00000f,0.00000f,1.00000f,-0.15708f,0.00000f,-0.08727f));
        var part37 = part34.addOrReplaceChild("mantle_panel_2", CubeListBuilder.create()
            .texOffs(204,67).mirror(false).addBox(-2.40000f,0.00000f,0.00000f,4.80000f,8.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(204,67).mirror(false).addBox(-1.50000f,7.00000f,0.00000f,3.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,1.00000f,-0.10472f,0.00000f,0.00000f));
        var part38 = part34.addOrReplaceChild("mantle_panel_3", CubeListBuilder.create()
            .texOffs(259,67).mirror(false).addBox(-2.40000f,0.00000f,0.00000f,4.80000f,7.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(259,67).mirror(false).addBox(-1.50000f,6.00000f,0.00000f,3.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(4.00000f,0.00000f,1.00000f,-0.15708f,0.00000f,0.08727f));
        var part39 = part34.addOrReplaceChild("mantle_panel_4", CubeListBuilder.create()
            .texOffs(314,67).mirror(false).addBox(-2.40000f,0.00000f,0.00000f,4.80000f,6.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(314,67).mirror(false).addBox(-1.50000f,5.00000f,0.00000f,3.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(8.00000f,0.00000f,1.00000f,-0.15708f,0.00000f,0.17453f));
        var part40 = part0.addOrReplaceChild("front_tasset_-1", CubeListBuilder.create()
            .texOffs(369,67).mirror(false).addBox(-2.50000f,0.00000f,-1.00000f,5.00000f,6.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(398,67).mirror(false).addBox(-2.50000f,5.20000f,-1.20000f,5.00000f,0.80000f,2.20000f, new CubeDeformation(0.00000f))
            .texOffs(427,67).mirror(false).addBox(-0.80000f,3.30000f,-1.45000f,1.60000f,0.70000f,0.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-4.00000f,-23.00000f,-3.00000f,0.00000f,0.00000f,-0.13963f));
        var part41 = part0.addOrReplaceChild("front_tasset_1", CubeListBuilder.create()
            .texOffs(481,67).mirror(false).addBox(-2.50000f,0.00000f,-1.00000f,5.00000f,6.00000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,94).mirror(false).addBox(-2.50000f,5.20000f,-1.20000f,5.00000f,0.80000f,2.20000f, new CubeDeformation(0.00000f))
            .texOffs(31,94).mirror(false).addBox(-0.80000f,3.30000f,-1.45000f,1.60000f,0.70000f,0.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(4.00000f,-23.00000f,-3.00000f,0.00000f,0.00000f,0.13963f));
        var part42 = part0.addOrReplaceChild("pendulum", CubeListBuilder.create()
            .texOffs(85,94).mirror(false).addBox(-0.40000f,0.00000f,-0.20000f,0.80000f,13.00000f,0.80000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-25.00000f,-6.00000f,0.00000f,0.00000f,0.00000f));
        var part43 = part42.addOrReplaceChild("pendulum_bob", CubeListBuilder.create()
            .texOffs(93,94).mirror(false).addBox(-2.00000f,-2.00000f,-0.70000f,4.00000f,4.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(109,94).mirror(false).addBox(-1.20000f,-1.20000f,-0.90000f,2.40000f,2.40000f,2.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,14.00000f,0.00000f,0.00000f,0.00000f,-0.78540f));
        var part44 = part0.addOrReplaceChild("dial", CubeListBuilder.create()
            .texOffs(169,121).mirror(false).addBox(-2.00000f,-2.00000f,-1.00000f,4.00000f,4.00000f,3.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-35.00000f,7.00000f,0.00000f,0.00000f,0.00000f));
        var part45 = part44.addOrReplaceChild("outer_ring_0", CubeListBuilder.create()
            .texOffs(122,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(140,94).mirror(false).addBox(-0.65000f,-19.30000f,-1.30000f,1.30000f,3.50000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part46 = part44.addOrReplaceChild("outer_ring_1", CubeListBuilder.create()
            .texOffs(150,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(168,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.26180f));
        var part47 = part44.addOrReplaceChild("outer_ring_2", CubeListBuilder.create()
            .texOffs(178,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(196,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.52360f));
        var part48 = part44.addOrReplaceChild("outer_ring_3", CubeListBuilder.create()
            .texOffs(206,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(224,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.78540f));
        var part49 = part44.addOrReplaceChild("outer_ring_4", CubeListBuilder.create()
            .texOffs(234,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(252,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.04720f));
        var part50 = part44.addOrReplaceChild("outer_ring_5", CubeListBuilder.create()
            .texOffs(262,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(280,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.30900f));
        var part51 = part44.addOrReplaceChild("outer_ring_6", CubeListBuilder.create()
            .texOffs(290,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(308,94).mirror(false).addBox(-0.65000f,-19.30000f,-1.30000f,1.30000f,3.50000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.57080f));
        var part52 = part44.addOrReplaceChild("outer_ring_7", CubeListBuilder.create()
            .texOffs(318,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(336,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.83260f));
        var part53 = part44.addOrReplaceChild("outer_ring_8", CubeListBuilder.create()
            .texOffs(346,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(364,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.09440f));
        var part54 = part44.addOrReplaceChild("outer_ring_9", CubeListBuilder.create()
            .texOffs(374,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(392,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.35619f));
        var part55 = part44.addOrReplaceChild("outer_ring_10", CubeListBuilder.create()
            .texOffs(402,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(420,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.61799f));
        var part56 = part44.addOrReplaceChild("outer_ring_11", CubeListBuilder.create()
            .texOffs(430,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(448,94).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.87979f));
        var part57 = part44.addOrReplaceChild("outer_ring_12", CubeListBuilder.create()
            .texOffs(458,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(476,94).mirror(false).addBox(-0.65000f,-19.30000f,-1.30000f,1.30000f,3.50000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.14159f));
        var part58 = part44.addOrReplaceChild("outer_ring_13", CubeListBuilder.create()
            .texOffs(486,94).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.40339f));
        var part59 = part44.addOrReplaceChild("outer_ring_14", CubeListBuilder.create()
            .texOffs(12,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(30,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.66519f));
        var part60 = part44.addOrReplaceChild("outer_ring_15", CubeListBuilder.create()
            .texOffs(40,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(58,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.92699f));
        var part61 = part44.addOrReplaceChild("outer_ring_16", CubeListBuilder.create()
            .texOffs(68,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(86,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.18879f));
        var part62 = part44.addOrReplaceChild("outer_ring_17", CubeListBuilder.create()
            .texOffs(96,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(114,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.45059f));
        var part63 = part44.addOrReplaceChild("outer_ring_18", CubeListBuilder.create()
            .texOffs(124,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(142,112).mirror(false).addBox(-0.65000f,-19.30000f,-1.30000f,1.30000f,3.50000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.71239f));
        var part64 = part44.addOrReplaceChild("outer_ring_19", CubeListBuilder.create()
            .texOffs(152,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(170,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.97419f));
        var part65 = part44.addOrReplaceChild("outer_ring_20", CubeListBuilder.create()
            .texOffs(180,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(198,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.23599f));
        var part66 = part44.addOrReplaceChild("outer_ring_21", CubeListBuilder.create()
            .texOffs(208,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(226,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.49779f));
        var part67 = part44.addOrReplaceChild("outer_ring_22", CubeListBuilder.create()
            .texOffs(236,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(254,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.75959f));
        var part68 = part44.addOrReplaceChild("outer_ring_23", CubeListBuilder.create()
            .texOffs(264,112).mirror(false).addBox(-2.50000f,-19.50000f,-1.00000f,5.00000f,1.50000f,2.00000f, new CubeDeformation(0.00000f))
            .texOffs(282,112).mirror(false).addBox(-0.65000f,-17.80000f,-1.30000f,1.30000f,2.00000f,1.40000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,6.02139f));
        var part69 = part44.addOrReplaceChild("halo_hand", CubeListBuilder.create()
            .texOffs(152,121).mirror(false).addBox(-0.35000f,-17.00000f,-0.70000f,0.70000f,17.00000f,1.00000f, new CubeDeformation(0.00000f))
            .texOffs(160,121).mirror(false).addBox(-0.60000f,0.00000f,-0.80000f,1.20000f,6.00000f,1.20000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part70 = part0.addOrReplaceChild("inner_halo", CubeListBuilder.create(), PartPose.offsetAndRotation(0.00000f,-35.00000f,8.00000f,0.00000f,0.31416f,0.00000f));
        var part71 = part70.addOrReplaceChild("inner_ring_0", CubeListBuilder.create()
            .texOffs(292,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(313,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.00000f));
        var part72 = part70.addOrReplaceChild("inner_ring_1", CubeListBuilder.create()
            .texOffs(322,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(343,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,0.52360f));
        var part73 = part70.addOrReplaceChild("inner_ring_2", CubeListBuilder.create()
            .texOffs(352,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(373,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.04720f));
        var part74 = part70.addOrReplaceChild("inner_ring_3", CubeListBuilder.create()
            .texOffs(382,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(403,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,1.57080f));
        var part75 = part70.addOrReplaceChild("inner_ring_4", CubeListBuilder.create()
            .texOffs(412,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(433,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.09440f));
        var part76 = part70.addOrReplaceChild("inner_ring_5", CubeListBuilder.create()
            .texOffs(442,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(463,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,2.61799f));
        var part77 = part70.addOrReplaceChild("inner_ring_6", CubeListBuilder.create()
            .texOffs(472,112).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(493,112).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.14159f));
        var part78 = part70.addOrReplaceChild("inner_ring_7", CubeListBuilder.create()
            .texOffs(2,121).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(23,121).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,3.66519f));
        var part79 = part70.addOrReplaceChild("inner_ring_8", CubeListBuilder.create()
            .texOffs(32,121).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(53,121).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.18879f));
        var part80 = part70.addOrReplaceChild("inner_ring_9", CubeListBuilder.create()
            .texOffs(62,121).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(83,121).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,4.71239f));
        var part81 = part70.addOrReplaceChild("inner_ring_10", CubeListBuilder.create()
            .texOffs(92,121).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(113,121).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.23599f));
        var part82 = part70.addOrReplaceChild("inner_ring_11", CubeListBuilder.create()
            .texOffs(122,121).mirror(false).addBox(-3.60000f,-14.00000f,-0.50000f,7.20000f,1.00000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(143,121).mirror(false).addBox(-0.55000f,-13.70000f,-1.00000f,1.10000f,2.00000f,1.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,0.00000f,0.00000f,0.00000f,0.00000f,5.75959f));
        var part83 = part0.addOrReplaceChild("orbit_page_0", CubeListBuilder.create()
            .texOffs(172,172).mirror(false).addBox(-2.00000f,-2.50000f,0.00000f,3.50000f,4.50000f,0.30000f, new CubeDeformation(0.00000f))
            .texOffs(184,172).mirror(false).addBox(-1.00000f,0.50000f,-0.20000f,1.50000f,0.50000f,0.30000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(12.00000f,-30.00000f,-2.00000f,0.00000f,0.34907f,0.26180f));
        var part84 = part0.addOrReplaceChild("orbit_page_1", CubeListBuilder.create()
            .texOffs(192,172).mirror(false).addBox(-2.00000f,-2.50000f,0.00000f,3.50000f,4.50000f,0.30000f, new CubeDeformation(0.00000f))
            .texOffs(204,172).mirror(false).addBox(-1.00000f,0.50000f,-0.20000f,1.50000f,0.50000f,0.30000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(15.00000f,-32.00000f,1.00000f,-0.17453f,0.34907f,-0.08727f));
        var part85 = part0.addOrReplaceChild("orbit_page_2", CubeListBuilder.create()
            .texOffs(212,172).mirror(false).addBox(-2.00000f,-2.50000f,0.00000f,3.50000f,4.50000f,0.30000f, new CubeDeformation(0.00000f))
            .texOffs(224,172).mirror(false).addBox(-1.00000f,0.50000f,-0.20000f,1.50000f,0.50000f,0.30000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(18.00000f,-34.00000f,4.00000f,-0.34907f,0.34907f,-0.43633f));
        var part86 = part0.addOrReplaceChild("ghost_tail", CubeListBuilder.create()
            .texOffs(2,2).mirror(false).addBox(-4.00000f,0.00000f,-3.00000f,8.00000f,6.00000f,6.00000f, new CubeDeformation(0.00000f))
            .texOffs(2,2).mirror(false).addBox(-3.20000f,5.00000f,-2.60000f,6.40000f,6.00000f,5.20000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,-23.00000f,1.00000f,0.00000f,0.00000f,0.00000f));
        var part87 = part86.addOrReplaceChild("ghost_mid", CubeListBuilder.create()
            .texOffs(2,2).mirror(false).addBox(-2.30000f,0.00000f,-1.80000f,4.60000f,7.00000f,4.00000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,9.00000f,0.00000f,0.00000f,0.00000f,-0.20944f));
        var part88 = part87.addOrReplaceChild("ghost_tip", CubeListBuilder.create()
            .texOffs(204,67).mirror(false).addBox(-0.80000f,-0.50000f,-1.00000f,2.80000f,5.50000f,2.80000f, new CubeDeformation(0.00000f))
            .texOffs(204,67).mirror(false).addBox(0.60000f,4.20000f,-0.30000f,1.60000f,2.40000f,1.80000f, new CubeDeformation(0.00000f))
            .texOffs(204,67).mirror(false).addBox(1.50000f,5.50000f,0.00000f,2.20000f,1.00000f,1.20000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,6.00000f,0.00000f,0.00000f,0.00000f,0.69813f));
        var part89 = part86.addOrReplaceChild("ghost_ribbon_0", CubeListBuilder.create()
            .texOffs(94,67).mirror(false).addBox(-1.50000f,0.00000f,0.00000f,3.00000f,8.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(94,67).mirror(false).addBox(-1.10000f,7.50000f,0.40000f,2.20000f,6.50000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(94,67).mirror(false).addBox(-0.45000f,13.50000f,0.70000f,0.90000f,3.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.25000f,7.00000f,-0.15000f,0.50000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-4.30000f,1.00000f,-4.00000f,0.08727f,0.00000f,-0.27925f));
        var part90 = part86.addOrReplaceChild("ghost_ribbon_1", CubeListBuilder.create()
            .texOffs(149,67).mirror(false).addBox(-1.50000f,0.00000f,0.00000f,3.00000f,7.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(149,67).mirror(false).addBox(-1.10000f,6.50000f,0.40000f,2.20000f,6.50000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(149,67).mirror(false).addBox(-0.45000f,12.50000f,0.70000f,0.90000f,3.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.25000f,6.00000f,-0.15000f,0.50000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-2.15000f,1.00000f,1.70000f,-0.13963f,0.00000f,-0.13963f));
        var part91 = part86.addOrReplaceChild("ghost_ribbon_2", CubeListBuilder.create()
            .texOffs(204,67).mirror(false).addBox(-1.50000f,0.00000f,0.00000f,3.00000f,8.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(204,67).mirror(false).addBox(-1.10000f,5.50000f,0.40000f,2.20000f,6.50000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(204,67).mirror(false).addBox(-0.45000f,11.50000f,0.70000f,0.90000f,3.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.25000f,5.00000f,-0.15000f,0.50000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,1.00000f,-4.00000f,0.08727f,0.00000f,0.00000f));
        var part92 = part86.addOrReplaceChild("ghost_ribbon_3", CubeListBuilder.create()
            .texOffs(259,67).mirror(false).addBox(-1.50000f,0.00000f,0.00000f,3.00000f,7.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(259,67).mirror(false).addBox(-1.10000f,7.50000f,0.40000f,2.20000f,6.50000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(259,67).mirror(false).addBox(-0.45000f,13.50000f,0.70000f,0.90000f,3.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.25000f,7.00000f,-0.15000f,0.50000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(2.15000f,1.00000f,1.70000f,-0.13963f,0.00000f,0.13963f));
        var part93 = part86.addOrReplaceChild("ghost_ribbon_4", CubeListBuilder.create()
            .texOffs(314,67).mirror(false).addBox(-1.50000f,0.00000f,0.00000f,3.00000f,8.00000f,1.60000f, new CubeDeformation(0.00000f))
            .texOffs(314,67).mirror(false).addBox(-1.10000f,6.50000f,0.40000f,2.20000f,6.50000f,1.30000f, new CubeDeformation(0.00000f))
            .texOffs(314,67).mirror(false).addBox(-0.45000f,12.50000f,0.70000f,0.90000f,3.50000f,0.70000f, new CubeDeformation(0.00000f))
            .texOffs(40,67).mirror(false).addBox(-0.25000f,6.00000f,-0.15000f,0.50000f,3.00000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(4.30000f,1.00000f,-4.00000f,0.08727f,0.00000f,0.27925f));
        var part94 = part86.addOrReplaceChild("ghost_ember_0", CubeListBuilder.create()
            .texOffs(40,67).mirror(false).addBox(-0.30000f,-0.20000f,0.00000f,0.60000f,1.20000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(-3.50000f,18.00000f,-2.00000f,0.00000f,0.00000f,-0.34907f));
        var part95 = part86.addOrReplaceChild("ghost_ember_1", CubeListBuilder.create()
            .texOffs(40,67).mirror(false).addBox(-0.30000f,-0.20000f,0.00000f,0.60000f,1.20000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(0.00000f,16.00000f,-2.00000f,0.00000f,0.00000f,-0.78540f));
        var part96 = part86.addOrReplaceChild("ghost_ember_2", CubeListBuilder.create()
            .texOffs(40,67).mirror(false).addBox(-0.30000f,-0.20000f,0.00000f,0.60000f,1.20000f,0.50000f, new CubeDeformation(0.00000f)), PartPose.offsetAndRotation(3.50000f,14.00000f,-2.00000f,0.00000f,0.00000f,-1.22173f));
        return LayerDefinition.create(mesh, 512, 512);
    }
    private KeeperGeometry() {}
}
