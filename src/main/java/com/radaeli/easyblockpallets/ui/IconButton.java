package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Compact keyboard/mouse button with a required narratable name and an icon glyph. */
public final class IconButton extends UiComponent {
    private final Component accessibleName;
    private final Component icon;
    private final Runnable action;
    private boolean pressed;

    public IconButton(Component accessibleName, Component icon, Runnable action) {
        this.accessibleName = Objects.requireNonNull(accessibleName, "accessibleName");
        this.icon = Objects.requireNonNull(icon, "icon");
        this.action = Objects.requireNonNull(action, "action");
        if (accessibleName.getString().isBlank()) {
            throw new IllegalArgumentException("IconButton requires a non-empty accessible name");
        }
        tooltip(accessibleName);
    }

    public Component accessibleName() { return accessibleName; }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int size = Math.max(theme.tokens().buttonMinimumHeight(),
                font.width(icon) + theme.tokens().buttonPadding().left() + theme.tokens().buttonPadding().right());
        return new UiSize(size, theme.tokens().buttonMinimumHeight()).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPaint.button(context, bounds(), hovered, pressed, isEnabled(), isFocused());
        UiPaint.textCentered(context, icon, bounds().centerX(),
                bounds().centerY() - context.font().lineHeight / 2,
                isEnabled() ? context.theme().palette().text() : context.theme().palette().mutedText());
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
        if (isEnabled() && bounds().contains(mouseX, mouseY)) action.run();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isFocused() || (keyCode != 257 && keyCode != 335 && keyCode != 32)) return false;
        action.run();
        return true;
    }

    @Override
    public boolean isFocusable() { return true; }
}
