package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.ChronalBoltEntity;

/** Solid clockwork geometry, oriented along velocity; never a camera-facing item quad. */
public final class ChronalBoltRenderer extends EntityRenderer<ChronalBoltEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(TimeMod.id("chronal_bolt"),"main");
    private static final ResourceLocation TEXTURE=TimeMod.id("textures/entity/chronal_bolt.png");
    private final ModelPart root,dial,core;
    public ChronalBoltRenderer(EntityRendererProvider.Context context){
        super(context);root=context.bakeLayer(LAYER).getChild("root");dial=root.getChild("dial");core=root.getChild("core");
    }
    @Override public void render(ChronalBoltEntity bolt,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        root.getAllParts().forEach(ModelPart::resetPose);root.y=0;
        float age=bolt.tickCount+partial;dial.zRot=age*.22f;
        float pulse=1+(float)Math.sin(age*.6f)*.07f;core.xScale=pulse;core.yScale=pulse;
        pose.pushPose();
        var v=bolt.getDeltaMovement();
        if(v.lengthSqr()>1e-8)pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,0,1),new Vector3f((float)v.x,(float)v.y,(float)v.z).normalize()));
        pose.scale(.8f,.8f,.8f);
        root.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
        pose.popPose();super.render(bolt,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(ChronalBoltEntity bolt){return TEXTURE;}
}
