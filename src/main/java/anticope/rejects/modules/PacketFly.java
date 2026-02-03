package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;

public class PacketFly extends Module {
    private final HashSet<ServerboundMovePlayerPacket> packets = new HashSet<>();
    private final SettingGroup sgMovement = settings.createGroup("movement");
    private final SettingGroup sgClient = settings.createGroup("client");
    private final SettingGroup sgBypass = settings.createGroup("bypass");

    private final Setting<Double> horizontalSpeed = sgMovement.add(new DoubleSetting.Builder()
            .name("horizontal-speed")
            .description("Horizontal speed in blocks per second.")
            .defaultValue(5.2)
            .min(0.0)
            .max(20.0)
            .sliderMin(0.0)
            .sliderMax(20.0)
            .build()
    );

    private final Setting<Double> verticalSpeed = sgMovement.add(new DoubleSetting.Builder()
            .name("vertical-speed")
            .description("Vertical speed in blocks per second.")
            .defaultValue(1.24)
            .min(0.0)
            .max(20.0)
            .sliderMin(0.0)
            .sliderMax(20.0)
            .build()
    );

    private final Setting<Boolean> sendTeleport = sgMovement.add(new BoolSetting.Builder()
            .name("teleport")
            .description("Sends teleport packets.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> setYaw = sgClient.add(new BoolSetting.Builder()
            .name("set-yaw")
            .description("Sets yaw client side.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> setMove = sgClient.add(new BoolSetting.Builder()
            .name("set-move")
            .description("Sets movement client side.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> setPos = sgClient.add(new BoolSetting.Builder()
            .name("set-pos")
            .description("Sets position client side.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> setID = sgClient.add(new BoolSetting.Builder()
            .name("set-id")
            .description("Updates teleport id when a position packet is received.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> antiKick = sgBypass.add(new BoolSetting.Builder()
            .name("anti-kick")
            .description("Moves down occasionally to prevent kicks.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> downDelay = sgBypass.add(new IntSetting.Builder()
            .name("down-delay")
            .description("How often you move down when not flying upwards. (ticks)")
            .defaultValue(4)
            .sliderMin(1)
            .sliderMax(30)
            .min(1)
            .max(30)
            .build()
    );

    private final Setting<Integer> downDelayFlying = sgBypass.add(new IntSetting.Builder()
            .name("flying-down-delay")
            .description("How often you move down when flying upwards. (ticks)")
            .defaultValue(10)
            .sliderMin(1)
            .sliderMax(30)
            .min(1)
            .max(30)
            .build()
    );

    private final Setting<Boolean> invalidPacket = sgBypass.add(new BoolSetting.Builder()
            .name("invalid-packet")
            .description("Sends invalid movement packets.")
            .defaultValue(false)
            .build()
    );

    private int flightCounter = 0;
    private int teleportID = 0;

    public PacketFly() {
        super(MeteorRejectsAddon.CATEGORY, "packet-fly", "Fly using packets.");
    }

    @EventHandler
    public void onSendMovementPackets(SendMovementPacketsEvent.Pre event) {
        mc.player.setDeltaMovement(0.0,0.0,0.0);
        double speed = 0.0;
        boolean checkCollisionBoxes = checkHitBoxes();

        speed = mc.player.input.keyPresses.jump() && (checkCollisionBoxes || !(mc.player.input.getMoveVector().y != 0.0 || mc.player.input.getMoveVector().x != 0.0)) ? (antiKick.get() && !checkCollisionBoxes ? (resetCounter(downDelayFlying.get()) ? -0.032 : verticalSpeed.get()/20) : verticalSpeed.get()/20) : (mc.player.input.keyPresses.shift() ? verticalSpeed.get()/-20 : (!checkCollisionBoxes ? (resetCounter(downDelay.get()) ? (antiKick.get() ? -0.04 : 0.0) : 0.0) : 0.0));

        Vec3 horizontal = PlayerUtils.getHorizontalVelocity(horizontalSpeed.get());

        mc.player.setDeltaMovement(horizontal.x, speed, horizontal.z);
        sendPackets(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z, sendTeleport.get());
    }

    @EventHandler
    public void onMove (PlayerMoveEvent event) {
        if (setMove.get() && flightCounter != 0) {
            event.movement = new Vec3(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z);
        }
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundMovePlayerPacket && !packets.remove((ServerboundMovePlayerPacket) event.packet)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundPlayerPositionPacket && !(mc.player == null || mc.level == null)) {
            ClientboundPlayerPositionPacket packet = (ClientboundPlayerPositionPacket) event.packet;
            PositionMoveRotation oldPos = packet.change();
            if (setYaw.get()) {
                PositionMoveRotation newPos = new PositionMoveRotation(oldPos.position(), oldPos.deltaMovement(), mc.player.getYRot(), mc.player.getXRot());
                event.packet = ClientboundPlayerPositionPacket.of(
                        packet.id(),
                        newPos,
                        packet.relatives()
                );
            }
            if (setID.get()) {
                teleportID = packet.id();
            }
        }
    }

    private boolean checkHitBoxes() {
        return !mc.level.getBlockCollisions(mc.player, mc.player.getBoundingBox().expandTowards(-0.0625,-0.0625,-0.0625)).iterator().hasNext();
    }

    private boolean resetCounter(int counter) {
        if (++flightCounter >= counter) {
            flightCounter = 0;
            return true;
        }
        return false;
    }

    private void sendPackets(double x, double y, double z, boolean teleport) {
        Vec3 vec = new Vec3(x, y, z);
        Vec3 playerPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3 position = playerPos.add(vec);
        Vec3 outOfBoundsVec = outOfBoundsVec(vec, position);
        packetSender(new ServerboundMovePlayerPacket.Pos(position.x, position.y, position.z, mc.player.onGround(), mc.player.horizontalCollision));
        if (invalidPacket.get()) {
            packetSender(new ServerboundMovePlayerPacket.Pos(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, mc.player.onGround(), mc.player.horizontalCollision));
        }
        if (setPos.get()) {
            mc.player.setPosRaw(position.x, position.y, position.z);
        }
        teleportPacket(position, teleport);
    }

    private void teleportPacket(Vec3 pos, boolean shouldTeleport) {
        if (shouldTeleport) {
            mc.player.connection.send(new ServerboundAcceptTeleportationPacket(++teleportID));
        }
    }

    private Vec3 outOfBoundsVec(Vec3 offset, Vec3 position) {
        return position.add(0.0, 1500.0, 0.0);
    }

    private void packetSender(ServerboundMovePlayerPacket packet) {
        packets.add(packet);
        mc.player.connection.send(packet);
    }
}
