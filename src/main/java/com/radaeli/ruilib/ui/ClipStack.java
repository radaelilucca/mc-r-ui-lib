package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;

/** Overlays children in one viewport and clips their rendering and hit testing to its bounds. */
public final class ClipStack extends UiContainer {
    private final Alignment horizontalAlignment;
    private final Alignment verticalAlignment;

    public ClipStack() {
        this(Alignment.STRETCH, Alignment.STRETCH);
    }

    public ClipStack(Alignment horizontalAlignment, Alignment verticalAlignment) {
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
            int childWidth = horizontalAlignment == Alignment.STRETCH && child.preferredWidth() == null
                    ? bounds().width() : Math.min(size.width(), bounds().width());
            int childHeight = verticalAlignment == Alignment.STRETCH && child.preferredHeight() == null
                    ? bounds().height() : Math.min(size.height(), bounds().height());
            int x = Column.alignedStart(bounds().x(), bounds().width(), childWidth, horizontalAlignment);
            int y = Column.alignedStart(bounds().y(), bounds().height(), childHeight, verticalAlignment);
            child.layout(font, theme, new Bounds(x, y, childWidth, childHeight));
        }
    }

    @Override
    public void render(UiContext context) {
        context.graphics().enableScissor(bounds().x(), bounds().y(), bounds().right(), bounds().bottom());
        try {
            super.render(context);
        } finally {
            context.graphics().disableScissor();
        }
    }
}
