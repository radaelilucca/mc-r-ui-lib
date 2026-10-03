package com.radaeli.ruilib.ui;

import java.util.Objects;

/** Process-wide default theme used by toolkit screens and components without a local override. */
public final class UiThemes {
    private static volatile UiTheme globalTheme = UiTheme.DARK;

    private UiThemes() {
    }

    public static UiTheme globalTheme() {
        return globalTheme;
    }

    /** Sets the default used by screens and components that do not provide a specific theme. */
    public static void setGlobalTheme(UiTheme theme) {
        globalTheme = Objects.requireNonNull(theme, "theme");
    }

    public static void resetGlobalTheme() {
        globalTheme = UiTheme.DARK;
    }
}
