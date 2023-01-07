package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
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
import net.minecraft.world.WorldView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoFarm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTill = settings.createGroup("Till");
    private final SettingGroup sgHarvest = settings.createGroup("Harvest");
    private final SettingGroup sgPlant = settings.createGroup("Plant");
    private final SettingGroup sgBonemeal = settings.createGroup("Bonemeal");

    private final Map<BlockPos, Item> replantMap = new HashMap<>();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
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

    private final Setting<List<Block>> harvestBlocks = sgHarvest.add(new BlockListSetting.Builder()
            .name("harvest-blocks")
            .description("Crops to use for harvesting.")
            .defaultValue()
            .filter(this::harvestFilter)
            .build()
    );

    private final Setting<List<Item>> plantItems = sgPlant.add(new ItemListSetting.Builder()
            .name("plant-items")
            .description("Crops to use for planting.")
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

    private final Setting<List<Block>> bonemealBlocks = sgBonemeal.add(new BlockListSetting.Builder()
            .name("bonemeal-blocks")
            .description("Crops to use for bonemealing.")
            .defaultValue()
            .filter(this::bonemealFilter)
            .build()
    );

    public AutoFarm() {
        super(MeteorRejectsAddon.CATEGORY, "auto-farm", "All-in-one farm utility.");
    }

    @Override
    public void onDeactivate() {
        replantMap.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int actions = 0;
        for (BlockPos pos : WorldUtils.getCube(range.get())) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            if (till.get() && shouldTill(pos)) {
                FindItemResult hoe = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof HoeItem);
                if (hoe.found()) {
                    WorldUtils.interact(pos, hoe, rotate.get());
                    actions++;
                }
            }
            if (shouldHarvest(state, block)) {
                if (block instanceof SweetBerryBushBlock)
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(pos), Direction.UP, pos, false));
                else {
                    mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                    if (onlyReplant.get()) {
                        Item item = null;
                        if (block == Blocks.WHEAT) item = Items.WHEAT_SEEDS;
                        else if (block == Blocks.CARROTS) item = Items.CARROT;
                        else if (block == Blocks.POTATOES) item = Items.POTATO;
                        else if (block == Blocks.BEETROOTS) item = Items.BEETROOT_SEEDS;
                        else if (block == Blocks.NETHER_WART) item = Items.NETHER_WART;
                        if (item != null) replantMap.put(pos, item);
                    }
                }
                actions++;
            } else if (plant(pos)) {
                actions++;
            } else if (shouldBonemeal(state, block)) {
                FindItemResult bonemeal = InvUtils.findInHotbar(Items.BONE_MEAL);
                if (bonemeal.found()) {
                    WorldUtils.interact(pos, bonemeal, rotate.get());
                    actions++;
                }
            }

            if (actions >= bpt.get()) break;
        }
    }

    private boolean plant(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
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

    private boolean isWaterNearby(WorldView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) return true;
        }
        return false;
    }

    private boolean shouldTill(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        boolean moist = !this.moist.get() || isWaterNearby(mc.world, pos);
        boolean tillable = (block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT_PATH ||
                block == Blocks.DIRT ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT);
        return moist && tillable && mc.world.getBlockState(pos.up()).isAir();
    }

    private boolean shouldBonemeal(BlockState state, Block block) {
        if (!bonemealBlocks.get().contains(block)) return false;
        return !isMature(state, block);
    }

    private boolean shouldHarvest(BlockState state, Block block) {
        if (!harvestBlocks.get().contains(block)) return false;
        return isMature(state, block);
    }

    private boolean isMature(BlockState state, Block block) {
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        } else if (block instanceof CocoaBlock cocoaBlock) {
            return !cocoaBlock.hasRandomTicks(state);
        } else if (block instanceof StemBlock) {
            return state.get(StemBlock.AGE) == StemBlock.MAX_AGE;
        } else if (block instanceof SweetBerryBushBlock sweetBerryBushBlock) {
            return !sweetBerryBushBlock.hasRandomTicks(state);
        } else if (block instanceof NetherWartBlock netherWartBlock) {
            return !netherWartBlock.hasRandomTicks(state);
        }
        return true;
    }

    private boolean bonemealFilter(Block block) {
        return block instanceof CropBlock ||
                block instanceof CocoaBlock ||
                block instanceof StemBlock ||
                block instanceof MushroomPlantBlock ||
                block instanceof SweetBerryBushBlock ||
                block instanceof AzaleaBlock ||
                block instanceof SaplingBlock;
    }

    private boolean harvestFilter(Block block) {
        return block instanceof CropBlock ||
                block instanceof GourdBlock ||
                block instanceof NetherWartBlock ||
                block instanceof SweetBerryBushBlock;
    }

    private boolean plantFilter(Item item) {
        return item == Items.WHEAT_SEEDS ||
                item == Items.CARROT ||
                item == Items.POTATO ||
                item == Items.BEETROOT_SEEDS ||
                item == Items.PUMPKIN_SEEDS ||
                item == Items.MELON_SEEDS ||
                item == Items.NETHER_WART;
    }
}
