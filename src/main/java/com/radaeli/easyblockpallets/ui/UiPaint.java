package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Small, theme-aware drawing primitives. Components should use these instead of raw shapes. */
public final class UiPaint {
    private UiPaint() {
    }

    public static void rect(UiContext context, Bounds bounds, int color) {
        if (bounds.width() > 0 && bounds.height() > 0) {
            context.graphics().fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
        }
    }

    public static void outline(UiContext context, Bounds bounds, int color) {
        if (bounds.width() > 0 && bounds.height() > 0) {
            context.graphics().renderOutline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
        }
    }

    public static void panel(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().panel());
        outline(context, bounds, context.theme().palette().border());
    }

    public static void card(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().card());
        outline(context, bounds, context.theme().palette().border());
    }

    public static void divider(UiContext context, Bounds bounds) {
        rect(context, bounds, context.theme().palette().divider());
    }

    public static void button(UiContext context, Bounds bounds, boolean hovered,
                              boolean pressed, boolean enabled, boolean focused) {
        UiPalette palette = context.theme().palette();
        int fill = !enabled ? palette.disabled()
                : pressed ? palette.buttonPressed()
                : hovered ? palette.buttonHover() : palette.button();
        rect(context, bounds, fill);
        outline(context, bounds, focused ? palette.focus() : palette.border());
    }

    public static void toggle(UiContext context, Bounds bounds, boolean selected, boolean hovered,
                              boolean enabled, boolean focused) {
        UiPalette palette = context.theme().palette();
        Insets padding = context.theme().tokens().buttonPadding();
        int trackHeight = Math.max(10, Math.min(14, bounds.height() - padding.top() - padding.bottom()));
        int trackY = bounds.centerY() - trackHeight / 2;
        int trackX = bounds.x() + padding.left();
        int trackColor = !enabled ? palette.disabled()
                : selected ? palette.accent() : hovered ? palette.buttonHover() : palette.button();
        rect(context, new Bounds(trackX, trackY, 24, trackHeight), trackColor);
        outline(context, new Bounds(trackX, trackY, 24, trackHeight),
                focused ? palette.focus() : palette.border());
        int knob = Math.max(6, trackHeight - 4);
        int knobX = selected ? trackX + 24 - knob - 2 : trackX + 2;
        rect(context, new Bounds(knobX, trackY + 2, knob, knob), enabled ? palette.text() : palette.mutedText());
    }

    public static void chip(UiContext context, Bounds bounds, boolean selected, boolean hovered,
                            boolean enabled, boolean focused) {
        UiPalette palette = context.theme().palette();
        int fill = !enabled ? palette.disabled()
                : selected ? palette.accent()
                : hovered ? palette.buttonHover() : palette.card();
        rect(context, bounds, fill);
        outline(context, bounds, focused ? palette.focus() : palette.border());
    }

    public static void tab(UiContext context, Bounds bounds, boolean selected, boolean focused) {
        UiPalette palette = context.theme().palette();
        rect(context, bounds, selected ? palette.card() : palette.panel());
        outline(context, bounds, focused ? palette.focus() : palette.border());
        if (selected) rect(context, new Bounds(bounds.x(), bounds.bottom() - 2, bounds.width(), 2), palette.accent());
    }

    public static void input(UiContext context, Bounds bounds, boolean hovered,
                             boolean enabled, boolean focused) {
        UiPalette palette = context.theme().palette();
        rect(context, bounds, !enabled ? palette.disabled()
                : hovered ? palette.panel() : palette.card());
        outline(context, bounds, focused ? palette.focus() : palette.border());
    }

    public static void text(UiContext context, String value, int x, int y, int color) {
        context.graphics().drawString(context.font(), value, x, y, color, false);
    }

    public static void textCentered(UiContext context, String value, int centerX, int y, int color) {
        context.graphics().drawCenteredString(context.font(), value, centerX, y, color);
    }

    public static void text(UiContext context, net.minecraft.network.chat.Component value,
                            int x, int y, int color) {
        context.graphics().drawString(context.font(), value, x, y, color, false);
    }

    public static void textCentered(UiContext context, net.minecraft.network.chat.Component value,
                                    int centerX, int y, int color) {
        context.graphics().drawCenteredString(context.font(), value, centerX, y, color);
    }
}
