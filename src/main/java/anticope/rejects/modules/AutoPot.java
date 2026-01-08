package anticope.rejects.modules;

//import baritone.api.BaritoneAPI;

import anticope.rejects.MeteorRejectsAddon;
import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import java.util.ArrayList;
import java.util.List;

public class AutoPot extends Module {
    @SuppressWarnings("unchecked")
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<MobEffect>> usablePotions = sgGeneral.add(new StatusEffectListSetting.Builder()
            .name("potions-to-use")
            .description("The potions to use.")
            .defaultValue(
                MobEffects.INSTANT_HEALTH.value(),
                MobEffects.STRENGTH.value()
            )
            .build()
    );

    private final Setting<Boolean> useSplashPots = sgGeneral.add(new BoolSetting.Builder()
            .name("splash-potions")
            .description("Allow the use of splash potions")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
            .name("health")
            .description("If health goes below this point, Healing potions will trigger.")
            .defaultValue(15)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Boolean> pauseAuras = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-auras")
            .description("Pauses all auras when eating.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> pauseBaritone = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-baritone")
            .description("Pause baritone when eating.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> lookDown = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Forces you to rotate downwards when throwing splash potions.")
            .defaultValue(true)
            .build()
    );

    private int slot, prevSlot;
    private boolean drinking, splashing;
    private final List<Class<? extends Module>> wasAura = new ArrayList<>();
    private boolean wasBaritone;

    public AutoPot() {
        super(MeteorRejectsAddon.CATEGORY, "auto-pot", "Automatically Drinks Potions");
    }

    // TODO : Add option to scan whole inv - then either swap item to hotbar if full or just place in first empty slot
    // Note, Sometimes two or multiple splash pots are thrown - since the effect is not instant, the second pot is thrown before the effect of first is applied
    @Override
    public void onDeactivate() {
        stopPotionUsage();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.isUsingItem()) return;
        for (MobEffect statusEffect : usablePotions.get()) {
            Holder<MobEffect> registryEntry = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(statusEffect);
            if (!mc.player.hasEffect(registryEntry)) {
                slot = potionSlot(statusEffect);
                if (slot != -1) {
                    if (registryEntry == MobEffects.INSTANT_HEALTH && ShouldDrinkHealth()) {
                        startPotionUse();
                        return;
                    } else if (registryEntry == MobEffects.INSTANT_HEALTH) {
                        return;
                    }
                    startPotionUse();
                }
            }
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (drinking) event.target = null;
    }

    private void setPressed(boolean pressed) {
        mc.options.keyUse.setDown(pressed);
    }

    private void drink() {
        changeSlot(slot);
        setPressed(true);
        if (!mc.player.isUsingItem()) Utils.rightClick();

        drinking = true;
    }

    private void splash() {
        changeSlot(slot);
        setPressed(true);
        splashing = true;
    }

    private void stopPotionUsage() {
        changeSlot(prevSlot);
        setPressed(false);
        drinking = false;
        splashing = false;

        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (wasAura.contains(klass) && !module.isActive()) {
                    module.toggle();
                }
            }
        }
        if (pauseBaritone.get() && wasBaritone) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
        }
    }

    private double trueHealth() {
        assert mc.player != null;
        return mc.player.getHealth();
    }

    private void changeSlot(int slot) {
        mc.player.getInventory().setSelectedSlot(slot);
        this.slot = slot;
    }

    //Sunk 7 hours into these checks, if i die blame checks
    private int potionSlot(MobEffect statusEffect) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == Items.POTION || (stack.getItem() == Items.SPLASH_POTION && useSplashPots.get())) {
                PotionContents effects = stack.getComponents().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                for (MobEffectInstance effectInstance : effects.getAllEffects()) {
                    if (effectInstance.getDescriptionId().equals(statusEffect.getDescriptionId())) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }

    private void startPotionUse() {
        prevSlot = mc.player.getInventory().getSelectedSlot();

        if (useSplashPots.get()) {
            if (lookDown.get()) {
                Rotations.rotate(mc.player.getYRot(), 90);
                splash();
            } else {
                splash();
            }
        } else {
            drink();
        }
        wasAura.clear();
        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (module.isActive()) {
                    wasAura.add(klass);
                    module.toggle();
                }
            }
        }
        wasBaritone = false;
        if (pauseBaritone.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            wasBaritone = true;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
        }
    }

    private boolean ShouldDrinkHealth() {
        return trueHealth() < health.get();
    }
}
