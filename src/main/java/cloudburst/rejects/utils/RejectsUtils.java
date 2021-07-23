package cloudburst.rejects.utils;

import meteordevelopment.meteorclient.MeteorClient;

import java.util.Timer;
import java.util.TimerTask;

public class RejectsUtils {
    public static int CPS = 0;

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            RejectsConfig.get().save(MeteorClient.FOLDER);
        }));

        new Timer().scheduleAtFixedRate(newTimerTaskFromLambda(() -> CPS = 0), 0, 1000);
    }

    public static TimerTask newTimerTaskFromLambda(Runnable runnable)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
    }
}
