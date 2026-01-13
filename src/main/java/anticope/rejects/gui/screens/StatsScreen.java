package anticope.rejects.gui.screens;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;

public class StatsScreen extends WindowScreen {
    public final Entity entity;
    private boolean effectListExpanded = true;
    private boolean attribListExpanded = true;
    private boolean dimensionExpanded = true;
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
        Language lang = ClientLanguage.getInstance();
        add(theme.label(String.format("Type: %s", lang.getOrDefault(entity.getType().getDescriptionId()))));
        add(theme.label(String.format("Age: %d", entity.tickCount)));
        add(theme.label(String.format("UUID: %s", entity.getStringUUID())));
        if (entity instanceof LivingEntity liv) {
            add(theme.label(String.format("Health: %.2f/%.2f", liv.getHealth(), liv.getMaxHealth())));
            add(theme.label(String.format("Armor: %d/20", liv.getArmorValue())));

            WSection effectList = add(theme.section("Status Effects", effectListExpanded)).expandX().widget();
            effectList.action = () -> effectListExpanded = effectList.isExpanded();
            liv.getActiveEffectsMap().forEach((effect, instance) -> {
                String status = lang.getOrDefault(effect.value().getDescriptionId());
                float tps = TickRate.INSTANCE.getTickRate();
                if (instance.getAmplifier() != 0) {
                    status += (String.format(" %d (%s)", instance.getAmplifier()+1, MobEffectUtil.formatDuration(instance, 1, tps)));
                } else {
                    status += (String.format(" (%s)", MobEffectUtil.formatDuration(instance, 1, tps)));
                }
                effectList.add(theme.label(status)).expandX();
            });
            if (liv.getActiveEffectsMap().isEmpty()) {
                effectList.add(theme.label("No effects")).expandX();
            }

            WSection attribList = add(theme.section("Attributes", attribListExpanded)).expandX().widget();
            attribList.action = () -> attribListExpanded = attribList.isExpanded();
            liv.getAttributes().getAttributesToSync().forEach((attrib) -> attribList.add(theme.label(String.format("%s: %.2f",
                lang.getOrDefault(attrib.getAttribute().value().getDescriptionId()),
                attrib.getValue()
            ))).expandX());
        }
        WSection dimension = add(theme.section("Dimensions", dimensionExpanded)).expandX().widget();
        dimension.action = () -> dimensionExpanded = dimension.isExpanded();
        dimension.add(theme.label(String.format("Position: %.2f, %.2f, %.2f", entity.getX(), entity.getY(), entity.getZ()))).expandX();
        dimension.add(theme.label(String.format("Yaw: %.2f, Pitch: %.2f", entity.getYRot(), entity.getXRot()))).expandX();
        AABB box = entity.getBoundingBox();
        dimension.add(theme.label(String.format("Bounding Box: %.2f, %.2f, %.2f",
            box.maxX-box.minX, box.maxY-box.minY, box.maxZ-box.minZ
        ))).expandX();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        updateData();
    }

    @Override
    public void initWidgets() {}
}
