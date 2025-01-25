package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.*;

public class AutoFarm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTill = settings.createGroup("Till");
    private final SettingGroup sgHarvest = settings.createGroup("Harvest");
    private final SettingGroup sgPlant = settings.createGroup("Plant");
    private final SettingGroup sgBonemeal = settings.createGroup("Bonemeal");

    private final Map<BlockPos, Item> replantMap = new HashMap<>();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .description("Auto farm range.")
            .defaultValue(4)
            .min(1)
            .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("Amount of operations that can be applied in one tick.")
            .min(1)
            .defaultValue(1)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Whether or not to rotate towards block.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> till = sgTill.add(new BoolSetting.Builder()
            .name("till")
            .description("Turn nearby dirt into farmland.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> moist = sgTill.add(new BoolSetting.Builder()
            .name("moist")
            .description("Only till moist blocks.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> harvest = sgHarvest.add(new BoolSetting.Builder()
            .name("harvest")
            .description("Harvest crops.")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<Block>> harvestBlocks = sgHarvest.add(new BlockListSetting.Builder()
            .name("harvest-blocks")
            .description("Which crops to harvest.")
            .defaultValue()
            .filter(this::harvestFilter)
            .build()
    );

    private final Setting<Boolean> plant = sgPlant.add(new BoolSetting.Builder()
            .name("plant")
            .description("Plant crops.")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<Item>> plantItems = sgPlant.add(new ItemListSetting.Builder()
            .name("plant-items")
            .description("Which crops to plant.")
            .defaultValue()
            .filter(this::plantFilter)
            .build()
    );

    private final Setting<Boolean> onlyReplant = sgPlant.add(new BoolSetting.Builder()
            .name("only-replant")
            .description("Only replant planted crops.")
            .defaultValue(true)
            .onChanged(b -> replantMap.clear())
            .build()
    );

    private final Setting<Boolean> bonemeal = sgBonemeal.add(new BoolSetting.Builder()
            .name("bonemeal")
            .description("Bonemeal crops.")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<Block>> bonemealBlocks = sgBonemeal.add(new BlockListSetting.Builder()
            .name("bonemeal-blocks")
            .description("Which crops to bonemeal.")
            .defaultValue()
            .filter(this::bonemealFilter)
            .build()
    );

    private final Pool<BlockPos.Mutable> blockPosPool = new Pool<>(BlockPos.Mutable::new);
    private final List<BlockPos.Mutable> blocks = new ArrayList<>();

    int actions = 0;

    public AutoFarm() {
        super(MeteorRejectsAddon.CATEGORY, "auto-farm", "All-in-one farm utility.");
    }

    @Override
    public void onDeactivate() {
        replantMap.clear();
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        BlockState state = mc.world.getBlockState(event.blockPos);
        Block block = state.getBlock();
        if (onlyReplant.get()) {
            Item item = null;
            if (block == Blocks.WHEAT) item = Items.WHEAT_SEEDS;
            else if (block == Blocks.CARROTS) item = Items.CARROT;
            else if (block == Blocks.POTATOES) item = Items.POTATO;
            else if (block == Blocks.BEETROOTS) item = Items.BEETROOT_SEEDS;
            else if (block == Blocks.NETHER_WART) item = Items.NETHER_WART;
            else if (block == Blocks.PITCHER_CROP) item = Items.PITCHER_POD;
            else if (block == Blocks.TORCHFLOWER) item = Items.TORCHFLOWER_SEEDS;
            if (item != null) replantMap.put(event.blockPos, item);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        actions = 0;
        BlockIterator.register(range.get(), range.get(), (pos, state) -> {
            if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) <= range.get())
                blocks.add(blockPosPool.get().set(pos));
        });

        BlockIterator.after(() -> {
            blocks.sort(Comparator.comparingDouble(value -> mc.player.getEyePos().distanceTo(Vec3d.ofCenter(value))));

            for (BlockPos pos : blocks) {
                BlockState state = mc.world.getBlockState(pos);
                Block block = state.getBlock();
                if (till(pos, block) || harvest(pos, state, block) || plant(pos, block) || bonemeal(pos, state, block))
                    actions++;
                if (actions >= bpt.get()) break;
            }

            for (BlockPos.Mutable blockPos : blocks) blockPosPool.free(blockPos);
            blocks.clear();

        });
    }

    private boolean till(BlockPos pos, Block block) {
        if (!till.get()) return false;
        boolean moist = !this.moist.get() || isWaterNearby(mc.world, pos);
        boolean tillable = block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT_PATH ||
                block == Blocks.DIRT ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT;
        if (moist && tillable && mc.world.getBlockState(pos.up()).isAir()) {
            FindItemResult hoe = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof HoeItem);
            return WorldUtils.interact(pos, hoe, rotate.get());
        }
        return false;
    }

    private boolean harvest(BlockPos pos, BlockState state, Block block) {
        if (!harvest.get()) return false;
        if (!harvestBlocks.get().contains(block)) return false;
        if (!isMature(state, block)) return false;
        if (block instanceof SweetBerryBushBlock)
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(pos), Direction.UP, pos, false));
        else {
            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
        }
        return true;
    }

    private boolean plant(BlockPos pos, Block block) {
        if (!plant.get()) return false;
        if (!mc.world.isAir(pos.up())) return false;
        FindItemResult findItemResult = null;
        if (onlyReplant.get()) {
            for (BlockPos replantPos : replantMap.keySet()) {
                if (replantPos.equals(pos.up())) {
                    findItemResult = InvUtils.find(replantMap.get(replantPos));
                    replantMap.remove(replantPos);
                    break;
                }
            }
        } else if (block instanceof FarmlandBlock) {
            findItemResult = InvUtils.find(itemStack -> {
                Item item = itemStack.getItem();
                return item != Items.NETHER_WART && plantItems.get().contains(item);
            });
        } else if (block instanceof SoulSandBlock) {
            findItemResult = InvUtils.find(itemStack -> {
                Item item = itemStack.getItem();
                return item == Items.NETHER_WART && plantItems.get().contains(Items.NETHER_WART);
            });
        }
        if (findItemResult != null && findItemResult.found()) {
            BlockUtils.place(pos.up(), findItemResult, rotate.get(), -100, false);
            return true;
        }
        return false;
    }

    private boolean bonemeal(BlockPos pos, BlockState state, Block block) {
        if (!bonemeal.get()) return false;
        if (!bonemealBlocks.get().contains(block)) return false;
        if (isMature(state, block)) return false;

        FindItemResult bonemeal = InvUtils.findInHotbar(Items.BONE_MEAL);
        return WorldUtils.interact(pos, bonemeal, rotate.get());
    }

    private boolean isWaterNearby(WorldView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) return true;
        }
        return false;
    }

    private boolean isMature(BlockState state, Block block) {
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        } else if (block instanceof CocoaBlock cocoaBlock) {
            return state.get(cocoaBlock.AGE) >= 2;
        } else if (block instanceof StemBlock) {
            return state.get(StemBlock.AGE) == StemBlock.MAX_AGE;
        } else if (block instanceof SweetBerryBushBlock sweetBerryBushBlock) {
            return state.get(sweetBerryBushBlock.AGE) >= 2;
        } else if (block instanceof NetherWartBlock netherWartBlock) {
            return state.get(netherWartBlock.AGE) >= 3;
        } else if (block instanceof PitcherCropBlock pitcherCropBlock) {
            return state.get(pitcherCropBlock.AGE) >= 4;
        }
        return true;
    }

    private boolean bonemealFilter(Block block) {
        return block instanceof CropBlock ||
                block instanceof StemBlock ||
                block instanceof MushroomPlantBlock ||
                block instanceof AzaleaBlock ||
                block instanceof SaplingBlock ||
                block == Blocks.COCOA ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.PITCHER_CROP ||
                block == Blocks.TORCHFLOWER;
    }

    private boolean harvestFilter(Block block) {
        return block instanceof CropBlock ||
                block == Blocks.PUMPKIN ||
                block == Blocks.MELON ||
                block == Blocks.NETHER_WART ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.COCOA ||
                block == Blocks.PITCHER_CROP ||
                block == Blocks.TORCHFLOWER;
    }

    private boolean plantFilter(Item item) {
        return item == Items.WHEAT_SEEDS ||
                item == Items.CARROT ||
                item == Items.POTATO ||
                item == Items.BEETROOT_SEEDS ||
                item == Items.PUMPKIN_SEEDS ||
                item == Items.MELON_SEEDS ||
                item == Items.NETHER_WART ||
                item == Items.PITCHER_POD ||
                item == Items.TORCHFLOWER_SEEDS;
    }
}
