package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.DoubleConsumer;

/** Horizontal range input supporting pointer dragging and keyboard adjustment. */
public final class Slider extends UiComponent {
    private double minimum;
    private double maximum;
    private double step;
    private double value;
    private final DoubleConsumer onChange;
    private int trackStartX;
    private int trackEndX;
    private boolean dragging;

    public Slider(double minimum, double maximum, double value, DoubleConsumer onChange) {
        this(minimum, maximum, value, 0, onChange);
    }

    /** A step of zero gives a continuous range; positive steps are snapped from the minimum. */
    public Slider(double minimum, double maximum, double value, double step, DoubleConsumer onChange) {
        validateRange(minimum, maximum, step);
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        this.value = normalize(value);
        this.onChange = Objects.requireNonNull(onChange, "onChange");
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }

    public double step() {
        return step;
    }

    public double value() {
        return value;
    }

    public void setRange(double minimum, double maximum) {
        validateRange(minimum, maximum, step);
        this.minimum = minimum;
        this.maximum = maximum;
        setValue(value);
    }

    public void setStep(double step) {
        validateRange(minimum, maximum, step);
        this.step = step;
        setValue(value);
    }

    public void setValue(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Slider value must be finite");
        double normalized = normalize(value);
        if (Double.compare(normalized, this.value) == 0) return;
        this.value = normalized;
        onChange.accept(normalized);
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return new UiSize(140, Math.max(18, font.lineHeight + 8)).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        int decimals = displayDecimals();
        String negativeFraction = decimals == 0 ? "-0" : "-0." + "0".repeat(decimals);
        int readoutWidth = Math.max(font.width(negativeFraction),
                Math.max(font.width(formatForDisplay(minimum)), font.width(formatForDisplay(maximum))));
        trackStartX = bounds().x() + 6;
        trackEndX = Math.max(trackStartX + 1, bounds().right() - readoutWidth - 14);
    }

    @Override
    public void render(UiContext context) {
        double ratio = (value - minimum) / (maximum - minimum);
        int knobX = trackStartX + (int) Math.round((trackEndX - trackStartX) * ratio);
        int trackY = bounds().centerY();
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPalette palette = context.theme().palette();
        int trackColor = isEnabled() ? palette.divider() : palette.disabled();
        UiPaint.rect(context, new Bounds(trackStartX, trackY - 2,
                Math.max(1, trackEndX - trackStartX), 4), trackColor);
        UiPaint.rect(context, new Bounds(trackStartX, trackY - 2,
                Math.max(0, knobX - trackStartX), 4), isEnabled() ? palette.accent() : palette.disabled());
        int knobColor = !isEnabled() ? palette.mutedText()
                : UiPaint.mixColor(palette.text(), palette.buttonHover(), hovered || dragging ? 1 : 0);
        UiPaint.rect(context, new Bounds(knobX - 3, trackY - 6, 7, 12), knobColor);
        UiPaint.outline(context, new Bounds(knobX - 3, trackY - 6, 7, 12),
                isFocused() ? palette.focus() : palette.border());
        UiPaint.text(context, formatForDisplay(value), trackEndX + 8,
                trackY - context.font().lineHeight / 2, isEnabled() ? palette.text() : palette.mutedText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || button != 0 || !bounds().contains(mouseX, mouseY)
                || mouseX < trackStartX - 5 || mouseX > trackEndX + 5) return false;
        setFocused(true);
        dragging = true;
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isEnabled() || !dragging || button != 0) return false;
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging || button != 0) return false;
        dragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isEnabled() || scrollY == 0 || !bounds().contains(mouseX, mouseY)) return false;
        setFocused(true);
        double increment = step > 0 ? step : (maximum - minimum) / 100.0;
        double notches = Math.max(1, Math.round(Math.abs(scrollY)));
        setValue(value + Math.copySign(increment * notches, scrollY));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isFocused()) return false;
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            setValue(minimum);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            setValue(maximum);
            return true;
        }
        double increment = step > 0 ? step : (maximum - minimum) / 100.0;
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) increment *= 10;
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_UP
                || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            setValue(value + increment);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_DOWN
                || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            setValue(value - increment);
            return true;
        }
        return false;
    }

    private void updateFromMouse(double mouseX) {
        double ratio = Math.max(0, Math.min(1,
                (mouseX - trackStartX) / Math.max(1.0, trackEndX - trackStartX)));
        setValue(minimum + ratio * (maximum - minimum));
    }

    private double normalize(double value) {
        double clamped = Math.max(minimum, Math.min(maximum, value));
        if (step == 0) return clamped;
        double steps = (clamped - minimum) / step;
        if (!Double.isFinite(steps) || steps > Long.MAX_VALUE) return clamped;
        double snapped = minimum + Math.round(steps) * step;
        return Math.max(minimum, Math.min(maximum, snapped));
    }

    private static void validateRange(double minimum, double maximum, double step) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum)
                || !Double.isFinite(maximum - minimum) || !Double.isFinite(step)
                || maximum <= minimum || step < 0) {
            throw new IllegalArgumentException("Slider requires finite min < max and a non-negative step");
        }
    }

    private String formatForDisplay(double value) {
        return BigDecimal.valueOf(value).setScale(displayDecimals(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    private int displayDecimals() {
        return step == 0 ? 3 : Math.max(0, Math.min(6,
                BigDecimal.valueOf(step).stripTrailingZeros().scale()));
    }

    @Override
    public boolean isFocusable() {
        return true;
    }
}
