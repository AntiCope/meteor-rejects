package anticope.rejects;

import anticope.rejects.gui.hud.RadarHud;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.utils.MeteorManagerKt;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeteorRejectsAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Rejects");
    public static final Category CATEGORY = new Category("Rejects", Items.BARRIER.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Rejects");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Rejects Addon");

        // Modules
        MeteorManagerKt.moduleRegister("anticope.rejects.modules");

        // Commands
        MeteorManagerKt.commandRegister("anticope.rejects.commands");

        // HUD
        Hud hud = Systems.get(Hud.class);
        hud.register(RadarHud.INFO);

        // Themes
        GuiThemes.add(new MeteorRoundedGuiTheme());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getWebsite() {
        return "https://github.com/AntiCope/meteor-rejects";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("AntiCope", "meteor-rejects");
    }

    @Override
    public String getCommit() {
        String commit = FabricLoader
                .getInstance()
                .getModContainer("meteor-rejects")
                .get().getMetadata()
                .getCustomValue("github:sha")
                .getAsString();
        LOG.info(String.format("Rejects version: %s", commit));
        return commit.isEmpty() ? null : commit.trim();
    }

    public String getPackage() {
        return "anticope.rejects";
    }
}
