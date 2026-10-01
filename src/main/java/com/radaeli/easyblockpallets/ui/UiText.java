package com.radaeli.easyblockpallets.ui;

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

    public static int height(Font font) {
        return font.lineHeight;
    }

    public static void draw(UiContext context, String text, int x, int y, int color) {
        UiPaint.text(context, text, x, y, color);
    }

    public static void draw(UiContext context, Component text, int x, int y, int color) {
        UiPaint.text(context, text, x, y, color);
    }
}
