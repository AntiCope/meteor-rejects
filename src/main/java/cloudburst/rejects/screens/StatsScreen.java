package cloudburst.rejects.screens;

import cloudburst.rejects.modules.InteractionMenu;
import minegame159.meteorclient.systems.modules.Modules;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.util.Language;
import net.minecraft.util.math.Box;

import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.GuiThemes;
import minegame159.meteorclient.gui.WindowScreen;
import minegame159.meteorclient.gui.widgets.containers.WSection;

import java.util.Collection;

public class StatsScreen extends WindowScreen {
    public final Entity entity;
    public StatsScreen(Entity e) {
        super(GuiThemes.get(),e.getName().getString());
        this.entity = e;
        GuiTheme theme = GuiThemes.get();
        add(theme.label(String.format("Age: %d", entity.age)));
        if (entity instanceof LivingEntity) {
            LivingEntity liv = (LivingEntity) entity;
            add(theme.label(String.format("Health: %.2f/%.2f", liv.getHealth(), liv.getMaxHealth())));
            
            Collection<StatusEffectInstance> statusEff = liv.getStatusEffects();
            if (!statusEff.isEmpty()) {
                WSection effectList = add(theme.section("Status Effects", true)).expandCellX().widget();
                Language lang = TranslationStorage.getInstance();
                statusEff.forEach((effect) -> {
                    String status = lang.get(effect.getTranslationKey());
                    if (effect.getAmplifier() != 0) {
                        status += (String.format(" %d (%s)", effect.getAmplifier()+1, StatusEffectUtil.durationToString(effect, 1)));
                    } else {
                        status += (String.format(" (%s)", StatusEffectUtil.durationToString(effect, 1)));
                    }
                    effectList.add(theme.label(status)).expandCellX();
                });
            }
            
        }
        WSection dimension = add(theme.section("Dimensions", false)).expandCellX().widget();
        dimension.add(theme.label(String.format("Position: %.2f, %.2f, %.2f", entity.getX(), entity.getY(), entity.getZ()))).expandCellX();
        dimension.add(theme.label(String.format("Yaw: %.2f, Pitch: %.2f", entity.yaw, entity.pitch))).expandCellX();
        Box box = entity.getBoundingBox();
        dimension.add(theme.label(String.format("Bounding Box: [%.2f, %.2f, %.2f] -> [%.2f, %.2f, %.2f]", 
            box.minX, box.minY, box.minZ,
            box.maxX, box.maxY, box.maxZ
        ))).expandCellX();
    }

    @Override
    protected void onClosed() {
        super.onClosed();
        //Modules.get().get(InteractionMenu.class).isOpen = false;
    }
}
