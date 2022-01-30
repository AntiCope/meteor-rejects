package anticope.rejects;

import anticope.rejects.commands.*;
import anticope.rejects.gui.hud.*;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.modules.*;
import anticope.rejects.modules.modifier.NoRenderModifier;
import anticope.rejects.utils.GiveUtils;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class MeteorRejectsAddon extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    public static final Category CATEGORY = new Category("Rejects", Items.BARRIER.getDefaultStack());
    
    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Rejects Addon");
        
        MeteorClient.EVENT_BUS.registerLambdaFactory("anticope.rejects", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        
        RejectsUtils.init();
        GiveUtils.init();
        
        // Modules
        Modules modules = Modules.get();
        modules.add(new AntiBot());
        modules.add(new AntiSpawnpoint());
        modules.add(new AntiVanish());
        modules.add(new Auto32K());
        modules.add(new AutoBedTrap());
        modules.add(new AutoCraft());
        modules.add(new AutoExtinguish());
        modules.add(new AutoEz());
        modules.add(new AutoPot());
        modules.add(new AutoTNT());
        modules.add(new AutoWither());
        modules.add(new BedrockWalk());
        modules.add(new BoatGlitch());
        modules.add(new BlockIn());
        modules.add(new BoatPhase());
        modules.add(new Boost());
        modules.add(new ChatBot());
        modules.add(new ColorSigns());
        modules.add(new Confuse());
        modules.add(new CoordLogger());
        modules.add(new CustomPackets());
        modules.add(new GhostMode());
        modules.add(new InteractionMenu());
        modules.add(new Lavacast());
        modules.add(new NewChunks());
        modules.add(new ObsidianFarm());
        modules.add(new OreSim());
        modules.add(new PacketFly());
        modules.add(new Painter());
        modules.add(new Prone());
        modules.add(new Rendering());
        modules.add(new SkeletonESP());
        modules.add(new SoundLocator());
        
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
        HUD hud = Systems.get(HUD.class);
        hud.elements.add(new BaritoneHud(hud));
        hud.elements.add(new CpsHud(hud));
        
        // Themes
        GuiThemes.add(new MeteorRoundedGuiTheme());
    }
    
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
}
