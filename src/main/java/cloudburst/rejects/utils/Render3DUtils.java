package cloudburst.rejects.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.opengl.GL11;


public enum Render3DUtils {
    INSTANCE;

    public Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(double x, double y, double z) {
        double minX = x - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(Vec3d vec3d) {
        double minX = vec3d.getX() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public void fixCameraRots() {
        Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
    }

    public void applyCameraRots() {
        Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
    }

    public void setup3DRender(boolean disableDepth) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public void drawSphere(MatrixStack matrixStack, float radius, int gradation, Color color, boolean testDepth, Vec3d pos) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        setup3DRender(!testDepth);
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha)));
                Vec3d renderPos = Render3DUtils.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color.r, color.g, color.b, color.a).next();
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha + PI / gradation)));
                renderPos = Render3DUtils.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color.r, color.g, color.b, color.a).next();
            }
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
        }
        end3DRender();
    }

    public void drawBox(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public void drawBoxOutline(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public void drawBoxInside(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color);
        end3DRender();
    }

    public void drawEntityBox(MatrixStack matrixStack, Entity entity, float partialTicks, Color color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixStack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(MatrixStack matrixStack, Entity entity, double x, double y, double z, Color color) {
        float yaw = MathHelper.lerpAngleDegrees(MinecraftClient.getInstance().getTickDelta(), entity.prevYaw, entity.yaw);
        setup3DRender(true);
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vector3f(0, -1, 0), yaw, true));
        matrixStack.translate(-x, -y, -z);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);

        drawFilledBox(matrixStack, bb, color);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);

        end3DRender();
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vector3f(0, 1, 0), yaw, true));
        matrixStack.translate(-x, -y, -z);
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawFilledBox(MatrixStack matrixStack, Box bb, Color color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7/*QUADS*/, VertexFormats.POSITION_COLOR);
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color.r, color.g, color.b, color.a).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public void drawOutlineBox(MatrixStack matrixStack, Box bb, Color color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(1/*LINES*/, VertexFormats.POSITION_COLOR);

        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color.r, color.g, color.b, color.a).next();
        });

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
