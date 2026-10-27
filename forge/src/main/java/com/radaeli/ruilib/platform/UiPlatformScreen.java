package com.radaeli.ruilib.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Adapts 1.20.1's vertical scroll event to the toolkit's four-argument API. */
public abstract class UiPlatformScreen extends Screen {
    protected UiPlatformScreen(Component title) { super(title); }
    @Override
    public final boolean mouseScrolled(double x, double y, double delta) {
        return mouseScrolled(x, y, 0, delta);
    }
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return super.mouseScrolled(x, y, scrollY);
    }
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics);
    }
}
