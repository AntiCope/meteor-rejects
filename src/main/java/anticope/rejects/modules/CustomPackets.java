package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.CustomPayloadEvent;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomPackets extends Module {
    private static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Type BADLION_MODS_TYPE = new TypeToken<Map<String, BadlionMod>>() {}.getType();

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
    private void onCustomPayloadPacket(CustomPayloadEvent event) {
        switch (event.packet.getChannel().toString()) {
            case "badlion:mods" -> onBadlionModsPacket(event);
            default -> onUnknownPacket(event);
        }
    }

    private void onUnknownPacket(CustomPayloadEvent event) {
        if (!unknownPackets.get()) return;
        MutableText text = Text.literal(event.packet.getChannel().toString());
        text.setStyle(text.getStyle()
        .withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Text.literal(readString(event.packet.getData()))
        )));
        info(text);
    }

    private void onBadlionModsPacket(CustomPayloadEvent event) {
        if (!mods.get()) return;
        String json = readString(event.packet.getData());
        Map<String, BadlionMod> mods = GSON_NON_PRETTY.fromJson(json, BADLION_MODS_TYPE);
        ChatUtils.sendMsg("Badlion", format("Mods", formatMods(mods)));
        event.cancel();
    }

    private MutableText format(String type, MutableText message) {
        MutableText text = Text.literal(String.format("[%s%s%s]",
                Formatting.AQUA,
                type,
                Formatting.GRAY
        ));
        text.append(" ");
        text.append(message);
        return text;
    }

    private String readString(PacketByteBuf data) {
        return data.readCharSequence(
                data.readableBytes(),
                StandardCharsets.UTF_8
        ).toString();
    }

    private MutableText formatMods(Map<String, BadlionMod> mods) {
        MutableText text = Text.literal("Disallowed mods: \n");

        mods.forEach((name, data) -> {
            MutableText modLine = Text.literal(String.format("- %s%s%s ", Formatting.YELLOW, name, Formatting.GRAY));
            modLine.append(data.disabled ? "disabled" : "enabled");
            modLine.append("\n");
            if (data.extra_data != null) {
                modLine.setStyle(modLine.getStyle()
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Text.literal(data.extra_data.toString())
                        )));
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
