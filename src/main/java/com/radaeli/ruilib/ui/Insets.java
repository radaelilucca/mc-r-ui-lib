package com.radaeli.ruilib.ui;

/** Insets ordered clockwise from the top edge. */
public record Insets(int top, int right, int bottom, int left) {
    public static final Insets ZERO = new Insets(0, 0, 0, 0);

    public Insets {
        if (top < 0 || right < 0 || bottom < 0 || left < 0) {
            throw new IllegalArgumentException("Insets cannot be negative");
        }
    }

    public static Insets all(int value) {
        return new Insets(value, value, value, value);
    }

    public static Insets symmetric(int horizontal, int vertical) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }
}
