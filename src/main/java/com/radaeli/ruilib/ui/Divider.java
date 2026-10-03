package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;

/** Non-interactive themed divider, horizontal by default. */
public final class Divider extends UiComponent {
    public enum Orientation { HORIZONTAL, VERTICAL }

    private final Orientation orientation;
    private final int thickness;
    private final Integer color;

    public Divider() {
        this(Orientation.HORIZONTAL, 1, null);
    }

    public Divider(Orientation orientation) {
        this(orientation, 1, null);
    }

    public Divider(Orientation orientation, int thickness, Integer color) {
        if (thickness < 1) throw new IllegalArgumentException("Divider thickness must be positive");
        this.orientation = java.util.Objects.requireNonNull(orientation, "orientation");
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return (orientation == Orientation.HORIZONTAL
                ? new UiSize(Math.min(maxWidth, 64), thickness)
                : new UiSize(thickness, Math.min(maxHeight, 64)));
    }

    @Override
    public void render(UiContext context) {
        UiPaint.rect(context, bounds(), color == null ? context.theme().palette().divider() : color);
    }
}
