package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.world.LightType;

import java.util.List;

//TODO: add setting for replaceable blocks
public class AutoTorch extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> torches = sgGeneral.add(new ItemListSetting.Builder()
            .name("torch-type")
            .description("The type of torches to use.")
            .defaultValue(Items.TORCH)
            .filter(this::torchFilter)
            .build()
    );

    private final Setting<Integer> lightLevel = sgGeneral.add(new IntSetting.Builder()
            .name("light-level")
            .description("At what light level and below to place torches.")
            .defaultValue(7)
            .min(0)
            .sliderMax(15)
            .build()
    );

    public AutoTorch() {super(MeteorRejectsAddon.CATEGORY, "auto-torch", "Automatically places torches where you stand.");}

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (torches.get().isEmpty() || mc.player == null || mc.world == null) return;

        boolean torchFound = false;

        for (int i = 0; i < torches.get().size(); i++) {
            FindItemResult findItemResult = InvUtils.findInHotbar(torches.get().get(i));

            if (findItemResult.found()) {
                torchFound = true;

                if (mc.world.getLightLevel(LightType.BLOCK, mc.player.getBlockPos()) <= lightLevel.get()) {
                    WorldUtils.interact(mc.player.getBlockPos(), findItemResult, false, false);
                    return;
                }
            }
        }

        if (!torchFound) {
            ChatUtils.sendMsg(Text.of("No torches found in hotbar."));
            toggle();
        }
    }


    private boolean torchFilter(Item item) {
        return item == Items.TORCH ||
                item == Items.REDSTONE_TORCH ||
                item == Items.SOUL_TORCH;
    }

}
