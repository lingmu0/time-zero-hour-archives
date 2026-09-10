package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.*;

/** Code-native cuboid model; editable Blockbench sources are shipped in model_source/. */
public final class ChronalModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TimeMod.id("archive_scribe"), "main");
    private final ModelPart root, dial, pendulum, arms;
    public ChronalModel(ModelPart root) {
        this.root = root.getChild("root"); dial = this.root.getChild("dial"); pendulum = this.root.getChild("pendulum"); arms = this.root.getChild("arms");
    }
    public static LayerDefinition createLayer() {
        return ChronalGeometry.createLayer();
    }
    @Override public void setupAnim(T entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        root.y = 24 + (float)Math.sin(age * .055f) * 1.2f;
        dial.zRot = age * .018f;
        pendulum.zRot = (float)Math.sin(age * .08f) * .3f;
        arms.xRot = -.08f + (float)Math.sin(age * .05f) * .08f;
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
            float red, float green, float blue, float alpha) {
        root.render(pose, buffer, light, overlay);
    }
}
