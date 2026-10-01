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
        int screenHeight
) {
    public UiContext {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(font, "font");
        Objects.requireNonNull(theme, "theme");
    }
}
