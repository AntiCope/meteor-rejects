package anticope.rejects.gui.screens;

import anticope.rejects.mixin.EntityAccessor;
import anticope.rejects.modules.InteractionMenu;
import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.utils.StarscriptError;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
    Ported from: https://github.com/BleachDrinker420/BleachHack/pull/211
*/
public class InteractionScreen extends Screen {

    public static Entity interactionMenuEntity;

    private final Entity entity;
    private String focusedString = null;
    private int crosshairX, crosshairY, focusedDot = -1;
    private float yaw, pitch;
    private final Map<String, Consumer<Entity>> functions;
    private final Map<String, String> msgs;

    private final net.minecraft.resources.Identifier GUI_ICONS_TEXTURE = net.minecraft.resources.Identifier.parse("textures/gui/icons.png");

    private final StaticListener shiftListener = new StaticListener();

    // Style
    private final int selectedDotColor;
    private final int dotColor;
    private final int backgroundColor;
    private final int borderColor;
    private final int textColor;

    public InteractionScreen(Entity e) {
        this(e, Modules.get().get(InteractionMenu.class));
    }

    public InteractionScreen(Entity entity, InteractionMenu module) {
        super(Component.literal("Menu Screen"));
        if (module == null) closeScreen();

        selectedDotColor = module.selectedDotColor.get().getPacked();
        dotColor = module.dotColor.get().getPacked();
        backgroundColor = module.backgroundColor.get().getPacked();
        borderColor = module.borderColor.get().getPacked();
        textColor = module.textColor.get().getPacked();

        this.entity = entity;
        functions = new HashMap<>();
        functions.put("Stats", (Entity e) -> {
            closeScreen();
            minecraft.setScreen(new StatsScreen(e));
        });
        switch (entity) {
            case Player playerEntity -> functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                minecraft.setScreen(new InventoryScreen((Player) e));
            });
            case AbstractHorse abstractHorseEntity -> functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                if (minecraft.player.isHandsBusy()) {
//                    client.player.networkHandler.sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                    minecraft.player.connection.send(new ServerboundPlayerInputPacket(new net.minecraft.world.entity.player.Input(false, false, false, false, false, true, false)));

                }
                minecraft.player.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, true, InteractionHand.MAIN_HAND));
                minecraft.player.setShiftKeyDown(false);
            });
            case AbstractMinecartContainer storageMinecartEntity -> functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                minecraft.player.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, true, InteractionHand.MAIN_HAND));
            });
            case null, default -> functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                ItemStack container = new ItemStack(Items.CHEST);
                container.set(DataComponents.CUSTOM_NAME, e.getName());
                minecraft.setScreen(new PeekScreen(container, getInventory(e)));
            });
        }

        functions.put("Spectate", (Entity e) -> {
            Minecraft.getInstance().setCameraEntity(e);
            minecraft.player.displayClientMessage(Component.literal("Sneak to un-spectate."), true);
            MeteorClient.EVENT_BUS.subscribe(shiftListener);
            closeScreen();
        });

        if (entity.isCurrentlyGlowing()) {
            functions.put("Remove glow", (Entity e) -> {
                e.setGlowingTag(false);
                ((EntityAccessor) e).invokeSetFlag(6, false);
                closeScreen();
            });
        } else {
            functions.put("Glow", (Entity e) -> {
                e.setGlowingTag(true);
                ((EntityAccessor) e).invokeSetFlag(6, true);
                closeScreen();
            });
        }
        if (entity.noPhysics) {
            functions.put("Disable NoClip", (Entity e) -> {
                entity.noPhysics = false;
                closeScreen();
            });
        } else {
            functions.put("NoClip", (Entity e) -> {
                entity.noPhysics = true;
                closeScreen();
            });
        }
        msgs = Modules.get().get(InteractionMenu.class).messages.get();
        msgs.keySet().forEach((key) -> {
            functions.put(key, (Entity e) -> {
                closeScreen();
                interactionMenuEntity = e;
                var result = Parser.parse(msgs.get(key));
                if (result.hasErrors()) {
                    for (Error error : result.errors) MeteorStarscript.printChatError(error);
                    return;
                }
                var script = Compiler.compile(result);
                try {
                    var section = MeteorStarscript.ss.run(script);
                    minecraft.setScreen(new ChatScreen(section.text, false));
                } catch (StarscriptError err) {
                    MeteorStarscript.printChatError(err);
                }
            });
        });
        functions.put("Cancel", (Entity e) -> {
            closeScreen();
        });
    }

    private ItemStack[] getInventory(Entity e) {
        ItemStack[] stack = new ItemStack[27];
        final int[] index = {0};
        if (e instanceof EnderMan) {
            try {
                stack[index[0]] = ((EnderMan) e).getCarriedBlock().getBlock().asItem().getDefaultInstance();
                index[0]++;
            } catch (NullPointerException ex) {
            }
        }
        // Check for saddle in body slot (horses, pigs, striders, etc.)
        LivingEntity a = (LivingEntity) e;
        ItemStack bodyStack = a.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.BODY);
        if (bodyStack != null && !bodyStack.isEmpty()) {
            stack[index[0]] = bodyStack;
            index[0]++;
        }

        // Hand items
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.entity.EquipmentSlot.OFFHAND}) {
            ItemStack itemStack = a.getItemBySlot(slot);
            if (itemStack != null && !itemStack.isEmpty()) {
                stack[index[0]] = itemStack;
                index[0]++;
            }
        }

        // Armor items
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD}) {
            ItemStack itemStack = a.getItemBySlot(slot);
            if (itemStack != null && !itemStack.isEmpty()) {
                stack[index[0]] = itemStack;
                index[0]++;
            }
        }

        for (int i = index[0]; i < 27; i++) stack[i] = Items.AIR.getDefaultInstance();
        return stack;
    }

    public void init() {
        super.init();
        this.cursorMode(GLFW.GLFW_CURSOR_HIDDEN);
        yaw = minecraft.player.getYRot();
        pitch = minecraft.player.getXRot();
    }

    private void cursorMode(int mode) {
        KeyMapping.releaseAll();
        double x = (double) this.minecraft.getWindow().getScreenWidth() / 2;
        double y = (double) this.minecraft.getWindow().getScreenHeight() / 2;
        // InputUtil.setCursorParameters(this.client.getWindow().getHandle(), mode, x, y);
        InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), mode, x, y);
    }

    public void tick() {
        if (Modules.get().get(InteractionMenu.class).keybind.get().isPressed())
            onClose();
    }

    private void closeScreen() {
        minecraft.setScreen(null);
    }

    public void onClose() {
        cursorMode(GLFW.GLFW_CURSOR_NORMAL);
        // This makes the magic
        if (focusedString != null) {
            functions.get(focusedString).accept(this.entity);
        } else
            minecraft.setScreen(null);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Draw crosshair icon (simplified - using drawTexture instead)
        // context.drawGuiTexture(GUI_ICONS_TEXTURE, crosshairX - 8, crosshairY - 8, 0, 0, 15, 15);

        drawDots(context, (int) (Math.min(height, width) / 2 * 0.75), mouseX, mouseY);

        // Draw entity name (without scaling, as Matrix3x2fStack doesn't support push/pop/scale in 3D)
        context.drawCenteredString(font, entity.getName(), width / 2, 12, 0xFFFFFFFF);

        Vector2f mouse = getMouseVecs(mouseX, mouseY);

        this.crosshairX = (int) mouse.x + width / 2;
        this.crosshairY = (int) mouse.y + height / 2;

        minecraft.player.setYRot(yaw + mouse.x / 3);
        minecraft.player.setXRot(Mth.clamp(pitch + mouse.y / 3, -90f, 90f));
        super.render(context, mouseX, mouseY, delta);
    }

    private Vector2f getMouseVecs(int mouseX, int mouseY) {
        int scale = minecraft.options.guiScale().get();
        Vector2f mouse = new Vector2f(mouseX, mouseY);
        Vector2f center = new Vector2f(width / 2, height / 2);
        mouse.sub(center).normalize();

        if (scale == 0) scale = 4;

        // Move crossHair based on distance between mouse and center. But with limit
        if (Math.hypot(width / 2 - mouseX, height / 2 - mouseY) < 1f / scale * 200f)
            mouse.mul((float) Math.hypot(width / 2 - mouseX, height / 2 - mouseY));
        else
            mouse.mul(1f / scale * 200f);
        return mouse;
    }

    private void drawDots(GuiGraphics context, int radius, int mouseX, int mouseY) {
        ArrayList<Point> pointList = new ArrayList<Point>();
        String[] cache = new String[functions.size()];
        double lowestDistance = Double.MAX_VALUE;
        int i = 0;

        for (String string : functions.keySet()) {
            // Just some fancy calculations to get the positions of the dots
            double s = (double) i / functions.size() * 2 * Math.PI;
            int x = (int) Math.round(radius * Math.cos(s) + width / 2);
            int y = (int) Math.round(radius * Math.sin(s) + height / 2);
            drawTextField(context, x, y, string);

            // Calculate lowest distance between mouse and dot
            if (Math.hypot(x - mouseX, y - mouseY) < lowestDistance) {
                lowestDistance = Math.hypot(x - mouseX, y - mouseY);
                focusedDot = i;
            }

            cache[i] = string;
            pointList.add(new Point(x, y));
            i++;
        }

        // Go through all point and if it is focused -> drawing different color, changing closest string value
        for (int j = 0; j < functions.size(); j++) {
            Point point = pointList.get(j);
            if (pointList.get(focusedDot) == point) {
                drawDot(context, point.x - 4, point.y - 4, selectedDotColor);
                this.focusedString = cache[focusedDot];
            } else
                drawDot(context, point.x - 4, point.y - 4, dotColor);
        }
    }

    private void drawRect(GuiGraphics context, int startX, int startY, int width, int height, int colorInner, int colorOuter) {
        context.hLine(startX, startX + width, startY, colorOuter);
        context.hLine(startX, startX + width, startY + height, colorOuter);
        context.vLine(startX, startY, startY + height, colorOuter);
        context.vLine(startX + width, startY, startY + height, colorOuter);
        context.fill(startX + 1, startY + 1, startX + width, startY + height, colorInner);
    }

    private void drawTextField(GuiGraphics context, int x, int y, String key) {
        if (x >= width / 2) {
            drawRect(context, x + 10, y - 8, font.width(key) + 3, 15, backgroundColor, borderColor);
            context.drawString(font, key, x + 12, y - 4, textColor);
        } else {
            drawRect(context, x - 14 - font.width(key), y - 8, font.width(key) + 3, 15, backgroundColor, borderColor);
            context.drawString(font, key, x - 12 - font.width(key), y - 4, textColor);
        }
    }

    // Literally drawing it in code
    private void drawDot(GuiGraphics context, int startX, int startY, int colorInner) {
        // Draw dot itself
        context.hLine(startX + 2, startX + 5, startY, borderColor);
        context.hLine(startX + 1, startX + 6, startY + 1, borderColor);
        context.hLine(startX + 2, startX + 5, startY + 1, colorInner);
        context.fill(startX, startY + 2, startX + 8, startY + 6, borderColor);
        context.fill(startX + 1, startY + 2, startX + 7, startY + 6, colorInner);
        context.hLine(startX + 1, startX + 6, startY + 6, borderColor);
        context.hLine(startX + 2, startX + 5, startY + 6, colorInner);
        context.hLine(startX + 2, startX + 5, startY + 7, borderColor);

        // Draw light overlay
        context.hLine(startX + 2, startX + 3, startY + 1, 0x80FFFFFF);
        context.hLine(startX + 1, startX + 1, startY + 2, 0x80FFFFFF);
    }

    private class StaticListener {
        @EventHandler
        private void onKey(KeyEvent event) {
            if (event.key() == minecraft.options.keyShift.getDefaultKey().getValue()) {
                minecraft.setCameraEntity(minecraft.player);
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}
