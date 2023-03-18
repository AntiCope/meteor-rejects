package anticope.rejects.gui.screens;

import anticope.rejects.mixin.EntityAccessor;
import anticope.rejects.modules.InteractionMenu;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
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
        super(Text.literal("Menu Screen"));
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
            client.setScreen(new StatsScreen(e));
        });
        if (entity instanceof PlayerEntity) {
            functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                client.setScreen(new InventoryScreen((PlayerEntity) e));
            });
        } else if (entity instanceof AbstractHorseEntity) {
            functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                if (client.player.isRiding()) {
                    client.player.networkHandler.sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                }
                client.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, true, Hand.MAIN_HAND));
                client.player.setSneaking(false);
            });
        } else if (entity instanceof StorageMinecartEntity) {
            functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                client.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, true, Hand.MAIN_HAND));
            });
        } else {
            functions.put("Open Inventory", (Entity e) -> {
                closeScreen();
                ItemStack container = new ItemStack(Items.CHEST);
                container.setCustomName(e.getName());
                client.setScreen(new PeekScreen(container, getInventory(e)));
            });
        }

        functions.put("Spectate", (Entity e) -> {
            MinecraftClient.getInstance().setCameraEntity(e);
            client.player.sendMessage(Text.literal("Sneak to un-spectate."), true);
            MeteorClient.EVENT_BUS.subscribe(shiftListener);
            closeScreen();
        });

        if (entity.isGlowing()) {
            functions.put("Remove glow", (Entity e) -> {
                e.setGlowing(false);
                ((EntityAccessor) e).invokeSetFlag(6, false);
                closeScreen();
            });
        } else {
            functions.put("Glow", (Entity e) -> {
                e.setGlowing(true);
                ((EntityAccessor) e).invokeSetFlag(6, true);
                closeScreen();
            });
        }
        if (entity.noClip) {
            functions.put("Disable NoClip", (Entity e) -> {
                entity.noClip = false;
                closeScreen();
            });
        } else {
            functions.put("NoClip", (Entity e) -> {
                entity.noClip = true;
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
                    client.setScreen(new ChatScreen(section.text));
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
        if (e instanceof EndermanEntity) {
            try {
                stack[index[0]] = ((EndermanEntity) e).getCarriedBlock().getBlock().asItem().getDefaultStack();
                index[0]++;
            } catch (NullPointerException ex) {
            }
        }
        if (Saddleable.class.isInstance(e)) {
            if (((Saddleable) e).isSaddled()) {
                stack[index[0]] = Items.SADDLE.getDefaultStack();
                index[0]++;
            }
        }
        e.getHandItems().forEach(itemStack -> {
            if (itemStack != null) {
                stack[index[0]] = itemStack;
                index[0]++;
            }
        });
        e.getArmorItems().forEach(itemStack -> {
            if (itemStack != null) {
                stack[index[0]] = itemStack;
                index[0]++;
            }
        });
        for (int i = index[0]; i < 27; i++) stack[i] = Items.AIR.getDefaultStack();
        return stack;
    }

    public void init() {
        super.init();
        this.cursorMode(GLFW.GLFW_CURSOR_HIDDEN);
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
    }

    private void cursorMode(int mode) {
        KeyBinding.unpressAll();
        double x = (double) (this.client.getWindow().getWidth() / 2);
        double y = (double) (this.client.getWindow().getHeight() / 2);
        InputUtil.setCursorParameters(this.client.getWindow().getHandle(), mode, x, y);
    }

    public void tick() {
        if (Modules.get().get(InteractionMenu.class).keybind.get().isPressed())
            close();
    }

    private void closeScreen() {
        client.setScreen((Screen) null);
    }

    public void close() {
        cursorMode(GLFW.GLFW_CURSOR_NORMAL);
        // This makes the magic
        if (focusedString != null) {
            functions.get(focusedString).accept(this.entity);
        } else
            client.setScreen((Screen) null);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        // Fake crosshair stuff
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        drawTexture(matrix, crosshairX - 8, crosshairY - 8, 0, 0, 15, 15);

        drawDots(matrix, (int) (Math.min(height, width) / 2 * 0.75), mouseX, mouseY);
        matrix.scale(2f, 2f, 1f);
        drawCenteredTextWithShadow(matrix, textRenderer, entity.getName(), width / 4, 6, 0xFFFFFFFF);

        int scale = client.options.getGuiScale().getValue();
        Vector2 mouse = new Vector2(mouseX, mouseY);
        Vector2 center = new Vector2(width / 2, height / 2);
        mouse.subtract(center);
        mouse.normalize();
        Vector2 cross = mouse;

        if (scale == 0)
            scale = 4;

        // Move crossHair based on distance between mouse and center. But with limit
        if (Math.hypot(width / 2 - mouseX, height / 2 - mouseY) < 1f / scale * 200f)
            mouse.multiply((float) Math.hypot(width / 2 - mouseX, height / 2 - mouseY));
        else
            mouse.multiply(1f / scale * 200f);

        this.crosshairX = (int) mouse.x + width / 2;
        this.crosshairY = (int) mouse.y + height / 2;

        client.player.setYaw(yaw + cross.x / 3);
        client.player.setPitch(MathHelper.clamp(pitch + cross.y / 3, -90f, 90f));
        super.render(matrix, mouseX, mouseY, delta);
    }


    private void drawDots(MatrixStack matrix, int radius, int mouseX, int mouseY) {
        ArrayList<Point> pointList = new ArrayList<Point>();
        String cache[] = new String[functions.size()];
        double lowestDistance = Double.MAX_VALUE;
        int i = 0;

        for (String string : functions.keySet()) {
            // Just some fancy calculations to get the positions of the dots
            double s = (double) i / functions.size() * 2 * Math.PI;
            int x = (int) Math.round(radius * Math.cos(s) + width / 2);
            int y = (int) Math.round(radius * Math.sin(s) + height / 2);
            drawTextField(matrix, x, y, string);

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
                drawDot(matrix, point.x - 4, point.y - 4, selectedDotColor);
                this.focusedString = cache[focusedDot];
            } else
                drawDot(matrix, point.x - 4, point.y - 4, dotColor);
        }
    }

    private void drawRect(MatrixStack matrix, int startX, int startY, int width, int height, int colorInner, int colorOuter) {
        drawHorizontalLine(matrix, startX, startX + width, startY, colorOuter);
        drawHorizontalLine(matrix, startX, startX + width, startY + height, colorOuter);
        drawVerticalLine(matrix, startX, startY, startY + height, colorOuter);
        drawVerticalLine(matrix, startX + width, startY, startY + height, colorOuter);
        fill(matrix, startX + 1, startY + 1, startX + width, startY + height, colorInner);
    }

    private void drawTextField(MatrixStack matrix, int x, int y, String key) {
        if (x >= width / 2) {
            drawRect(matrix, x + 10, y - 8, textRenderer.getWidth(key) + 3, 15, backgroundColor, borderColor);
            drawTextWithShadow(matrix, textRenderer, key, x + 12, y - 4, textColor);
        } else {
            drawRect(matrix, x - 14 - textRenderer.getWidth(key), y - 8, textRenderer.getWidth(key) + 3, 15, backgroundColor, borderColor);
            drawTextWithShadow(matrix, textRenderer, key, x - 12 - textRenderer.getWidth(key), y - 4, textColor);
        }
    }

    // Literally drawing it in code
    private void drawDot(MatrixStack matrix, int startX, int startY, int colorInner) {
        // Draw dot itself
        drawHorizontalLine(matrix, startX + 2, startX + 5, startY, borderColor);
        drawHorizontalLine(matrix, startX + 1, startX + 6, startY + 1, borderColor);
        drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 1, colorInner);
        fill(matrix, startX, startY + 2, startX + 8, startY + 6, borderColor);
        fill(matrix, startX + 1, startY + 2, startX + 7, startY + 6, colorInner);
        drawHorizontalLine(matrix, startX + 1, startX + 6, startY + 6, borderColor);
        drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 6, colorInner);
        drawHorizontalLine(matrix, startX + 2, startX + 5, startY + 7, borderColor);

        // Draw light overlay
        drawHorizontalLine(matrix, startX + 2, startX + 3, startY + 1, 0x80FFFFFF);
        drawHorizontalLine(matrix, startX + 1, startX + 1, startY + 2, 0x80FFFFFF);
    }

    private class StaticListener {
        @EventHandler
        private void onKey(KeyEvent event) {
            if (client.options.sneakKey.matchesKey(event.key, 0) || client.options.sneakKey.matchesMouse(event.key)) {
                client.setCameraEntity(client.player);
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}


// Creating my own Vector class beacause I couldn´t find a good one in minecrafts code
class Vector2 {
    float x, y;

    Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    void normalize() {
        float mag = getMag();
        if (mag != 0 && mag != 1)
            divide(mag);
    }

    void subtract(Vector2 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
    }

    void divide(float n) {
        x /= n;
        y /= n;
    }

    void multiply(float n) {
        x *= n;
        y *= n;
    }

    private float getMag() {
        return (float) Math.sqrt(x * x + y * y);
    }
}
