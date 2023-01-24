package anticope.rejects.utils;

import com.google.gson.JsonArray;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class NameLookup implements Runnable {
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
        name = this.lookUpName();
    }

    public String lookUpName() {
        PlayerEntity player = null;
        if (mc.world != null) {
            player = mc.world.getPlayerByUuid(uuid);
        }
        if (player == null) {
            final String url = "https://api.mojang.com/user/profiles/" + uuidstr.replace("-", "") + "/names";
            try {
                JsonArray res = Http.get(url).sendJson(JsonArray.class);
                return res.get(res.size() - 1).getAsJsonObject().get("name").getAsString();
            } catch (Exception e) {
                return uuidstr;
            }
        }
        return player.getName().getString();
    }

    public String getName() {
        return this.name;
    }
}
