package cloudburst.rejects;

import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.commands.Commands;
import minegame159.meteorclient.modules.Modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cloudburst.rejects.commands.*;
import cloudburst.rejects.modules.*;

public class MeteorRejectsAddon extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();

	@Override
	public void onInitialize() {
		LOG.info("Initializing Meteor Rejects Addon");

		Modules.get().add(new AutoMountBypassDupe());
		Modules.get().add(new AutoPot());
		Modules.get().add(new RenderInvisible());

		Commands.get().add(new GiveCommand());
		Commands.get().add(new Notebot());
	}
}
