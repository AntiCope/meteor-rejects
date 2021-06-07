/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package cloudburst.rejects.gui.themes.rounded.widgets;

import cloudburst.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import cloudburst.rejects.utils.gui.GuiUtils;
import minegame159.meteorclient.gui.renderer.GuiRenderer;
import minegame159.meteorclient.gui.widgets.WQuad;
import minegame159.meteorclient.utils.render.color.Color;

public class WMeteorQuad extends WQuad {
    public WMeteorQuad(Color color) {
        super(color);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        GuiUtils.quadRounded(renderer, x, y, width, height, color, ((MeteorRoundedGuiTheme)theme).roundAmount());
    }
}
