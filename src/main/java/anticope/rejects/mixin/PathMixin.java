package anticope.rejects.mixin;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public abstract class PathMixin {

    @Shadow
    @Mutable
    @Nullable
    private Path.DebugNodeInfo debugNodeInfos;

    @Shadow
    @Final
    private List<PathNode> nodes;

    @Shadow
    @Final
    private BlockPos target;

    @Inject(method = {"toBuf"}, at = {@At("HEAD")})
    private void toBuf(PacketByteBuf buf, CallbackInfo ci) {
        this.debugNodeInfos = new Path.DebugNodeInfo(this.nodes.stream().filter((pathNode) -> {
            return !pathNode.visited;
        }).toArray((x$0) -> {
            return new PathNode[x$0];
        }), this.nodes.stream().filter((pathNode) -> {
            return pathNode.visited;
        }).toArray((x$0) -> {
            return new PathNode[x$0];
        }), Set.of(new TargetPathNode(this.target.getX(), this.target.getY(), this.target.getZ())));
    }
}
