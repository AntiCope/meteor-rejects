package cloudburst.rejects.mixin.meteor;

import minegame159.meteorclient.utils.network.Capes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.*;

@Mixin(Capes.class)
public interface CapesAccessor {

    @Accessor("OWNERS")
    public static Map<UUID, String> getOwners() {
        throw new AssertionError();
    }

    @Accessor("URLS")
    public static Map<String, String> getURLs() {
        throw new AssertionError();
    }
}
