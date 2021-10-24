/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package anticope.rejects.gui.themes.rounded.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorMultiLabel extends WMultiLabel implements MeteorWidget {
    public WMeteorMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color color = theme().textColor.get();

        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, y + h * i, color, false);
        }
    }
}
