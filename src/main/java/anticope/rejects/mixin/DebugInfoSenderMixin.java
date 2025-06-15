package anticope.rejects.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.custom.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.*;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.Collectors;

// https://modrinth.com/mod/dev-tools-unlocker/versions - for good base
@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    @Shadow
    private static void sendToAll(ServerWorld world, CustomPayload payload) {
        throw new AssertionError();
    }

    @Shadow
    private static List<String> listMemories(LivingEntity entity, long currentTime) {
        throw new AssertionError();
    }

    @Inject(method = {"sendChunkWatchingChange"}, at = {@At("HEAD")})
    private static void sendChunkWatchingChange(ServerWorld world, ChunkPos pos, CallbackInfo ci) {
        sendToAll(world, new DebugWorldgenAttemptCustomPayload(pos.getStartPos().up(100), 1.0F, 1.0F, 1.0F, 1.0F, 1.0F));
    }

    @Inject(method = {"sendPoiAddition"}, at = {@At("HEAD")})
    private static void sendPoiAddition(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        world.getPointOfInterestStorage().getType(pos).ifPresent((registryEntry) -> {
            sendToAll(world, new DebugPoiAddedCustomPayload(pos, registryEntry.getIdAsString(), world.getPointOfInterestStorage().getFreeTickets(pos)));
        });
    }

    @Inject(method = {"sendPoiRemoval"}, at = {@At("HEAD")})
    private static void sendPoiRemoval(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        sendToAll(world, new DebugPoiRemovedCustomPayload(pos));
    }

    @Inject(method = {"sendPointOfInterest"}, at = {@At("HEAD")})
    private static void sendPointOfInterest(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        sendToAll(world, new DebugPoiTicketCountCustomPayload(pos, world.getPointOfInterestStorage().getFreeTickets(pos)));
    }

    @Inject(method = {"sendPoi"}, at = {@At("HEAD")})
    private static void sendPoi(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        Registry<Structure> structureRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(pos);

        for (RegistryEntry<Structure> entry : structureRegistry.iterateEntries(StructureTags.VILLAGE)) {
            if (!world.getStructureAccessor().getStructureStarts(chunkSectionPos, entry.value()).isEmpty()) {
                return;
            }
        }

        sendToAll(world, new DebugVillageSectionsCustomPayload(Set.of(chunkSectionPos), Set.of()));
    }

    private static void addPoi(Brain<?> brain, MemoryModuleType<GlobalPos> memoryModuleType, Set<BlockPos> set) {
        brain.getOptionalMemory(memoryModuleType)
                .map(GlobalPos::pos)
                .ifPresent(Objects.requireNonNull(set)::add);
    }

    @Inject(method = "sendPathfindingData", at = @At("HEAD"))
    private static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity, CallbackInfo ci) {
        if (path != null) {
            sendToAll((ServerWorld) world, new DebugPathCustomPayload(mob.getId(), path, nodeReachProximity));
        }}

    //TODO: Add change delay
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
    private static void sendBrainDebugData(LivingEntity livingEntity, CallbackInfo ci) {
        MobEntity entity = (MobEntity)livingEntity;
        ServerWorld serverWorld = (ServerWorld)entity.getWorld();

        List<String> gossips = new ArrayList<>();
        Set<BlockPos> pois = new HashSet<>();
        Set<BlockPos> potentialPois = new HashSet<>();

        String profession = "";
        String inventory = "";
        int xp = 0;
        int angerLevel = -1;
        boolean wantsGolem = false;

        if (entity instanceof WardenEntity wardenEntity) {
            angerLevel = wardenEntity.getAnger();
        } else if (entity instanceof VillagerEntity villager) {
            profession = villager.getVillagerData().getProfession().toString();
            xp = villager.getExperience();
            inventory = villager.getInventory().toString();
            wantsGolem = villager.canSummonGolem(serverWorld.getTime());

            villager.getGossip().getEntityReputationAssociatedGossips().forEach((uuid, associatedGossip) -> {
                Entity gossipEntity = serverWorld.getEntity(uuid);
                if (gossipEntity != null) {
                    String name = NameGenerator.name(gossipEntity);

                    associatedGossip.object2IntEntrySet().forEach(entry ->
                            gossips.add(name + ": " + entry.getKey().asString() + " " + entry.getValue())
                    );
                }
            });

            Brain<?> brain = villager.getBrain();
            addPoi(brain, MemoryModuleType.HOME, pois);
            addPoi(brain, MemoryModuleType.JOB_SITE, pois);
            addPoi(brain, MemoryModuleType.MEETING_POINT, pois);
            addPoi(brain, MemoryModuleType.HIDING_PLACE, pois);
            addPoi(brain, MemoryModuleType.POTENTIAL_JOB_SITE, potentialPois);
        }

        sendToAll(serverWorld, new DebugBrainCustomPayload(new DebugBrainCustomPayload.Brain(entity.getUuid(), entity.getId(), entity.getName().getString(), profession, xp, entity.getHealth(), entity.getMaxHealth(), entity.getPos(), inventory, entity.getNavigation().getCurrentPath(), wantsGolem, angerLevel, entity.getBrain().getPossibleActivities().stream().map(Activity::toString).toList(), entity.getBrain().getRunningTasks().stream().map(Task::getName).toList(), listMemories(entity, serverWorld.getTime()), gossips, pois, potentialPois)));
    }

    @Inject(method = "sendBeeDebugData", at = @At("HEAD"))
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        sendToAll((ServerWorld) bee.getWorld(), new DebugBeeCustomPayload(new DebugBeeCustomPayload.Bee(bee.getUuid(), bee.getId(), bee.getPos(), bee.getNavigation().getCurrentPath(), bee.getHivePos(), bee.getFlowerPos(), bee.getMoveGoalTicks(), bee.getGoalSelector().getGoals().stream().map((prioritizedGoal) -> prioritizedGoal.getGoal().toString()).collect(Collectors.toSet()), bee.getPossibleHives())));
    }

    @Inject(method = "sendBreezeDebugData", at = @At("HEAD"))
    private static void sendBreezeDebugData(BreezeEntity breeze, CallbackInfo ci) {
        sendToAll((ServerWorld) breeze.getWorld(), new DebugBreezeCustomPayload(new DebugBreezeCustomPayload.BreezeInfo(breeze.getUuid(), breeze.getId(), breeze.getTarget() == null ? null : breeze.getTarget().getId(), breeze.getBrain().getOptionalMemory(MemoryModuleType.BREEZE_JUMP_TARGET).orElse((null)))));
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

    @Inject(method = "sendBeehiveDebugData", at = @At("HEAD"))
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity, CallbackInfo ci) {
        sendToAll((ServerWorld) world, new DebugHiveCustomPayload(new DebugHiveCustomPayload.HiveInfo(pos, blockEntity.getType().toString(), blockEntity.getBeeCount(), blockEntity.getHoneyLevel(state), blockEntity.isSmoked())));
    }

}
