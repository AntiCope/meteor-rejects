package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SkeletonESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> skeletonColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("players-color")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    public final Setting<Boolean> distance = sgGeneral.add(new BoolSetting.Builder()
            .name("distance-colors")
            .description("Changes the color of skeletons depending on distance.")
            .defaultValue(false)
            .build()
    );

    private final Freecam freecam;

    public SkeletonESP() {
        super(MeteorRejectsAddon.CATEGORY, "skeleton-esp", "Looks cool as fuck");
        freecam = Modules.get().get(Freecam.class);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        // NOTE: Rendering in 1.21.10 has been completely reworked to use OrderedRenderCommandQueue
        // This module requires a full rewrite to use the new rendering pipeline
        // The old BufferBuilder/RenderSystem API used here is no longer available
        // See: https://fabricmc.net/2025/09/23/1219.html for migration guide
        return;

        /* Old rendering code - kept for reference during future rewrite
        MatrixStack matrixStack = event.matrices;
        float g = event.tickDelta;

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.getInstance().options.getGraphicsMode().getValue().getId() >= 1);
        RenderSystem.enableCull();

        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity)) return;
            if (mc.options.getPerspective() == Perspective.FIRST_PERSON && !freecam.isActive() && mc.player == entity)
                return;
            int rotationHoldTicks = Config.get().rotationHoldTicks.get();

            Color skeletonColor = PlayerUtils.getPlayerColor((PlayerEntity) entity, skeletonColorSetting.get());
            if (distance.get()) skeletonColor = getColorFromDistance(entity);
            PlayerEntity playerEntity = (PlayerEntity) entity;

            Vec3d footPos = getEntityRenderPosition(playerEntity, g);
            PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer) (LivingEntityRenderer<?, ?>) mc.getEntityRenderDispatcher().getRenderer(playerEntity);
            PlayerEntityModel<?> playerEntityModel = livingEntityRenderer.getModel();

            float h = MathHelper.lerpAngleDegrees(g, playerEntity.lastBodyYaw, playerEntity.bodyYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) h = Rotations.serverYaw;
            float j = MathHelper.lerpAngleDegrees(g, playerEntity.lastHeadYaw, playerEntity.headYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) j = Rotations.serverYaw;

            float q = playerEntity.limbAnimator.getAnimationProgress() - playerEntity.limbAnimator.getSpeed() * (1.0F - g);
            float p = playerEntity.limbAnimator.getAmplitude(g);
            float o = (float) playerEntity.age + g;
            float k = j - h;
            float m = playerEntity.getPitch(g);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) m = Rotations.serverPitch;

            PlayerEntityRenderState renderState = new PlayerEntityRenderState();
            renderState.limbSwingAnimationProgress = q;
            renderState.limbSwingAmplitude = p;
            renderState.age = o;
            renderState.relativeHeadYaw = k;
            renderState.pitch = m;
            playerEntityModel.setAngles(renderState);

            boolean swimming = playerEntity.isInSwimmingPose();
            boolean sneaking = playerEntity.isSneaking();
            boolean flying = playerEntity.isGliding();

            ModelPart head = playerEntityModel.head;
            ModelPart leftArm = playerEntityModel.leftArm;
            ModelPart rightArm = playerEntityModel.rightArm;
            ModelPart leftLeg = playerEntityModel.leftLeg;
            ModelPart rightLeg = playerEntityModel.rightLeg;

            matrixStack.translate(footPos.x, footPos.y, footPos.z);
            if (swimming) matrixStack.translate(0, 0.35f, 0);

            matrixStack.multiply(new Quaternionf().setAngleAxis((h + 180) * Math.PI / 180F, 0, -1, 0));
            if (swimming || flying)
                matrixStack.multiply(new Quaternionf().setAngleAxis((90 + m) * Math.PI / 180F, -1, 0, 0));
            if (swimming) matrixStack.translate(0, -0.95f, 0);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);//spine

            bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);//shoulders
            bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);

            bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);//pelvis
            bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);

            // Head
            matrixStack.push();
            matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
            rotate(matrixStack, head);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            matrixStack.pop();

            // Right Leg
            matrixStack.push();
            matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, rightLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            matrixStack.pop();

            // Left Leg
            matrixStack.push();
            matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, leftLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            matrixStack.pop();

            // Right Arm
            matrixStack.push();
            matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, rightArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            matrixStack.pop();

            // Left Arm
            matrixStack.push();
            matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, leftArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(skeletonColor.r, skeletonColor.g, skeletonColor.b, skeletonColor.a);
            matrixStack.pop();

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            if (swimming) matrixStack.translate(0, 0.95f, 0);
            if (swimming || flying)
                matrixStack.multiply(new Quaternionf().setAngleAxis((90 + m) * Math.PI / 180F, 1, 0, 0));
            if (swimming) matrixStack.translate(0, -0.35f, 0);

            matrixStack.multiply(new Quaternionf().setAngleAxis((h + 180) * Math.PI / 180F, 0, 1, 0));
            matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
        });

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        */
    }

    private void rotate(PoseStack matrix, ModelPart modelPart) {
        if (modelPart.zRot != 0.0F) {
            matrix.mulPose(Axis.ZP.rotation(modelPart.zRot));
        }

        if (modelPart.yRot != 0.0F) {
            matrix.mulPose(Axis.YN.rotation(modelPart.yRot));
        }

        if (modelPart.xRot != 0.0F) {
            matrix.mulPose(Axis.XN.rotation(modelPart.xRot));
        }
    }

    private Vec3 getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.xo + ((entity.getX() - entity.xo) * partial) - mc.getEntityRenderDispatcher().camera.position().x;
        double y = entity.yo + ((entity.getY() - entity.yo) * partial) - mc.getEntityRenderDispatcher().camera.position().y;
        double z = entity.zo + ((entity.getZ() - entity.zo) * partial) - mc.getEntityRenderDispatcher().camera.position().z;
        return new Vec3(x, y, z);
    }

    private Color getColorFromDistance(Entity entity) {
        Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        double distance = mc.gameRenderer.getMainCamera().position().distanceTo(entityPos);
        double percent = distance / 60;

        if (percent < 0 || percent > 1) {
            color.set(0, 255, 0, 255);
            return color;
        }

        int r, g;

        if (percent < 0.5) {
            r = 255;
            g = (int) (255 * percent / 0.5);
        } else {
            g = 255;
            r = 255 - (int) (255 * (percent - 0.5) / 0.5);
        }

        color.set(r, g, 0, 255);
        return color;
    }
}