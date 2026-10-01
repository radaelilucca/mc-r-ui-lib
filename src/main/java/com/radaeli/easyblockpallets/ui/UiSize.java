package com.radaeli.easyblockpallets.ui;

public record UiSize(int width, int height) {
    public static final UiSize ZERO = new UiSize(0, 0);

    public UiSize {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("UI size cannot be negative");
        }
    }

    public UiSize constrain(int maxWidth, int maxHeight) {
        return new UiSize(Math.min(width, Math.max(0, maxWidth)),
                Math.min(height, Math.max(0, maxHeight)));
    }
}
