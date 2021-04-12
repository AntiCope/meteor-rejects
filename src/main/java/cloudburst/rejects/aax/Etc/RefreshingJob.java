package cloudburst.rejects.aax.Etc;

public class RefreshingJob {
    public Runner refresher;
    public Thread runner;
    public boolean done = false;
    
    public RefreshingJob(Runner refresher) {
        this.refresher = refresher;
        this.runner = new Thread(refresher);
        this.runner.start();

    }

    public void cancel() {
        refresher.isRunning = false;
        done = true;
    }
}