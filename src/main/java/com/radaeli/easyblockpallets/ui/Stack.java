package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;

/** Overlays children in insertion order; later children render and receive input on top. */
public final class Stack extends UiContainer {
    private final Alignment horizontalAlignment;
    private final Alignment verticalAlignment;

    public Stack() {
        this(Alignment.STRETCH, Alignment.STRETCH);
    }

    public Stack(Alignment horizontalAlignment, Alignment verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int width = 0;
        int height = 0;
        for (UiComponent child : children()) {
            UiSize size = child.measure(font, theme, maxWidth, maxHeight);
            width = Math.max(width, size.width());
            height = Math.max(height, size.height());
        }
        return new UiSize(width, height).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        for (UiComponent child : children()) {
            UiSize size = child.measure(font, theme, bounds().width(), bounds().height());
            int width = horizontalAlignment == Alignment.STRETCH && child.preferredWidth() == null
                    ? bounds().width() : size.width();
            int height = verticalAlignment == Alignment.STRETCH && child.preferredHeight() == null
                    ? bounds().height() : size.height();
            width = Math.min(width, bounds().width());
            height = Math.min(height, bounds().height());
            int x = Column.alignedStart(bounds().x(), bounds().width(), width, horizontalAlignment);
            int y = Column.alignedStart(bounds().y(), bounds().height(), height, verticalAlignment);
            child.layout(font, theme, new Bounds(x, y, width, height));
        }
    }
}
