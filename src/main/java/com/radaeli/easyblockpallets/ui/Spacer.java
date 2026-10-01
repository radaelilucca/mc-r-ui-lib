package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;

/** Empty layout component for fixed spacing or remaining space in a Row or Column. */
public final class Spacer extends UiComponent {
    private final int width;
    private final int height;
    private final boolean flexible;

    private Spacer(int width, int height, boolean flexible) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Spacer dimensions cannot be negative");
        this.width = width;
        this.height = height;
        this.flexible = flexible;
    }

    public static Spacer fixed(int width, int height) {
        return new Spacer(width, height, false);
    }

    /** Creates a square spacer. Use {@link #fixed(int, int)} for different width and height. */
    public static Spacer fixed(int size) {
        return fixed(size, size);
    }

    /** Fills remaining main-axis space when placed in a {@link Row} or {@link Column}. */
    public static Spacer flex() {
        return new Spacer(0, 0, true);
    }

    boolean isFlexible() {
        return flexible;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return new UiSize(flexible ? 0 : width, flexible ? 0 : height).constrain(maxWidth, maxHeight);
    }
}
