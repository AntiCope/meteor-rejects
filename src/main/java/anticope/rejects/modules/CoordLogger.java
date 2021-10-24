package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class CoordLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTeleports = settings.createGroup("Teleports");
    private final SettingGroup sgWorldEvents = settings.createGroup("World Events");
    private final SettingGroup sgSounds = settings.createGroup("Sounds");

    // General
    
    private final Setting<Double> minDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("minimum-distance")
            .description("Minimum distance to log event.")
            .min(5)
            .max(100)
            .sliderMin(5)
            .sliderMax(100)
            .defaultValue(10)
            .build()
    );
    
    // Teleports
    
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

    // World events
    
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
    
    // Sounds
    
    private final Setting<Boolean> thunder = sgSounds.add(new BoolSetting.Builder()
            .name("thunder")
            .description("Logs thunder sounds.")
            .defaultValue(false)
            .build()
    );

    public CoordLogger() {
        super(MeteorRejectsAddon.CATEGORY,"coord-logger", "Logs coordinates of various events. Might not work on Spigot/Paper servers.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        // Teleports
        if (event.packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacket packet = (EntityPositionS2CPacket) event.packet;
            
            try {
                Entity entity = mc.world.getEntityById(packet.getId());
                
                // Player teleport
                if (entity.getType().equals(EntityType.PLAYER) && players.get()) {
                    Vec3d packetPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Vec3d playerPosition = entity.getPos();
                    
                    if (playerPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Player '" + entity.getEntityName() + "' has teleported to ", packetPosition));
                    }
                }

                // World teleport
                else if (entity.getType().equals(EntityType.WOLF) && wolves.get()) {
                    Vec3d packetPosition = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Vec3d wolfPosition = entity.getPos();
                    
                    UUID ownerUuid = ((TameableEntity) entity).getOwnerUuid();
                    
                    if (ownerUuid != null && wolfPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Wolf has teleported to ", packetPosition));
                    }
                }
            } catch(NullPointerException ignored) {}
            
        // World events
        } else if (event.packet instanceof WorldEventS2CPacket) {
            WorldEventS2CPacket worldEventS2CPacket = (WorldEventS2CPacket) event.packet;
            
            if (worldEventS2CPacket.isGlobal()) {
                // Min distance
                if (PlayerUtils.distanceTo(worldEventS2CPacket.getPos()) <= minDistance.get()) return;
                
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
            
        // Sounds
        } else if (thunder.get() && event.packet instanceof PlaySoundS2CPacket playSoundS2CPacket) {
            // Check for thunder sound
            if (playSoundS2CPacket.getSound() != SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT) return;
            
            // Min distance
            if (PlayerUtils.distanceTo(playSoundS2CPacket.getX(), playSoundS2CPacket.getY(), playSoundS2CPacket.getZ()) <= minDistance.get()) return;
            
            info("Thunder noise at %d %d %d", playSoundS2CPacket.getX(), playSoundS2CPacket.getY(), playSoundS2CPacket.getZ());
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
