package com.radaeli.ruilib.ui;

/** Scales for the toolkit's semantic text styles. Vanilla glyphs remain the source font. */
public record UiTypography(float smallScale, float bodyScale, float headingScale, float titleScale) {
    public static final UiTypography DEFAULT = new UiTypography(0.9f, 1.0f, 1.15f, 1.3f);

    public UiTypography {
        if (!valid(smallScale) || !valid(bodyScale) || !valid(headingScale) || !valid(titleScale)) {
            throw new IllegalArgumentException("Font scales must be finite and greater than zero");
        }
    }

    public float scale(UiTextStyle style) {
        return switch (style) {
            case SMALL -> smallScale;
            case BODY -> bodyScale;
            case HEADING -> headingScale;
            case TITLE -> titleScale;
        };
    }

    private static boolean valid(float scale) {
        return Float.isFinite(scale) && scale > 0;
    }
}
