package anticope.rejects.commands;

import com.google.gson.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SaveSkinCommand extends Command {

    private final static SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(Text.literal("An exception occurred"));

    private final PointerBuffer filters;
    private final Gson GSON = new Gson();

    public SaveSkinCommand() {
        super("save-skin","Download a player's skin by name.", "skin","skinsteal");

        filters = BufferUtils.createPointerBuffer(1);

        ByteBuffer pngFilter = MemoryUtil.memASCII("*.png");

        filters.put(pngFilter);
        filters.rewind();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(ctx -> {
            UUID id = PlayerListEntryArgumentType.get(ctx).getProfile().getId();
            String path = TinyFileDialogs.tinyfd_saveFileDialog("Save image", null, filters, null);
            if (path == null) IO_EXCEPTION.create();
            if (path != null) {
                if (!path.endsWith(".png")) path += ".png";
                saveSkin(id.toString(),path);
            }
            
            return SINGLE_SUCCESS;
        }));
    }

    private void saveSkin(String uuid, String path) throws CommandSyntaxException {
        try {
            //going to explain what happens so I don't forget
            //request their minecraft profile, all so we can get a base64 encoded string that contains ANOTHER json that then has the skin URL
            String PROFILE_REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

            JsonObject object = Http.get(String.format(PROFILE_REQUEST_URL, uuid)).sendJson(JsonObject.class);
            //Get the properties array which has what we need
            JsonArray array = object.getAsJsonArray("properties");
            JsonObject property = array.get(0).getAsJsonObject();
            //value is what we grab but it's encoded so we have to decode it
            String base64String = property.get("value").getAsString();
            byte[] bs = Base64.decodeBase64(base64String);
            //Convert the response to json and pull the skin url from there
            String secondResponse = new String(bs, StandardCharsets.UTF_8);
            JsonObject finalResponseObject = GSON.fromJson(secondResponse, JsonObject.class);
            JsonObject texturesObject = finalResponseObject.getAsJsonObject("textures");
            JsonObject skinObj = texturesObject.getAsJsonObject("SKIN");
            String skinURL = skinObj.get("url").getAsString();

            InputStream in = new BufferedInputStream(new URL(skinURL).openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file.getPath());
            fos.write(response);
            fos.close();
        } catch (IOException | NullPointerException e) {
            throw IO_EXCEPTION.create();
        }
    }
}
