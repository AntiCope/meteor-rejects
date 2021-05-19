package cloudburst.rejects.commands;

import cloudburst.rejects.aax.AntiAntiXray;
import cloudburst.rejects.aax.Etc.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minegame159.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AntiAntiXrayCommand extends Command {
    public AntiAntiXrayCommand() {
        super("anti-anti-xray", "Circumvent antixray plugin", "aax");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            AntiAntiXray.scanForFake(Config.rad, Config.delay);
            info("Refreshing blocks");
            return SINGLE_SUCCESS;
        });
    }
}
