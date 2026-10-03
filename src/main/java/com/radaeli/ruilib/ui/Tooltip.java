package com.radaeli.ruilib.ui;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

/** Immutable tooltip content rendered by Minecraft's native tooltip pipeline. */
public record Tooltip(List<Component> lines) {
    public Tooltip {
        lines = List.copyOf(lines);
        if (lines.isEmpty()) throw new IllegalArgumentException("Tooltip requires at least one line");
    }

    public static Tooltip of(Component... lines) {
        return new Tooltip(Arrays.asList(lines));
    }
}
