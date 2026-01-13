package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.PlayerRespawnEvent;
import anticope.rejects.events.SeedChangedEvent;
import anticope.rejects.utils.Ore;
import anticope.rejects.utils.seeds.Seed;
import anticope.rejects.utils.seeds.Seeds;
import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OreSim extends Module {

    private final Map<Long, Map<Ore, Set<Vec3>>> chunkRenderers = new ConcurrentHashMap<>();
    private Seed worldSeed = null;
    private Map<ResourceKey<Biome>, List<Ore>> oreConfig;
    public List<BlockPos> oreGoals = new ArrayList<>();

    public enum AirCheck {
        ON_LOAD,
        RECHECK,
        OFF
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-range")
            .description("Taxi cap distance of chunks being shown.")
            .defaultValue(5)
            .min(1)
            .sliderMax(10)
            .build()
    );

    private final Setting<AirCheck> airCheck = sgGeneral.add(new EnumSetting.Builder<AirCheck>()
            .name("air-check-mode")
            .description("Checks if there is air at a calculated ore pos.")
            .defaultValue(AirCheck.RECHECK)
            .build()
    );

    private final Setting<Boolean> baritone = sgGeneral.add(new BoolSetting.Builder()
            .name("baritone")
            .description("Set baritone ore positions to the simulated ones.")
            .defaultValue(false)
            .build()
    );


    public OreSim() {
        super(MeteorRejectsAddon.CATEGORY, "ore-sim", "Xray on crack.");
        SettingGroup sgOres = settings.createGroup("Ores");
        Ore.oreSettings.forEach(sgOres::add);
    }

    public boolean baritone() {
        return isActive() && baritone.get() && BaritoneUtils.IS_AVAILABLE;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || oreConfig == null) {
            return;
        }
        if (Seeds.get().getSeed() != null) {
            int chunkX = mc.player.chunkPosition().x;
            int chunkZ = mc.player.chunkPosition().z;

            int rangeVal = horizontalRadius.get();
            for (int range = 0; range <= rangeVal; range++) {
                for (int x = -range + chunkX; x <= range + chunkX; x++) {
                    renderChunk(x, chunkZ + range - rangeVal, event);
                }
                for (int x = (-range) + 1 + chunkX; x < range + chunkX; x++) {
                    renderChunk(x, chunkZ - range + rangeVal + 1, event);
                }
            }
        }

    }

    private void renderChunk(int x, int z, Render3DEvent event) {
        long chunkKey = ChunkPos.asLong(x,z);

        if (chunkRenderers.containsKey(chunkKey)) {
            Map<Ore, Set<Vec3>> chunk = chunkRenderers.get(chunkKey);

            for (Map.Entry<Ore, Set<Vec3>> oreRenders : chunk.entrySet()) {
                if (oreRenders.getKey().active.get()) {
                    for (Vec3 pos : oreRenders.getValue()) {
                        event.renderer.boxLines(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 1, pos.z + 1, oreRenders.getKey().color, 0);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (airCheck.get() != AirCheck.RECHECK || event.newState.canOcclude()) return;

        long chunkKey = ChunkPos.asLong(event.pos);
        if (chunkRenderers.containsKey(chunkKey)) {
            Vec3 pos = Vec3.atLowerCornerOf(event.pos);
            for (var ore : chunkRenderers.get(chunkKey).values()) {
                ore.remove(pos);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || oreConfig == null) return;

        if (baritone() && BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
            oreGoals.clear();
            var chunkPos = mc.player.chunkPosition();
            int rangeVal = 4;
            for (int range = 0; range <= rangeVal; ++range) {
                for (int x = -range + chunkPos.x; x <= range + chunkPos.x; ++x) {
                    oreGoals.addAll(addToBaritone(x, chunkPos.z + range - rangeVal));
                }
                for (int x = -range + 1 + chunkPos.x; x < range + chunkPos.x; ++x) {
                    oreGoals.addAll(this.addToBaritone(x, chunkPos.z - range + rangeVal + 1));
                }
            }
        }
    }

    private ArrayList<BlockPos> addToBaritone(int chunkX, int chunkZ) {
        ArrayList<BlockPos> baritoneGoals = new ArrayList<>();
        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
        if (this.chunkRenderers.containsKey(chunkKey)) {
            this.chunkRenderers.get(chunkKey).entrySet().stream()
                    .filter(entry -> entry.getKey().active.get())
                    .flatMap(entry -> entry.getValue().stream())
                    .map(BlockPos::containing)
                    .forEach(baritoneGoals::add);
        }
        return baritoneGoals;
    }

    @Override
    public void onActivate() {
        if (Seeds.get().getSeed() == null) {
            error("No seed found. To set a seed do .seed <seed>");
            this.toggle();
        }
        reload();
    }

    @Override
    public void onDeactivate() {
        this.chunkRenderers.clear();
        this.oreConfig = null;
    }

    @EventHandler
    private void onSeedChanged(SeedChangedEvent event) {
        reload();
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        reload();
    }

    private void loadVisibleChunks() {
        if (mc.player == null) {
            return;
        }

        for (ChunkAccess chunk : Utils.chunks(false)) {
            doMathOnChunk(chunk);
        }
    }

    private void reload() {
        Seed seed = Seeds.get().getSeed();
        if (seed == null) return;
        worldSeed = seed;
        oreConfig = Ore.getRegistry(PlayerUtils.getDimension());

        chunkRenderers.clear();
        if (mc.level != null && worldSeed != null) {
            loadVisibleChunks();
        }
    }

    @EventHandler
    public void onChunkData(ChunkDataEvent event) {
        doMathOnChunk(event.chunk());
    }

    private void doMathOnChunk(ChunkAccess chunk) {

        var chunkPos = chunk.getPos();
        long chunkKey = chunkPos.toLong();

        ClientLevel world = mc.level;

        if (chunkRenderers.containsKey(chunkKey) || world == null) {
            return;
        }

        Set<ResourceKey<Biome>> biomes = new HashSet<>();
        ChunkPos.rangeClosed(chunkPos, 1).forEach(chunkPosx -> {
            ChunkAccess chunkxx = world.getChunk(chunkPosx.x, chunkPosx.z, ChunkStatus.BIOMES, false);
            if (chunkxx == null) return;

            for(LevelChunkSection chunkSection : chunkxx.getSections()) {
                chunkSection.getBiomes().getAll(entry -> biomes.add(entry.unwrapKey().get()));
            }
        });
        Set<Ore> oreSet = biomes.stream().flatMap(b -> getDefaultOres(b).stream()).collect(Collectors.toSet());

        int chunkX = chunkPos.x << 4;
        int chunkZ = chunkPos.z << 4;
        WorldgenRandom random = new WorldgenRandom(WorldgenRandom.Algorithm.XOROSHIRO.newInstance(0));

        long populationSeed = random.setDecorationSeed(worldSeed.seed, chunkX, chunkZ);
        HashMap<Ore, Set<Vec3>> h = new HashMap<>();

        for (Ore ore : oreSet) {

            HashSet<Vec3> ores = new HashSet<>();

            random.setFeatureSeed(populationSeed, ore.index, ore.step);

            int repeat = ore.count.sample(random);

            for (int i = 0; i < repeat; i++) {

                if (ore.rarity != 1F && random.nextFloat() >= 1/ore.rarity) {
                    continue;
                }

                int x = random.nextInt(16) + chunkX;
                int z = random.nextInt(16) + chunkZ;
                int y = ore.heightProvider.sample(random, ore.heightContext);
                BlockPos origin = new BlockPos(x,y,z);

                ResourceKey<Biome> biome = chunk.getNoiseBiome(x,y,z).unwrapKey().get();

                if (!getDefaultOres(biome).contains(ore)) {
                    continue;
                }

                if (ore.scattered) {
                    ores.addAll(generateHidden(world, random, origin, ore.size));
                } else {
                    ores.addAll(generateNormal(world, random, origin, ore.size, ore.discardOnAirChance));
                }
            }
            if (!ores.isEmpty()) {
                h.put(ore, ores);
            }
        }
        chunkRenderers.put(chunkKey, h);
    }

    private List<Ore> getDefaultOres(ResourceKey<Biome> biomeRegistryKey) {
        if (oreConfig.containsKey(biomeRegistryKey)) {
            return oreConfig.get(biomeRegistryKey);
        } else {
            return this.oreConfig.values().stream().findAny().get();
        }
    }

    // ====================================
    // Mojang code
    // ====================================

    private ArrayList<Vec3> generateNormal(ClientLevel world, WorldgenRandom random, BlockPos blockPos, int veinSize, float discardOnAir) {
        float f = random.nextFloat() * 3.1415927F;
        float g = (float) veinSize / 8.0F;
        int i = Mth.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
        double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
        double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
        double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
        double l = (blockPos.getY() + random.nextInt(3) - 2);
        double m = (blockPos.getY() + random.nextInt(3) - 2);
        int n = blockPos.getX() - Mth.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - Mth.ceil(g) - i;
        int q = 2 * (Mth.ceil(g) + i);
        int r = 2 * (2 + i);

        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o <= world.getHeight(Heightmap.Types.MOTION_BLOCKING, s, t)) {
                    return this.generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
                }
            }
        }

        return new ArrayList<>();
    }

    private ArrayList<Vec3> generateVeinPart(ClientLevel world, WorldgenRandom random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir) {

        BitSet bitSet = new BitSet(size * i * size);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double[] ds = new double[veinSize * 4];

        ArrayList<Vec3> poses = new ArrayList<>();

        int n;
        double p;
        double q;
        double r;
        double s;
        for (n = 0; n < veinSize; ++n) {
            float f = (float) n / (float) veinSize;
            p = Mth.lerp(f, startX, endX);
            q = Mth.lerp(f, startY, endY);
            r = Mth.lerp(f, startZ, endZ);
            s = random.nextDouble() * (double) veinSize / 16.0D;
            double m = ((double) (Mth.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
            ds[n * 4] = p;
            ds[n * 4 + 1] = q;
            ds[n * 4 + 2] = r;
            ds[n * 4 + 3] = m;
        }

        for (n = 0; n < veinSize - 1; ++n) {
            if (!(ds[n * 4 + 3] <= 0.0D)) {
                for (int o = n + 1; o < veinSize; ++o) {
                    if (!(ds[o * 4 + 3] <= 0.0D)) {
                        p = ds[n * 4] - ds[o * 4];
                        q = ds[n * 4 + 1] - ds[o * 4 + 1];
                        r = ds[n * 4 + 2] - ds[o * 4 + 2];
                        s = ds[n * 4 + 3] - ds[o * 4 + 3];
                        if (s * s > p * p + q * q + r * r) {
                            if (s > 0.0D) {
                                ds[o * 4 + 3] = -1.0D;
                            } else {
                                ds[n * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        for (n = 0; n < veinSize; ++n) {
            double u = ds[n * 4 + 3];
            if (!(u < 0.0D)) {
                double v = ds[n * 4];
                double w = ds[n * 4 + 1];
                double aa = ds[n * 4 + 2];
                int ab = Math.max(Mth.floor(v - u), x);
                int ac = Math.max(Mth.floor(w - u), y);
                int ad = Math.max(Mth.floor(aa - u), z);
                int ae = Math.max(Mth.floor(v + u), ab);
                int af = Math.max(Mth.floor(w + u), ac);
                int ag = Math.max(Mth.floor(aa + u), ad);

                for (int ah = ab; ah <= ae; ++ah) {
                    double ai = ((double) ah + 0.5D - v) / u;
                    if (ai * ai < 1.0D) {
                        for (int aj = ac; aj <= af; ++aj) {
                            double ak = ((double) aj + 0.5D - w) / u;
                            if (ai * ai + ak * ak < 1.0D) {
                                for (int al = ad; al <= ag; ++al) {
                                    double am = ((double) al + 0.5D - aa) / u;
                                    if (ai * ai + ak * ak + am * am < 1.0D) {
                                        int an = ah - x + (aj - y) * size + (al - z) * size * i;
                                        if (!bitSet.get(an)) {
                                            bitSet.set(an);
                                            mutable.set(ah, aj, al);
                                            if (aj >= -64 && aj < 320 && (airCheck.get() == AirCheck.OFF || world.getBlockState(mutable).canOcclude())) {
                                                if (shouldPlace(world, mutable, discardOnAir, random)) {
                                                    poses.add(new Vec3(ah, aj, al));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return poses;
    }

    private boolean shouldPlace(ClientLevel world, BlockPos orePos, float discardOnAir, WorldgenRandom random) {
        if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) {
            return true;
        }

        for (Direction direction : Direction.values()) {
            if (!world.getBlockState(orePos.offset(direction.getUnitVec3i())).canOcclude() && discardOnAir != 1F) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Vec3> generateHidden(ClientLevel world, WorldgenRandom random, BlockPos blockPos, int size) {

        ArrayList<Vec3> poses = new ArrayList<>();

        int i = random.nextInt(size + 1);

        for (int j = 0; j < i; ++j) {
            size = Math.min(j, 7);
            int x = this.randomCoord(random, size) + blockPos.getX();
            int y = this.randomCoord(random, size) + blockPos.getY();
            int z = this.randomCoord(random, size) + blockPos.getZ();
            if (airCheck.get() == AirCheck.OFF || world.getBlockState(new BlockPos(x, y, z)).canOcclude()) {
                if (shouldPlace(world, new BlockPos(x, y, z), 1F, random)) {
                    poses.add(new Vec3(x, y, z));
                }
            }
        }

        return poses;
    }

    private int randomCoord(WorldgenRandom random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
    }
}
