package cloudburst.rejects.utils.gui;

import cloudburst.rejects.mixin.meteor.GuiRendererAccessor;
import minegame159.meteorclient.gui.renderer.GuiRenderer;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.rendering.MeshBuilder;
import minegame159.meteorclient.utils.render.color.Color;

public class GuiUtils {
    public static void quadRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round, boolean roundTop) {
        MeshBuilder mb = ((GuiRendererAccessor)renderer).getMeshbuilder();
        RoundedMeshBuilder.quadRounded(mb, x, y, width, height, color, round, roundTop);
    }
    public static void quadRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round) {
        quadRounded(renderer, x, y, width, height, color, round, true);
    }
    public static void quadRounded(GuiRenderer renderer, WWidget widget, Color color, int round) {
        quadRounded(renderer, widget.x, widget.y, widget.width, widget.height, color, round);
    }
    public static void quadOutlineRounded(GuiRenderer renderer, double x, double y, double width, double height, Color color, int round, double s) {
        MeshBuilder mb = ((GuiRendererAccessor)renderer).getMeshbuilder();
        RoundedMeshBuilder.quadRoundedOutline(mb, x, y, width, height, color, round, s);
    }
    public static void quadOutlineRounded(GuiRenderer renderer, WWidget widget, Color color, int round, double s) {
        quadOutlineRounded(renderer, widget.x, widget.y, widget.width, widget.height, color, round, s);
    }
    public static void quadRoundedSide(GuiRenderer renderer, double x, double y, double width, double height, Color color, int r, boolean right) {
        MeshBuilder mb = ((GuiRendererAccessor)renderer).getMeshbuilder();
        RoundedMeshBuilder.quadRoundedSide(mb, x, y, width, height, color, r, right);
    }
    public static void quadRoundedSide(GuiRenderer renderer, WWidget widget, Color color, int round, boolean right) {
        quadRoundedSide(renderer, widget.x, widget.y, widget.width, widget.height, color, round, right);
    }
    public static void circlePart(GuiRenderer renderer, double x, double y, double r, double startAngle, double angle, Color color) {
        MeshBuilder mb = ((GuiRendererAccessor)renderer).getMeshbuilder();
        RoundedMeshBuilder.circlePart(mb, x, y, r, startAngle, angle, color);
    }
    public static void circlePartOutline(GuiRenderer renderer, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        MeshBuilder mb = ((GuiRendererAccessor)renderer).getMeshbuilder();
        RoundedMeshBuilder.circlePartOutline(mb, x, y, r, startAngle, angle, color, outlineWidth);
    }
}
