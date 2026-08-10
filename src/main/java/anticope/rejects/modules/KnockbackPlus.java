package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class KnockbackPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ka = sgGeneral.add(new BoolSetting.Builder()
            .name("only-killaura")
            .description("Only perform more KB when using killaura.")
            .defaultValue(false)
            .build()
    );

    public KnockbackPlus() {
        super(MeteorRejectsAddon.CATEGORY, "knockback-plus", "Performs more KB when you hit your target.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        // Attacks are their own packet since 26.1; the entity is looked up from its id.
        if (!(event.packet instanceof ServerboundAttackPacket packet)) return;
        if (mc.player == null || mc.level == null) return;

        Entity entity = mc.level.getEntity(packet.entityId());
        if (!(entity instanceof LivingEntity) || (entity != Modules.get().get(KillAura.class).getTarget() && ka.get()))
            return;

        mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
    }
}
