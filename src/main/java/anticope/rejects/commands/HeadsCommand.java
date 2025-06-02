package anticope.rejects.commands;

import anticope.rejects.gui.screens.HeadScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.command.CommandSource;

public class HeadsCommand extends Command {

    public HeadsCommand() {
        super("heads", "Display heads gui");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            Utils.screenToOpen = new HeadScreen(GuiThemes.get());
            return SINGLE_SUCCESS;
        });
    }
}
