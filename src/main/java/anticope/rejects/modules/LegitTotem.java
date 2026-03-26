package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import java.util.List;
import java.util.ArrayList;

public class LegitTotem extends Module {

    // Variables for delay time and mode
    private int tickCounter = 0;
    private boolean needsTotemSwap = false;
    private VanillaState vanillaState = VanillaState.IDLE;
    private int vanillaTimer = 0;
    private int totemSlot = -1;

    // Variables for module management
    private List<Module> disabledModules = new ArrayList<>();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Time delay before swapping the totem in ticks.")
            .defaultValue(100)
            .min(1)
            .max(400)
            .sliderMax(200)
            .build()
    );

    private final Setting<Mode> modeSetting = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("Mode of switching totem to offhand")
            .defaultValue(Mode.Vanilla)
            .build()
    );

    private final Setting<Boolean> healthCheck = sgGeneral.add(new BoolSetting.Builder()
            .name("health-check")
            .description("Only swap totem when health is low")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> healthThreshold = sgGeneral.add(new IntSetting.Builder()
            .name("health-threshold")
            .description("Health threshold to trigger totem swap")
            .defaultValue(6)
            .min(1)
            .max(20)
            .sliderMax(20)
            .visible(healthCheck::get)
            .build()
    );

    private final Setting<Boolean> onlyWhenEmpty = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-empty")
            .description("Only swap when offhand is empty or doesn't contain a totem")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<Module>> disableModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("disable-modules")
            .description("Modules to temporarily disable during vanilla totem swap")
            .visible(() -> modeSetting.get() == Mode.Vanilla)
            .build()
    );

    public LegitTotem() {
        super(MeteorRejectsAddon.CATEGORY, "legit-totem", "Automatically swaps totems to offhand with realistic delay.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        needsTotemSwap = false;
        vanillaState = VanillaState.IDLE;
        vanillaTimer = 0;
        totemSlot = -1;
        disabledModules.clear();
    }

    @Override
    public void onDeactivate() {
        // Re-enable any disabled modules
        reEnableModules();

        // Schließe Inventar falls es noch offen ist
        if (vanillaState != VanillaState.IDLE && mc.currentScreen instanceof InventoryScreen) {
            mc.player.closeHandledScreen();
        }
        vanillaState = VanillaState.IDLE;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Handle vanilla state machine - höchste Priorität
        if (modeSetting.get() == Mode.Vanilla && vanillaState != VanillaState.IDLE) {
            handleVanillaStateMachine();
            return;
        }

        // Reset needsTotemSwap if conditions are no longer met
        if (needsTotemSwap && !shouldSwapTotem()) {
            needsTotemSwap = false;
            tickCounter = 0;
            return;
        }

        // Check if we need to swap a totem
        if (!needsTotemSwap && shouldSwapTotem()) {
            needsTotemSwap = true;
            tickCounter = 0;
        }

        // If we need to swap and delay has passed
        if (needsTotemSwap) {
            tickCounter++;

            if (tickCounter >= delaySetting.get()) {
                performTotemSwap();
                needsTotemSwap = false;
                tickCounter = 0;
            }
        }
    }

    private void disableSelectedModules() {
        for (Module module : disableModules.get()) {
            if (module.isActive() && !module.name.equals("legit-totem")) {
                module.toggle();
                disabledModules.add(module);
            }
        }
    }

    private void reEnableModules() {
        for (Module module : disabledModules) {
            if (!module.isActive()) {
                module.toggle();
            }
        }
        disabledModules.clear();
    }

    private void handleVanillaStateMachine() {
        vanillaTimer++;

        switch (vanillaState) {
            case OPENING_INVENTORY:
                // !! DIESEN BLOCK ENTFERNEN !!
                // if (modeSetting.get() == Mode.Vanilla && disabledModules.isEmpty()) {
                //     disableSelectedModules();
                // }
                // !! ENDE DES ZU ENTFERNENDEN BLOCKS !!

                // Warte auf Inventar öffnen + erste Hälfte des Timers
                if (vanillaTimer >= delaySetting.get() / 2) {
                    // Totem in Offhand bewegen
                    if (totemSlot != -1) {
                        try {
                            // ... (bestehender Code für ClickSlotC2SPacket und HandSwingC2SPacket) ...
                            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
                                    mc.player.playerScreenHandler.syncId,
                                    mc.player.playerScreenHandler.getRevision(),
                                    totemSlot,
                                    0,
                                    SlotActionType.SWAP,
                                    mc.player.getOffHandStack().copy(),
                                    (Int2ObjectMap<ItemStack>) mc.player.playerScreenHandler.getStacks()
                            ));

                            // Add slight hand swing for realism
                            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        } catch (Exception e) {
                            // Fallback
                            FindItemResult totemResult = InvUtils.find(Items.TOTEM_OF_UNDYING);
                            if (totemResult.found()) {
                                InvUtils.move().from(totemResult.slot()).toOffhand();
                            }
                        }
                    }

                    vanillaState = VanillaState.WAITING_TO_CLOSE;
                    vanillaTimer = 0;
                }
                break;

            case WAITING_TO_CLOSE:
                // ... (bestehender Code) ...
                if (vanillaTimer >= delaySetting.get() / 2) {
                    // Inventar schließen
                    if (mc.currentScreen instanceof InventoryScreen) {
                        mc.player.closeHandledScreen();
                    }

                    // Re-enable modules after inventory is closed
                    reEnableModules();

                    vanillaState = VanillaState.IDLE;
                    vanillaTimer = 0;
                    totemSlot = -1;
                }
                break;
        }
    }

    private boolean shouldSwapTotem() {
        // Don't check while vanilla operation is in progress
        if (vanillaState != VanillaState.IDLE) {
            return false;
        }

        // Check if health condition is met
        if (healthCheck.get() && mc.player.getHealth() + mc.player.getAbsorptionAmount() > healthThreshold.get()) {
            return false;
        }

        // Always check if offhand already has a totem - regardless of onlyWhenEmpty setting
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return false;
        }

        // If onlyWhenEmpty is enabled, also check if offhand is not empty with non-totem item
        if (onlyWhenEmpty.get() && !mc.player.getOffHandStack().isEmpty()) {
            return false;
        }

        // Check if we have a totem in inventory
        FindItemResult totemResult = InvUtils.find(Items.TOTEM_OF_UNDYING);
        return totemResult.found();
    }

    private void performTotemSwap() {
        if (modeSetting.get() == Mode.Silent) {
            swapTotemSilent();
        } else if (modeSetting.get() == Mode.Vanilla) {
            swapTotemVanilla();
        }
    }

    private void swapTotemSilent() {
        FindItemResult totemResult = InvUtils.find(Items.TOTEM_OF_UNDYING);
        if (totemResult.found()) {
            InvUtils.move().from(totemResult.slot()).toOffhand();
        }
    }

    private void swapTotemVanilla() {
        FindItemResult totemResult = InvUtils.find(Items.TOTEM_OF_UNDYING);
        if (!totemResult.found()) return;

        if (mc.player != null && mc.getNetworkHandler() != null) {
            // Store totem slot for later use
            totemSlot = totemResult.slot();

            // NEUE POSITION FÜR DAS DEAKTIVIEREN DER MODULE
            // Module deaktivieren, BEVOR das Inventar geöffnet wird.
            if (modeSetting.get() == Mode.Vanilla && disabledModules.isEmpty()) {
                disableSelectedModules();
            }

            // Open inventory
            mc.setScreen(new InventoryScreen(mc.player));

            // Start vanilla state machine
            vanillaState = VanillaState.OPENING_INVENTORY;
            vanillaTimer = 0;
        }
    }

    @Override
    public String getInfoString() {
        if (vanillaState != VanillaState.IDLE) {
            return modeSetting.get().toString() + " (" + vanillaState.toString().toLowerCase().replace("_", " ") + ")";
        }
        return modeSetting.get().toString();
    }

    // Enum for vanilla state machine
    private enum VanillaState {
        IDLE,
        OPENING_INVENTORY,
        WAITING_TO_CLOSE
    }

    // Enum for Mode selection
    public enum Mode {
        Vanilla("Vanilla"),
        Silent("Silent");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}