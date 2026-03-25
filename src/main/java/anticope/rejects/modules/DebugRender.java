package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.*;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.Map;

public class DebugRender extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Map<Setting<Boolean>, DebugRenderer.SimpleDebugRenderer> renderers = new HashMap<>();

    public DebugRender() {
        super(MeteorRejectsAddon.CATEGORY, "debug-renders", "Render useful debug information.");

        BrainDebugRenderer brainRenderer = new BrainDebugRenderer(mc);

        addRenderer("bee-brain-&-hive", new BeeDebugRenderer(mc));
        addRenderer("breeze-brain", new BreezeDebugRenderer(mc));
        addRenderer("chunk-culling", new ChunkCullingDebugRenderer(mc));
        addRenderer("chunk-debug", new ChunkDebugRenderer(mc));
        addRenderer("collision", new CollisionBoxRenderer(mc));
        addRenderer("entity-block-intersection", new EntityBlockIntersectionDebugRenderer());
        addRenderer("game-event", new GameEventListenerRenderer());
        addRenderer("mob-goals", new GoalSelectorDebugRenderer(mc));
        addRenderer("heightmap", new HeightMapRenderer(mc));
        addRenderer("block-light", new LightDebugRenderer(mc, true, false));
        addRenderer("sky-light", new LightDebugRenderer(mc, false, true));
        addRenderer("light-sections-sky", new LightSectionDebugRenderer(mc, LightLayer.SKY));
        addRenderer("light-sections-block", new LightSectionDebugRenderer(mc, LightLayer.BLOCK));
        addRenderer("neighbor-updates", new NeighborsUpdateRenderer());
        addRenderer("octree", new OctreeDebugRenderer(mc));
        addRenderer("pathfinding-debug", new PathfindingRenderer());
        addRenderer("raid-center", new RaidDebugRenderer(mc));
        addRenderer("redstone-wire-orientations", new RedstoneWireOrientationsRenderer());
        addRenderer("solid-faces", new SolidFaceRenderer(mc));
        addRenderer("structure-outlines", new StructureRenderer());
        addRenderer("support-blocks", new SupportBlockRenderer(mc));
        addRenderer("brain-debug", brainRenderer);
        addRenderer("poi-debug", new PoiDebugRenderer(brainRenderer));
        addRenderer("village-sections", new VillageSectionsDebugRenderer());
        addRenderer("water", new WaterDebugRenderer(mc));
    }

    // TODO add descriptions to each
    private void addRenderer(String name, DebugRenderer.SimpleDebugRenderer renderer) {
        Setting<Boolean> setting = sgGeneral.add(new BoolSetting.Builder()
                .name(name)
                .defaultValue(false)
                .build()
        );
        renderers.put(setting, renderer);
    }

    public void render(Frustum frustum, double x, double y, double z, float partialTick) {
        if (mc.getConnection() == null) return;
        DebugValueAccess debugValueAccess = mc.getConnection().createDebugValueAccess();
        for (Map.Entry<Setting<Boolean>, DebugRenderer.SimpleDebugRenderer> entry : renderers.entrySet()) {
            if (entry.getKey().get()) {
                entry.getValue().emitGizmos(x, y, z, debugValueAccess, frustum, partialTick);
            }
        }
    }
}
