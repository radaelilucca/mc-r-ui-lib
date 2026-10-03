package com.radaeli.ruilib.ui;

import java.util.Objects;

/** Global transition settings shared by toolkit components. A zero duration disables motion. */
public record UiMotion(int transitionDurationMillis, UiEasing easing) {
    public static final UiMotion DEFAULT = new UiMotion(140, UiEasing.EASE_IN_OUT);

    public UiMotion {
        if (transitionDurationMillis < 0) throw new IllegalArgumentException("Transition duration cannot be negative");
        Objects.requireNonNull(easing, "easing");
    }
}
