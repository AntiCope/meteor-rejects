// Credit to https://github.com/IAFEnvoy/AccountSwitcher

package anticope.rejects.utils.accounts;

import com.google.gson.*;
import com.mojang.authlib.Environment;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CustomYggdrasilLogin {
    public static Environment localYggdrasilApi = new Environment("/authserver", "/sessionserver", "/minecraftservices", "Custom-Yggdrasil");

    public static Session login(String name, String password, String server) throws AuthenticationException {
        try {
            String url = server + "/authserver/authenticate";
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);

            JsonObject root = new JsonObject();
            root.add("agent", agent);
            root.addProperty("username", name);
            root.addProperty("password", password);

            String data = Http.post(url).bodyJson(root).sendString();
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            if (json.has("error")) {
                throw new AuthenticationException(json.get("errorMessage").getAsString());
            }
            String token = json.get("accessToken").getAsString();
            UUID uuid = UUID.fromString(json.get("selectedProfile").getAsJsonObject().get("id").getAsString());
            String username = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();
            return new Session(username, uuid, token, Optional.empty(), Optional.empty());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public static class LocalYggdrasilMinecraftSessionService extends YggdrasilMinecraftSessionService {
        private static final Logger LOGGER = LogManager.getLogger();
        private final ServicesKeyInfo publicKey;
        private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        public LocalYggdrasilMinecraftSessionService(YggdrasilAuthenticationService service, String serverUrl) {
            super(service.getServicesKeySet(), mc.getNetworkProxy(), localYggdrasilApi);
            String data = Http.get(serverUrl).sendString();
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            this.publicKey = getPublicKey(json.get("signaturePublickey").getAsString());
        }

        private static ServicesKeyInfo getPublicKey(String key) {
            key = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            try {
                byte[] byteKey = Base64.getDecoder().decode(key.replace("\n", ""));
                return YggdrasilServicesKeyInfo.parse(byteKey);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return null;
        }

        private SignatureState getPropertySignatureState(final Property property) {
            if (!property.hasSignature()) {
                return SignatureState.UNSIGNED;
            }
            if (!publicKey.validateProperty(property)) {
                return SignatureState.INVALID;
            }
            return SignatureState.SIGNED;
        }

        @Override
        public MinecraftProfileTextures unpackTextures(final Property packedTextures) {
            final String value = packedTextures.value();
            final SignatureState signatureState =  getPropertySignatureState(packedTextures);

            final MinecraftTexturesPayload result;
            try {
                final String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                result = gson.fromJson(json, MinecraftTexturesPayload.class);
            } catch (final JsonParseException | IllegalArgumentException e) {
                LOGGER.error("Could not decode textures payload", e);
                return MinecraftProfileTextures.EMPTY;
            }

            if (result == null || result.textures() == null || result.textures().isEmpty()) {
                return MinecraftProfileTextures.EMPTY;
            }

            final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = result.textures();

            return new MinecraftProfileTextures(
                    textures.get(MinecraftProfileTexture.Type.SKIN),
                    textures.get(MinecraftProfileTexture.Type.CAPE),
                    textures.get(MinecraftProfileTexture.Type.ELYTRA),
                    signatureState
            );
        }
    }

    public static class LocalYggdrasilAuthenticationService extends YggdrasilAuthenticationService {
        public final String server;

        public LocalYggdrasilAuthenticationService(Proxy proxy, String server) {
            super(proxy, localYggdrasilApi);
            this.server = server;
        }
    }

}
