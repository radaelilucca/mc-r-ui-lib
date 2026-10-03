package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Single-line text primitive rendered and measured by the Minecraft font. */
public final class Label extends UiComponent {
    private final Component text;
    private final Integer color;

    public Label(Component text) {
        this.text = Objects.requireNonNull(text, "text");
        this.color = null;
    }

    public Label(Component text, int color) {
        this.text = Objects.requireNonNull(text, "text");
        this.color = color;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return new UiSize(UiText.width(font, text, theme), UiText.height(font, theme))
                .constrain(maxWidth, maxHeight);
    }

    @Override
    public void render(UiContext context) {
        int textColor = color != null ? color : context.theme().palette().text();
        UiPaint.text(context, text, bounds().x(), bounds().y(), textColor);
    }
}
