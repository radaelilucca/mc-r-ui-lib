package com.radaeli.easyblockpallets.ui;

/** Pure geometry helpers for clockwise radial menus starting at twelve o'clock. */
public final class RadialGeometry {
    private static final double FULL_TURN = Math.PI * 2;

    private RadialGeometry() {
    }

    public record Point(double x, double y) {
    }

    public static Point center(Bounds bounds) {
        return new Point(bounds.centerX(), bounds.centerY());
    }

    public static double outerRadius(Bounds bounds) {
        return Math.max(0, Math.min(bounds.width(), bounds.height()) / 2.0 - 1);
    }

    public static double innerRadius(double outerRadius, double deadZoneFraction) {
        if (!Double.isFinite(outerRadius) || outerRadius <= 0
                || !Double.isFinite(deadZoneFraction)) return 0;
        return outerRadius * Math.max(0, Math.min(0.9, deadZoneFraction));
    }

    public static double segmentSweep(int segmentCount) {
        return segmentCount <= 0 ? 0 : FULL_TURN / segmentCount;
    }

    public static double segmentCenterAngle(int index, int segmentCount) {
        if (segmentCount <= 0 || index < 0 || index >= segmentCount) {
            throw new IllegalArgumentException("Segment index must be within a non-empty radial menu");
        }
        return -Math.PI / 2 + (index + 0.5) * segmentSweep(segmentCount);
    }

    /** Returns -1 inside the dead zone, beyond the outer radius, or for an empty menu. */
    public static int segmentAt(double x, double y, Point center, double innerRadius,
                                double outerRadius, int segmentCount) {
        if (segmentCount <= 0 || !Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(innerRadius) || !Double.isFinite(outerRadius)
                || outerRadius <= 0 || outerRadius < innerRadius) return -1;
        double dx = x - center.x();
        double dy = y - center.y();
        double distanceSquared = dx * dx + dy * dy;
        if (distanceSquared < innerRadius * innerRadius || distanceSquared > outerRadius * outerRadius) {
            return -1;
        }
        double angleFromTop = Math.atan2(dy, dx) + Math.PI / 2;
        double normalizedAngle = (angleFromTop % FULL_TURN + FULL_TURN) % FULL_TURN;
        return Math.min(segmentCount - 1, (int) (normalizedAngle / segmentSweep(segmentCount)));
    }
}
