package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seeds;
import meteordevelopment.meteorclient.MeteorClient;

public class RejectsUtils {
    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            RejectsConfig.get().save(MeteorClient.FOLDER);
            Seeds.get().save(MeteorClient.FOLDER);
        }));
    }
}
