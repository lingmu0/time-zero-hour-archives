package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xuwu.time.entity.ChronicleKeeperEntity;

/** The same scrolling additive energy material used by a charged creeper. */
public final class KeeperShieldLayer extends RenderLayer<ChronicleKeeperEntity, KeeperModel<ChronicleKeeperEntity>> {
    private static final ResourceLocation ENERGY = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    public KeeperShieldLayer(RenderLayerParent<ChronicleKeeperEntity, KeeperModel<ChronicleKeeperEntity>> parent) { super(parent); }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, ChronicleKeeperEntity boss,
            float swing, float amount, float partial, float age, float yaw, float pitch) {
        if (!boss.isAlive() || !boss.shielded() || boss.isInvisible()) return;
        pose.pushPose();
        pose.scale(1.055f, 1.035f, 1.055f);
        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.energySwirl(ENERGY, age * .012f % 1, age * .009f % 1)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFF80BFFF);
        pose.popPose();
    }
}
