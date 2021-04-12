package cloudburst.rejects;

import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.systems.commands.Commands;
import minegame159.meteorclient.systems.modules.Modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cloudburst.rejects.commands.*;
import cloudburst.rejects.modules.*;

public class MeteorRejectsAddon extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();

	@Override
	public void onInitialize() {
		LOG.info("Initializing Meteor Rejects Addon");

		Modules modules = Modules.get();
		modules.add(new AutoPot());
		modules.add(new Confuse());
		modules.add(new Glide());
		modules.add(new Lavacast());
		modules.add(new RenderInvisible());
		modules.add(new SoundLocator());

		Commands commands = Commands.get();
		commands.add(new AntiAntiXrayCommand());
		commands.add(new BookDupeCommand());
		commands.add(new GiveCommand());
	}
}
