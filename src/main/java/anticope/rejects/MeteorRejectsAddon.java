package anticope.rejects;

import anticope.rejects.commands.*;
import anticope.rejects.gui.hud.RadarHud;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.modules.*;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
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
        Modules modules = Modules.get();
        modules.add(new AimAssist());
        modules.add(new AntiBot());
        modules.add(new AntiCrash());
        modules.add(new AntiSpawnpoint());
        modules.add(new AntiVanish());
        modules.add(new ArrowDmg());
        modules.add(new AutoBedTrap());
        modules.add(new AutoCraft());
        modules.add(new AutoExtinguish());
        modules.add(new AutoFarm());
        modules.add(new AutoGrind());
        modules.add(new AutoLogin());
        modules.add(new AutoPot());
        modules.add(new AutoSoup());
        modules.add(new AutoTorch());
        modules.add(new AutoTNT());
        modules.add(new AutoWither());
        modules.add(new BoatGlitch());
        modules.add(new BlockIn());
        modules.add(new BoatPhase());
        modules.add(new Boost());
        modules.add(new BungeeCordSpoof());
        modules.add(new ChatBot());
        modules.add(new ChestAura());
        modules.add(new ChorusExploit());
        modules.add(new ColorSigns());
        modules.add(new Confuse());
        modules.add(new CoordLogger());
        modules.add(new CustomPackets());
        modules.add(new ExtraElytra());
        modules.add(new FullFlight());
        modules.add(new GamemodeNotifier());
        modules.add(new GhostMode());
        modules.add(new Glide());
        modules.add(new ItemGenerator());
        modules.add(new InteractionMenu());
        modules.add(new Jetpack());
        modules.add(new KnockbackPlus());
        modules.add(new LawnBot());
        modules.add(new Lavacast());
        modules.add(new MossBot());
        modules.add(new NewChunks());
        modules.add(new NoJumpDelay());
        modules.add(new ObsidianFarm());
        modules.add(new OreSim());
        modules.add(new PacketFly());
        modules.add(new Painter());
        modules.add(new Rendering());
        modules.add(new RoboWalk());
        modules.add(new ShieldBypass());
        modules.add(new SilentDisconnect());
        modules.add(new SkeletonESP());
        modules.add(new SoundLocator());
        modules.add(new TreeAura());
        modules.add(new VehicleOneHit());
        modules.add(new AutoEnchant());
        modules.add(new AutoRename());

        // Commands
        Commands.add(new CenterCommand());
        Commands.add(new ClearChatCommand());
        Commands.add(new GhostCommand());
        Commands.add(new GiveCommand());
        Commands.add(new HeadsCommand());
        Commands.add(new KickCommand());
        Commands.add(new LocateCommand());
        Commands.add(new PanicCommand());
        Commands.add(new ReconnectCommand());
        Commands.add(new ServerCommand());
        Commands.add(new SaveSkinCommand());
        Commands.add(new SeedCommand());
        Commands.add(new SetBlockCommand());
        Commands.add(new SetVelocityCommand());
        Commands.add(new TeleportCommand());
        Commands.add(new TerrainExport());

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
