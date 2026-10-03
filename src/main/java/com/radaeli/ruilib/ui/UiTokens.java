package com.radaeli.ruilib.ui;

/** Shared spacing, sizing, and motion defaults for toolkit primitives. */
public record UiTokens(
        Insets panelPadding,
        Insets cardPadding,
        int smallGap,
        int mediumGap,
        int largeGap,
        Insets buttonPadding,
        int buttonMinimumWidth,
        int buttonMinimumHeight,
        UiTypography typography,
        UiRadii radii,
        UiMotion motion
) {
    public UiTokens(Insets panelPadding, Insets cardPadding, int smallGap, int mediumGap, int largeGap,
                    Insets buttonPadding, int buttonMinimumWidth, int buttonMinimumHeight) {
        this(panelPadding, cardPadding, smallGap, mediumGap, largeGap, buttonPadding,
                buttonMinimumWidth, buttonMinimumHeight, UiTypography.DEFAULT, UiRadii.DEFAULT, UiMotion.DEFAULT);
    }

    public UiTokens(Insets panelPadding, Insets cardPadding, int smallGap, int mediumGap, int largeGap,
                    Insets buttonPadding, int buttonMinimumWidth, int buttonMinimumHeight,
                    int transitionDurationMillis) {
        this(panelPadding, cardPadding, smallGap, mediumGap, largeGap, buttonPadding,
                buttonMinimumWidth, buttonMinimumHeight, UiTypography.DEFAULT, UiRadii.DEFAULT,
                new UiMotion(transitionDurationMillis, UiEasing.EASE_IN_OUT));
    }

    public UiTokens {
        if (smallGap < 0 || mediumGap < 0 || largeGap < 0
                || buttonMinimumWidth < 0 || buttonMinimumHeight < 0) {
            throw new IllegalArgumentException("UI tokens cannot be negative");
        }
        java.util.Objects.requireNonNull(panelPadding, "panelPadding");
        java.util.Objects.requireNonNull(cardPadding, "cardPadding");
        java.util.Objects.requireNonNull(buttonPadding, "buttonPadding");
        java.util.Objects.requireNonNull(typography, "typography");
        java.util.Objects.requireNonNull(radii, "radii");
        java.util.Objects.requireNonNull(motion, "motion");
    }

    /** Backwards-compatible accessor; animation settings now live in {@link #motion()}. */
    public int transitionDurationMillis() {
        return motion.transitionDurationMillis();
    }
}
