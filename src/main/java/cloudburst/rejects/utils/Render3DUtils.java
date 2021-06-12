package cloudburst.rejects.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.opengl.GL11;

public class Render3DUtils {

    private final static MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - mc.getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - mc.getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public static Vec3d getRenderPosition(double x, double y, double z) {
        double minX = x - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public static Vec3d getRenderPosition(Vec3d vec3d) {
        double minX = vec3d.getX() - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public static Vec3d getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public static void fixCameraRots() {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
    }

    public static void applyCameraRots() {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
    }

    public static void setup3DRender(boolean disableDepth) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public static void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void drawSphere(MatrixStack matrixStack, float radius, int gradation, Color color, boolean testDepth, Vec3d pos) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        setup3DRender(!testDepth);
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha)));
                Vec3d renderPos = Render3DUtils.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color.r, color.g, color.b, color.a).next();
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha + PI / gradation)));
                renderPos = Render3DUtils.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color.r, color.g, color.b, color.a).next();
            }
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
        }
        end3DRender();
    }

    public static void drawBox(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public static void drawBoxOutline(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public static void drawBoxInside(MatrixStack matrixStack, Box bb, Color color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color);
        end3DRender();
    }

    public static void drawEntityBox(MatrixStack matrixStack, Entity entity, float partialTicks, Color color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixStack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public static void drawEntityBox(MatrixStack matrixStack, Entity entity, double x, double y, double z, Color color) {
        float yaw = MathHelper.lerpAngleDegrees(mc.getTickDelta(), entity.prevYaw, entity.getYaw());
        setup3DRender(true);
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), yaw, true));
        matrixStack.translate(-x, -y, -z);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);

        drawFilledBox(matrixStack, bb, color);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);

        end3DRender();
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
        matrixStack.translate(-x, -y, -z);
    }

    public static double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public static void drawFilledBox(MatrixStack matrixStack, Box bb, Color color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
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

    public static void drawOutlineBox(MatrixStack matrixStack, Box bb, Color color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color.r, color.g, color.b, color.a).next();
        });

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
