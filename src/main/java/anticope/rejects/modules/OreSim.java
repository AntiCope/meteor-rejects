package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.ChunkPosDataEvent;
import anticope.rejects.events.PlayerRespawnEvent;
import anticope.rejects.events.SeedChangedEvent;
import anticope.rejects.utils.Ore;
import anticope.rejects.utils.seeds.Seed;
import anticope.rejects.utils.seeds.Seeds;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.random.ChunkRandom;
import com.seedfinding.mccore.version.MCVersion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class OreSim extends Module {

    private final HashMap<Long, HashMap<Ore, HashSet<Vec3d>>> chunkRenderers = new HashMap<>();
    private Seed worldSeed = null;
    private List<Ore> oreConfig;
    private ChunkPos prevOffset = new ChunkPos(0, 0);

    public enum AirCheck {
        ON_LOAD,
        RECHECK,
        OFF
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgOres = settings.createGroup("Ores");

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-range")
            .description("taxi cap distance of chunks being shown")
            .defaultValue(5)
            .min(1)
            .sliderMax(10)
            .build()
    );

    private final Setting<AirCheck> airCheck = sgGeneral.add(new EnumSetting.Builder<AirCheck>()
            .name("air-check-mode")
            .description("checks if there is air at a calculated ore pos")
            .defaultValue(AirCheck.RECHECK)
            .build()
    );


    public OreSim() {
        super(MeteorRejectsAddon.CATEGORY, "ore-sim", "Xray on crack.");
        Ore.oreSettings.forEach(sgOres::add);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || oreConfig == null) {
            return;
        }
        if (Seeds.get().getSeed() != null) {
            int chunkX = mc.player.getChunkPos().x;
            int chunkZ = mc.player.getChunkPos().z;

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
        long chunkKey = (long) x + ((long) z << 32);

        if (chunkRenderers.containsKey(chunkKey)) {
            for (Ore ore : oreConfig) {
                if (ore.enabled.get()) {
                    if (!chunkRenderers.get(chunkKey).containsKey(ore)) {
                        continue;
                    }
                    for (Vec3d pos : chunkRenderers.get(chunkKey).get(ore)) {
                        event.renderer.boxLines(pos.x,pos.y,pos.z,pos.x+1,pos.y+1,pos.z+1, ore.color,0);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (airCheck.get() == AirCheck.RECHECK) {
            if (mc.player == null || mc.world == null || oreConfig == null) {
                return;
            }
            long chunkX = mc.player.getChunkPos().x;
            long chunkZ = mc.player.getChunkPos().z;
            ClientWorld world = mc.world;
            int renderdistance = mc.options.viewDistance;

            //maybe another config option? But its already crowded
            int chunkCounter = 5;

            loop:
            while (true) {
                for (long offsetX = prevOffset.x; offsetX <= renderdistance; offsetX++) {
                    for (long offsetZ = prevOffset.z; offsetZ <= renderdistance; offsetZ++) {
                        prevOffset = new ChunkPos((int) offsetX, (int) offsetZ);
                        if (chunkCounter <= 0) {
                            break loop;
                        }
                        long chunkKey = (chunkX + offsetX) + ((chunkZ + offsetZ) << 32);

                        if (chunkRenderers.containsKey(chunkKey)) {
                            chunkRenderers.get(chunkKey).values().forEach(oreSet -> oreSet.removeIf(ore -> !world.getBlockState(new BlockPos((int) ore.x, (int) ore.y, (int) ore.z)).isOpaque()));
                        }
                        chunkCounter--;
                    }
                    prevOffset = new ChunkPos((int) offsetX, -renderdistance);
                }
                prevOffset = new ChunkPos(-renderdistance, -renderdistance);
            }
        }

    }

    @Override
    public void onActivate() {
        if (Seeds.get().getSeed() == null) {
            error("No seed found. To set a seed do .seed <seed>");
            this.toggle();
        }
        reload();
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
        int renderdistance = mc.options.viewDistance;

        if (mc.player == null) {
            return;
        }
        int playerChunkX = mc.player.getChunkPos().x;
        int playerChunkZ = mc.player.getChunkPos().z;

        for (int i = playerChunkX - renderdistance; i < playerChunkX + renderdistance; i++) {
            for (int j = playerChunkZ - renderdistance; j < playerChunkZ + renderdistance; j++) {
                doMathOnChunk(i, j);
            }
        }
    }

    private void reload() {
        Seed seed = Seeds.get().getSeed();
        if (seed == null) return;
        worldSeed = seed;
        oreConfig = Ore.getConfig(Seeds.get().getSeed().version);
        chunkRenderers.clear();
        if (mc.world != null && worldSeed != null) {
            loadVisibleChunks();
        }
    }

    @EventHandler
    public void onChunkData(ChunkPosDataEvent event) {
        doMathOnChunk(event.chunkX, event.chunkZ);
    }

    private void doMathOnChunk(int chunkX, int chunkZ) {
        long chunkKey = (long) chunkX + ((long) chunkZ << 32);

        ClientWorld world = mc.world;

        if (chunkRenderers.containsKey(chunkKey) || world == null) {
            return;
        }

        if (world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
            return;
        }

        chunkX = chunkX << 4;
        chunkZ = chunkZ << 4;


        ChunkRandom random = new ChunkRandom(ChunkRandom.RandomProvider.LEGACY.create(0));

        if (worldSeed.version.isNewerOrEqualTo(MCVersion.v1_18)) { //1.18 and above
            random = new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(0));
        }

        HashMap<Ore, HashSet<Vec3d>> h = new HashMap<>();

        long populationSeed = random.setPopulationSeed(worldSeed.seed, chunkX, chunkZ);

        Identifier id = world.getRegistryManager().get(Registry.BIOME_KEY).getId(world.getBiomeAccess().getBiomeForNoiseGen(new BlockPos(chunkX, 0, chunkZ)));
        if (id == null) {
            error("Something went wrong, you may have some mods that mess with world generation");
            toggle();
            return;
        }
        String biomeName = id.getPath();
        Identifier dimensionName = world.getDimension().getEffects();

        for (Ore ore : oreConfig) {

            if (!dimensionName.getPath().equals(ore.dimension.getPath())) {
                continue;
            }

            HashSet<Vec3d> ores = new HashSet<>();

            int index;
            if (ore.index.containsKey(biomeName)) {
                index = ore.index.get(biomeName);
            } else {
                index = ore.index.get("default");
            }
            if (index < 0) {
                continue;
            }

            random.setDecoratorSeed(populationSeed, index, ore.step);

            int repeat = ore.count.get(random);

            for (int i = 0; i < repeat; i++) {

                if (ore.chance != 1F && random.nextFloat() >= ore.chance) {
                    continue;
                }

                int x = random.nextInt(16) + chunkX;
                int z;
                int y;
                if (worldSeed.version.isBetween(MCVersion.v1_14, MCVersion.v1_14_4)) {
                    y = ore.depthAverage ? random.nextInt(ore.maxY) + random.nextInt(ore.maxY) - ore.maxY : random.nextInt(ore.maxY - ore.minY);
                    z = random.nextInt(16) + chunkZ;
                } else {
                    z = random.nextInt(16) + chunkZ;
                    y = ore.depthAverage ? random.nextInt(ore.maxY) + random.nextInt(ore.maxY) - ore.maxY : random.nextInt(ore.maxY - ore.minY);
                }
                y += ore.minY;

                switch (ore.generator) {
                    case DEFAULT -> ores.addAll(generateNormal(world, random, new BlockPos(x, y, z), ore.size, ore.discardOnAir));
                    case EMERALD -> {
                        if (airCheck.get() == AirCheck.OFF || world.getBlockState(new BlockPos(x, y, z)).isOpaque()) {
                            ores.add(new Vec3d(x, y, z));
                        }
                    }
                    case NO_SURFACE -> ores.addAll(generateHidden(world, random, new BlockPos(x, y, z), ore.size));
                    default -> System.out.println(ore.type + " has some unknown generator. Fix it!");
                }
            }
            if (!ores.isEmpty()) {
                h.put(ore, ores);
            }
        }
        chunkRenderers.put(chunkKey, h);
    }

    // ====================================
    // Mojang code
    // ====================================

    private ArrayList<Vec3d> generateNormal(ClientWorld world, Random random, BlockPos blockPos, int veinSize, float discardOnAir) {
        float f = random.nextFloat() * 3.1415927F;
        float g = (float) veinSize / 8.0F;
        int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
        double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
        double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
        double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
        double l = (blockPos.getY() + random.nextInt(3) - 2);
        double m = (blockPos.getY() + random.nextInt(3) - 2);
        int n = blockPos.getX() - MathHelper.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - MathHelper.ceil(g) - i;
        int q = 2 * (MathHelper.ceil(g) + i);
        int r = 2 * (2 + i);

        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, s, t)) {
                    return this.generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
                }
            }
        }

        return new ArrayList<>();
    }

    private ArrayList<Vec3d> generateVeinPart(ClientWorld world, Random random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir) {

        BitSet bitSet = new BitSet(size * i * size);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        double[] ds = new double[veinSize * 4];

        ArrayList<Vec3d> poses = new ArrayList<>();

        int n;
        double p;
        double q;
        double r;
        double s;
        for (n = 0; n < veinSize; ++n) {
            float f = (float) n / (float) veinSize;
            p = MathHelper.lerp(f, startX, endX);
            q = MathHelper.lerp(f, startY, endY);
            r = MathHelper.lerp(f, startZ, endZ);
            s = random.nextDouble() * (double) veinSize / 16.0D;
            double m = ((double) (MathHelper.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
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
                int ab = Math.max(MathHelper.floor(v - u), x);
                int ac = Math.max(MathHelper.floor(w - u), y);
                int ad = Math.max(MathHelper.floor(aa - u), z);
                int ae = Math.max(MathHelper.floor(v + u), ab);
                int af = Math.max(MathHelper.floor(w + u), ac);
                int ag = Math.max(MathHelper.floor(aa + u), ad);

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
                                            if (aj >= -64 && aj < 320 && (airCheck.get() == AirCheck.OFF || world.getBlockState(mutable).isOpaque())) {
                                                if (shouldPlace(world, mutable, discardOnAir, random)) {
                                                    poses.add(new Vec3d(ah, aj, al));
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

    private boolean shouldPlace(ClientWorld world, BlockPos orePos, float discardOnAir, Random random) {
        if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) {
            return true;
        }

        for (Direction direction : Direction.values()) {
            if (!world.getBlockState(orePos.add(direction.getVector())).isOpaque() && discardOnAir != 1F) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Vec3d> generateHidden(ClientWorld world, Random random, BlockPos blockPos, int size) {

        ArrayList<Vec3d> poses = new ArrayList<>();

        int i = random.nextInt(size + 1);

        for (int j = 0; j < i; ++j) {
            size = Math.min(j, 7);
            int x = this.randomCoord(random, size) + blockPos.getX();
            int y = this.randomCoord(random, size) + blockPos.getY();
            int z = this.randomCoord(random, size) + blockPos.getZ();
            if (airCheck.get() == AirCheck.OFF || world.getBlockState(new BlockPos(x, y, z)).isOpaque()) {
                if (shouldPlace(world, new BlockPos(x, y, z), 1F, random)) {
                    poses.add(new Vec3d(x, y, z));
                }
            }
        }

        return poses;
    }

    private int randomCoord(Random random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
    }
}
