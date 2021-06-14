package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class AntiSpawnpoint extends Module {

    private SettingGroup sgDefault = settings.getDefaultGroup();

    private Setting<Boolean> fakeUse = sgDefault.add(new BoolSetting.Builder()
            .name("fake-use")
            .description("Fake using the bed or anchor.")
            .defaultValue(true)
            .build()
    );

    public AntiSpawnpoint() {
        super(MeteorRejectsAddon.CATEGORY, "anti-spawnpoint", "Protects the player from losing the respawn point.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.world == null) return;
        if(!(event.packet instanceof PlayerInteractBlockC2SPacket)) return;


        BlockPos blockPos = ((PlayerInteractBlockC2SPacket) event.packet).getBlockHitResult().getBlockPos();
        boolean IsOverWorld = mc.world.getDimension().isBedWorking();
        boolean IsNetherWorld = mc.world.getDimension().isRespawnAnchorWorking();
        boolean BlockIsBed = mc.world.getBlockState(blockPos).getBlock() instanceof BedBlock;
        boolean BlockIsAnchor = mc.world.getBlockState(blockPos).getBlock().equals(Blocks.RESPAWN_ANCHOR);

        if (fakeUse.get()) {
            if (BlockIsBed && IsOverWorld) {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.updatePosition(blockPos.getX(),blockPos.up().getY(),blockPos.getZ());
            }
            else if (BlockIsAnchor && IsNetherWorld) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }

        if((BlockIsBed && IsOverWorld)||(BlockIsAnchor && IsNetherWorld)) {
            event.cancel();
        }
    }
}
