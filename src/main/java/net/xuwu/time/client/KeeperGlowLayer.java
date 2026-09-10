package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Mob;
import net.xuwu.time.entity.TemporalEchoEntity;

public final class KeeperGlowLayer<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final RenderType GLOW = RenderType.eyes(KeeperRenderer.EMISSIVE);
    public KeeperGlowLayer(RenderLayerParent<T, M> parent) { super(parent); }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
            float limbSwing, float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (entity.isInvisible() || entity instanceof TemporalEchoEntity echo && echo.mode() != TemporalEchoEntity.PARADOX) return;
        getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
