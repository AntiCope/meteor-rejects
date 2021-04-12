package cloudburst.rejects.aax.Etc;


import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Runner implements Runnable {
    boolean isRunning = true;
    long delay;
    int rad;

    public Runner(int rad, long delay) {
        this.rad = rad;
        this.delay = delay;
    }
    
    @Override
    public void run() {
    	MinecraftClient mc = MinecraftClient.getInstance();
    	BlockPos pos = mc.player.getBlockPos();


        // Blocks that aren't ores but still needs to be checked
        Block[] blocks2check = Config.checkblocks;

        for (int cx = -rad; cx <= rad; cx++) {
            for (int cy = -rad; cy <= rad; cy++) {
                for (int cz = -rad; cz <= rad; cz++) {
                    if (!isRunning) break;
                    BlockPos current = new BlockPos(pos.getX() + cx, pos.getY() + cy, pos.getZ() + cz);
                    if (mc.world.getBlockState(current).getBlock() == null) continue;
                    Block block = mc.world.getBlockState(current).getBlock();

                    boolean good = Config.scanAll; // cool for else man

                    // only check if block is a ore or in blocks2check (obsidian for example)
                    for (Block block1 : blocks2check) {
                        if (block.equals(block1)) {
                            good = true;
                            break;
                        }
                    }

                    if (!good) {
                        continue;
                    }
                    
                    
                    PlayerActionC2SPacket packet = new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, current, Direction.UP);
                    
                    mc.getNetworkHandler().sendPacket(packet);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }
}