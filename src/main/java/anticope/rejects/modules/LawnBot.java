package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

//TODO: add settings to find/use shovel, delay, range ?
//https://github.com/DustinRepo/JexClient/blob/main/src/main/java/me/dustin/jex/feature/mod/impl/world/LawnBot.java
public class LawnBot extends Module {
    private final ArrayList<BlockPos> myceliumSpots = new ArrayList<>();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blockWhitelist = sgGeneral.add(new BlockListSetting.Builder()
            .name("block-whitelist")
            .description("Which blocks to replace with grass.")
            .defaultValue()
            .filter(this::grassFilter)
            .build()
    );

    public LawnBot() {
        super(MeteorRejectsAddon.CATEGORY, "lawnbot", "Replace a variety of dirt-type blocks with grass");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Item grassBlockItem = Items.GRASS_BLOCK;
        int grassCount = InvUtils.find(grassBlockItem).count();
        if (grassCount == 0) {
            return;
        }

        int grassHotbarSlot = InvUtils.findInHotbar((itemStack -> itemStack.getItem() == grassBlockItem)).slot();
        if (grassHotbarSlot == -1) {
            int grassInvSlot = InvUtils.find((itemStack -> itemStack.getItem() == grassBlockItem)).slot();
            if (grassInvSlot == -1)
                return;

            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, grassInvSlot < 9 ? grassInvSlot + 36 : grassInvSlot, 8, SlotActionType.SWAP, mc.player);
            return;
        }
        for (int i = 0; i < myceliumSpots.size(); i++) {
            BlockPos pos = myceliumSpots.get(i);
            Block block = mc.world.getBlockState(pos).getBlock();
            double distance = mc.player.getPos().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            if (block == Blocks.AIR && distance <= 5) {
                mc.player.getInventory().setSelectedSlot(grassHotbarSlot);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
                return;
            } else if (!blockWhitelist.get().contains(block)) {
                myceliumSpots.remove(i);
            }
        }
        for (int i = 0; i < myceliumSpots.size(); i++) {
            BlockPos pos = myceliumSpots.get(i);
            Block block = mc.world.getBlockState(pos).getBlock();
            double distance = mc.player.getPos().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            if (blockWhitelist.get().contains(block) && distance <= 5) {
                mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                return;
            }
        }
        myceliumSpots.clear();
        for (int x = -5; x < 5; x++) {
            for (int y = -3; y < 3; y++) {
                for (int z = -5; z < 5; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    if (blockWhitelist.get().contains(mc.world.getBlockState(pos).getBlock())) {
                        myceliumSpots.add(pos);
                    }
                }
            }
        }
    }

    private boolean grassFilter(Block block) {
        return  block == Blocks.MYCELIUM ||
                block == Blocks.PODZOL ||
                block == Blocks.DIRT_PATH ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT;
    }
}
