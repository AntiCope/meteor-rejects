/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package anticope.rejects.gui.themes.rounded.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;

public class WMeteorPlus extends WPlus implements MeteorWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorRoundedGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.plusColor.get());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.plusColor.get());
    }
}
