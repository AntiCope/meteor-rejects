package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public class ItemGenerator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("speed")
            .description("\u00a74\u00a7lWARNING:\u00a7r High speeds will cause a ton\n"
                    + "of lag and can easily crash the game!")
            .defaultValue(1)
            .min(1)
            .max(36)
            .sliderMax(36)
            .build()
    );

    private final Setting<Integer> stackSize = sgGeneral.add(new IntSetting.Builder()
            .name("stack-size")
            .description("How many items to place in each stack.\n"
                    + "Doesn't seem to affect performance.")
            .defaultValue(1)
            .min(1)
            .max(64)
            .sliderMax(64)
            .build()
    );

    private final Random random = Random.create();

    public ItemGenerator() {
        super(MeteorRejectsAddon.CATEGORY, "item-generator", "Spawns a lot of unwanted items");
    }

    @Override
    public void onActivate() {
        if(!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int stacks = speed.get();
        int size = stackSize.get();
        for(int i = 9; i < 9 + stacks; i++) {
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(i, new ItemStack(Registry.ITEM.getRandom(random).map(RegistryEntry::value).orElse(Items.DIRT), size)));
        }

        for(int i = 9; i < 9 + stacks; i++) {
            InvUtils.drop().slot(i);
        }
    }
}
