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
        // Get the "httpAllowed" string safely
        String httpAllowedString = "Everything";
        if (tag.contains("httpAllowed")) {
            NbtElement httpAllowedElement = tag.get("httpAllowed");
            if (httpAllowedElement != null) {
                httpAllowedString = String.valueOf(httpAllowedElement.asString());
                // Remove quotes if present (asString() may wrap in quotes)
                if (httpAllowedString.startsWith("\"") && httpAllowedString.endsWith("\"")) {
                    httpAllowedString = httpAllowedString.substring(1, httpAllowedString.length() - 1);
                }
                if (httpAllowedString.startsWith("Optional[")) {
                    httpAllowedString = httpAllowedString.substring(9, httpAllowedString.length() - 1);
                }
            }
        }

        try {
            httpAllowed = HttpAllowed.valueOf(httpAllowedString);
        } catch (IllegalArgumentException e) {
            java.lang.System.err.println("Invalid value for httpAllowed: " + httpAllowedString);
            httpAllowed = HttpAllowed.Everything;
        }

        httpUserAgent = tag.contains("httpUserAgent") ? String.valueOf(tag.getString("httpUserAgent")) : "Meteor Client";
        loadSystemFonts = tag.contains("loadSystemFonts") && tag.getBoolean("loadSystemFonts").orElse(false);
        duplicateModuleNames = tag.contains("duplicateModuleNames") && tag.getBoolean("duplicateModuleNames").orElse(false);

        NbtList valueTag = tag.getListOrEmpty("hiddenModules");
        for (NbtElement tagI : valueTag) {
            hiddenModules.add(String.valueOf(tagI.asString()));
        }

        return this;
    }
}
