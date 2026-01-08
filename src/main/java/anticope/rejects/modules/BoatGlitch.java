package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.Boat;

public class BoatGlitch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> toggleAfter = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-after")
            .description("Disables the module when finished.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> remount = sgGeneral.add(new BoolSetting.Builder()
            .name("remount")
            .description("Remounts the boat when finished.")
            .defaultValue(true)
            .build()
    );

    private Entity boat = null;
    private int dismountTicks = 0;
    private int remountTicks = 0;
    private boolean dontPhase = true;
    private boolean boatPhaseEnabled;

    public BoatGlitch() {
        super(MeteorRejectsAddon.CATEGORY, "boat-glitch", "Glitches your boat into the block beneath you.  Dismount to trigger.");
    }

    @Override
    public void onActivate() {
        dontPhase = true;
        dismountTicks = 0;
        remountTicks = 0;
        boat = null;
        if (Modules.get().isActive(BoatPhase.class)) {
            boatPhaseEnabled = true;
            Modules.get().get(BoatPhase.class).toggle();
        }
        else {
            boatPhaseEnabled = false;
        }
    }

    @Override
    public void onDeactivate() {
        if (boat != null) {
            boat.noPhysics = false;
            boat = null;
        }
        if (boatPhaseEnabled && !(Modules.get().isActive(BoatPhase.class))) {
            Modules.get().get(BoatPhase.class).toggle();
        }
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (dismountTicks == 0 && !dontPhase) {
            if (boat != event.boat) {
                if (boat != null) {
                    boat.noPhysics = false;
                }
                if (mc.player.getVehicle() != null && event.boat == mc.player.getVehicle()) {
                    boat = event.boat;
                }
                else {
                    boat = null;
                }
            }
            if (boat != null) {
                boat.noPhysics = true;
                //boat.pushSpeedReduction = 1;
                dismountTicks = 5;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (dismountTicks > 0) {
            dismountTicks--;
            if (dismountTicks == 0) {
                if (boat != null) {
                    boat.noPhysics = false;
                    if (toggleAfter.get() && !remount.get()) {
                        toggle();
                    }
                    else if (remount.get()) {
                        remountTicks = 5;
                    }
                }
                dontPhase = true;
            }
        }
        if (remountTicks > 0) {
            remountTicks--;
            if (remountTicks == 0) {
                mc.getConnection().send( ServerboundInteractPacket.createInteractionPacket(boat, false, InteractionHand.MAIN_HAND));
                if (toggleAfter.get()) {
                    toggle();
                }
            }
        }
    }
    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.key() == mc.options.keyShift.getDefaultKey().getValue() && event.action == KeyAction.Press) {
            if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof Boat) {
                dontPhase = false;
                boat = null;
            }
        }
    }
}
