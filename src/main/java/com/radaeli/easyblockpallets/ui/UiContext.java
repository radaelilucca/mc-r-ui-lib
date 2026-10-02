package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Objects;

/** Rendering inputs shared by every component during one frame. */
public record UiContext(
        GuiGraphics graphics,
        Font font,
        UiTheme theme,
        double mouseX,
        double mouseY,
        int screenWidth,
        int screenHeight,
        long frameTimeNanos,
        double opacity
) {
    public UiContext(GuiGraphics graphics, Font font, UiTheme theme, double mouseX, double mouseY,
                     int screenWidth, int screenHeight) {
        this(graphics, font, theme, mouseX, mouseY, screenWidth, screenHeight, System.nanoTime());
    }

    public UiContext(GuiGraphics graphics, Font font, UiTheme theme, double mouseX, double mouseY,
                     int screenWidth, int screenHeight, long frameTimeNanos) {
        this(graphics, font, theme, mouseX, mouseY, screenWidth, screenHeight, frameTimeNanos, 1);
    }

    public UiContext {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(font, "font");
        Objects.requireNonNull(theme, "theme");
        opacity = Math.max(0, Math.min(1, opacity));
    }

    public UiContext withOpacity(double opacity) {
        return new UiContext(graphics, font, theme, mouseX, mouseY, screenWidth, screenHeight,
                frameTimeNanos, this.opacity * Math.max(0, Math.min(1, opacity)));
    }
}
