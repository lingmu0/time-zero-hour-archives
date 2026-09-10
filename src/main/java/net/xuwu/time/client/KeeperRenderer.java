package net.xuwu.time.client;

import net.minecraft.client.renderer.entity.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.ChronicleKeeperEntity;

public final class KeeperRenderer extends MobRenderer<ChronicleKeeperEntity, KeeperModel<ChronicleKeeperEntity>> {
    public static final ResourceLocation TEXTURE = TimeMod.id("textures/entity/chronicle_keeper.png");
    public static final ResourceLocation EMISSIVE = TimeMod.id("textures/entity/chronicle_keeper_emissive.png");
    public KeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new KeeperModel<>(context.bakeLayer(KeeperModel.LAYER)), .85f);
        addLayer(new KeeperGlowLayer<>(this));
        addLayer(new KeeperShieldLayer(this));
    }
    @Override public void render(ChronicleKeeperEntity boss, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        super.render(boss, yaw, partial, pose, buffers, light);
        KeeperTelegraph.render(boss, partial, pose, buffers);
    }
    @Override public boolean shouldRender(ChronicleKeeperEntity boss, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(boss, frustum, x, y, z) || KeeperTelegraph.visible(boss) && frustum.isVisible(boss.visualArena());
    }
    @Override public ResourceLocation getTextureLocation(ChronicleKeeperEntity entity) { return TEXTURE; }
}
