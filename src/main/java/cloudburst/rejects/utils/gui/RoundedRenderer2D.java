package cloudburst.rejects.utils.gui;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class RoundedRenderer2D {

    private static final double circleNone = 0;
    private static final double circleQuarter = Math.PI / 2;
    private static final double circleHalf = circleQuarter * 2;
    private static final double circleThreeQuarter = circleQuarter * 3;

    public static void quadRoundedOutline(Renderer2D  mb, double x, double y, double width, double height, Color color, int r, double s) {
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

    public static void quadRounded(Renderer2D  mb, double x, double y, double width, double height, Color color, int r, boolean roundTop) {
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

    public static void quadRoundedSide(Renderer2D  mb, double x, double y, double width, double height, Color color, int r, boolean right) {
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

    public static void circlePart(Renderer2D  mb, double x, double y, double r, double startAngle, double angle, Color color) {
        int cirDepth = getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        int center = mb.triangles.vec2(x, y).color(color).next();
        int prev = vecOnCircle(mb, x, y, r, startAngle, color);
        for (int i = 1; i < cirDepth + 1; i++) {
            int next = vecOnCircle(mb, x, y, r, startAngle + cirPart * i, color);
            mb.triangles.quad(prev, center, next, next);
            prev = next;
        }
    }

    public static void circlePartOutline(Renderer2D  mb, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        if (outlineWidth >= r) {
            circlePart(mb, x, y, r, startAngle, angle, color);
            return;
        }
        int cirDepth = getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        int innerPrev = vecOnCircle(mb, x, y, r - outlineWidth, startAngle, color);
        int outerPrev = vecOnCircle(mb, x, y, r, startAngle, color);
        for (int i = 1; i < cirDepth + 1; i++) {
            int inner = vecOnCircle(mb, x, y, r - outlineWidth, startAngle + cirPart * i, color);
            int outer = vecOnCircle(mb, x, y, r, startAngle + cirPart * i, color);
            mb.triangles.quad(inner, innerPrev, outerPrev, outer);
            innerPrev = inner;
            outerPrev = outer;
        }
    }

    private static int vecOnCircle(Renderer2D  mb, double x, double y, double r, double angle, Color color) {
        return mb.triangles.vec2(x + Math.sin(angle) * r, y - Math.cos(angle) * r).color(color).next();
    }
}
