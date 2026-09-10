package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.*;

public final class ChronalRenderer<T extends net.minecraft.world.entity.Mob> extends MobRenderer<T, ChronalModel<T>> {
    public static final ResourceLocation TEXTURE = TimeMod.id("textures/entity/archive_scribe.png");
    private final float size;
    public ChronalRenderer(EntityRendererProvider.Context context, float size) {
        super(context, new ChronalModel<>(context.bakeLayer(ChronalModel.LAYER)), .7f * size); this.size = size;
    }
    @Override public ResourceLocation getTextureLocation(T entity) { return TEXTURE; }
    @Override protected void scale(T entity, PoseStack poses, float partialTick) {
        poses.scale(size, size, size);
    }
}
