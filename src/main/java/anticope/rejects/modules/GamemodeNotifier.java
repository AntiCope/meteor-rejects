package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.GameModeListSetting;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.GameType;
import java.util.List;

public class GamemodeNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<List<GameType>> gamemodes = sgGeneral.add(new GameModeListSetting.Builder()
            .name("gamemode")
            .description("Which gamemodes to notify.")
            .build()
    );

    public GamemodeNotifier() {
        super(MeteorRejectsAddon.CATEGORY, "gamemode-notifier", "Notifies user a player's gamemode was changed.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundPlayerInfoUpdatePacket packet) {
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
                if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)) continue;
                PlayerInfo entry1 = mc.getConnection().getPlayerInfo(entry.profileId());
                if (entry1 == null) continue;
                GameType gameMode = entry.gameMode();
                if (entry1.getGameMode() != gameMode) {
                    if (!gamemodes.get().contains(gameMode)) continue;
                    info("Player %s changed gamemode to %s", entry1.getProfile().name(), entry.gameMode());
                }
            }
        }
    }
}


