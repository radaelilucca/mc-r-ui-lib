package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;

public final class Row extends UiContainer {
    private final int gap;
    private final Alignment horizontalAlignment;
    private final Alignment verticalAlignment;

    public Row(int gap) {
        this(gap, Alignment.START, Alignment.CENTER);
    }

    public Row(int gap, Alignment horizontalAlignment, Alignment verticalAlignment) {
        if (gap < 0) throw new IllegalArgumentException("gap cannot be negative");
        this.gap = gap;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return measureChildren(font, theme, maxWidth, maxHeight, gap, true);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        int count = children().size();
        if (count == 0) return;
        int innerWidth = Math.max(0, bounds().width() - gap * (count - 1));
        UiSize[] sizes = new UiSize[count];
        int totalWidth = 0;
        int flexibleCount = 0;
        for (int i = 0; i < count; i++) {
            int remaining = Math.max(0, innerWidth - totalWidth);
            sizes[i] = children().get(i).measure(font, theme, remaining, bounds().height());
            totalWidth += sizes[i].width();
            if (isFlexibleSpacer(children().get(i))) flexibleCount++;
        }
        int flexibleSpace = Math.max(0, innerWidth - totalWidth);
        int flexibleBase = flexibleCount == 0 ? 0 : flexibleSpace / flexibleCount;
        int flexibleRemainder = flexibleCount == 0 ? 0 : flexibleSpace % flexibleCount;
        int x = Column.alignedStart(bounds().x(), bounds().width(),
                totalWidth + flexibleSpace + gap * (count - 1), horizontalAlignment);
        for (int i = 0; i < count; i++) {
            UiComponent child = children().get(i);
            UiSize size = sizes[i];
            if (isFlexibleSpacer(child)) {
                int extra = flexibleBase + (flexibleRemainder > 0 ? 1 : 0);
                if (flexibleRemainder > 0) flexibleRemainder--;
                size = new UiSize(size.width() + extra, size.height());
            }
            int height = verticalAlignment == Alignment.STRETCH && child.preferredHeight() == null
                    ? bounds().height() : size.height();
            height = Math.min(height, bounds().height());
            int y = Column.alignedStart(bounds().y(), bounds().height(), height, verticalAlignment);
            child.layout(font, theme, new Bounds(x, y, size.width(), height));
            x += size.width() + gap;
        }
    }

    private static boolean isFlexibleSpacer(UiComponent component) {
        return component instanceof Spacer spacer && spacer.isFlexible();
    }
}
