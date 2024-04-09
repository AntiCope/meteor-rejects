package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
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
        if (event.packet instanceof GameJoinS2CPacket) {
            checkWarning();
            return;
        }
        if (signs.get() && event.packet instanceof UpdateSignC2SPacket packet) {
            for (int line = 0; line < packet.getText().length; line++) {
                packet.getText()[line] = packet.getText()[line]
                        .replaceAll("(?i)(?:&|(?<!§)§)([0-9A-Z])", "§§$1$1");
            }
        }
        if (books.get() && event.packet instanceof BookUpdateC2SPacket packet) {
            List<String> newPages = packet.getPages().stream().map(text ->
                    text.replaceAll("(?i)&([0-9A-Z])", "§$1")).toList();
            // BookUpdateC2SPacket.pages is final, so we need to create a new packet
            if (!packet.getPages().equals(newPages)) {
                assert mc.getNetworkHandler() != null;
                mc.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(
                        packet.getSlot(), newPages, packet.getTitle()));
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
        MinecraftServer server = mc.player.getServer();
        if (server == null) return;
        String brand = server.getServerModName();
        if (brand == null) return;
        if (brand.contains("Paper")) warning("You are on a paper server. Color signs won't work here");
    }
}
