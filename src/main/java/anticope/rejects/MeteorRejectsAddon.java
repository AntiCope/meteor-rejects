package anticope.rejects;

import anticope.rejects.commands.*;
import anticope.rejects.gui.hud.*;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.modules.*;
import anticope.rejects.modules.modifier.NoRenderModifier;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
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
        Modules modules = Modules.get();
        modules.add(new AntiBot());
        modules.add(new AntiSpawnpoint());
        modules.add(new AntiVanish());
        modules.add(new AutoBedTrap());
        modules.add(new AutoCraft());
        modules.add(new AutoExtinguish());
        modules.add(new AutoPot());
        modules.add(new AutoSoup());
        modules.add(new AutoTNT());
        modules.add(new AutoWither());
        modules.add(new BoatGlitch());
        modules.add(new BlockIn());
        modules.add(new BoatPhase());
        modules.add(new BonemealAura());
        modules.add(new Boost());
        modules.add(new ChatBot());
        modules.add(new ChestAura());
        modules.add(new ChorusExploit());
        modules.add(new ColorSigns());
        modules.add(new Confuse());
        modules.add(new CoordLogger());
        modules.add(new CustomPackets());
        modules.add(new ExtraElytra());
        modules.add(new GhostMode());
        modules.add(new Glide());
        modules.add(new InstaMine());
        modules.add(new ItemGenerator());
        modules.add(new InteractionMenu());
        modules.add(new Lavacast());
        modules.add(new NewChunks());
        modules.add(new ObsidianFarm());
        modules.add(new OreSim());
        modules.add(new PacketFly());
        modules.add(new Painter());
        modules.add(new Rendering());
        modules.add(new SkeletonESP());
        modules.add(new SoundLocator());
        modules.add(new TillAura());
        modules.add(new TreeAura());
        
        // Module modifications
        NoRenderModifier.init();
        
        // Commands
        Commands commands = Commands.get();
        commands.add(new CenterCommand());
        commands.add(new ClearChatCommand());
        commands.add(new GhostCommand());
        commands.add(new GiveCommand());
        commands.add(new SaveSkinCommand());
        commands.add(new SeedCommand());
        commands.add(new HeadsCommand());
        commands.add(new KickCommand());
        // commands.add(new LocateCommand());   I wish it was that simple -_-
        commands.add(new ServerCommand());
        commands.add(new PanicCommand());
        commands.add(new SetBlockCommand());
        commands.add(new SetVelocityCommand());
        commands.add(new TeleportCommand());
        commands.add(new TerrainExport());
        
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
