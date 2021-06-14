package cloudburst.rejects.gui.screens;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.util.Language;
import net.minecraft.util.math.Box;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;

public class StatsScreen extends WindowScreen {
    public final Entity entity;
    private boolean effectListExpanded = true;
    private boolean attribListExpanded = true;
    private boolean dimensionExpanded = false;
    public StatsScreen(Entity e) {
        super(GuiThemes.get(),e.getName().getString());
        this.entity = e;
        updateData();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    protected void onClosed() {
        MeteorClient.EVENT_BUS.unsubscribe(this);
        super.onClosed();
    }

    private void updateData() {
        clear();
        GuiTheme theme = GuiThemes.get();
        Language lang = TranslationStorage.getInstance();
        add(theme.label(String.format("Type: %s", lang.get(entity.getType().getTranslationKey()))));
        add(theme.label(String.format("Age: %d", entity.age)));
        add(theme.label(String.format("UUID: %s", entity.getUuidAsString())));
        if (entity instanceof LivingEntity) {
            LivingEntity liv = (LivingEntity) entity;
            add(theme.label(String.format("Health: %.2f/%.2f", liv.getHealth(), liv.getMaxHealth())));
            add(theme.label(String.format("Armor: %d/20", liv.getArmor())));
            
            WSection effectList = add(theme.section("Status Effects", effectListExpanded)).expandX().widget();
            effectList.action = () -> {
                effectListExpanded = effectList.isExpanded();
            };
            liv.getActiveStatusEffects().forEach((effect, instance) -> {
                String status = lang.get(effect.getTranslationKey());
                if (instance.getAmplifier() != 0) {
                    status += (String.format(" %d (%s)", instance.getAmplifier()+1, StatusEffectUtil.durationToString(instance, 1)));
                } else {
                    status += (String.format(" (%s)", StatusEffectUtil.durationToString(instance, 1)));
                }
                effectList.add(theme.label(status)).expandX();
            });
            if (liv.getActiveStatusEffects().isEmpty()) {
                effectList.add(theme.label("No effects")).expandX();
            }
            
            WSection attribList = add(theme.section("Attributes", attribListExpanded)).expandX().widget();
            attribList.action = () -> {
                attribListExpanded = attribList.isExpanded();
            };
            liv.getAttributes().getTracked().forEach((attrib) -> {
                attribList.add(theme.label(String.format("%s: %.2f",
                    lang.get(attrib.getAttribute().getTranslationKey()),
                    attrib.getValue()
                ))).expandX();
            });
        }
        WSection dimension = add(theme.section("Dimensions", dimensionExpanded)).expandX().widget();
        dimension.action = () -> {
            dimensionExpanded = dimension.isExpanded();
        };
        dimension.add(theme.label(String.format("Position: %.2f, %.2f, %.2f", entity.getX(), entity.getY(), entity.getZ()))).expandX();
        dimension.add(theme.label(String.format("Yaw: %.2f, Pitch: %.2f", entity.getYaw(), entity.getPitch()))).expandX();
        Box box = entity.getBoundingBox();
        dimension.add(theme.label(String.format("Bounding Box: %.2f, %.2f, %.2f", 
            box.maxX-box.minX, box.maxY-box.minY, box.maxZ-box.minZ
        ))).expandX();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        updateData();
    }
}
