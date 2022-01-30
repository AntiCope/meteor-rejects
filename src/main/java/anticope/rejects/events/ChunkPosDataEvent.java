package anticope.rejects.events;

import meteordevelopment.meteorclient.utils.misc.Pool;

public class ChunkPosDataEvent {
    private static final Pool<ChunkPosDataEvent> INSTANCE = new Pool<>(ChunkPosDataEvent::new);

    public int chunkX;
    public int chunkZ;

    public static ChunkPosDataEvent get(int x, int z) {
        ChunkPosDataEvent event = INSTANCE.get();
        event.chunkX = x;
        event.chunkZ = z;
        return event;
    }

    public static void returnChunkDataEvent(ChunkPosDataEvent event) {
        INSTANCE.free(event);
    }
}