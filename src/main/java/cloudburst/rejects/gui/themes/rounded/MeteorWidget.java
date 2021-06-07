/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package cloudburst.rejects.gui.themes.rounded;

import cloudburst.rejects.utils.gui.GuiUtils;
import minegame159.meteorclient.gui.renderer.GuiRenderer;
import minegame159.meteorclient.gui.utils.BaseWidget;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.utils.render.color.Color;

public interface MeteorWidget extends BaseWidget {
    default MeteorRoundedGuiTheme theme() {
        return (MeteorRoundedGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        MeteorRoundedGuiTheme theme = theme();
        int r = theme.roundAmount();
        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
        GuiUtils.quadRounded(renderer, widget, theme.backgroundColor.get(pressed, mouseOver), r);
        GuiUtils.quadOutlineRounded(renderer, widget, outlineColor, r, theme.scale(2));
    }
}
