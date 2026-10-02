package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Function;

/** Determinate, non-interactive progress indicator. */
public final class ProgressBar extends UiComponent {
    private double minimum;
    private double maximum;
    private double value;
    private int barHeight = 10;
    private Integer trackColor;
    private Integer fillColor;
    private Function<Double, Component> formatter;

    public ProgressBar(double minimum, double maximum, double value) {
        validateFinite(minimum, "minimum");
        validateFinite(maximum, "maximum");
        validateFinite(value, "value");
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = clampValue(value);
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }

    public double value() {
        return value;
    }

    public double progress() {
        double range = maximum - minimum;
        if (!Double.isFinite(range) || range <= 0) return 0;
        return Math.max(0, Math.min(1, (value - minimum) / range));
    }

    /** Invalid or reversed ranges render empty; correcting the range restores the clamped value. */
    public ProgressBar setRange(double minimum, double maximum) {
        validateFinite(minimum, "minimum");
        validateFinite(maximum, "maximum");
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = clampValue(value);
        return this;
    }

    public ProgressBar setValue(double value) {
        validateFinite(value, "value");
        this.value = clampValue(value);
        return this;
    }

    public int barHeight() {
        return barHeight;
    }

    public ProgressBar setBarHeight(int barHeight) {
        if (barHeight < 1 || barHeight > 256) {
            throw new IllegalArgumentException("Progress bar height must be between 1 and 256");
        }
        this.barHeight = barHeight;
        return this;
    }

    public ProgressBar setColors(Integer trackColor, Integer fillColor) {
        this.trackColor = trackColor;
        this.fillColor = fillColor;
        return this;
    }

    /** A null formatter hides the optional text label. */
    public ProgressBar setFormatter(Function<Double, Component> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int height = Math.max(barHeight, formatter == null ? 0 : font.lineHeight + 4);
        return new UiSize(120, height).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        Bounds bounds = bounds();
        int height = Math.min(barHeight, bounds.height());
        if (height <= 0 || bounds.width() <= 0) return;
        int y = bounds.y() + (bounds.height() - height) / 2;
        UiPalette palette = context.theme().palette();
        int track = isEnabled() ? (trackColor == null ? palette.divider() : trackColor) : palette.disabled();
        int fill = isEnabled() ? (fillColor == null ? palette.accent() : fillColor) : palette.mutedText();
        UiPaint.rect(context, new Bounds(bounds.x(), y, bounds.width(), height), track);
        int fillWidth = (int) Math.round(bounds.width() * progress());
        if (fillWidth > 0) UiPaint.rect(context, new Bounds(bounds.x(), y, fillWidth, height), fill);
        if (formatter != null) {
            Component label = Objects.requireNonNull(formatter.apply(value), "formatter result");
            int textX = bounds.centerX() - context.font().width(label) / 2;
            int textY = bounds.centerY() - context.font().lineHeight / 2;
            UiPaint.text(context, label, textX, textY, isEnabled() ? palette.text() : palette.mutedText());
        }
    }

    private double clampValue(double candidate) {
        if (maximum <= minimum) return candidate;
        return Math.max(minimum, Math.min(maximum, candidate));
    }

    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Progress bar " + name + " must be finite");
    }
}
