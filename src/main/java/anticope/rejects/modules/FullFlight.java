package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.google.common.collect.Streams;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

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
        .defaultValue(AntiKickMode.Old)
        .build()
    );
	
	public FullFlight() {
		super(MeteorRejectsAddon.CATEGORY, "fullflight", "FullFlight.");
	}
	
    private double getDir() {
        double dir = 0;

        if (Utils.canUpdate()) {
            dir = mc.player.getYaw() + ((mc.player.forwardSpeed < 0) ? 180 : 0);

            if (mc.player.sidewaysSpeed > 0) {
                dir += -90F * ((mc.player.forwardSpeed < 0) ? -0.5F : ((mc.player.forwardSpeed > 0) ? 0.5F : 1F));
            } else if (mc.player.sidewaysSpeed < 0) {
                dir += 90F * ((mc.player.forwardSpeed < 0) ? -0.5F : ((mc.player.forwardSpeed > 0) ? 0.5F : 1F));
            }
        }
        return dir;
    }
	
    private double calculateGround() {
        for(double ground = mc.player.getY(); ground > 0D; ground -= 0.05) {
				Box box = mc.player.getBoundingBox();
				Box adjustedBox = box.offset(0, ground - mc.player.getY(), 0);
				
				Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
				
				if(blockCollisions.findAny().isPresent()) return ground;
        }

        return 0F;
    }
	
    private int floatingTicks = 0;
	
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
		if (floatingTicks >= 40)
		{
        switch (antiKickMode.get()) {
            case New -> {
				Box box = mc.player.getBoundingBox();
				Box adjustedBox = box.offset(0, -0.4, 0);
				
				Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
				
				if(blockCollisions.findAny().isPresent()) break;
				
				mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.4, mc.player.getZ(), mc.player.isOnGround()));
				mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
				
				break;
			}
            case Old -> {
				Box box = mc.player.getBoundingBox();
				Box adjustedBox = box.offset(0, -0.4, 0);
				
				Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
				
				if(blockCollisions.findAny().isPresent()) break;
				
				double ground = calculateGround();
				double groundExtra = ground + 0.5D;
				
				for(double posY = mc.player.getY(); posY > groundExtra; posY -= 8D) {
					mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), posY, mc.player.getZ(), true));

					if(posY - 8D < groundExtra) break; // Prevent next step
				}

				mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), groundExtra, mc.player.getZ(), true));


				for(double posY = groundExtra; posY < mc.player.getY(); posY += 8D) {
					mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), posY, mc.player.getZ(), mc.player.isOnGround()));

					if(posY + 8D > mc.player.getY()) break; // Prevent next step
				}

				mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
				
				break;
			}
		}
		floatingTicks = 0;
		}
		
		if (PlayerUtils.isMoving()) {
			double dir = getDir();
			
			double xDir = Math.cos(Math.toRadians(dir + 90));
			double zDir = Math.sin(Math.toRadians(dir + 90));
			
			((IVec3d) event.movement).setXZ(xDir * speed.get(), zDir * speed.get());
		}
		else {
			((IVec3d) event.movement).setXZ(0, 0);
		}
		
		float ySpeed = 0;
		
		if (mc.options.jumpKey.isPressed())
			ySpeed += speed.get();
		if (mc.options.sneakKey.isPressed())
			ySpeed -= speed.get();
		((IVec3d) event.movement).setY(verticalSpeedMatch.get() ? ySpeed : ySpeed/2);
		
		if (ySpeed >= 0 && floatingTicks < 40)
			floatingTicks++;
	}
	
    public enum AntiKickMode {
        Old,
        New,
        None
    }
	
}