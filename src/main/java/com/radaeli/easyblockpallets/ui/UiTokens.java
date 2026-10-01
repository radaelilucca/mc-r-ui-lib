package com.radaeli.easyblockpallets.ui;

/** Shared fixed spacing and sizing values for the first toolkit primitives. */
public record UiTokens(
        Insets panelPadding,
        Insets cardPadding,
        int smallGap,
        int mediumGap,
        int largeGap,
        Insets buttonPadding,
        int buttonMinimumWidth,
        int buttonMinimumHeight
) {
    public UiTokens {
        if (smallGap < 0 || mediumGap < 0 || largeGap < 0
                || buttonMinimumWidth < 0 || buttonMinimumHeight < 0) {
            throw new IllegalArgumentException("UI tokens cannot be negative");
        }
    }
}
