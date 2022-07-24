package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seeds;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PostInit;
public class RejectsUtils {

    @PostInit
    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("saving seeds...");
            RejectsConfig.get().save(MeteorClient.FOLDER);
            Seeds.get().save(MeteorClient.FOLDER);
        }));
    }
}
