package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import cloudburst.rejects.utils.Render3DUtils;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.Render3DEvent;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.Freecam;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.options.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

public class SkeletonESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Freecam freecam;

    private final Setting<SettingColor> skeletonColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("players-color")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    public SkeletonESP() {
        super(MeteorRejectsAddon.CATEGORY, "skeleton-esp", "Looks cool as fuck");
        freecam = Modules.get().get(Freecam.class);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        MatrixStack matrixStack = event.matrices;
        float g = event.tickDelta;
        Render3DUtils.setup3DRender(true);
        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity)) return;
            if (mc.options.getPerspective() == Perspective.FIRST_PERSON && !freecam.isActive() && (Entity)mc.player == entity) return;
            
            Color skeletonColor = PlayerUtils.getPlayerColor((PlayerEntity)entity, skeletonColorSetting.get());
            PlayerEntity playerEntity = (PlayerEntity) entity;

            Vec3d footPos = Render3DUtils.getEntityRenderPosition(playerEntity, g);
            PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer)(LivingEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(playerEntity);
            PlayerEntityModel playerEntityModel = (PlayerEntityModel)livingEntityRenderer.getModel();

            float h = MathHelper.lerpAngleDegrees(g, playerEntity.prevBodyYaw, playerEntity.bodyYaw);
            float j = MathHelper.lerpAngleDegrees(g, playerEntity.prevHeadYaw, playerEntity.headYaw);

            float q = playerEntity.limbAngle - playerEntity.limbDistance * (1.0F - g);
            float p = MathHelper.lerp(g, playerEntity.lastLimbDistance, playerEntity.limbDistance);
            float o = (float)playerEntity.age + g;
            float k = j - h;
            float m = MathHelper.lerp(g, playerEntity.prevPitch, playerEntity.pitch);

            playerEntityModel.setAngles(playerEntity, q, p, o, k, m);
            boolean sneaking = playerEntity.isSneaking();

            ModelPart head = playerEntityModel.head;
            ModelPart leftArm = playerEntityModel.leftArm;
            ModelPart rightArm = playerEntityModel.rightArm;
            ModelPart leftLeg = playerEntityModel.leftLeg;
            ModelPart rightLeg = playerEntityModel.rightLeg;

            matrixStack.translate(footPos.x, footPos.y, footPos.z);
            matrixStack.multiply(new Quaternion(new Vector3f(0, -1, 0), playerEntity.bodyYaw + 180, true));
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);

            Matrix4f matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();//spine

            bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();//shoulders
            bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();

            bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();//pelvis
            bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();

            matrixStack.push();//head
            matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
            rotate(matrixStack, head);
            matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            matrixStack.pop();

            matrixStack.push();//right leg
            matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, rightLeg);
            matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            matrixStack.pop();

            matrixStack.push();//left leg
            matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, leftLeg);
            matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            matrixStack.pop();

            matrixStack.push();//right arm
            matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, rightArm);
            matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            matrixStack.pop();

            matrixStack.push();//left arm
            matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, leftArm);
            matrix4f = matrixStack.peek().getModel();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a).next();
            matrixStack.pop();

            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            matrixStack.multiply(new Quaternion(new Vector3f(0, 1, 0), playerEntity.bodyYaw + 180, true));
            matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
        });
        Render3DUtils.end3DRender();
    }

    private void rotate(MatrixStack matrix, ModelPart modelPart) {
        if (modelPart.roll != 0.0F) {
            matrix.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(modelPart.roll));
        }

        if (modelPart.yaw != 0.0F) {
            matrix.multiply(Vector3f.NEGATIVE_Y.getRadialQuaternion(modelPart.yaw));
        }

        if (modelPart.pitch != 0.0F) {
            matrix.multiply(Vector3f.NEGATIVE_X.getRadialQuaternion(modelPart.pitch));
        }
    }
}
