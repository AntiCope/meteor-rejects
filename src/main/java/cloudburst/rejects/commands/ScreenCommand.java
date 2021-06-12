package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

import cloudburst.rejects.gui.screens.HeadScreen;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.gui.GuiThemes;
import minegame159.meteorclient.systems.commands.Command;

public class ScreenCommand extends Command {

    public ScreenCommand() {
        super("screen", "Displays different screens", "gui");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("heads").executes(ctx -> {
            MeteorClient.INSTANCE.screenToOpen = new HeadScreen(GuiThemes.get());

            return 1;
        }));
        
    }
    
    
}
