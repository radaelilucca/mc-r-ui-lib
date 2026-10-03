package com.radaeli.ruilib.ui;

/** Corner-radius tokens, in GUI pixels. */
public record UiRadii(int small, int medium, int large, int pill) {
    public static final UiRadii DEFAULT = new UiRadii(3, 5, 8, 999);

    public UiRadii {
        if (small < 0 || medium < 0 || large < 0 || pill < 0) {
            throw new IllegalArgumentException("Corner radii cannot be negative");
        }
    }
}
