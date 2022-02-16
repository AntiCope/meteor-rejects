package anticope.rejects.utils;

import com.google.gson.JsonObject;
import net.minecraft.item.Items;

import net.fabricmc.loader.api.FabricLoader;
import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.MeteorToast;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class UpdateUtil {
    private static boolean firstTimeTitleScreen = true;
    private static final String TAGNAME = "latest-1.18";

    public static void checkUpdate() {
        if (!firstTimeTitleScreen) return;
        firstTimeTitleScreen = false;

        MeteorRejectsAddon.LOG.info("Checking for Meteor Rejects update...");
        
        String gitHash = FabricLoader
            .getInstance()
            .getModContainer("meteor-rejects")
            .get().getMetadata()
            .getCustomValue("updater:sha")
            .getAsString().trim();

        JsonObject tag = Http.get("https://api.github.com/repos/AntiCope/meteor-rejects/git/ref/tags/"+TAGNAME).sendJson(JsonObject.class);
        if (tag.get("object").getAsJsonObject().get("sha").getAsString().trim().equals(gitHash)) return;

        mc.getToastManager().add(new MeteorToast(Items.BARRIER, "New Rejects update.", "Download it from Github", 8000));
    }

}
