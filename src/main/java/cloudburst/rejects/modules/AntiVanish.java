package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AntiVanish extends Module {
    
    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    private long lastTick = 0;
    
    public AntiVanish() {
        super(MeteorRejectsAddon.CATEGORY, "anti-vanish", "Notifies user when a admin uses /vanish");
    }
    
    @Override
    public void onDeactivate() {
        toLookup.clear();
    }
    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket) {
            PlayerListS2CPacket packet = (PlayerListS2CPacket) event.packet;
            if (packet.getAction() == PlayerListS2CPacket.Action.UPDATE_LATENCY) {
                try {
                    for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                        if (mc.getNetworkHandler().getPlayerListEntry(entry.getProfile().getId()) != null)
                            continue;
                        toLookup.add(entry.getProfile().getId());
                    }
                } catch (Exception ignore) {}
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        long time = mc.world.getTime();
        UUID lookup;

        if (Math.abs(lastTick - time) > 100 && (lookup = toLookup.poll()) != null) {
            try {
                String name = getPlayerNameFromUUID(lookup);
                if (name != null) {
                   warning(name + " has gone into vanish.");
                }
            } catch (Exception ignore) {}
            lastTick = time;
        }
    }

    public String getPlayerNameFromUUID(UUID id) {
        try {
            final NameLookup process = new NameLookup(id, mc);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class NameLookup implements Runnable {
        private final String uuidstr;
        private final UUID uuid;
        private final MinecraftClient mc;
        private volatile String name;

        public NameLookup(final String input, MinecraftClient mc) {
            this.uuidstr = input;
            this.uuid = UUID.fromString(input);
            this.mc = mc;
        }

        public NameLookup(final UUID input, MinecraftClient mc) {
            this.uuid = input;
            this.uuidstr = input.toString();
            this.mc = mc;
        }

        @Override
        public void run() {
            this.name = this.lookUpName();
        }

        public String lookUpName() {
            PlayerEntity player = null;
            if (mc.world != null) {
                player = mc.world.getPlayerByUuid(uuid);
            }
            if (player == null) {
                final String url = "https://api.mojang.com/user/profiles/" + this.uuidstr.replace("-", "") + "/names";
                try {
                    final JsonParser parser = new JsonParser();
                    final String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                    final JsonElement nameElement = parser.parse(nameJson);
                    final JsonArray nameArray = nameElement.getAsJsonArray();
                    final String playerSlot = nameArray.get(nameArray.size() - 1).toString();
                    final JsonObject nameObject = parser.parse(playerSlot).getAsJsonObject();
                    return nameObject.get("name").toString();
                } catch (Exception e) {
                    return null;
                }
            }
            return player.getName().asString();
        }

        public String getName() {
            return this.name;
        }
    }
}


