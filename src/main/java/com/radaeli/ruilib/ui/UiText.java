package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** Convenience wrapper that leaves glyph measurement and rendering to Minecraft's Font. */
public final class UiText {
    private UiText() {
    }

    public static int width(Font font, String text) {
        return font.width(text);
    }

    public static int width(Font font, Component text) {
        return font.width(text);
    }

    public static int width(Font font, String text, UiTheme theme) {
        return width(font, text, theme, UiTextStyle.BODY);
    }

    public static int width(Font font, Component text, UiTheme theme) {
        return width(font, text, theme, UiTextStyle.BODY);
    }

    public static int width(Font font, String text, UiTheme theme, UiTextStyle style) {
        return scaled(font.width(text), theme, style);
    }

    public static int width(Font font, Component text, UiTheme theme, UiTextStyle style) {
        return scaled(font.width(text), theme, style);
    }

    public static int height(Font font) {
        return font.lineHeight;
    }

    public static int height(Font font, UiTheme theme) {
        return height(font, theme, UiTextStyle.BODY);
    }

    public static int height(Font font, UiTheme theme, UiTextStyle style) {
        return scaled(font.lineHeight, theme, style);
    }

    public static int width(UiContext context, String text) {
        return width(context.font(), text, context.theme());
    }

    public static int width(UiContext context, Component text) {
        return width(context.font(), text, context.theme());
    }

    public static int height(UiContext context) {
        return height(context.font(), context.theme());
    }

    private static int scaled(int size, UiTheme theme, UiTextStyle style) {
        return Math.max(1, Math.round(size * theme.tokens().typography().scale(style)));
    }

    public static void draw(UiContext context, String text, int x, int y, int color) {
        UiPaint.text(context, text, x, y, color);
    }

    public static void draw(UiContext context, Component text, int x, int y, int color) {
        UiPaint.text(context, text, x, y, color);
    }
}
