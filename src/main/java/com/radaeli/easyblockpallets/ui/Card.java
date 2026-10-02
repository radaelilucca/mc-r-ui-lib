package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** Framed content group with an optional title and selectable/hover presentation states. */
public final class Card extends UiFrame {
    private final Component title;
    private boolean hoverEnabled = true;
    private boolean selected;

    public Card(UiComponent content) {
        this(null, content, UiTheme.DARK.tokens().cardPadding());
    }

    public Card(Component title, UiComponent content) {
        this(title, content, UiTheme.DARK.tokens().cardPadding());
    }

    public Card(Component title, UiComponent content, Insets padding) {
        super(content, padding);
        this.title = title;
    }

    public Card setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
        return this;
    }

    public Card setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    @Override
    protected int contentTopOffset(Font font, UiTheme theme) {
        return title == null ? 0 : font.lineHeight + theme.tokens().smallGap();
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        UiSize child = super.measureIntrinsic(font, theme, maxWidth, maxHeight);
        if (title == null) return child;
        int titleWidth = font.width(title) + padding().left() + padding().right();
        return new UiSize(Math.max(child.width(), titleWidth), child.height()).constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        boolean hovered = hoverEnabled && isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPaint.card(context, bounds(), hovered, selected, isFocused());
        if (title != null) {
            UiPaint.text(context, title, bounds().x() + padding().left(),
                    bounds().y() + padding().top(), context.theme().palette().text());
        }
        content().render(context);
    }

    public Component title() {
        return title;
    }

    public UiComponent contentComponent() {
        return content();
    }
}
