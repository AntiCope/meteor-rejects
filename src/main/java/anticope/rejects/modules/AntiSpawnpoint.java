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
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
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
        EnvironmentAttributeMap attributes = mc.level.dimensionType().attributes();
        boolean IsOverWorld = attributes.applyModifier(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK) != BedRule.CAN_SLEEP_WHEN_DARK;
        boolean IsNetherWorld = attributes.applyModifier(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false);
        boolean BlockIsBed = mc.level.getBlockState(blockPos).getBlock() instanceof BedBlock;
        boolean BlockIsAnchor = mc.level.getBlockState(blockPos).getBlock().equals(Blocks.RESPAWN_ANCHOR);

        assert mc.player != null;
        if (fakeUse.get()) {
            if (BlockIsBed && IsOverWorld) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.absSnapTo(blockPos.getX(),blockPos.above().getY(),blockPos.getZ());
            }
            else if (BlockIsAnchor && IsNetherWorld) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        if((BlockIsBed && IsOverWorld)||(BlockIsAnchor && IsNetherWorld)) {
            event.cancel();
        }
    }
}
