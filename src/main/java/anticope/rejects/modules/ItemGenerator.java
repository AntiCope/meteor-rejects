package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemGenerator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("speed")
            .description("Stacks per tick. High speeds may cause lag.")
            .defaultValue(1)
            .min(1)
            .max(36)
            .sliderMax(36)
            .build()
    );

    private final Setting<Integer> stackSize = sgGeneral.add(new IntSetting.Builder()
            .name("stack-size")
            .description("How many items to place in each stack. ")
            .defaultValue(1)
            .min(1)
            .max(64)
            .sliderMax(64)
            .build()
    );

    private final RandomSource random = RandomSource.create();

    public ItemGenerator() {
        super(MeteorRejectsAddon.CATEGORY, "item-generator", "Generates random items and drops them on the ground. Creative mode only.");
    }

    @Override
    public void onActivate() {
        if(!mc.player.getAbilities().instabuild) {
            error("Creative mode only.");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int stacks = speed.get();
        int size = stackSize.get();
        for(int i = 9; i < 9 + stacks; i++) {
            mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(i, new ItemStack(BuiltInRegistries.ITEM.getRandom(random).map(Holder::value).orElse(Items.DIRT), size)));
        }

        for(int i = 9; i < 9 + stacks; i++) {
            InvUtils.drop().slot(i);
        }
    }
}
