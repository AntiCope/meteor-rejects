package anticope.rejects.utils;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class RejectsConfig extends System<RejectsConfig> {
    private static final RejectsConfig INSTANCE = new RejectsConfig();

    public enum HttpAllowed {
        Everything,
        NotMeteorApi,
        NotMeteorPing,
        Nothing
    }

    public HttpAllowed httpAllowed = HttpAllowed.Everything;
    public boolean checkForUpdates = true;
    public Set<String> hiddenModules = new HashSet<String>();

    public RejectsConfig() {
        super("rejects-config");
        init();
        load(MeteorClient.FOLDER);
    }

    public static RejectsConfig get() {
        return INSTANCE;
    }

    public void setHiddenModules(List<Module> newList) {
        if (newList.size() < hiddenModules.size()) {
            OkPrompt.create()
                .title("Hidden Modules")
                .message("In order to see the modules you have removed from the list you need to restart Minecraft.")
                .id("hidden-modules-unhide")
                .show();
        }
        hiddenModules.clear();
        for (Module module : newList) {
            if (module == null) continue;
            if (module.isActive()) module.toggle();
            hiddenModules.add(module.name);
        }
    }

    public List<Module> getHiddenModules() {
        Modules modules = Modules.get();
        if (modules == null) return Arrays.asList();
        return hiddenModules.stream().map(modules::get).collect(Collectors.toList());
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("httpAllowed", httpAllowed.toString());
        tag.putBoolean("checkForUpdates", checkForUpdates);

        NbtList modulesTag = new NbtList();
        for (String module : hiddenModules) modulesTag.add(NbtString.of(module));
        tag.put("hiddenModules", modulesTag);

        return tag;
    }

    @Override
    public RejectsConfig fromTag(NbtCompound tag) {
        httpAllowed = HttpAllowed.valueOf(tag.getString("httpAllowed"));
        checkForUpdates = !tag.contains("checkForUpdates") || tag.getBoolean("checkForUpdates");

        NbtList valueTag = tag.getList("hiddenModules", 8);
        for (NbtElement tagI : valueTag) {
            hiddenModules.add(tagI.asString());
        }

        return this;
    }
}
