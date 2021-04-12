package cloudburst.rejects.aax;

import java.util.ArrayList;
import java.util.List;

import cloudburst.rejects.aax.Etc.*;

public class AntiAntiXray {
    public static List<RefreshingJob> jobs = new ArrayList<>();

    public static void scanForFake(int rad, long delayInMS) {
        RefreshingJob rfj = new RefreshingJob(new Runner(rad, delayInMS));
        jobs.add(rfj);
    }
    
}