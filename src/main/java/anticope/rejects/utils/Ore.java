package anticope.rejects.utils;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import com.seedfinding.mccore.version.MCVersion;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;
import java.util.*;

public class Ore {

    private static final Setting<Boolean> coal        = new BoolSetting.Builder().name("Coal").build();
    private static final Setting<Boolean> iron        = new BoolSetting.Builder().name("Iron").build();
    private static final Setting<Boolean> gold        = new BoolSetting.Builder().name("Gold").build();
    private static final Setting<Boolean> redstone    = new BoolSetting.Builder().name("Redstone").build();
    private static final Setting<Boolean> diamond     = new BoolSetting.Builder().name("Diamond").build();
    private static final Setting<Boolean> lapis       = new BoolSetting.Builder().name("Lapis").build();
    private static final Setting<Boolean> copper      = new BoolSetting.Builder().name("Kappa").build();
    private static final Setting<Boolean> emerald     = new BoolSetting.Builder().name("Emerald").build();
    private static final Setting<Boolean> quartz      = new BoolSetting.Builder().name("Quartz").build();
    private static final Setting<Boolean> debris      = new BoolSetting.Builder().name("Ancient Debris").build();
    public static final  List<Setting<Boolean>>   oreSettings = new ArrayList<>(Arrays.asList(coal, iron, gold, redstone, diamond, lapis, copper, emerald, quartz, debris));
    public final         Type                 type;
    public final         Identifier           dimension;
    public final         Map<String, Integer> index;
    public final         boolean              depthAverage;
    public final         Generator            generator;
    public final         int                  size;
    public final         Setting<Boolean>     enabled;
    public final         Color                color;
    public               int                  step;
    public               IntProvider          count;
    public               int                  minY;
    public               int                  maxY;
    public               float                discardOnAir;
    public               float                chance;

    Ore(Type type, Identifier dimension, Map<String, Integer> index, int step, IntProvider count, float chance, boolean depthAverage, int minY, int maxY, Generator generator, int size, float discardOnAir, Setting<Boolean> enabled, Color color) {
        this.type = type;
        this.dimension = dimension;
        this.index = index;
        this.step = step;
        this.count = count;
        this.depthAverage = depthAverage;
        this.minY = minY;
        this.maxY = maxY;
        this.generator = generator;
        this.size = size;
        this.enabled = enabled;
        this.color = color;
        this.discardOnAir = discardOnAir;
        this.chance = chance;
    }

    Ore(Type type, Identifier dimension, int index, int step, IntProvider count, float chance, boolean depthAverage, int minY, int maxY, Generator generator, int size, float discardOnAir, Setting<Boolean> enabled, Color color) {
        this(type, dimension, indexToMap(index), step, count, chance, depthAverage, minY, maxY, generator, size, discardOnAir, enabled, color);
    }

    Ore(Type type, Identifier dimension, int index, int step, IntProvider count, boolean depthAverage, int minY, int maxY,
        @SuppressWarnings("SameParameterValue") Generator generator, int size, Setting<Boolean> enabled, Color color) {
        this(type, dimension, indexToMap(index), step, count, 1F, depthAverage, minY, maxY, generator, size, 0F, enabled, color);
    }

    Ore(Type type, Identifier dimension, Map<String, Integer> index, int step, IntProvider count, boolean depthAverage, int minY, int maxY,
        @SuppressWarnings("SameParameterValue") Generator generator, int size, Setting<Boolean> enabled, Color color) {
        this(type, dimension, index, step, count, 1F, depthAverage, minY, maxY, generator, size, 0F, enabled, color);
    }

    private static HashMap<String, Integer> indexToMap(int index) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("default", index);
        return map;
    }

    private static List<Ore> V1_18() {
        List<Ore> ores = new ArrayList<>();

        HashMap<String, Integer> extraGoldIndex = new HashMap<>();
        extraGoldIndex.put("default", -1);
        String[] extraGoldBiomes = new String[]{"badlands", "eroded_badlands", "wooded_badlands"};
        for (String extraGoldBiome : extraGoldBiomes) {
            extraGoldIndex.put(extraGoldBiome, 27);
        }

        HashMap<String, Integer> emeraldIndex = new HashMap<>();
        emeraldIndex.put("default", -1);
        String[] emeraldBiomes = new String[]{"windswept_hills", "meadow", "grove", "jagged_peaks", "snowy_slopes", "frozen_peaks", "stony_peaks"};
        for (String emeraldBiome : emeraldBiomes) {
            emeraldIndex.put(emeraldBiome, 27);
        }

        ores.add(new Ore(Type.COAL, DimensionType.OVERWORLD_ID, 9, 6, ConstantIntProvider.create(30), false, 136, 320, Generator.DEFAULT, 17, coal, new Color(47, 44, 54)));
        ores.add(new Ore(Type.COAL, DimensionType.OVERWORLD_ID, 10, 6, ConstantIntProvider.create(20), 1F, true, 97, 97, Generator.DEFAULT, 17, 0.5F, coal, new Color(47, 44, 54)));
        ores.add(new Ore(Type.IRON, DimensionType.OVERWORLD_ID, 11, 6, ConstantIntProvider.create(90), true, 233, 153, Generator.DEFAULT, 9, iron, new Color(236, 173, 119)));
        ores.add(new Ore(Type.IRON, DimensionType.OVERWORLD_ID, 12, 6, ConstantIntProvider.create(10), true, 17, 41, Generator.DEFAULT, 9, iron, new Color(236, 173, 119)));
        ores.add(new Ore(Type.IRON, DimensionType.OVERWORLD_ID, 13, 6, ConstantIntProvider.create(10), false, -64, 73, Generator.DEFAULT, 4, iron, new Color(236, 173, 119)));
        ores.add(new Ore(Type.GOLD_EXTRA, DimensionType.OVERWORLD_ID, extraGoldIndex, 6, ConstantIntProvider.create(50), false, 32, 257, Generator.DEFAULT, 9, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.GOLD, DimensionType.OVERWORLD_ID, 14, 6, ConstantIntProvider.create(4), 1F, true, -15, 49, Generator.DEFAULT, 9, 0.5F, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.GOLD, DimensionType.OVERWORLD_ID, 15, 6, UniformIntProvider.create(0, 1), 1F, false, -64, -47, Generator.DEFAULT, 9, 0.5F, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.REDSTONE, DimensionType.OVERWORLD_ID, 16, 6, ConstantIntProvider.create(4), false, -64, 16, Generator.DEFAULT, 8, redstone, new Color(245, 7, 23)));
        ores.add(new Ore(Type.REDSTONE, DimensionType.OVERWORLD_ID, 17, 6, ConstantIntProvider.create(8), true, -63, 33, Generator.DEFAULT, 8, redstone, new Color(245, 7, 23)));
        ores.add(new Ore(Type.DIAMOND, DimensionType.OVERWORLD_ID, 18, 6, ConstantIntProvider.create(7), 1F, true, -63, 81, Generator.DEFAULT, 4, 0.5F, diamond, new Color(33, 244, 255)));
        ores.add(new Ore(Type.DIAMOND, DimensionType.OVERWORLD_ID, 19, 6, ConstantIntProvider.create(1), (1F / 9F), true, -63, 81, Generator.DEFAULT, 12, 0.7F, diamond, new Color(33, 244, 255)));
        ores.add(new Ore(Type.DIAMOND, DimensionType.OVERWORLD_ID, 20, 6, ConstantIntProvider.create(4), 1F, true, -63, 81, Generator.DEFAULT, 8, 1F, diamond, new Color(33, 244, 255)));
        ores.add(new Ore(Type.LAPIS, DimensionType.OVERWORLD_ID, 21, 6, ConstantIntProvider.create(2), true, 1, 33, Generator.DEFAULT, 7, lapis, new Color(8, 26, 189)));
        ores.add(new Ore(Type.LAPIS, DimensionType.OVERWORLD_ID, 22, 6, ConstantIntProvider.create(4), 1F, false, -64, 65, Generator.DEFAULT, 7, 1F, lapis, new Color(8, 26, 189)));
        ores.add(new Ore(Type.EMERALD, DimensionType.OVERWORLD_ID, emeraldIndex, 6, ConstantIntProvider.create(100), true, 233, 249, Generator.DEFAULT, 3, emerald, new Color(27, 209, 45)));
        //This only generates near dripstone caves. I'll need propper biome detection to get this right
        //ores.add(new Ore(Type.COPPER, "overworld", 23, 6, ConstantIntProvider.create(16), true, 49, 65, Generator.DEFAULT, 20, copper, new Color(239, 151, 0)));
        ores.add(new Ore(Type.COPPER, DimensionType.OVERWORLD_ID, 24, 6, ConstantIntProvider.create(16), true, 49, 65, Generator.DEFAULT, 10, copper, new Color(239, 151, 0)));
        ores.add(new Ore(Type.GOLD_NETHER, DimensionType.THE_NETHER_ID, Map.of("default", 19, "basalt_deltas", -1), 7, ConstantIntProvider.create(10), false, 10, 118, Generator.DEFAULT, 10, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, DimensionType.THE_NETHER_ID, Map.of("default", 20, "basalt_deltas", -1), 7, ConstantIntProvider.create(16), false, 10, 118, Generator.DEFAULT, 14, quartz, new Color(205, 205, 205)));
        ores.add(new Ore(Type.GOLD_NETHER, DimensionType.THE_NETHER_ID, Map.of("default", -1, "basalt_deltas", 13), 7, ConstantIntProvider.create(20), false, 10, 118, Generator.DEFAULT, 10, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, DimensionType.THE_NETHER_ID, Map.of("default", -1, "basalt_deltas", 14), 7, ConstantIntProvider.create(32), false, 10, 118, Generator.DEFAULT, 14, quartz, new Color(205, 205, 205)));
        ores.add(new Ore(Type.LDEBRIS, DimensionType.THE_NETHER_ID, 21, 7, ConstantIntProvider.create(1), true, 17, 9, Generator.NO_SURFACE, 3, debris, new Color(209, 27, 245)));
        ores.add(new Ore(Type.SDEBRIS, DimensionType.THE_NETHER_ID, 22, 7, ConstantIntProvider.create(1), false, 8, 120, Generator.NO_SURFACE, 2, debris, new Color(209, 27, 245)));
        return ores;
    }

    private static List<Ore> baseConfig() {
        List<Ore> ores = new ArrayList<>();
        HashMap<String, Integer> emeraldIndexes = new HashMap<>();
        emeraldIndexes.put("default", -1);
        String[] emeraldBiomes = new String[]{"mountains", "mountain_edge", "wooded_mountains", "gravelly_mountains", "modified_gravelly_mountains", "paper"};
        for (String emeraldBiome : emeraldBiomes) {
            emeraldIndexes.put(emeraldBiome, 17);
        }
        HashMap<String, Integer> LDebrisIndexes = new HashMap<>();
        LDebrisIndexes.put("default", 15);
        LDebrisIndexes.put("crimson_forest", 12);
        LDebrisIndexes.put("warped_forest", 13);
        HashMap<String, Integer> SDebrisIndexes = new HashMap<>();
        LDebrisIndexes.forEach((biome, index) -> SDebrisIndexes.put(biome, index + 1));
        HashMap<String, Integer> extraGoldIndexes = new HashMap<>();
        extraGoldIndexes.put("default", -1);
        String[] extraGoldBiomes = new String[]{"badlands", "badlands_plateau", "modified_badlands_plateau", "wooded_badlands_plateau", "modified_wooded_badlands_plateau", "eroded_badlands", "paper"};
        for (String extraGoldBiome : extraGoldBiomes) {
            extraGoldIndexes.put(extraGoldBiome, 14);
        }
        ores.add(new Ore(Type.COAL, DimensionType.OVERWORLD_ID, 7, 6, ConstantIntProvider.create(20), false, 0, 128, Generator.DEFAULT, 20, coal, new Color(47, 44, 54)));
        ores.add(new Ore(Type.IRON,DimensionType.OVERWORLD_ID, 8, 6, ConstantIntProvider.create(20), false, 0, 64, Generator.DEFAULT, 9, iron, new Color(236, 173, 119)));
        ores.add(new Ore(Type.GOLD, DimensionType.OVERWORLD_ID, 9, 6, ConstantIntProvider.create(2), false, 0, 32, Generator.DEFAULT, 9, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.REDSTONE, DimensionType.OVERWORLD_ID, 10, 6, ConstantIntProvider.create(8), false, 0, 16, Generator.DEFAULT, 8, redstone, new Color(245, 7, 23)));
        ores.add(new Ore(Type.DIAMOND, DimensionType.OVERWORLD_ID, 11, 6, ConstantIntProvider.create(1), false, 0, 16, Generator.DEFAULT, 8, diamond, new Color(33, 244, 255)));
        ores.add(new Ore(Type.LAPIS, DimensionType.OVERWORLD_ID, 12, 6, ConstantIntProvider.create(1), true, 16, 16, Generator.DEFAULT, 7, lapis, new Color(8, 26, 189)));
        ores.add(new Ore(Type.COPPER, DimensionType.OVERWORLD_ID, 13, 6, ConstantIntProvider.create(6), true, 49, 49, Generator.DEFAULT, 10, copper, new Color(239, 151, 0)));
        ores.add(new Ore(Type.GOLD_EXTRA, DimensionType.OVERWORLD_ID, extraGoldIndexes, 6, ConstantIntProvider.create(20), false, 32, 80, Generator.DEFAULT, 9, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.EMERALD, DimensionType.OVERWORLD_ID, emeraldIndexes, 6, UniformIntProvider.create(6, 8), false, 4, 32, Generator.EMERALD, 1, emerald, new Color(27, 209, 45)));
        ores.add(new Ore(Type.GOLD_NETHER, DimensionType.THE_NETHER_ID, Map.of("default", 13, "basalt_deltas", -1), 7, ConstantIntProvider.create(10), false, 10, 118, Generator.DEFAULT, 10, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, DimensionType.THE_NETHER_ID, Map.of("default", 14, "basalt_deltas", -1), 7, ConstantIntProvider.create(16), false, 10, 118, Generator.DEFAULT, 14, quartz, new Color(205, 205, 205)));
        ores.add(new Ore(Type.GOLD_NETHER, DimensionType.THE_NETHER_ID, Map.of("default", -1, "basalt_deltas", 13), 7, ConstantIntProvider.create(20), false, 10, 118, Generator.DEFAULT, 10, gold, new Color(247, 229, 30)));
        ores.add(new Ore(Type.QUARTZ, DimensionType.THE_NETHER_ID, Map.of("default", -1, "basalt_deltas", 14), 7, ConstantIntProvider.create(32), false, 10, 118, Generator.DEFAULT, 14, quartz, new Color(205, 205, 205)));
        ores.add(new Ore(Type.LDEBRIS, DimensionType.THE_NETHER_ID, LDebrisIndexes, 7, ConstantIntProvider.create(1), true, 17, 9, Generator.NO_SURFACE, 3, debris, new Color(209, 27, 245)));
        ores.add(new Ore(Type.SDEBRIS, DimensionType.THE_NETHER_ID, SDebrisIndexes, 7, ConstantIntProvider.create(1), false, 8, 120, Generator.NO_SURFACE, 2, debris, new Color(209, 27, 245)));
        return ores;
    }

    private static List<Ore> V1_17_1() {
        return baseConfig();
    }

    private static List<Ore> V1_17() {
        List<Ore> ores = baseConfig();
        for (Ore ore : ores) {
            if (ore.type.equals(Type.DIAMOND)) {
                ore.maxY = 17;
            }
            if (ore.type.equals(Type.EMERALD)) {
                ore.count = UniformIntProvider.create(6, 24);
            }
        }
        return ores;
    }

    private static List<Ore> V1_16() {
        List<Ore> ores = baseConfig();
        ores.removeIf(ore -> ore.type.equals(Type.COPPER));
        for (Ore ore : ores) {
            if (ore.type == Type.EMERALD || ore.type == Type.GOLD_EXTRA) {
                ore.index.keySet().forEach(key -> ore.index.put(key, ore.index.get(key) - 3));
            } else if (ore.dimension == DimensionType.OVERWORLD_ID) {
                ore.index.keySet().forEach(key -> ore.index.put(key, ore.index.get(key) - 2));
            } else if (ore.type == Type.LDEBRIS) {
                ore.minY = 16;
                ore.maxY = 8;
            }
        }
        return ores;
    }

    private static List<Ore> V1_15() {
        List<Ore> ores = V1_16();
        ores.removeIf(ore -> ore.type == Type.SDEBRIS || ore.type == Type.LDEBRIS || ore.type == Type.GOLD_NETHER);
        for (Ore ore : ores) {
            ore.step -= 2;
        }
        return ores;
    }

    public static List<Ore> getConfig(MCVersion version) {
        return switch (version) {
            case v1_18_1, v1_18 -> V1_18();
            case v1_17_1 -> V1_17_1();
            case v1_17 -> V1_17();
            case v1_16_5, v1_16_4, v1_16_3, v1_16_2, v1_16_1, v1_16 -> V1_16();
            case v1_15_2, v1_15_1, v1_15, v1_14_4, v1_14_3, v1_14_2, v1_14_1, v1_14 -> V1_15();
            default -> null;
        };
    }

    public enum Type {
        DIAMOND, REDSTONE, GOLD, IRON, COAL, EMERALD, SDEBRIS, LDEBRIS, LAPIS, COPPER, QUARTZ, GOLD_NETHER, GOLD_EXTRA
    }

    public enum Generator {
        DEFAULT, EMERALD, NO_SURFACE
    }
}
