package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seeds;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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

    public static boolean inFov(Entity entity, double fov) {
        float[] angle = PlayerUtils.calculateAngle(entity.getBoundingBox().getCenter());
        double xDist = MathHelper.angleBetween(angle[0], mc.player.getYaw());
        double yDist = MathHelper.angleBetween(angle[1], mc.player.getPitch());
        double angleDistance = Math.hypot(xDist, yDist);
        return angleDistance <= fov;
    }
}
