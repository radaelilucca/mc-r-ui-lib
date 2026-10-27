package com.radaeli.ruilib.platform;
import com.radaeli.ruilib.ui.UiScreen;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;
public record UiJeiProperties(UiScreen screen) implements IGuiProperties {
    @Override public Class<? extends Screen> screenClass() { return screen.getClass(); }
    @Override public int guiLeft() { return screen.jeiGuiBounds().x(); }
    @Override public int guiTop() { return screen.jeiGuiBounds().y(); }
    @Override public int guiXSize() { return screen.jeiGuiBounds().width(); }
    @Override public int guiYSize() { return screen.jeiGuiBounds().height(); }
    @Override public int screenWidth() { return screen.width; }
    @Override public int screenHeight() { return screen.height; }
}
