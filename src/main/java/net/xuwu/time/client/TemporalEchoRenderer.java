package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.xuwu.time.entity.TemporalEchoEntity;

/** Memory fragments retain the scribe rig; the false body must impersonate the new boss. */
public final class TemporalEchoRenderer extends MobRenderer<TemporalEchoEntity, EntityModel<TemporalEchoEntity>> {
    private final ChronalModel<TemporalEchoEntity> fragment;
    private final KeeperModel<TemporalEchoEntity> falseBody;
    public TemporalEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new ChronalModel<>(context.bakeLayer(ChronalModel.LAYER)), .42f);
        fragment = (ChronalModel<TemporalEchoEntity>)model;
        falseBody = new KeeperModel<>(context.bakeLayer(KeeperModel.LAYER));
        addLayer(new KeeperGlowLayer<>(this));
    }
    @Override public void render(TemporalEchoEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        model = entity.mode() == TemporalEchoEntity.PARADOX ? falseBody : fragment;
        super.render(entity, yaw, partial, pose, buffers, light);
        if (entity.mode()==TemporalEchoEntity.PARADOX) KeeperTelegraph.render(entity,partial,pose,buffers);
    }
    @Override public boolean shouldRender(TemporalEchoEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity,frustum,x,y,z) || KeeperTelegraph.visible(entity) && frustum.isVisible(entity.visualArena());
    }
    @Override public ResourceLocation getTextureLocation(TemporalEchoEntity entity) {
        return entity.mode() == TemporalEchoEntity.PARADOX ? KeeperRenderer.TEXTURE : ChronalRenderer.TEXTURE;
    }
    @Override protected void scale(TemporalEchoEntity entity, PoseStack pose, float partial) {
        float scale = entity.mode() == TemporalEchoEntity.PARADOX ? 1 : .60f;
        pose.scale(scale, scale, scale);
    }
    @Override protected float getShadowRadius(TemporalEchoEntity entity) {
        return (entity.mode() == TemporalEchoEntity.PARADOX ? .85f : .42f) * entity.getAgeScale();
    }
}
