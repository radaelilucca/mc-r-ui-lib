package com.radaeli.easyblockpallets.ui;

/** Immutable rectangle in Minecraft GUI-scaled coordinates. */
public record Bounds(int x, int y, int width, int height) {
    public static final Bounds EMPTY = new Bounds(0, 0, 0, 0);

    public Bounds {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Bounds dimensions cannot be negative");
        }
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
    }

    public Bounds inset(Insets insets) {
        int insetX = Math.min(width, insets.left());
        int insetY = Math.min(height, insets.top());
        int insetRight = Math.min(width - insetX, insets.right());
        int insetBottom = Math.min(height - insetY, insets.bottom());
        return new Bounds(x + insetX, y + insetY,
                width - insetX - insetRight, height - insetY - insetBottom);
    }
}
