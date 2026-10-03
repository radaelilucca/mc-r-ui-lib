package com.radaeli.ruilib.ui;

/** Built-in easing curves available to theme motion tokens. */
public enum UiEasing {
    LINEAR {
        @Override public double apply(double progress) { return progress; }
    },
    EASE_IN_OUT {
        @Override public double apply(double progress) { return progress * progress * (3 - 2 * progress); }
    },
    EASE_OUT {
        @Override public double apply(double progress) { return 1 - (1 - progress) * (1 - progress); }
    };

    public abstract double apply(double progress);
}
