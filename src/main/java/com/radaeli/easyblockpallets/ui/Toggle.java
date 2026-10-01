package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

/** Binary switch primitive with mouse and keyboard interaction. */
public final class Toggle extends UiComponent {
    private final Component label;
    private final Consumer<Boolean> onChange;
    private boolean selected;
    private boolean pressed;

    public Toggle(Component label, Consumer<Boolean> onChange) {
        this(label, false, onChange);
    }

    public Toggle(Component label, boolean selected, Consumer<Boolean> onChange) {
        this.label = Objects.requireNonNull(label, "label");
        this.selected = selected;
        this.onChange = Objects.requireNonNull(onChange, "onChange");
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        Insets padding = theme.tokens().buttonPadding();
        int switchWidth = 24;
        int switchHeight = Math.max(12, font.lineHeight - 1);
        return new UiSize(switchWidth + theme.tokens().smallGap() + font.width(label)
                + padding.left() + padding.right(), Math.max(switchHeight, font.lineHeight)
                + padding.top() + padding.bottom()).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPalette palette = context.theme().palette();
        UiPaint.toggle(context, bounds(), selected, hovered, isEnabled(), isFocused());
        int labelX = bounds().x() + context.theme().tokens().buttonPadding().left() + 24
                + context.theme().tokens().smallGap();
        int textY = bounds().centerY() - context.font().lineHeight / 2;
        UiPaint.text(context, label, labelX, textY,
                isEnabled() ? palette.text() : palette.mutedText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || button != 0 || !bounds().contains(mouseX, mouseY)) return false;
        pressed = true;
        setFocused(true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!pressed || button != 0) return false;
        pressed = false;
        if (isEnabled() && bounds().contains(mouseX, mouseY)) toggle();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isFocused() || (keyCode != 257 && keyCode != 335 && keyCode != 32)) return false;
        toggle();
        return true;
    }

    private void toggle() {
        selected = !selected;
        onChange.accept(selected);
    }

    @Override
    public boolean isFocusable() {
        return true;
    }
}
