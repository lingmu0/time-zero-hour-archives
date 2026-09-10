package net.xuwu.time.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.xuwu.time.TimeMod;
import net.xuwu.time.entity.*;
import java.util.HashMap;
import java.util.Map;

/** Independent clockwork sovereign rig. Cosmetic parts do not change the hitbox. */
public final class KeeperModel<T extends Mob> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TimeMod.id("chronicle_keeper_sovereign"), "main");
    private final ModelPart root, head, dial, innerHalo, hand, pendulum, mantle, staffArm, bookArm, staffForearm, bookForearm;
    private final ModelPart ghostTail, ghostMid, ghostTip;
    private final ModelPart staffHand, bookHand, hoveringBook;
    private final ModelPart[] ribbons = new ModelPart[5], embers = new ModelPart[3];
    private final ModelPart[] pages = new ModelPart[3];
    private final Map<String, ModelPart> castBones = new HashMap<>();

    public KeeperModel(ModelPart layer) {
        root = layer.getChild("root"); head = root.getChild("head"); dial = root.getChild("dial");
        innerHalo = root.getChild("inner_halo"); hand = dial.getChild("halo_hand");
        pendulum = root.getChild("pendulum"); mantle = root.getChild("mantle");
        staffArm = root.getChild("arms").getChild("staff_arm"); bookArm = root.getChild("arms").getChild("book_arm");
        staffForearm = staffArm.getChild("forearm_-1"); bookForearm = bookArm.getChild("forearm_1");
        staffHand = staffForearm.getChild("staff_hand"); bookHand = bookForearm.getChild("book_hand");
        hoveringBook = bookHand.getChild("held_book");
        // Fingers and prop share a wrist, so turning the wrist never breaks the grip.
        ghostTail = root.getChild("ghost_tail"); ghostMid = ghostTail.getChild("ghost_mid"); ghostTip = ghostMid.getChild("ghost_tip");
        for (int i = 0; i < ribbons.length; i++) ribbons[i] = ghostTail.getChild("ghost_ribbon_" + i);
        for (int i = 0; i < embers.length; i++) embers[i] = ghostTail.getChild("ghost_ember_" + i);
        for (int i = 0; i < pages.length; i++) pages[i] = root.getChild("orbit_page_" + i);
        castBones.put("root", root); castBones.put("head", head); castBones.put("dial", dial);
        castBones.put("inner_halo", innerHalo); castBones.put("mantle", mantle);
        castBones.put("staff_arm", staffArm); castBones.put("book_arm", bookArm);
        castBones.put("forearm_-1", staffForearm); castBones.put("forearm_1", bookForearm);
        castBones.put("staff_hand", staffHand); castBones.put("book_hand", bookHand);
        castBones.put("held_book", hoveringBook);
        for (int i = 0; i < pages.length; i++) castBones.put("orbit_page_" + i, pages[i]);
    }

    @Override public void setupAnim(T entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        // A renderer is shared across entities; reset every bone before applying this entity's pose.
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean falseBody = entity instanceof TemporalEchoEntity echo && echo.mode() == TemporalEchoEntity.PARADOX;
        int phase = entity instanceof ChronicleKeeperEntity boss ? boss.phase() : 4;
        boolean shielded = entity instanceof ChronicleKeeperEntity boss && boss.shielded();
        float direction = falseBody ? -1 : 1;
        float energy = shielded ? 1.7f : 1 + phase * .16f;
        root.y += Mth.sin(age * .045f) * .65f;
        head.yRot += Mth.clamp(headYaw, -35, 35) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(headPitch, -18, 18) * Mth.DEG_TO_RAD * .5f;
        dial.zRot += age * .008f * energy * direction;
        innerHalo.zRot -= age * .013f * energy * direction;
        innerHalo.yRot += Mth.sin(age * .02f) * .14f;
        hand.zRot -= age * .028f * energy * direction;
        pendulum.zRot += Mth.sin(age * .065f) * .16f;
        mantle.xRot += Mth.sin(age * .037f) * .045f;
        ghostTail.zRot += Mth.sin(age * .12f) * .055f;
        ghostMid.zRot += Mth.sin(age * .12f - .7f) * .10f;
        ghostTip.zRot += Mth.sin(age * .12f - 1.4f) * .20f;
        ghostMid.xRot += Mth.sin(age * .10f) * .06f;
        for (int i = 0; i < ribbons.length; i++) {
            ribbons[i].xRot += Mth.sin(age * .145f + i * .9f) * .08f;
            ribbons[i].zRot += Mth.sin(age * .125f + i * 1.1f) * .065f;
        }
        for (int i = 0; i < embers.length; i++) {
            embers[i].y += Mth.sin(age * .15f + i * 2) * .75f;
            embers[i].zRot += age * .015f * direction;
        }
        staffArm.xRot -= .045f + Mth.sin(age * .04f) * .035f;
        bookArm.xRot -= .10f + Mth.sin(age * .04f + 1) * .04f;
        for (int i = 0; i < pages.length; i++) {
            pages[i].y += Mth.sin(age * .048f + i * 1.7f) * 1.1f;
            pages[i].yRot += Mth.sin(age * .031f + i) * .14f;
        }
        if (entity instanceof ChronalCaster boss) {
            float t = boss.castAge(age - entity.tickCount);
            KeeperCastAnimations.apply(castBones, boss.castKind(), t);
        }
        // Counter-rotate the entire grip, not the prop alone. Keep the shaft upright
        // and the tome in front of the chest while elbows remain bent throughout.
        staffHand.xRot -= (staffArm.xRot - staffArm.getInitialPose().xRot)
            + (staffForearm.xRot - staffForearm.getInitialPose().xRot);
        bookHand.xRot -= (bookArm.xRot - bookArm.getInitialPose().xRot)
            + (bookForearm.xRot - bookForearm.getInitialPose().xRot);
        // Local -Z is above the upward-facing palm; the tome floats with the hand.
        hoveringBook.z -= Mth.sin(age * .09f) * .3f;
    }

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
            float red, float green, float blue, float alpha) {
        root.render(pose, buffer, light, overlay);
    }
}
