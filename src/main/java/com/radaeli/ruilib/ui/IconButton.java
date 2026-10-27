package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Compact keyboard/mouse button with a required narratable name and an icon glyph. */
public final class IconButton extends UiComponent {
    private final Component accessibleName;
    public enum Symbol { LOCK_OPEN, LOCK_CLOSED }
    private Symbol symbol;
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

    public IconButton(Component accessibleName, Symbol symbol, Runnable action) {
        this(accessibleName, Component.literal(" "), action);
        this.symbol = Objects.requireNonNull(symbol);
    }

    public Component accessibleName() { return accessibleName; }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int size = Math.max(theme.tokens().buttonMinimumHeight(),
                UiText.width(font, icon, theme) + theme.tokens().buttonPadding().left()
                        + theme.tokens().buttonPadding().right());
        return new UiSize(size, theme.tokens().buttonMinimumHeight()).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPaint.button(context, bounds(), animate(context, "hover", hovered ? 1 : 0),
                animate(context, "press", pressed ? 1 : 0), isEnabled(),
                animate(context, "focus", isFocused() ? 1 : 0));
        if (symbol != null) {
            int x=bounds().centerX()-5,y=bounds().centerY()-5;
            int color=isEnabled()?context.theme().palette().text():context.theme().palette().mutedText();
            UiPaint.outline(context,new Bounds(x,y+4,10,7),color);
            UiPaint.outline(context,new Bounds(x+2,y,6,6),color);
            if(symbol==Symbol.LOCK_OPEN) UiPaint.rect(context,new Bounds(x+6,y+2,2,4),context.theme().palette().button());
            UiPaint.rect(context,new Bounds(x+4,y+7,2,2),color);
            return;
        }
        UiPaint.textCentered(context, icon, bounds().centerX(),
                bounds().centerY() - UiText.height(context) / 2,
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
