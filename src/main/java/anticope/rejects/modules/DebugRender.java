package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.HashMap;
import java.util.Map;

public class DebugRender extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Map<Setting<Boolean>, DebugRenderer> renderers = new HashMap<>();

    public DebugRender() {
        super(MeteorRejectsAddon.CATEGORY, "debug-renders", "Render useful debug information.");

        addRenderer("bee-brain-&-hive", createRenderer(mc.debugRenderer.beeDebugRenderer::render));
        addRenderer("breeze-brain", createRenderer(mc.debugRenderer.breezeDebugRenderer::render));
        addRenderer("block-outline", createRenderer(mc.debugRenderer.blockOutlineDebugRenderer::render));
        addRenderer("chunk-loading-progress", createRenderer(mc.debugRenderer.chunkLoadingDebugRenderer::render));
        addRenderer("collision", createRenderer(mc.debugRenderer.collisionDebugRenderer::render));
        addRenderer("game-event-1", createRenderer(mc.debugRenderer.gameEventDebugRenderer::render));
        addRenderer("game-event-2", createRenderer(mc.debugRenderer.gameEventDebugRenderer::render));
        addRenderer("mob-goals", createRenderer(mc.debugRenderer.goalSelectorDebugRenderer::render));
        addRenderer("heightmap", createRenderer(mc.debugRenderer.heightmapDebugRenderer::render));
        addRenderer("lighting-debug", createRenderer(mc.debugRenderer.lightDebugRenderer::render));
        addRenderer("neighbor-updates", createRenderer(mc.debugRenderer.neighborUpdateDebugRenderer::render));
        addRenderer("pathfinding-debug", createRenderer(mc.debugRenderer.pathfindingDebugRenderer::render));
        addRenderer("raid-center", createRenderer(mc.debugRenderer.raidCenterDebugRenderer::render));
        addRenderer("skylight", createRenderer(mc.debugRenderer.skyLightDebugRenderer::render));
        addRenderer("structure-outlines", createRenderer(mc.debugRenderer.structureDebugRenderer::render));
        addRenderer("villager-/-brain-/-POI", createRenderer(mc.debugRenderer.villageDebugRenderer::render));
        addRenderer("village-sections", createRenderer(mc.debugRenderer.villageSectionsDebugRenderer::render));
        addRenderer("water", createRenderer(mc.debugRenderer.waterDebugRenderer::render));
        addRenderer("worldgen-attempt", createRenderer(mc.debugRenderer.worldGenAttemptDebugRenderer::render));
        //gametest
    }

    private DebugRenderer createRenderer(DebugRenderer debugRenderer) {
        return debugRenderer;
    }

    //TODO: add descriptions
    private void addRenderer(String name, DebugRenderer renderer) {
        Setting<Boolean> setting = sgGeneral.add(new BoolSetting.Builder()
                .name(name)
                .defaultValue(false)
                .build()
        );
        renderers.put(setting, renderer);
    }

    public interface DebugRenderer {
        void render(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, double cameraX, double cameraY, double cameraZ);
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, double cameraX, double cameraY, double cameraZ) {
        for (Map.Entry<Setting<Boolean>, DebugRenderer> entry : renderers.entrySet()) {
            if (entry.getKey().get()) {
                entry.getValue().render(matrixStack, immediate, cameraX, cameraY, cameraZ);
            }
        }
    }
}