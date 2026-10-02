package com.radaeli.easyblockpallets.ui;

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
        int transitionDurationMillis
) {
    public UiTokens(Insets panelPadding, Insets cardPadding, int smallGap, int mediumGap, int largeGap,
                    Insets buttonPadding, int buttonMinimumWidth, int buttonMinimumHeight) {
        this(panelPadding, cardPadding, smallGap, mediumGap, largeGap, buttonPadding,
                buttonMinimumWidth, buttonMinimumHeight, 140);
    }

    public UiTokens {
        if (smallGap < 0 || mediumGap < 0 || largeGap < 0
                || buttonMinimumWidth < 0 || buttonMinimumHeight < 0 || transitionDurationMillis < 0) {
            throw new IllegalArgumentException("UI tokens cannot be negative");
        }
    }
}
