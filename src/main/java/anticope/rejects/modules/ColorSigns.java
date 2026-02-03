package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class ColorSigns extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> signs = sgGeneral.add(new BoolSetting.Builder()
            .name("signs")
            .description("Allows you to use colors in signs.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> books = sgGeneral.add(new BoolSetting.Builder()
            .name("books")
            .description("Allows you to use colors in books.")
            .defaultValue(false)
            .build()
    );

    public ColorSigns() {
        super(MeteorRejectsAddon.CATEGORY, "color-signs", "Allows you to use colors on signs on NON-PAPER servers (use \"&\" for color symbols)");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ClientboundLoginPacket) {
            checkWarning();
            return;
        }
        if (signs.get() && event.packet instanceof ServerboundSignUpdatePacket packet) {
            for (int line = 0; line < packet.getLines().length; line++) {
                packet.getLines()[line] = packet.getLines()[line]
                        .replaceAll("(?i)(?:&|(?<!§)§)([0-9A-Z])", "§§$1$1");
            }
        }
        if (books.get() && event.packet instanceof ServerboundEditBookPacket packet) {
            List<String> newPages = packet.pages().stream().map(text ->
                    text.replaceAll("(?i)&([0-9A-Z])", "§$1")).toList();
            // BookUpdateC2SPacket.pages is final, so we need to create a new packet
            if (!packet.pages().equals(newPages)) {
                assert mc.getConnection() != null;
                mc.getConnection().send(new ServerboundEditBookPacket(
                        packet.slot(), newPages, packet.title()));
                event.cancel();
            }
        }
    }

    @Override
    public void onActivate() {
        super.onActivate();
        checkWarning();
    }

    private void checkWarning() {
        assert mc.player != null;
        MinecraftServer server = mc.player.level().getServer();
        if (server == null) return;
        String brand = server.getServerModName();
        if (brand == null) return;
        if (brand.contains("Paper")) warning("You are on a paper server. Color signs won't work here");
    }
}
