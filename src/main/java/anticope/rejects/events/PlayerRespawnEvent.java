package anticope.rejects.events;

public class PlayerRespawnEvent {
    private static final PlayerRespawnEvent INSTANCE = new PlayerRespawnEvent();

    public static PlayerRespawnEvent get() {
        return INSTANCE;
    }
}