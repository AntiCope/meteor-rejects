package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class CoordLogger extends Module {
    private final SettingGroup sgTeleports = settings.createGroup("Teleports");
    private final SettingGroup sgWorldEvents = settings.createGroup("World Events");
    
    private final Setting<Boolean> players = sgTeleports.add(new BoolSetting.Builder()
            .name("players")
            .description("Logs player teleports.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> wolves = sgTeleports.add(new BoolSetting.Builder()
            .name("wolves")
            .description("Logs wolf teleports.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Double> minDistance = sgTeleports.add(new DoubleSetting.Builder()
            .name("minimum-distance")
            .description("Minimum movement distance to log as teleport.")
            .min(5)
            .max(100)
            .sliderMin(5)
            .sliderMax(100)
            .defaultValue(10)
            .build()
    );
    
    private final Setting<Boolean> enderDragons = sgWorldEvents.add(new BoolSetting.Builder()
            .name("ender-dragons")
            .description("Logs killed ender dragons.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> endPortals = sgWorldEvents.add(new BoolSetting.Builder()
            .name("end-portals")
            .description("Logs opened end portals.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> withers = sgWorldEvents.add(new BoolSetting.Builder()
            .name("withers")
            .description("Logs wither spawns.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> otherEvents = sgWorldEvents.add(new BoolSetting.Builder()
            .name("other-global-events")
            .description("Logs other global events.")
            .defaultValue(false)
            .build()
    );
    
    public CoordLogger() {
        super(MeteorRejectsAddon.CATEGORY,"coord-logger", "Logs coordinates of various events. Doesn't work on Spigot/Paper servers.");
    }
    
    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacket packet = (EntityPositionS2CPacket) event.packet;
            try {
                Entity entity = mc.world.getEntityById(packet.getId());
                if (entity.getType().equals(EntityType.PLAYER) && players.get()) {
                    Vec3d packetPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Vec3d playerPosition = entity.getPos();
                    if (playerPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Player '" + entity.getEntityName() + "' has teleported to ", packetPosition));
                        return;
                    }
                }
                
                if (entity.getType().equals(EntityType.WOLF) && wolves.get()) {
                    Vec3d packetPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Vec3d wolfPosition = entity.getPos();
                    UUID ownerUuid = ((TameableEntity) entity).getOwnerUuid();
                    if (ownerUuid != null && wolfPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Wolf has teleported to ", packetPosition));
                        return;
                    }
                }
            } catch(NullPointerException e) {}
        } else if (event.packet instanceof WorldEventS2CPacket) {
            WorldEventS2CPacket worldEventS2CPacket = (WorldEventS2CPacket) event.packet;
            if (worldEventS2CPacket.isGlobal()) {
                System.out.println(worldEventS2CPacket.getEventId());
                switch (worldEventS2CPacket.getEventId()) {
                    case 1023:
                        if (withers.get()) info(formatMessage("Wither spawned at ", worldEventS2CPacket.getPos()));
                        break;
                    case 1038:
                        if (endPortals.get()) info(formatMessage("End portal opened at ", worldEventS2CPacket.getPos()));
                        break;
                    case 1028:
                        if (enderDragons.get()) info(formatMessage("Ender dragon killed at ", worldEventS2CPacket.getPos()));
                        break;
                    default:
                        if (otherEvents.get()) info(formatMessage("Unknown global event at ", worldEventS2CPacket.getPos()));
                }
            }
        }
        
    }
    
    public BaseText formatMessage(String message, Vec3d coords) {
        BaseText text = new LiteralText(message);
        text.append(ChatUtils.formatCoords(coords));
        text.append(Formatting.GRAY.toString()+".");
        return text;
    }

    public BaseText formatMessage(String message, BlockPos coords) {
        return formatMessage(message, new Vec3d(coords.getX(), coords.getY(), coords.getZ()));
    }
}
