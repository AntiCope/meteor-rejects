package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldGenUtils;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mcutils.version.MCVersion;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;

public class StructureRegions extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private int spacing = 20;
    private int separation = 11;
    private int size = 9;

    private final Setting<WorldGenUtils.Feature> regionEnum = sgGeneral.add(new EnumSetting.Builder<WorldGenUtils.Feature>()
            .name("Structure")
            .description("Select the structure region")
            .defaultValue(WorldGenUtils.Feature.end_city)
            .onChanged(this::onChanged)
            .build()
    );

    public StructureRegions() {
        super(MeteorRejectsAddon.CATEGORY, "Structure regions", "show spawnable regions for specific structures");
    }

    private void onChanged(WorldGenUtils.Feature struc) {
        Structure<?, ?> structure = WorldGenUtils.getStructure(struc, MCVersion.latest());
        if (structure instanceof RegionStructure<?, ?> regionStructure) {
            spacing = regionStructure.getSpacing();
            separation = regionStructure.getSeparation();
            size = (regionStructure.getSpacing() - regionStructure.getSeparation()) * 16;
        }
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        int viewDistance = mc.options.viewDistance + 1;
        viewDistance *= 16;

        int negX = getRegion(event.offsetX - viewDistance);
        int negZ = getRegion(event.offsetZ - viewDistance);

        int posX = getRegion(event.offsetX + viewDistance);
        int posZ = getRegion(event.offsetZ + viewDistance);

        for (int x = negX; x <= posX; x++) {
            for (int z = negZ; z <= posZ; z++) {

                int blockX = x * spacing * 16;
                int blockZ = z * spacing * 16;

                event.renderer.box(blockX, 64, blockZ, blockX + size, 64, blockZ + size, new Color(0, 0, 0, 0), new Color(255, 0, 0), ShapeMode.Lines, 0);
            }
        }
    }

    private int getRegion(double coord) {
        int region = ((int) coord / 16 + separation / 2) / spacing;
        if (coord < 0) {
            region--;
        }
        return region;
    }
}
