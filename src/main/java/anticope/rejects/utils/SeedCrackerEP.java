package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seeds;
import kaptainwutax.seedcrackerX.api.SeedCrackerAPI;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class SeedCrackerEP implements SeedCrackerAPI {
    @Override
    public void pushWorldSeed(long seed) {
        Seeds.get().setSeed(String.format("%d", seed));
        ChatUtils.info("Seed", "Added seed from SeedCrackerX");
    }
}
