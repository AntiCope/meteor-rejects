package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

import cloudburst.rejects.gui.screens.HeadScreen;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.gui.GuiThemes;
import minegame159.meteorclient.systems.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HeadsCommand extends Command {

    public HeadsCommand() {
        super("heads", "Display heads gui");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            MeteorClient.INSTANCE.screenToOpen = new HeadScreen(GuiThemes.get());
            return SINGLE_SUCCESS;
        });
        
    }
    
    
}
