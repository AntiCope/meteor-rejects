package anticope.rejects.mixin.meteor.modules;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mixin(value = AutoSign.class, remap = false)
public class AutoSignMixin extends Module {
    @Unique
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    @Shadow
    private String[] text;

    private final Setting<Boolean> random = sgGeneral.add(new BoolSetting.Builder()
            .name("random")
            .description("Spams trash text to make people lag.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> length = sgGeneral.add(new IntSetting.Builder()
            .name("random-length")
            .description("Random character length.")
            .defaultValue(500)
            .min(1)
            .sliderMax(1000)
            .build()
    );

    public AutoSignMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Inject(method = "onOpenScreen",at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/mixin/AbstractSignEditScreenAccessor;getSign()Lnet/minecraft/block/entity/SignBlockEntity;", shift = At.Shift.BEFORE))
    private void beforeGetSign(OpenScreenEvent event, CallbackInfo info) {
        if (random.get()) {
            text = new String[] {};
            while (text.length < 4) {
                IntStream chars = new Random().ints(0, 0x10FFFF);
                int amount = length.get();
                text = chars.limit(amount * 5L)
                        .mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining())
                        .split("(?<=\\G.{" + amount + "})");
            }
        }
    }
}
