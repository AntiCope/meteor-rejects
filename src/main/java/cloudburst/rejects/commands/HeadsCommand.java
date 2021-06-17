package cloudburst.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

import cloudburst.rejects.gui.screens.HeadScreen;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HeadsCommand extends Command {

    public HeadsCommand() {
        super("heads", "Display heads gui");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            MeteorClient.screenToOpen = new HeadScreen(GuiThemes.get());
            return SINGLE_SUCCESS;
        });
        
    }
    
    
}
