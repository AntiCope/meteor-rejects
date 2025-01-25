package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.HandshakeC2SPacketAccessor;
import com.google.gson.Gson;
import com.mojang.authlib.properties.PropertyMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;

import java.util.List;

public class BungeeCordSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private static final Gson GSON = new Gson();

    private final Setting<Boolean> whitelist = sgGeneral.add(new BoolSetting.Builder()
            .name("whitelist")
            .description("Use whitelist.")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<String>> whitelistedServers = sgGeneral.add(new StringListSetting.Builder()
            .name("whitelisted-servers")
            .description("Will only work if you joined the servers above.")
            .visible(whitelist::get)
            .build()
    );

    private final Setting<Boolean> spoofProfile = sgGeneral.add(new BoolSetting.Builder()
            .name("spoof-profile")
            .description("Spoof account profile.")
            .defaultValue(false)
            .build()
    );

    private final Setting<String> forwardedIP = sgGeneral.add(new StringSetting.Builder()
            .name("forwarded-IP")
            .description("The forwarded IP address.")
            .defaultValue("127.0.0.1")
            .build()
    );

    public BungeeCordSpoof() {
        super(MeteorRejectsAddon.CATEGORY, "bungeeCord-spoof", "Let you join BungeeCord servers, useful when bypassing proxies.");
        runInMainMenu = true;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof HandshakeC2SPacket packet && packet.intendedState() == ConnectionIntent.LOGIN) {
            if (whitelist.get() && !whitelistedServers.get().contains(Utils.getWorldName())) return;
            String address = packet.address() + "\0" + forwardedIP + "\0" + mc.getSession().getUuidOrNull().toString().replace("-", "")
                    + (spoofProfile.get() ? getProperty() : "");
            ((HandshakeC2SPacketAccessor) (Object) packet).setAddress(address);
        }
    }

    private String getProperty() {
        PropertyMap propertyMap = mc.getGameProfile().getProperties();
        return "\0" + GSON.toJson(propertyMap.values().toArray());
    }
}
