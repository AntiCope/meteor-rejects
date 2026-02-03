package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomPackets extends Module {
    private static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Type BADLION_MODS_TYPE = new TypeToken<Map<String, BadlionMod>>() {
    }.getType();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBadlion = settings.createGroup("Bad Lion");


    private final Setting<Boolean> unknownPackets = sgGeneral.add(new BoolSetting.Builder()
            .name("unknown-packets")
            .description("Whether to print unknown packets.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> mods = sgBadlion.add(new BoolSetting.Builder()
            .name("disallowed-mods")
            .description("Whether to print what badlion mods are disallowed.")
            .defaultValue(true)
            .build()
    );

    public CustomPackets() {
        super(MeteorRejectsAddon.CATEGORY, "custom-packets", "Handles different non-vanilla protocols.");
    }

    @EventHandler
    private void onCustomPayloadPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundCustomPayloadPacket packet) {
            if (packet.payload().type().toString().equals("badlion:mods")) {
                event.setCancelled(onBadlionModsPacket(packet));
            } else {
                onUnknownPacket(packet);
            }
        }
    }

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

    private void onUnknownPacket(ClientboundCustomPayloadPacket packet) {
        if (!unknownPackets.get()) return;
        MutableComponent text = Component.literal(packet.payload().type().toString());
        buffer.clear();
        text.setStyle(
            text.getStyle().withHoverEvent(new HoverEvent.ShowText(Component.literal(readString(buffer))))
        );
        info(text);
    }

    private boolean onBadlionModsPacket(ClientboundCustomPayloadPacket packet) {
        if (!mods.get()) return false;
        buffer.clear();
        String json = readString(buffer);
        Map<String, BadlionMod> mods = GSON_NON_PRETTY.fromJson(json, BADLION_MODS_TYPE);
        ChatUtils.sendMsg("Badlion", format("Mods", formatMods(mods)));
        return true;
    }

    private MutableComponent format(String type, MutableComponent message) {
        MutableComponent text = Component.literal(String.format("[%s%s%s]",
                ChatFormatting.AQUA,
                type,
                ChatFormatting.GRAY
        ));
        text.append(" ");
        text.append(message);
        return text;
    }

    private String readString(FriendlyByteBuf data) {
        return data.readCharSequence(
                data.readableBytes(),
                StandardCharsets.UTF_8
        ).toString();
    }

    private MutableComponent formatMods(Map<String, BadlionMod> mods) {
        MutableComponent text = Component.literal("Disallowed mods: \n");

        mods.forEach((name, data) -> {
            MutableComponent modLine = Component.literal(String.format("- %s%s%s ", ChatFormatting.YELLOW, name, ChatFormatting.GRAY));
            modLine.append(data.disabled ? "disabled" : "enabled");
            modLine.append("\n");
            if (data.extra_data != null) {
                modLine.setStyle(modLine.getStyle()
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(data.extra_data.toString()))));
            }
            text.append(modLine);
        });

        return text;
    }

    private static class BadlionMod {
        private boolean disabled;
        private JsonObject extra_data;
        private JsonObject settings;
    }
}
