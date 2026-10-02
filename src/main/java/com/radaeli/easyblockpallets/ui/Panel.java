package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;

/** Theme-aware framed content region. Put an explicit Row/Column/Grid inside for child layout. */
public final class Panel extends UiFrame {
    public Panel(UiComponent content) {
        this(content, UiTheme.DARK.tokens().panelPadding());
    }

    public Panel(UiComponent content, Insets padding) {
        super(content, padding);
    }

    public UiComponent contentComponent() {
        return content();
    }

    @Override
    public void render(UiContext context) {
        UiPaint.panel(context, bounds());
        content().render(context);
    }
}
