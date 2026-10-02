package com.radaeli.easyblockpallets.ui;

import java.util.Objects;

/** Immutable theme bundle; screens can supply another theme without changing components. */
public record UiTheme(UiPalette palette, UiTokens tokens) {
    public static final UiTheme DARK = new UiTheme(
            new UiPalette(
                    0xE6101319, 0xF01B2029, 0xF5222933, 0xFF3B4655,
                    0xFF35404D, 0xFFF0F3F7, 0xFFADB7C5, 0xFF67B7F7,
                    0xFF303A47, 0xFF3C4B5C, 0xFF26313D, 0xFF252A31,
                    0xFF8BCBFF),
            new UiTokens(Insets.all(8), Insets.all(6), 4, 8, 16,
                    Insets.symmetric(10, 5), 64, 20, 140));

    public UiTheme {
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(tokens, "tokens");
    }
}
