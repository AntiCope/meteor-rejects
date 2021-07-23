package cloudburst.rejects.utils;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.nbt.NbtCompound;

public class RejectsConfig extends System<RejectsConfig> {
    private static final RejectsConfig rejectsConfig = new RejectsConfig();

    public enum HttpAllowed {
        Everything,
        NotMeteorApi,
        NotMeteorPing,
        Nothing
    }

    public HttpAllowed httpAllowed = HttpAllowed.Everything;

    public RejectsConfig() {
        super("rejects-config");
        init();
        load(MeteorClient.FOLDER);
    }

    public static RejectsConfig get() {
        return rejectsConfig;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("httpAllowed", httpAllowed.toString());
        return tag;
    }

    @Override
    public RejectsConfig fromTag(NbtCompound tag) {
        httpAllowed = HttpAllowed.valueOf(tag.getString("httpAllowed"));
        return this;
    }
}
