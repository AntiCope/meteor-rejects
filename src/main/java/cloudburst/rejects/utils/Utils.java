package cloudburst.rejects.utils;

import java.util.Timer;
import java.util.TimerTask;

public class Utils {
    public static int CPS = 0;

    public static void init() {
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
