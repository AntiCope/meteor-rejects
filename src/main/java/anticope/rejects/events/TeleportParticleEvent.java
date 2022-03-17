package anticope.rejects.events;

public class TeleportParticleEvent {
    private static final TeleportParticleEvent INSTANCE = new TeleportParticleEvent();

    public double x, y, z;

    public static TeleportParticleEvent get(double x, double y, double z) {
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}