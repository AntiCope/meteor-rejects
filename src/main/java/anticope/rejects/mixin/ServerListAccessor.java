package anticope.rejects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

@Mixin(ServerList.class)
public interface ServerListAccessor {
    @Accessor
    List<ServerData> getServerList();
}
