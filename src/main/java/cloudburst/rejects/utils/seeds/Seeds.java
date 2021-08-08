package cloudburst.rejects.utils.seeds;

import java.util.HashMap;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;

import kaptainwutax.mcutils.version.MCVersion;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.utils.Utils;

import static meteordevelopment.meteorclient.utils.Utils.mc;

public class Seeds extends System<Seeds> {
    private static final Seeds INSTANCE = new Seeds();

    public HashMap<String, Seed> seeds = new HashMap<>();

    public Seeds() {
        super("seeds");
        init();
        load(MeteorClient.FOLDER);
    }

    public static Seeds get() {
        return INSTANCE;
    }

    public Seed getSeed() {
        if (mc.isIntegratedServerRunning() && mc.getServer() != null)
            return new Seed(mc.getServer().getOverworld().getSeed(), MCVersion.fromString(mc.getServer().getVersion()));

        return seeds.get(Utils.getWorldName());
    }

    public void setSeed(long seed, MCVersion version) {
        if (mc.isIntegratedServerRunning()) return;

        seeds.put(Utils.getWorldName(), new Seed(seed, version));
    }

    public void setSeed(long seed) {
        ServerInfo server = mc.getCurrentServerEntry();
        MCVersion ver = null;
        if (server != null)
            ver = MCVersion.fromString(server.version.asString());
        if (ver == null)
            ver = MCVersion.latest();
        setSeed(seed, ver);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        seeds.forEach((key, seed) -> {
            if (seed == null) return;
            tag.put(key, seed.toTag());
        });
        return tag;
    }

    @Override
    public Seeds fromTag(NbtCompound tag) {
        tag.getKeys().forEach(key -> {
            seeds.put(key, Seed.fromTag(tag.getCompound(key)));
        });
        return this;
    }
}
