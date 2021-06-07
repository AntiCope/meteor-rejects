/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package cloudburst.rejects.gui.themes.rounded.widgets;

import minegame159.meteorclient.gui.renderer.GuiRenderer;
import cloudburst.rejects.gui.themes.rounded.MeteorWidget;
import minegame159.meteorclient.gui.widgets.WLabel;

public class WMeteorLabel extends WLabel implements MeteorWidget {
    public WMeteorLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            renderer.text(text, x, y, color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get()), title);
        }
    }
}
