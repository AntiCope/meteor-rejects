package cloudburst.rejects;

import cloudburst.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.gui.GuiThemes;
import minegame159.meteorclient.systems.commands.Commands;
import minegame159.meteorclient.systems.modules.Category;
import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.hud.HUD;

import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cloudburst.rejects.commands.*;
import cloudburst.rejects.gui.hud.*;
import cloudburst.rejects.modules.*;

public class MeteorRejectsAddon extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();
	public static final Category CATEGORY = new Category("Rejects", Items.PODZOL.getDefaultStack());

	@Override
	public void onInitialize() {
		LOG.info("Initializing Meteor Rejects Addon");

		Modules modules = Modules.get();
		modules.add(new AntiBot());
		modules.add(new AntiSpawnpoint());
		modules.add(new AntiVanish());
		modules.add(new Auto32K());
		modules.add(new AutoBedTrap());
		modules.add(new AutoExtinguish());
		modules.add(new AutoHighway());
		modules.add(new AutoPot());
		modules.add(new AutoTNT());
		modules.add(new AutoWither());
		modules.add(new BoatGlitch());
		modules.add(new BoatPhase());
		modules.add(new ColorSigns());
		modules.add(new Confuse());
		modules.add(new Gravity());
		modules.add(new InteractionMenu());
		modules.add(new Glide());
		modules.add(new Lavacast());
		modules.add(new NewChunks());
		modules.add(new NoInteract());
		modules.add(new ObsidianFarm());
		modules.add(new PacketFly());
		modules.add(new Painter());
		modules.add(new Rendering());
		modules.add(new SkeletonESP());
		modules.add(new SoundLocator());
		modules.add(new SpawnProofer());

		Commands commands = Commands.get();
		commands.add(new AntiAntiXrayCommand());
		commands.add(new GiveCommand());
		commands.add(new SaveSkinCommand());
		commands.add(new ServerCommand());
		commands.add(new SetBlockCommand());
		commands.add(new TeleportCommand());
		commands.add(new TerrainExport());

		HUD hud = modules.get(HUD.class);
		hud.elements.add(new AppleHud(hud));
		hud.elements.add(new CrystalHud(hud));
		hud.elements.add(new ExpHud(hud));

		GuiThemes.add(new MeteorRoundedGuiTheme());
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}
}
