package anticope.rejects.utils;

import anticope.rejects.mixin.CountPlacementModifierAccessor;
import anticope.rejects.mixin.HeightRangePlacementModifierAccessor;
import anticope.rejects.mixin.RarityFilterPlacementModifierAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;

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

    public static Map<RegistryKey<Biome>, List<Ore>> getRegistry(Dimension dimension) {

        RegistryWrapper.WrapperLookup registry = BuiltinRegistries.createWrapperLookup();
        RegistryWrapper.Impl<PlacedFeature> features = registry.getOrThrow(RegistryKeys.PLACED_FEATURE);
        var reg = registry.getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.DEFAULT).value().createDimensionsRegistryHolder().dimensions();

        var dim = switch (dimension) {
            case Overworld -> reg.get(DimensionOptions.OVERWORLD);
            case Nether -> reg.get(DimensionOptions.NETHER);
            case End -> reg.get(DimensionOptions.END);
        };

        var biomes = dim.chunkGenerator().getBiomeSource().getBiomes();
        var biomes1 = biomes.stream().toList();

        List<PlacedFeatureIndexer.IndexedFeatures> indexer = PlacedFeatureIndexer.collectIndexedFeatures(
                biomes1, biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true
        );


        Map<PlacedFeature, Ore> featureToOre = new HashMap<>();
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COAL_LOWER, 6, coal, new Color(47, 44, 54));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COAL_UPPER, 6, coal, new Color(47, 44, 54));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_MIDDLE, 6, iron, new Color(236, 173, 119));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_SMALL, 6, iron, new Color(236, 173, 119));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_UPPER, 6, iron, new Color(236, 173, 119));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD, 6, gold, new Color(247, 229, 30));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_LOWER, 6, gold, new Color(247, 229, 30));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_EXTRA, 6, gold, new Color(247, 229, 30));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_NETHER, 7, gold, new Color(247, 229, 30));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_DELTAS, 7, gold, new Color(247, 229, 30));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_REDSTONE, 6, redstone, new Color(245, 7, 23));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_REDSTONE_LOWER, 6, redstone, new Color(245, 7, 23));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND, 6, diamond, new Color(33, 244, 255));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_BURIED, 6, diamond, new Color(33, 244, 255));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_LARGE, 6, diamond, new Color(33, 244, 255));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_MEDIUM, 6, diamond, new Color(33, 244, 255));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_LAPIS, 6, lapis, new Color(8, 26, 189));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_LAPIS_BURIED, 6, lapis, new Color(8, 26, 189));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COPPER, 6, copper, new Color(239, 151, 0));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COPPER_LARGE, 6, copper, new Color(239, 151, 0));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_EMERALD, 6, emerald, new Color(27, 209, 45));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_QUARTZ_NETHER, 7, quartz, new Color(205, 205, 205));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_QUARTZ_DELTAS, 7, quartz, new Color(205, 205, 205));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DEBRIS_SMALL, 7, debris, new Color(209, 27, 245));
        registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE, 7, debris, new Color(209, 27, 245));


        Map<RegistryKey<Biome>, List<Ore>> biomeOreMap = new HashMap<>();

        biomes1.forEach(biome -> {
            biomeOreMap.put(biome.getKey().get(), new ArrayList<>());
            biome.value().getGenerationSettings().getFeatures().stream()
                    .flatMap(RegistryEntryList::stream)
                    .map(RegistryEntry::value)
                    .filter(featureToOre::containsKey)
                    .forEach(feature -> {
                        biomeOreMap.get(biome.getKey().get()).add(featureToOre.get(feature));
                    });
        });
        return biomeOreMap;
    }

    private static void registerOre(
            Map<PlacedFeature, Ore> map,
            List<PlacedFeatureIndexer.IndexedFeatures> indexer,
            RegistryWrapper.Impl<PlacedFeature> oreRegistry,
            RegistryKey<PlacedFeature> oreKey,
            int genStep,
            Setting<Boolean> active,
            Color color
    ) {
        var orePlacement = oreRegistry.getOrThrow(oreKey).value();

        int index = indexer.get(genStep).indexMapping().applyAsInt(orePlacement);

        Ore ore = new Ore(orePlacement, genStep, index, active, color);

        map.put(orePlacement, ore);
    }

    public int step;
    public int index;
    public Setting<Boolean> active;
    public IntProvider count = ConstantIntProvider.create(1);
    public HeightProvider heightProvider;
    public HeightContext heightContext;
    public float rarity = 1;
    public float discardOnAirChance;
    public int size;
    public Color color;
    public boolean scattered;

    private Ore(PlacedFeature feature, int step, int index, Setting<Boolean> active, Color color) {
        this.step = step;
        this.index = index;
        this.active = active;
        this.color = color;
        int bottom = MinecraftClient.getInstance().world.getBottomY();
        int height = MinecraftClient.getInstance().world.getDimension().logicalHeight();
        this.heightContext = new HeightContext(null, HeightLimitView.create(bottom, height));

        for (PlacementModifier modifier : feature.placementModifiers()) {
            if (modifier instanceof CountPlacementModifier) {
                this.count = ((CountPlacementModifierAccessor) modifier).getCount();

            } else if (modifier instanceof HeightRangePlacementModifier) {
                this.heightProvider = ((HeightRangePlacementModifierAccessor) modifier).getHeight();

            } else if (modifier instanceof RarityFilterPlacementModifier) {
                this.rarity = ((RarityFilterPlacementModifierAccessor) modifier).getChance();
            }
        }

        FeatureConfig featureConfig = feature.feature().value().config();

        if (featureConfig instanceof OreFeatureConfig oreFeatureConfig) {
            this.discardOnAirChance = oreFeatureConfig.discardOnAirChance;
            this.size = oreFeatureConfig.size;
        } else {
            throw new IllegalStateException("config for " + feature + "is not OreFeatureConfig.class");
        }

        if (feature.feature().value().feature() instanceof ScatteredOreFeature) {
            this.scattered = true;
        }
    }
}
