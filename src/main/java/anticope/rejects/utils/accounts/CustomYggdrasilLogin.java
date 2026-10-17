// Credit to https://github.com/IAFEnvoy/AccountSwitcher

package anticope.rejects.utils.accounts;

import com.google.gson.*;
import com.mojang.authlib.Environment;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import com.mojang.authlib.services.MinecraftServicesKeyInfo;
import com.mojang.authlib.services.MinecraftServicesSessionService;
import com.mojang.authlib.services.ServicesKeyInfo;
import com.mojang.authlib.services.ServicesKeySet;
import com.mojang.authlib.services.ServicesKeyType;
import com.mojang.authlib.services.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CustomYggdrasilLogin {
    /**
     * authlib 10 (Minecraft 26.3) dropped the old Yggdrasil classes and resolves every
     * authentication server through its "Minecraft services" discovery document, so a custom
     * auth server is now described by an {@link Environment} pointing at that document.
     * The legacy layout exposed it as the "/minecraftservices" endpoint of the server root.
     */
    public static Environment environment(String server) {
        return new Environment(server + "/minecraftservices", "Custom-Yggdrasil");
    }

    public static MinecraftServicesDiscoveryService discoveryService(Proxy proxy, String server) {
        return MinecraftServicesDiscoveryService.create(proxy, true, environment(server));
    }

    public static User login(String name, String password, String server) throws AuthenticationException {
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
            return new User(username, uuid, token, Optional.empty(), Optional.empty());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    /** Restricts a service key set to the single profile key published by the custom server. */
    private static ServicesKeySet keySetOf(ServicesKeyInfo key) {
        return type -> type == ServicesKeyType.PROFILE_KEY ? List.of(key) : List.of();
    }

    public static class LocalYggdrasilMinecraftSessionService extends MinecraftServicesSessionService {
        private static final Logger LOGGER = LogManager.getLogger();
        private final ServicesKeyInfo publicKey;
        private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        public LocalYggdrasilMinecraftSessionService(MinecraftServicesDiscoveryService service, String serverUrl) {
            this(service, getPublicKey(serverUrl));
        }

        private LocalYggdrasilMinecraftSessionService(MinecraftServicesDiscoveryService service, ServicesKeyInfo publicKey) {
            super(publicKey == null ? service.getServicesKeySet() : keySetOf(publicKey), mc.getProxy(), service);
            this.publicKey = publicKey;
        }

        private static ServicesKeyInfo getPublicKey(String serverUrl) {
            String data = Http.get(serverUrl).sendString();
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            String key = json.get("signaturePublickey").getAsString();
            key = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            try {
                byte[] byteKey = Base64.getDecoder().decode(key.replace("\n", ""));
                return MinecraftServicesKeyInfo.parse(byteKey);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return null;
        }

        private SignatureState getPropertySignatureState(final Property property) {
            if (!property.hasSignature()) {
                return SignatureState.UNSIGNED;
            }
            if (publicKey == null || !publicKey.validateProperty(property)) {
                return SignatureState.INVALID;
            }
            return SignatureState.SIGNED;
        }

        @Override
        public MinecraftProfileTextures unpackTextures(final Property packedTextures) {
            final String value = packedTextures.value();
            final SignatureState signatureState = getPropertySignatureState(packedTextures);

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

}
