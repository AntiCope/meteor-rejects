package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.modules.ShieldBypass;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(value = KillAura.class, remap = false)
public class KillAuraMixin extends Module {
    @Shadow
    @Final
    private SettingGroup sgGeneral;
    @Shadow
    @Final
    private SettingGroup sgTargeting;
    @Shadow
    @Final
    private Setting<Boolean> onlyOnLook;
    @Shadow
    private int hitTimer;
    @Shadow
    @Final
    private SettingGroup sgTiming;
    @Shadow
    @Final
    private Setting<Boolean> customDelay;

    private final Random random = new Random();
    private Setting<Double> fov;
    private Setting<Boolean> ignoreInvisible;
    private Setting<Boolean> randomTeleport;
    private Setting<Double> hitChance;
    private Setting<Integer> randomDelayMax;

    public KillAuraMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        fov = sgGeneral.add(new DoubleSetting.Builder()
                .name("fov")
                .description("Will only aim entities in the fov.")
                .defaultValue(360)
                .min(0)
                .max(360)
                .build()
        );

        ignoreInvisible = sgTargeting.add(new BoolSetting.Builder()
                .name("ignore-invisible")
                .description("Whether or not to attack invisible entities.")
                .defaultValue(false)
                .build()
        );

        randomTeleport = sgGeneral.add(new BoolSetting.Builder()
                .name("random-teleport")
                .description("Randomly teleport around the target.")
                .defaultValue(false)
                .visible(() -> !onlyOnLook.get())
                .build()
        );

        hitChance = sgGeneral.add(new DoubleSetting.Builder()
                .name("hit-chance")
                .description("The probability of your hits landing.")
                .defaultValue(100)
                .range(1, 100)
                .sliderRange(1, 100)
                .build()
        );

        randomDelayMax = sgTiming.add(new IntSetting.Builder()
                .name("random-delay-max")
                .description("The maximum value for random delay.")
                .defaultValue(4)
                .min(0)
                .sliderMax(20)
                .visible(customDelay::get)
                .build()
        );
    }

    @Inject(method = "entityCheck", at = @At(value = "RETURN", ordinal = 14), cancellable = true)
    private void onReturn(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (ignoreInvisible.get() && entity.isInvisible()) info.setReturnValue(false);
        if (!RejectsUtils.inFov(entity, fov.get())) info.setReturnValue(false);
        info.setReturnValue(info.getReturnValueZ());
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity entity, CallbackInfo info) {
        if (hitChance.get() < 100 && Math.random() > hitChance.get() / 100) info.cancel();
    }

    @Inject(method = "onTick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onTick(TickEvent.Pre event, CallbackInfo ci, Entity primary) {
        if (randomTeleport.get() && !onlyOnLook.get()) {
            mc.player.setPosition(primary.getX() + randomOffset(), primary.getY(), primary.getZ() + randomOffset());
        }
    }

    @Inject(method = "attack", at = @At(value = "TAIL"))
    private void modifyHitDelay(CallbackInfo info) {
        if (randomDelayMax.get() == 0) return;
        hitTimer -= random.nextInt(randomDelayMax.get());
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    private void onHit(Entity target, CallbackInfo info) {
        ShieldBypass shieldBypass = Modules.get().get(ShieldBypass.class);
        if (shieldBypass.isActive()) {
            Cancellable dummyEvent = new Cancellable();
            shieldBypass.bypass(target, dummyEvent);
            if (dummyEvent.isCancelled()) {
                hitTimer = 0;
                info.cancel();
            }
        }
    }

    private double randomOffset() {
        return Math.random() * 4 - 2;
    }
}
