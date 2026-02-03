package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;

public class ClearChatCommand extends Command {
    public ClearChatCommand() {
        super("clear-chat", "Clears your chat.", "clear", "cls");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            mc.gui.getChat().clearMessages(false);
            return SINGLE_SUCCESS;
        });
    }
}