package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seeds;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PostInit;

import java.util.Random;

public class RejectsUtils {

    @PostInit
    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("saving seeds...");
            RejectsConfig.get().save(MeteorClient.FOLDER);
            Seeds.get().save(MeteorClient.FOLDER);
        }));
    }

    public static String getModuleName(String name) {
        int dupe = 0;
        for (Module module : Modules.get().getAll()) {
            if (module.name.equals(name)) {
                dupe++;
                break;
            }
        }
        return dupe == 0 ? name : getModuleName(name + "*".repeat(dupe));
    }

    public static String getRandomPassword(int num) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(63);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
