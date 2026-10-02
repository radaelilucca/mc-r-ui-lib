package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** Base node for measurement, layout, rendering and input in a toolkit tree. */
public abstract class UiComponent {
    private Bounds bounds = Bounds.EMPTY;
    private boolean focused;
    private boolean enabled = true;
    private Integer preferredWidth;
    private Integer preferredHeight;
    private Tooltip tooltip;

    public final UiSize measure(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        UiSize intrinsic = measureIntrinsic(font, theme, maxWidth, maxHeight);
        int width = preferredWidth == null ? intrinsic.width() : preferredWidth;
        int height = preferredHeight == null ? intrinsic.height() : preferredHeight;
        return new UiSize(width, height).constrain(maxWidth, maxHeight);
    }

    /** Measures natural content without applying this component's preferred outer size. */
    public final UiSize measureNatural(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return measureIntrinsic(font, theme, maxWidth, maxHeight).constrain(maxWidth, maxHeight);
    }

    protected abstract UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight);

    /** Sets optional exact preferred dimensions; null keeps the intrinsic measurement for that axis. */
    public void setPreferredSize(Integer width, Integer height) {
        if ((width != null && width < 0) || (height != null && height < 0)) {
            throw new IllegalArgumentException("Preferred dimensions cannot be negative");
        }
        preferredWidth = width;
        preferredHeight = height;
    }

    public Integer preferredWidth() {
        return preferredWidth;
    }

    public Integer preferredHeight() {
        return preferredHeight;
    }

    public final void layout(Font font, UiTheme theme, Bounds bounds) {
        this.bounds = bounds;
        layoutChildren(font, theme);
    }

    protected void layoutChildren(Font font, UiTheme theme) {
    }

    public void render(UiContext context) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public UiComponent hitTest(double mouseX, double mouseY) {
        return enabled && bounds.contains(mouseX, mouseY) ? this : null;
    }

    public void visitFocusable(java.util.function.Consumer<UiComponent> visitor) {
        if (isFocusable() && enabled) {
            visitor.accept(this);
        }
    }

    public boolean isFocusable() {
        return false;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused && isFocusable() && enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            focused = false;
        }
    }

    public Bounds bounds() {
        return bounds;
    }

    /** Attaches vanilla-rendered tooltip content to this component. */
    public UiComponent tooltip(Component... lines) {
        this.tooltip = Tooltip.of(lines);
        return this;
    }

    public UiComponent tooltip(Tooltip tooltip) {
        this.tooltip = java.util.Objects.requireNonNull(tooltip, "tooltip");
        return this;
    }

    public Tooltip tooltip() {
        return tooltip;
    }
}
