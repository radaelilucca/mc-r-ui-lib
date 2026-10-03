package com.radaeli.ruilib.ui;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Immutable description of a short-lived UI notification. Duration is measured in screen ticks. */
public record Toast(Component message, Level level, int durationTicks) {
    public static final int DEFAULT_DURATION_TICKS = 80;

    public Toast {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(level, "level");
        if (durationTicks < 1) {
            throw new IllegalArgumentException("Toast duration must be at least one tick");
        }
    }

    public Toast(Component message, Level level) {
        this(message, level, DEFAULT_DURATION_TICKS);
    }

    public static Toast info(Component message) {
        return new Toast(message, Level.INFO);
    }

    public static Toast success(Component message) {
        return new Toast(message, Level.SUCCESS);
    }

    public static Toast warning(Component message) {
        return new Toast(message, Level.WARNING);
    }

    public static Toast error(Component message) {
        return new Toast(message, Level.ERROR);
    }

    public enum Level {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
}
