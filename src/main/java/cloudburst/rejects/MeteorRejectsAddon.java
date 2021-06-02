package cloudburst.rejects;

import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.systems.commands.Commands;
import minegame159.meteorclient.systems.modules.Category;
import minegame159.meteorclient.systems.modules.Modules;

import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cloudburst.rejects.commands.*;
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
		//modules.add(new AutoTNT());
		modules.add(new Confuse());
		modules.add(new InteractionMenu());
		modules.add(new Glide());
		modules.add(new Lavacast());
		modules.add(new NewChunks());
		modules.add(new ObsidianFarm());
		modules.add(new Rendering());
		modules.add(new SkeletonESP());
		modules.add(new SoundLocator());

		Commands commands = Commands.get();
		commands.add(new AntiAntiXrayCommand());
		commands.add(new BookDupeCommand());
		commands.add(new GiveCommand());
		commands.add(new SaveSkinCommand());
		commands.add(new TerrainExport());
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}
}
