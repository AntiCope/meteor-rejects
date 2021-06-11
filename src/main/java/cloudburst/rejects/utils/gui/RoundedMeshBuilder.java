package cloudburst.rejects.utils.gui;

import minegame159.meteorclient.renderer.Renderer2D;
import minegame159.meteorclient.utils.render.color.Color;

public class RoundedRenderer2D {

    private static final double circleNone = 0;
    private static final double circleQuarter = Math.PI / 2;
    private static final double circleHalf = circleQuarter * 2;
    private static final double circleThreeQuarter = circleQuarter * 3;

    public static void quadRoundedOutline(MeshBuilder mb, double x, double y, double width, double height, Color color, int r, double s) {
        r = getR(r, width, height);
        if (r == 0) {
            mb.quad(x, y, width, s, color);
            mb.quad(x, y + height - s, width, s, color);
            mb.quad(x, y + s, s, height - s * 2, color);
            mb.quad(x + width - s, y + s, s, height - s * 2, color);
        }
        else {
            //top
            circlePartOutline(mb, x + r, y + r, r, circleThreeQuarter, circleQuarter, color, s);
            mb.quad(x + r, y, width - r * 2, s, color);
            circlePartOutline(mb, x + width - r, y + r, r, circleNone, circleQuarter, color, s);
            //middle
            mb.quad(x, y + r, s, height - r * 2, color);
            mb.quad(x + width - s, y + r, s, height - r * 2, color);
            //bottom
            circlePartOutline(mb, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color, s);
            mb.quad(x + r, y + height - s, width - r * 2, s, color);
            circlePartOutline(mb, x + r, y + height - r, r, circleHalf, circleQuarter, color, s);
        }
    }

    public static void quadRounded(MeshBuilder mb, double x, double y, double width, double height, Color color, int r, boolean roundTop) {
        r = getR(r, width, height);
        if (r == 0)
            mb.quad(x, y, width, height, color);
        else {
            if (roundTop) {
                //top
                circlePart(mb, x + r, y + r, r, circleThreeQuarter, circleQuarter, color);
                mb.quad(x + r, y, width - 2 * r, r, color);
                circlePart(mb, x + width - r, y + r, r, circleNone, circleQuarter, color);
                //middle
                mb.quad(x, y + r, width, height - 2 * r, color);
            }
            else {
                //middle
                mb.quad(x, y, width, height - r, color);
            }
            //bottom
            circlePart(mb, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color);
            mb.quad(x + r, y + height - r, width - 2 * r, r, color);
            circlePart(mb, x + r, y + height - r, r, circleHalf, circleQuarter, color);
        }
    }

    public static void quadRoundedSide(MeshBuilder mb, double x, double y, double width, double height, Color color, int r, boolean right) {
        r = getR(r, width, height);
        if (r == 0)
            mb.quad(x, y, width, height, color);
        else {
            if (right) {
                circlePart(mb, x + width - r, y + r, r, circleNone, circleQuarter, color);
                circlePart(mb, x + width - r, y + height - r, r, circleQuarter, circleQuarter, color);
                mb.quad(x, y, width - r, height, color);
                mb.quad(x + width - r, y + r, r, height - r * 2, color);
            }
            else {
                circlePart(mb, x + r, y + r, r, circleThreeQuarter, circleQuarter, color);
                circlePart(mb, x + r, y + height - r, r, circleHalf, circleQuarter, color);
                mb.quad(x + r, y, width - r, height, color);
                mb.quad(x, y + r, r, height - r * 2, color);
            }
        }
    }

    private static int getR(int r, double w, double h) {
        if (r * 2 > h) {
            r = (int)h / 2;
        }
        if (r * 2 > w) {
            r = (int)w / 2;
        }
        return r;
    }

    private static int getCirDepth(double r, double angle) {
        return Math.max(1, (int)(angle * r / circleQuarter));
    }

    public static void circlePart(MeshBuilder mb, double x, double y, double r, double startAngle, double angle, Color color) {
        int cirDepth = getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        vert2(mb,x + Math.sin(startAngle) * r, y - Math.cos(startAngle) * r, color);
        for (int i = 1; i < cirDepth + 1; i++) {
            double xV = x + Math.sin(startAngle + cirPart * i) * r;
            double yV = y - Math.cos(startAngle + cirPart * i) * r;
            vert2(mb, xV, yV, color);
            if (i != cirDepth)
                vert2(mb, xV, yV, color);
        }
    }

    public static void circlePartOutline(MeshBuilder mb, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        int cirDepth = getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        for (int i = 0; i < cirDepth; i++) {
            double xOC = x + Math.sin(startAngle + cirPart * i) * r;
            double yOC = y - Math.cos(startAngle + cirPart * i) * r;
            double xIC = x + Math.sin(startAngle + cirPart * i) * (r - outlineWidth);
            double yIC = y - Math.cos(startAngle + cirPart * i) * (r - outlineWidth);
            double xON = x + Math.sin(startAngle + cirPart * (i + 1)) * r;
            double yON = y - Math.cos(startAngle + cirPart * (i + 1)) * r;
            double xIN = x + Math.sin(startAngle + cirPart * (i + 1)) * (r - outlineWidth);
            double yIN = y - Math.cos(startAngle + cirPart * (i + 1)) * (r - outlineWidth);
            //
            vert2(mb, xOC, yOC, color);
            vert2(mb, xON, yON, color);
            vert2(mb, xIC, yIC, color);
            //
            vert2(mb, xIC, yIC, color);
            vert2(mb, xON, yON, color);
            vert2(mb, xIN, yIN, color);
        }
    }

    public static void circlePartOutline(MeshBuilder mb, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        int cirDepth = getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        for (int i = 0; i < cirDepth; i++) {
            double xOC = x + Math.sin(startAngle + cirPart * i) * r;
            double yOC = y - Math.cos(startAngle + cirPart * i) * r;
            double xIC = x + Math.sin(startAngle + cirPart * i) * (r - outlineWidth);
            double yIC = y - Math.cos(startAngle + cirPart * i) * (r - outlineWidth);
            double xON = x + Math.sin(startAngle + cirPart * (i + 1)) * r;
            double yON = y - Math.cos(startAngle + cirPart * (i + 1)) * r;
            double xIN = x + Math.sin(startAngle + cirPart * (i + 1)) * (r - outlineWidth);
            double yIN = y - Math.cos(startAngle + cirPart * (i + 1)) * (r - outlineWidth);

            triangles.quad(
                triangles.vec2(xOC, yOC).color(color).next(),
                triangles.vec2(xON, yON).color(color).next(),
                triangles.vec2(xIC, yIC).color(color).next(),
                triangles.vec2(xIN, yIN).color(color).next()
            );
        }
    }
}
