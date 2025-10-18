package anticope.rejects.utils;

import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {
    @SuppressWarnings("UnstableApiUsage")
    public static JsonObject getJsonObject(String url) {
        try {
            JsonElement element = JsonParser.parseString(Resources.toString(new URL(url), StandardCharsets.UTF_8));

            return element == null ? null : element.getAsJsonObject();
        } catch (IOException | IllegalStateException exception) {
            return null;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static JsonArray getJsonArray(String url) {
        try {
            JsonElement element = JsonParser.parseString(Resources.toString(new URL(url), StandardCharsets.UTF_8));

            return element == null ? null : element.getAsJsonArray();
        } catch (IOException | IllegalStateException exception) {
            return null;
        }
    }
}