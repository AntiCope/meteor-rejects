package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

//https://github.com/DustinRepo/JexClient/blob/main/src/main/java/me/dustin/jex/feature/mod/impl/world/LawnBot.java
public class LawnBot extends Module {
    private final ArrayList<BlockPos> myceliumSpots = new ArrayList<>();
    //TODO: add settings to find/use shovel, delay, range ?
    public LawnBot() {
        super(MeteorRejectsAddon.CATEGORY, "lawnbot", "Replaces mycelium with grass blocks");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Item grassBlockItem = Items.GRASS_BLOCK;
        int grassCount = countItems(grassBlockItem);
        if (grassCount == 0) {
            return;
        }
        int grassHotbarSlot = getFromInv(grassBlockItem, 9);
        if (grassHotbarSlot == -1) {
            int grassInvSlot = getFromInv(grassBlockItem, 36);
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
                mc.player.getInventory().selectedSlot = grassHotbarSlot;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
                return;
            } else if (block != Blocks.MYCELIUM) {
                myceliumSpots.remove(i);
            }
        }
        for (int i = 0; i < myceliumSpots.size(); i++) {
            BlockPos pos = myceliumSpots.get(i);
            Block block = mc.world.getBlockState(pos).getBlock();
            double distance = mc.player.getPos().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            if (block == Blocks.MYCELIUM && distance <= 5) {
                mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                return;
            }
        }
        myceliumSpots.clear();
        for (int x = -5; x < 5; x++) {
            for (int y = -3; y < 3; y++) {
                for (int z = -5; z < 5; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.MYCELIUM) {
                        myceliumSpots.add(pos);
                    }
                }
            }
        }
    }

    public int countItems(Item item) {
        int count = 0;
        for (int i = 0; i < 44; i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem() == item)
                count+=itemStack.getCount();
        }
        return count;
    }

    // Maybe move this into utils and use for a refactor someday
    public int getFromInv(Item item, int maxSlot) {
        for (int i = 0; i < maxSlot; i++) {
            if (mc.player.getInventory().getStack(i) != null && mc.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }
}
