/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.utils.gui.GuiUtils;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

public class WMeteorWindow extends WWindow implements MeteorWidget {
    public WMeteorWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            GuiUtils.quadRounded(renderer,x , y + header.height / 2, width, height - header.height / 2, theme().backgroundColor.get(), ((MeteorRoundedGuiTheme)theme).roundAmount(), false);
        }
    }

    private class WMeteorHeader extends WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            GuiUtils.quadRounded(renderer, this, theme().accentColor.get(), ((MeteorRoundedGuiTheme)theme).roundAmount());
        }
    }
}
