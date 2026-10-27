package com.radaeli.ruilib.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.EditBox;

public final class UiPlatform {
    private UiPlatform() { }
    public static ResourceLocation id(String namespace, String path) { return ResourceLocation.tryParse(namespace + ":" + path); }
    public static void tickEditor(EditBox editor) { editor.tick(); }
    public static boolean scroll(UiPlatformScreen screen, double x, double y, double delta) {
        return screen.mouseScrolled(x, y, delta);
    }
    public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.renderOutline(x, y, width, height, color);
    }
}
