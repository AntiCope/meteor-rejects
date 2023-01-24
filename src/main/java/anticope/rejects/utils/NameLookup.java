package anticope.rejects.utils;

import com.google.gson.JsonArray;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;
import java.util.function.Consumer;

public class NameLookup implements Runnable {
    private final String uuidString;
    private final UUID uuid;
    private final MinecraftClient mc;
    private final Consumer<String> callback;

    public NameLookup(UUID input, MinecraftClient mc, Consumer<String> callback) {
        this.uuid = input;
        this.uuidString = input.toString();
        this.mc = mc;
        this.callback = callback;
    }

    @Override
    public void run() {
        callback.accept(lookUpName());
    }

    public String lookUpName() {
        PlayerEntity player = null;
        if (mc.world != null) {
            player = mc.world.getPlayerByUuid(uuid);
        }
        if (player == null) {
            final String url = "https://api.mojang.com/user/profiles/" + uuidString.replace("-", "") + "/names";
            try {
                JsonArray res = Http.get(url).sendJson(JsonArray.class);
                return res.get(res.size() - 1).getAsJsonObject().get("name").getAsString();
            } catch (Exception e) {
                return uuidString;
            }
        }
        return player.getName().getString();
    }
}
