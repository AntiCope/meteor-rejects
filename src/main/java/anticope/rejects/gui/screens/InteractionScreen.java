package anticope.rejects.gui.screens;

import anticope.rejects.mixin.EntityAccessor;
import anticope.rejects.modules.InteractionMenu;
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;

public class InteractionScreen extends Screen {
    public static Entity interactionMenuEntity;
    private final Entity entity;
    private String focusedString = null;
    private int crosshairX, crosshairY, focusedDot = -1;
    private float yaw, pitch;
    private final Map<String, Consumer<Entity>> functions;
    private final Map<String, String> msgs;

    private final Identifier GUI_ICONS_TEXTURE = new Identifier("textures/gui/icons.png");
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
            functions.put("Open Inventory", e -> {
                closeScreen();
                client.setScreen(new InventoryScreen((PlayerEntity) e));
            });
        } else if (entity instanceof AbstractHorseEntity) {
            functions.put("Open Inventory", e -> {
                closeScreen();
                if (client.player.hasVehicle()) {
                    client.player.networkHandler.sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, true, false)));
                }
                client.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, true, Hand.MAIN_HAND));
                client.player.setSneaking(false);
            });
        } else if (entity instanceof StorageMinecartEntity) {
            functions.put("Open Inventory", e -> {
                closeScreen();
                client.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, true, Hand.MAIN_HAND));
            });
        } else {
            functions.put("Open Inventory", e -> {
                closeScreen();
                ItemStack container = new ItemStack(Items.CHEST);
                container.set(DataComponentTypes.CUSTOM_NAME, e.getName());
                client.setScreen(new PeekScreen(container, getInventory(e)));
            });
        }

        functions.put("Spectate", e -> {
            MinecraftClient.getInstance().setCameraEntity(e);
            client.player.sendMessage(Text.literal("Sneak to un-spectate."), true);
            MeteorClient.EVENT_BUS.subscribe(shiftListener);
            closeScreen();
        });

        if (entity.isGlowing()) {
            functions.put("Remove glow", e -> {
                e.setGlowing(false);
                ((EntityAccessor) e).invokeSetFlag(6, false);
                closeScreen();
            });
        } else {
            functions.put("Glow", e -> {
                e.setGlowing(true);
                ((EntityAccessor) e).invokeSetFlag(6, true);
                closeScreen();
            });
        }

        if (entity.noClip) {
            functions.put("Disable NoClip", e -> {
                entity.noClip = false;
                closeScreen();
            });
        } else {
            functions.put("NoClip", e -> {
                entity.noClip = true;
                closeScreen();
            });
        }

        msgs = Modules.get().get(InteractionMenu.class).messages.get();
        msgs.keySet().forEach(key -> {
            functions.put(key, e -> {
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

        functions.put("Cancel", e -> closeScreen());
    }

    private ItemStack[] getInventory(Entity e) {
        ItemStack[] stack = new ItemStack[27];
        final int[] index = {0};
        if (e instanceof EndermanEntity enderman && enderman.getCarriedBlock() != null) {
            stack[index[0]++] = enderman.getCarriedBlock().getBlock().asItem().getDefaultStack();
        }
        if (e instanceof Saddleable saddleable && saddleable.isSaddled()) {
            stack[index[0]++] = Items.SADDLE.getDefaultStack();
        }
        if (e instanceof LivingEntity living) {
            for (ItemStack item : living.getHandItems()) stack[index[0]++] = item;
            for (ItemStack item : living.getArmorItems()) stack[index[0]++] = item;
        }
        Arrays.fill(stack, index[0], 27, Items.AIR.getDefaultStack());
        return stack;
    }

    @Override
    protected void init() {
        super.init();
        this.cursorMode(GLFW.GLFW_CURSOR_HIDDEN);
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
    }

    private void cursorMode(int mode) {
        KeyBinding.unpressAll();
        InputUtil.setCursorParameters(this.client.getWindow().getHandle(), mode, width / 2.0, height / 2.0);
    }

    @Override
    public void tick() {
        if (Modules.get().get(InteractionMenu.class).keybind.get().isPressed()) close();
    }

    private void closeScreen() {
        client.setScreen(null);
    }

    public void close() {
        cursorMode(GLFW.GLFW_CURSOR_NORMAL);
        if (focusedString != null) functions.get(focusedString).accept(entity);
        else client.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        context.drawTexture(GUI_ICONS_TEXTURE, crosshairX - 8, crosshairY - 8, 0, 0, 15, 15, 256, 256);

        drawDots(context, (int) (Math.min(height, width) / 2 * 0.75), mouseX, mouseY);
        matrix.scale(2f, 2f, 1f);
        context.drawCenteredTextWithShadow(textRenderer, entity.getName(), width / 4, 6, 0xFFFFFFFF);

        Vector2f mouse = getMouseVecs(mouseX, mouseY);
        crosshairX = (int) mouse.x + width / 2;
        crosshairY = (int) mouse.y + height / 2;
        client.player.setYaw(yaw + mouse.x / 3);
        client.player.setPitch(MathHelper.clamp(pitch + mouse.y / 3, -90f, 90f));

        super.render(context, mouseX, mouseY, delta);
    }

    private Vector2f getMouseVecs(int mouseX, int mouseY) {
        int scale = client.options.getGuiScale().getValue();
        if (scale == 0) scale = 4;
        Vector2f mouse = new Vector2f(mouseX, mouseY);
        Vector2f center = new Vector2f(width / 2, height / 2);
        mouse.sub(center).normalize();
        float distance = (float) Math.hypot(mouseX - center.x, mouseY - center.y);
        mouse.mul(Math.min(distance, 1f / scale * 200f));
        return mouse;
    }

    private void drawDots(DrawContext context, int radius, int mouseX, int mouseY) {
        ArrayList<Point> pointList = new ArrayList<>();
        String[] cache = new String[functions.size()];
        double lowestDistance = Double.MAX_VALUE;
        int i = 0;

        for (String string : functions.keySet()) {
            double s = (double) i / functions.size() * 2 * Math.PI;
            int x = (int) Math.round(radius * Math.cos(s) + width / 2);
            int y = (int) Math.round(radius * Math.sin(s) + height / 2);
            drawTextField(context, x, y, string);

            double dist = Math.hypot(x - mouseX, y - mouseY);
            if (dist < lowestDistance) {
                lowestDistance = dist;
                focusedDot = i;
            }

            cache[i] = string;
            pointList.add(new Point(x, y));
            i++;
        }

        for (int j = 0; j < functions.size(); j++) {
            Point point = pointList.get(j);
            if (pointList.get(focusedDot).equals(point)) {
                drawDot(context, point.x - 4, point.y - 4, selectedDotColor);
                focusedString = cache[focusedDot];
            } else {
                drawDot(context, point.x - 4, point.y - 4, dotColor);
            }
        }
    }

    private void drawRect(DrawContext context, int startX, int startY, int width, int height, int colorInner, int colorOuter) {
        context.fill(startX, startY, startX + width, startY + 1, colorOuter);
        context.fill(startX, startY + height, startX + width, startY + height + 1, colorOuter);
        context.fill(startX, startY + 1, startX + 1, startY + height, colorOuter);
        context.fill(startX + width, startY + 1, startX + width + 1, startY + height, colorOuter);
        context.fill(startX + 1, startY + 1, startX + width, startY + height, colorInner);
    }

    private void drawTextField(DrawContext context, int x, int y, String key) {
        if (x >= width / 2) {
            drawRect(context, x + 10, y - 8, textRenderer.getWidth(key) + 3, 15, backgroundColor, borderColor);
            context.drawTextWithShadow(textRenderer, key, x + 12, y - 4, textColor);
        } else {
            drawRect(context, x - 14 - textRenderer.getWidth(key), y - 8, textRenderer.getWidth(key) + 3, 15, backgroundColor, borderColor);
            context.drawTextWithShadow(textRenderer, key, x - 12 - textRenderer.getWidth(key), y - 4, textColor);
        }
    }

    private void drawDot(DrawContext context, int startX, int startY, int colorInner) {
        context.fill(startX + 2, startY, startX + 6, startY + 1, borderColor);
        context.fill(startX + 1, startY + 1, startX + 7, startY + 2, borderColor);
        context.fill(startX + 2, startY + 1, startX + 6, startY + 2, colorInner);
        context.fill(startX, startY + 2, startX + 8, startY + 6, borderColor);
        context.fill(startX + 1, startY + 2, startX + 7, startY + 6, colorInner);
        context.fill(startX + 1, startY + 6, startX + 7, startY + 7, borderColor);
        context.fill(startX + 2, startY + 6, startX + 6, startY + 7, colorInner);
        context.fill(startX + 2, startY + 7, startX + 6, startY + 8, borderColor);
        context.fill(startX + 2, startY + 1, startX + 4, startY + 2, 0x80FFFFFF);
        context.fill(startX + 1, startY + 2, startX + 2, startY + 3, 0x80FFFFFF);
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
