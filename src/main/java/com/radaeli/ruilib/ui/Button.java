package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Mouse and keyboard activatable button primitive. */
public final class Button extends UiComponent {
    private final Component label;
    private final Runnable action;
    private boolean pressed;

    public Button(Component label, Runnable action) {
        this.label = Objects.requireNonNull(label, "label");
        this.action = Objects.requireNonNull(action, "action");
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        Insets padding = theme.tokens().buttonPadding();
        int width = Math.max(theme.tokens().buttonMinimumWidth(),
                UiText.width(font, label, theme) + padding.left() + padding.right());
        int height = Math.max(theme.tokens().buttonMinimumHeight(),
                UiText.height(font, theme) + padding.top() + padding.bottom());
        return new UiSize(width, height).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPaint.button(context, bounds(), animate(context, "hover", hovered ? 1 : 0),
                animate(context, "press", pressed ? 1 : 0), isEnabled(),
                animate(context, "focus", isFocused() ? 1 : 0));
        int color = isEnabled() ? context.theme().palette().text() : context.theme().palette().mutedText();
        UiPaint.textCentered(context, label, bounds().centerX(),
                bounds().centerY() - UiText.height(context) / 2, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || button != 0 || !bounds().contains(mouseX, mouseY)) {
            return false;
        }
        pressed = true;
        setFocused(true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!pressed || button != 0) {
            return false;
        }
        pressed = false;
        if (isEnabled() && bounds().contains(mouseX, mouseY)) {
            activate();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isFocused() || (keyCode != 257 && keyCode != 335 && keyCode != 32)) {
            return false;
        }
        activate();
        return true;
    }

    public void activate() {
        if (isEnabled()) {
            action.run();
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }
}
