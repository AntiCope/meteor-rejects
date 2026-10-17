package anticope.rejects.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SaveSkinCommand extends Command {

    private final static SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(Component.literal("An exception occurred"));

    private final SDL_DialogFileFilter.Buffer filters;
    private final Gson GSON = new Gson();

    public SaveSkinCommand() {
        super("save-skin", "Download a player's skin by name.", "skin", "skinsteal");

        // 26.3: GLFW/tinyfd was replaced by SDL. The dialog is asynchronous, so the filter
        // strings have to stay allocated after this constructor returns.
        MemoryStack stack = MemoryStack.stackPush();
        filters = SDL_DialogFileFilter.malloc(1, stack);
        filters.name(stack.UTF8("PNG Files")).pattern(stack.UTF8("png"));
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(ctx -> {
            UUID id = PlayerListEntryArgumentType.get(ctx).getProfile().id();

            SDLDialog.SDL_ShowSaveFileDialog((_, file, _) -> {
                if (file == 0) return;

                long filePointer = MemoryUtil.memGetAddress(file);
                if (filePointer == 0) return;

                String path = MemoryUtil.memUTF8(filePointer);
                if (path.isBlank()) return;
                if (!path.endsWith(".png")) path += ".png";

                try {
                    saveSkin(id.toString(), path);
                } catch (CommandSyntaxException e) {
                    error("An exception occurred while saving the skin.");
                }
            }, mc.getWindow().handle(), 0, filters, "");

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
