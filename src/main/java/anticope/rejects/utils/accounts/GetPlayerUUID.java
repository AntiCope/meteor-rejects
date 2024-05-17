package anticope.rejects.utils.accounts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GetPlayerUUID {

    public static UUID getUUID(String playerName) {
        // Thanks Bento
        try {
            Gson gsonReader = new Gson();
            JsonObject jsonObject = gsonReader.fromJson(
                    getURLContent("https://api.mojang.com/users/profiles/minecraft/" + playerName),
                    JsonObject.class);

            String userIdString = jsonObject.get("id").toString().replace("\"", "")
                    .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

            return UUID.fromString(userIdString);
        } catch (Exception ignored) {
            return UUID.randomUUID();
        }
    }

    private static String getURLContent(String requestedUrl) {
        String returnValue;

        try {
            URL url = new URL(requestedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            returnValue = br.lines().collect(Collectors.joining());
            br.close();
        } catch (Exception e) {
            returnValue = "";
        }

        return returnValue;
    }

}