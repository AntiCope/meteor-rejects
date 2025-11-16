package anticope.rejects.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RejectsConfig extends System<RejectsConfig> {
    private static final RejectsConfig INSTANCE = new RejectsConfig();

    public enum HttpAllowed {
        Everything,
        NotMeteorApi,
        NotMeteorPing,
        Nothing
    }

    public HttpAllowed httpAllowed = HttpAllowed.Everything;
    public String httpUserAgent = "Meteor Client";
    public Set<String> hiddenModules = new HashSet<>();
    public boolean loadSystemFonts = true;
    public boolean duplicateModuleNames = false;

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
        if (modules == null) return List.of();
        return hiddenModules.stream().map(modules::get).collect(Collectors.toList());
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("httpAllowed", httpAllowed.toString());
        tag.putString("httpUserAgent", httpUserAgent);
        tag.putBoolean("loadSystemFonts", loadSystemFonts);
        tag.putBoolean("duplicateModuleNames", duplicateModuleNames);

        NbtList modulesTag = new NbtList();
        for (String module : hiddenModules) modulesTag.add(NbtString.of(module));
        tag.put("hiddenModules", modulesTag);

        return tag;
    }

    @Override
    public RejectsConfig fromTag(NbtCompound tag) {
        tag.getString("httpAllowed").ifPresent(s -> httpAllowed = HttpAllowed.valueOf(s));
        httpUserAgent = tag.getString("httpUserAgent").orElse(httpUserAgent);
        loadSystemFonts = tag.getBoolean("loadSystemFonts").orElse(loadSystemFonts);
        duplicateModuleNames = tag.getBoolean("duplicateModuleNames").orElse(duplicateModuleNames);

        tag.getList("hiddenModules").ifPresent(valueTag -> {
            for (NbtElement tagI : valueTag) {
                tagI.asString().ifPresent(hiddenModules::add);
            }
        });

        return this;
    }
}
