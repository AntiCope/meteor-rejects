package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;

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
        if (mc.level == null) return;
        if(!(event.packet instanceof ServerboundUseItemOnPacket)) return;


        BlockPos blockPos = ((ServerboundUseItemOnPacket) event.packet).getHitResult().getBlockPos();
        boolean bedDangerous = !mc.level.dimensionType().bedWorks();
        boolean anchorWorks = mc.level.dimensionType().respawnAnchorWorks();
        boolean BlockIsBed = mc.level.getBlockState(blockPos).getBlock() instanceof BedBlock;
        boolean BlockIsAnchor = mc.level.getBlockState(blockPos).getBlock().equals(Blocks.RESPAWN_ANCHOR);

        assert mc.player != null;
        if (fakeUse.get()) {
            if (BlockIsBed && bedDangerous) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.absSnapTo(blockPos.getX(),blockPos.above().getY(),blockPos.getZ());
            }
            else if (BlockIsAnchor && anchorWorks) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        if((BlockIsBed && bedDangerous)||(BlockIsAnchor && anchorWorks)) {
            event.cancel();
        }
    }
}
