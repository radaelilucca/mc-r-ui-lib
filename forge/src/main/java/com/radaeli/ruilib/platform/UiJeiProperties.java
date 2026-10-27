package com.radaeli.ruilib.platform;
import com.radaeli.ruilib.ui.UiScreen;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;
public record UiJeiProperties(UiScreen screen) implements IGuiProperties {
    @Override public Class<? extends Screen> getScreenClass() { return screen.getClass(); }
    @Override public int getGuiLeft() { return screen.jeiGuiBounds().x(); }
    @Override public int getGuiTop() { return screen.jeiGuiBounds().y(); }
    @Override public int getGuiXSize() { return screen.jeiGuiBounds().width(); }
    @Override public int getGuiYSize() { return screen.jeiGuiBounds().height(); }
    @Override public int getScreenWidth() { return screen.width; }
    @Override public int getScreenHeight() { return screen.height; }
}
