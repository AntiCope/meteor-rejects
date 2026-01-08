package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.commands.SharedSuggestionProvider;

public class ReconnectCommand extends Command {
    public ReconnectCommand() {
        super("reconnect", "Reconnects server.");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            ServerData info = mc.isLocalServer() ? null : mc.getCurrentServer();
            if (info != null) {
                mc.level.disconnect(net.minecraft.network.chat.Component.literal("Reconnecting"));
                ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), mc,
                        ServerAddress.parseString(info.ip), info, false, null);
            }
            return SINGLE_SUCCESS;
        });
    }
}
