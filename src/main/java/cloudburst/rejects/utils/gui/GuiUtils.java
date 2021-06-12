package cloudburst.rejects.utils.gui;

import cloudburst.rejects.mixin.meteor.GuiRendererAccessor;
import minegame159.meteorclient.gui.renderer.GuiRenderer;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.renderer.Renderer2D;
import minegame159.meteorclient.utils.render.color.Color;

public class GuiUtils {
    public static void quadRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round, boolean roundTop) {
        Renderer2D mb = ((GuiRendererAccessor)renderer).getRenderer2D();
        RoundedRenderer2D.quadRounded(mb, x, y, width, height, color, round, roundTop);
    }
    public static void quadRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round) {
        quadRounded(renderer, x, y, width, height, color, round, true);
    }
    public static void quadRounded(GuiRenderer renderer, WWidget widget, Color color, int round) {
        quadRounded(renderer, widget.x, widget.y, widget.width, widget.height, color, round);
    }
    public static void quadOutlineRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round, double s) {
        Renderer2D mb = ((GuiRendererAccessor)renderer).getRenderer2D();
        RoundedRenderer2D.quadRoundedOutline(mb, x, y, width, height, color, round, s);
    }
    public static void quadOutlineRounded(GuiRenderer renderer, WWidget widget, Color color, int round, double s) {
        quadOutlineRounded(renderer, widget.x, widget.y, widget.width, widget.height, color, round, s);
    }
    public static void quadRoundedSide(GuiRenderer renderer, double x, double y, double width, double height, Color color, int r, boolean right) {
        Renderer2D mb = ((GuiRendererAccessor)renderer).getRenderer2D();
        RoundedRenderer2D.quadRoundedSide(mb, x, y, width, height, color, r, right);
    }
    public static void quadRoundedSide(GuiRenderer renderer, WWidget widget, Color color, int round, boolean right) {
        quadRoundedSide(renderer, widget.x, widget.y, widget.width, widget.height, color, round, right);
    }
    public static void circlePart(GuiRenderer renderer, double x, double y, double r, double startAngle, double angle, Color color) {
        Renderer2D mb = ((GuiRendererAccessor)renderer).getRenderer2D();
        RoundedRenderer2D.circlePart(mb, x, y, r, startAngle, angle, color);
    }
    public static void circlePartOutline(GuiRenderer renderer, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        Renderer2D mb = ((GuiRendererAccessor)renderer).getRenderer2D();
        RoundedRenderer2D.circlePartOutline(mb, x, y, r, startAngle, angle, color, outlineWidth);
    }
}
