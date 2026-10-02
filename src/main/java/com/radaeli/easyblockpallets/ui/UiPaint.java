package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Small, theme-aware drawing primitives. Components should use these instead of raw shapes. */
public final class UiPaint {
    private UiPaint() {
    }

    public static void rect(UiContext context, Bounds bounds, int color) {
        if (bounds.width() > 0 && bounds.height() > 0) {
            context.graphics().fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(),
                    withAlpha(color, context.opacity()));
        }
    }

    public static void outline(UiContext context, Bounds bounds, int color) {
        if (bounds.width() > 0 && bounds.height() > 0) {
            context.graphics().renderOutline(bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                    withAlpha(color, context.opacity()));
        }
    }

    public static void panel(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().panel());
        outline(context, bounds, context.theme().palette().border());
    }

    public static void panel(UiContext context, Bounds bounds, double opacity) {
        rect(context, bounds, withAlpha(context.theme().palette().panel(), opacity));
        outline(context, bounds, withAlpha(context.theme().palette().border(), opacity));
    }

    public static void card(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().card());
        outline(context, bounds, context.theme().palette().border());
    }

    public static void card(UiContext context, Bounds bounds, boolean hovered,
                            boolean selected, boolean focused) {
        card(context, bounds, hovered ? 1 : 0, selected ? 1 : 0, focused ? 1 : 0);
    }

    public static void card(UiContext context, Bounds bounds, double hover, double selected, double focused) {
        UiPalette palette = context.theme().palette();
        int fill = mixColor(mixColor(palette.card(), palette.panel(), hover), palette.buttonPressed(), selected);
        rect(context, bounds, fill);
        int border = mixColor(mixColor(palette.border(), palette.accent(), selected), palette.focus(), focused);
        outline(context, bounds, border);
    }

    public static void divider(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().divider());
    }

    public static void button(UiContext context, Bounds bounds, boolean hovered,
                              boolean pressed, boolean enabled, boolean focused) {
        button(context, bounds, hovered ? 1 : 0, pressed ? 1 : 0, enabled, focused ? 1 : 0);
    }

    public static void button(UiContext context, Bounds bounds, double hover, double pressed,
                              boolean enabled, double focused) {
        UiPalette palette = context.theme().palette();
        int fill = !enabled ? palette.disabled()
                : mixColor(mixColor(palette.button(), palette.buttonHover(), hover), palette.buttonPressed(), pressed);
        rect(context, bounds, fill);
        outline(context, bounds, mixColor(palette.border(), palette.focus(), focused));
    }

    public static void toggle(UiContext context, Bounds bounds, boolean selected, boolean hovered,
                              boolean enabled, boolean focused) {
        toggle(context, bounds, selected ? 1 : 0, hovered ? 1 : 0, enabled, focused ? 1 : 0);
    }

    public static void toggle(UiContext context, Bounds bounds, double selected, double hovered,
                             boolean enabled, double focused) {
        UiPalette palette = context.theme().palette();
        Insets padding = context.theme().tokens().buttonPadding();
        int trackHeight = Math.max(10, Math.min(14, bounds.height() - padding.top() - padding.bottom()));
        int trackY = bounds.centerY() - trackHeight / 2;
        int trackX = bounds.x() + padding.left();
        int trackColor = !enabled ? palette.disabled()
                : mixColor(mixColor(palette.button(), palette.buttonHover(), hovered), palette.accent(), selected);
        rect(context, new Bounds(trackX, trackY, 24, trackHeight), trackColor);
        outline(context, new Bounds(trackX, trackY, 24, trackHeight),
                mixColor(palette.border(), palette.focus(), focused));
        int knob = Math.max(6, trackHeight - 4);
        int knobX = (int) Math.round(trackX + 2 + selected * (24 - knob - 4));
        rect(context, new Bounds(knobX, trackY + 2, knob, knob), enabled ? palette.text() : palette.mutedText());
    }

    public static void chip(UiContext context, Bounds bounds, boolean selected, boolean hovered,
                            boolean enabled, boolean focused) {
        chip(context, bounds, selected ? 1 : 0, hovered ? 1 : 0, enabled, focused ? 1 : 0);
    }

    public static void chip(UiContext context, Bounds bounds, double selected, double hovered,
                            boolean enabled, double focused) {
        UiPalette palette = context.theme().palette();
        int fill = !enabled ? palette.disabled()
                : mixColor(mixColor(palette.card(), palette.buttonHover(), hovered), palette.accent(), selected);
        rect(context, bounds, fill);
        outline(context, bounds, mixColor(palette.border(), palette.focus(), focused));
    }

    public static void tab(UiContext context, Bounds bounds, boolean selected, boolean focused) {
        tab(context, bounds, selected ? 1 : 0, focused ? 1 : 0);
    }

    public static void tab(UiContext context, Bounds bounds, double selected, double focused) {
        UiPalette palette = context.theme().palette();
        rect(context, bounds, mixColor(palette.panel(), palette.card(), selected));
        outline(context, bounds, mixColor(palette.border(), palette.focus(), focused));
        if (selected > 0) rect(context, new Bounds(bounds.x(), bounds.bottom() - 2, bounds.width(), 2),
                withAlpha(palette.accent(), selected));
    }

    public static void input(UiContext context, Bounds bounds, boolean hovered,
                             boolean enabled, boolean focused) {
        input(context, bounds, hovered ? 1 : 0, enabled, focused ? 1 : 0);
    }

    public static void input(UiContext context, Bounds bounds, double hovered,
                             boolean enabled, double focused) {
        UiPalette palette = context.theme().palette();
        rect(context, bounds, !enabled ? palette.disabled()
                : mixColor(palette.card(), palette.panel(), hovered));
        outline(context, bounds, mixColor(palette.border(), palette.focus(), focused));
    }

    public static int mixColor(int from, int to, double progress) {
        double amount = Math.max(0, Math.min(1, progress));
        int a = interpolateChannel(from >>> 24, to >>> 24, amount);
        int r = interpolateChannel((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, amount);
        int g = interpolateChannel((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, amount);
        int b = interpolateChannel(from & 0xFF, to & 0xFF, amount);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int withAlpha(int color, double opacity) {
        int alpha = interpolateChannel(0, color >>> 24, opacity);
        return color & 0x00FFFFFF | alpha << 24;
    }

    private static int interpolateChannel(int from, int to, double amount) {
        return (int) Math.round(from + (to - from) * amount);
    }

    public static void text(UiContext context, String value, int x, int y, int color) {
        context.graphics().drawString(context.font(), value, x, y, withAlpha(color, context.opacity()), false);
    }

    public static void textCentered(UiContext context, String value, int centerX, int y, int color) {
        context.graphics().drawCenteredString(context.font(), value, centerX, y,
                withAlpha(color, context.opacity()));
    }

    public static void text(UiContext context, net.minecraft.network.chat.Component value,
                            int x, int y, int color) {
        context.graphics().drawString(context.font(), value, x, y, withAlpha(color, context.opacity()), false);
    }

    public static void textCentered(UiContext context, net.minecraft.network.chat.Component value,
                                    int centerX, int y, int color) {
        context.graphics().drawCenteredString(context.font(), value, centerX, y,
                withAlpha(color, context.opacity()));
    }
}
