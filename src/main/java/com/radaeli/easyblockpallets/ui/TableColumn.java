package com.radaeli.easyblockpallets.ui;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Function;

/** Column definition for a {@link Table}; width zero measures the header and row values. */
public record TableColumn<T>(Component title, int width, Alignment alignment,
                             Function<T, Component> renderer) {
    public TableColumn {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(alignment, "alignment");
        Objects.requireNonNull(renderer, "renderer");
        if (width < 0) throw new IllegalArgumentException("Column width cannot be negative");
    }

    public static <T> TableColumn<T> fixed(Component title, int width,
                                            Alignment alignment, Function<T, Component> renderer) {
        if (width < 1) throw new IllegalArgumentException("Fixed column width must be positive");
        return new TableColumn<>(title, width, alignment, renderer);
    }

    public static <T> TableColumn<T> preferred(Component title, Alignment alignment,
                                                Function<T, Component> renderer) {
        return new TableColumn<>(title, 0, alignment, renderer);
    }
}
