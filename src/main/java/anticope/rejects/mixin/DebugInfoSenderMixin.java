package anticope.rejects.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.custom.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    @Shadow
    private static void sendToAll(ServerWorld world, CustomPayload payload) {
        throw new AssertionError();
    }

    //TODO: Fix this
    @Inject(method = "sendPathfindingData", at = @At("HEAD"))
    private static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity, CallbackInfo ci) {
        //sendToAll((ServerWorld) world, new DebugPathCustomPayload(mob.getId(), path, nodeReachProximity));
    }

    //TODO: Change delay
    @Inject(method = "sendNeighborUpdate", at = @At("HEAD"))
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo ci) {
        sendToAll((ServerWorld) world, new DebugNeighborsUpdateCustomPayload(world.getTime(), pos));
    }

    //TODO: fix
    @Inject(method = "sendStructureStart", at = @At("HEAD"))
    private static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart, CallbackInfo ci) {
        List<DebugStructuresCustomPayload.Piece> pieces = new ArrayList<>();
        ServerWorld serverWorld = world.toServerWorld();

        for (int i = 0; i < structureStart.getChildren().size(); i++) {
            StructurePiece piece = structureStart.getChildren().get(i);
            pieces.add(new DebugStructuresCustomPayload.Piece(piece.getBoundingBox(), false));
        }

        sendToAll(serverWorld, new DebugStructuresCustomPayload(serverWorld.getRegistryKey(), structureStart.getBoundingBox(), pieces));
    }

    @Inject(method = "sendGoalSelector", at = @At("HEAD"))
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo ci) {
        List<DebugGoalSelectorCustomPayload.Goal> goals = ((MobEntityAccessor)mob).getGoalSelector().getGoals().stream().map((goal) ->
                new DebugGoalSelectorCustomPayload.Goal(goal.getPriority(), goal.isRunning(), goal.getGoal().toString())).toList();

        sendToAll((ServerWorld) world, new DebugGoalSelectorCustomPayload(mob.getId(), mob.getBlockPos(), goals));
    }

    @Inject(method = "sendRaids", at = @At("HEAD"))
    private static void sendRaids(ServerWorld server, Collection<Raid> raids, CallbackInfo ci) {
        sendToAll(server, new DebugRaidsCustomPayload(raids.stream().map(Raid::getCenter).toList()));
    }

    @Inject(method = "sendBrainDebugData", at = @At("HEAD"))
    private static void sendBrainDebugData(LivingEntity living, CallbackInfo ci) {

    }

    /*@Inject(method = "sendBeeDebugData", at = @At("HEAD"))
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        sendToAll((ServerWorld) bee.getWorld(), new DebugBeeCustomPayload(new DebugBeeCustomPayload.Bee(bee.getUuid(), bee.getId(), bee.getPos(), bee.getNavigation().getCurrentPath(), bee.getHivePos(), bee.getFlowerPos(), bee.getMoveGoalTicks(), (Set)bee.getGoalSelector().getGoals().stream().map((prioritizedGoal) -> {
            return prioritizedGoal.getGoal().toString();
        }).collect(Collectors.toSet()), bee.getPossibleHives())));
    }*/

    @Inject(method = "sendBreezeDebugData", at = @At("HEAD"))
    private static void sendBreezeDebugData(BreezeEntity breeze, CallbackInfo ci) {
        sendToAll((ServerWorld) breeze.getWorld(), new DebugBreezeCustomPayload(new DebugBreezeCustomPayload.BreezeInfo(breeze.getUuid(), breeze.getId(), breeze.getTarget() == null ? null : breeze.getTarget().getId(), (BlockPos)breeze.getBrain().getOptionalMemory(MemoryModuleType.BREEZE_JUMP_TARGET).orElse((null)))));
    }

    @Inject(method = "sendGameEvent", at = @At("HEAD"))
    private static void sendGameEvent(World world, RegistryEntry<GameEvent> event, Vec3d pos, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            event.getKey().ifPresent((key) -> {
                sendToAll(serverWorld, new DebugGameEventCustomPayload(key, pos));
            });
        }
    }

    @Inject(method = "sendGameEventListener", at = @At("HEAD"))
    private static void sendGameEventListener(World world, GameEventListener eventListener, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            sendToAll(serverWorld, new DebugGameEventListenersCustomPayload(eventListener.getPositionSource(), eventListener.getRange()));
        }
    }

    /*
    public static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
    }
     */

    //gametest
    //poi

}
