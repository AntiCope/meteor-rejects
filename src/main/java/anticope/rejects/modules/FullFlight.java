package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import com.google.common.collect.Streams;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.stream.Stream;

public class FullFlight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAntiKick = settings.createGroup("Anti Kick");

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Your speed when flying.")
            .defaultValue(0.3)
            .min(0.0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> verticalSpeedMatch = sgGeneral.add(new BoolSetting.Builder()
            .name("vertical-speed-match")
            .description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.")
            .defaultValue(false)
            .build()
    );

    private final Setting<AntiKickMode> antiKickMode = sgAntiKick.add(new EnumSetting.Builder<AntiKickMode>()
            .name("mode")
            .description("The mode for anti kick.")
            .defaultValue(AntiKickMode.PaperNew)
            .build()
    );

    public FullFlight() {
        super(MeteorRejectsAddon.CATEGORY, "fullflight", "FullFlight.");
    }

    private double calculateGround() {
        for (double ground = mc.player.getY(); ground > 0D; ground -= 0.05) {
            AABB box = mc.player.getBoundingBox();
            AABB adjustedBox = box.move(0, ground - mc.player.getY(), 0);

            Stream<VoxelShape> blockCollisions = Streams.stream(mc.level.getBlockCollisions(mc.player, adjustedBox));

            if (blockCollisions.findAny().isPresent()) return ground + 0.05;
        }

        return 0F;
    }

    // Copied from ServerPlayNetworkHandler#isEntityOnAir
    private boolean isEntityOnAir(Entity entity) {
        return mc.level.getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockBehaviour.BlockStateBase::isAir);
    }

    private int delayLeft = 20;
    private double lastPacketY = Double.MAX_VALUE;

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        } else return lastY - currentY < 0.03130D;
    }

    private ServerboundMovePlayerPacket antiKickPacket(ServerboundMovePlayerPacket packet, double currentY) {
        // maximum time we can be "floating" is 80 ticks, so 4 seconds max
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE &&
                shouldFlyDown(currentY, this.lastPacketY) && isEntityOnAir(mc.player)) {
            // actual check is for >= -0.03125D, but we have to do a bit more than that
            // due to the fact that it's a bigger or *equal* to, and not just a bigger than
            double newY = lastPacketY - 0.03130D;
            lastPacketY = newY;
            delayLeft = 20;

            // Create a new packet with the modified Y value
            if (packet.hasRotation()) {
                return new ServerboundMovePlayerPacket.PosRot(
                    packet.getX(0),
                    newY,
                    packet.getZ(0),
                    packet.getYRot(0),
                    packet.getXRot(0),
                    packet.isOnGround(),
                    packet.horizontalCollision()
                );
            } else {
                return new ServerboundMovePlayerPacket.Pos(
                    packet.getX(0),
                    newY,
                    packet.getZ(0),
                    packet.isOnGround(),
                    packet.horizontalCollision()
                );
            }
        } else {
            lastPacketY = currentY;
            if (!isEntityOnAir(mc.player))
                delayLeft = 20;
        }
        if (delayLeft > 0) delayLeft--;
        return packet;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player.getVehicle() == null || !(event.packet instanceof ServerboundMovePlayerPacket packet) || antiKickMode.get() != AntiKickMode.PaperNew)
            return;

        double currentY = packet.getY(Double.MAX_VALUE);
        ServerboundMovePlayerPacket modifiedPacket;

        if (currentY != Double.MAX_VALUE) {
            modifiedPacket = antiKickPacket(packet, currentY);
        } else {
            // if the packet is a LookAndOnGround packet or an OnGroundOnly packet then we need to
            // make it a Full packet or a PositionAndOnGround packet respectively, so it has a Y value
            ServerboundMovePlayerPacket fullPacket;
            if (packet.hasRotation()) {
                fullPacket = new ServerboundMovePlayerPacket.PosRot(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        packet.getYRot(0),
                        packet.getXRot(0),
                        packet.isOnGround(),
                        packet.horizontalCollision()
                );
            } else {
                fullPacket = new ServerboundMovePlayerPacket.Pos(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        packet.isOnGround(),
                        packet.horizontalCollision()
                );
            }
            modifiedPacket = antiKickPacket(fullPacket, mc.player.getY());
        }

        // Only cancel and resend if the packet was actually modified
        if (modifiedPacket != packet) {
            event.cancel();
            mc.getConnection().send(modifiedPacket);
        }
    }

    private int floatingTicks = 0;

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (floatingTicks >= 20) {
            switch (antiKickMode.get()) {
                case New -> {
                    AABB box = mc.player.getBoundingBox();
                    AABB adjustedBox = box.move(0, -0.4, 0);

                    Stream<VoxelShape> blockCollisions = Streams.stream(mc.level.getBlockCollisions(mc.player, adjustedBox));

                    if (blockCollisions.findAny().isPresent()) break;

                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() - 0.4, mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));
                }
                case Old -> {
                    AABB box = mc.player.getBoundingBox();
                    AABB adjustedBox = box.move(0, -0.4, 0);

                    Stream<VoxelShape> blockCollisions = Streams.stream(mc.level.getBlockCollisions(mc.player, adjustedBox));

                    if (blockCollisions.findAny().isPresent()) break;

                    double ground = calculateGround();
                    double groundExtra = ground + 0.1D;

                    for (double posY = mc.player.getY(); posY > groundExtra; posY -= 4D) {
                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), posY, mc.player.getZ(), true, mc.player.horizontalCollision));

                        if (posY - 4D < groundExtra) break; // Prevent next step
                    }

                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), groundExtra, mc.player.getZ(), true, mc.player.horizontalCollision));

                    for (double posY = groundExtra; posY < mc.player.getY(); posY += 4D) {
                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), posY, mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));

                        if (posY + 4D > mc.player.getY()) break; // Prevent next step
                    }

                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));

                }
            }
            floatingTicks = 0;
        }

        float ySpeed = RejectsUtils.fullFlightMove(event, speed.get(), verticalSpeedMatch.get());

        if (floatingTicks < 20)
            if (ySpeed >= -0.1)
                floatingTicks++;
            else if (antiKickMode.get() == AntiKickMode.New)
                floatingTicks = 0;
    }

    public enum AntiKickMode {
        Old,
        New,
        PaperNew,
        None
    }
}