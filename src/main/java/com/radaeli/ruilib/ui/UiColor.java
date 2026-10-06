package com.radaeli.ruilib.ui;

import java.util.Locale;
import java.util.Objects;

/** ARGB color value with named presets and RGB/ARGB factories. */
public record UiColor(int argb) {
    public static final UiColor WHITE = new UiColor(0xFFFFFFFF);
    public static final UiColor BLACK = new UiColor(0xFF000000);
    public static final UiColor RED = new UiColor(0xFFFF0000);
    public static final UiColor GREEN = new UiColor(0xFF008000);
    public static final UiColor BLUE = new UiColor(0xFF0000FF);
    public static final UiColor YELLOW = new UiColor(0xFFFFFF00);
    public static final UiColor CYAN = new UiColor(0xFF00FFFF);
    public static final UiColor MAGENTA = new UiColor(0xFFFF00FF);
    public static final UiColor ORANGE = new UiColor(0xFFFFA500);
    public static final UiColor PURPLE = new UiColor(0xFF800080);
    public static final UiColor PINK = new UiColor(0xFFFFC0CB);
    public static final UiColor BROWN = new UiColor(0xFFA52A2A);
    public static final UiColor GRAY = new UiColor(0xFF808080);
    public static final UiColor SILVER = new UiColor(0xFFC0C0C0);
    public static final UiColor MAROON = new UiColor(0xFF800000);
    public static final UiColor OLIVE = new UiColor(0xFF808000);
    public static final UiColor LIME = new UiColor(0xFF00FF00);
    public static final UiColor TEAL = new UiColor(0xFF008080);
    public static final UiColor NAVY = new UiColor(0xFF000080);
    public static final UiColor TRANSPARENT = new UiColor(0x00000000);

    public static UiColor rgb(int rgb) {
        return new UiColor(0xFF000000 | (rgb & 0x00FFFFFF));
    }

    public static UiColor named(String name) {
        String normalized = Objects.requireNonNull(name, "name").trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "white" -> WHITE;
            case "black" -> BLACK;
            case "red" -> RED;
            case "green" -> GREEN;
            case "blue" -> BLUE;
            case "yellow" -> YELLOW;
            case "cyan", "aqua" -> CYAN;
            case "magenta", "fuchsia" -> MAGENTA;
            case "orange" -> ORANGE;
            case "purple" -> PURPLE;
            case "pink" -> PINK;
            case "brown" -> BROWN;
            case "gray", "grey" -> GRAY;
            case "silver" -> SILVER;
            case "maroon" -> MAROON;
            case "olive" -> OLIVE;
            case "lime" -> LIME;
            case "teal" -> TEAL;
            case "navy" -> NAVY;
            case "transparent" -> TRANSPARENT;
            default -> throw new IllegalArgumentException("Unknown UI color name: " + name);
        };
    }

    /** Parses #RRGGBB as opaque RGB or #AARRGGBB as ARGB. */
    public static UiColor hex(String hex) {
        String value = Objects.requireNonNull(hex, "hex").trim();
        if (value.startsWith("#")) value = value.substring(1);
        if (value.length() != 6 && value.length() != 8) {
            throw new IllegalArgumentException("Color hex must use RRGGBB or AARRGGBB");
        }
        try {
            long parsed = Long.parseLong(value, 16);
            return value.length() == 6 ? rgb((int) parsed) : new UiColor((int) parsed);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid UI color hex: " + hex, exception);
        }
    }
}
