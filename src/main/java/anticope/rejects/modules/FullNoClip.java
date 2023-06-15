package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.shape.VoxelShapes;

public class FullNoClip extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Your speed when noclipping.")
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
	
	public FullNoClip() {
		super(MeteorRejectsAddon.CATEGORY, "fullnoclip", "FullNoClip.");
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
	
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
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
	}
	
}