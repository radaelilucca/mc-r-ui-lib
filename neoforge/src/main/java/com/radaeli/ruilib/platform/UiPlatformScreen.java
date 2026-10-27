package com.radaeli.ruilib.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Native 1.21.1 boundary for shared toolkit screens. */
public abstract class UiPlatformScreen extends Screen {
    protected UiPlatformScreen(Component title) { super(title); }
}
