package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ClearChatCommand extends Command {
    public ClearChatCommand() {
        super("clear-chat", "Clears your chat.", "clear", "cls");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            mc.gui.hud.getChat().clearMessages(false);
            return SINGLE_SUCCESS;
        });
    }
}