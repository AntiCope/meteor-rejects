package anticope.rejects.mixin.meteor.modules;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mixin(value = AutoSign.class, remap = false)
public class AutoSignMixin {
    // Meteor 26.3: AutoSign#sgGeneral is the group the extra settings belong to,
    // and AutoSign#text is a List<String> (sign packet lines), not a String[4].
    @Shadow
    @Final
    private SettingGroup sgGeneral;

    @Shadow
    private List<String> text;

    private Setting<Boolean> random;
    private Setting<Integer> length;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        random = sgGeneral.add(new BoolSetting.Builder()
                .name("random")
                .description("Spams trash text to make people lag.")
                .defaultValue(false)
                .build()
        );

        length = sgGeneral.add(new IntSetting.Builder()
                .name("random-length")
                .description("Random character length.")
                .defaultValue(500)
                .min(1)
                .sliderMax(1000)
                .build()
        );
    }

    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/mixin/AbstractSignEditScreenAccessor;meteor$getSign()Lnet/minecraft/world/level/block/entity/SignBlockEntity;"))
    private void beforeGetSign(OpenScreenEvent event, CallbackInfo info) {
        if (random.get()) {
            // text is already non-null here: onOpenScreen returns early when text or slot is null.
            List<String> lines = new ArrayList<>(text.size());
            for (int i = 0; i < text.size(); i++) {
                IntStream chars = new Random().ints(0, 0x10FFFF);
                lines.add(chars.limit(length.get() * 5L).mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining()));
            }
            text = lines;
        }
    }
}
