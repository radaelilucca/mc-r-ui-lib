package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;

public final class Column extends UiContainer {
    private final Integer gapOverride;
    private final Alignment verticalAlignment;
    private final Alignment horizontalAlignment;

    public Column() {
        this(null, Alignment.START, Alignment.STRETCH);
    }

    public Column(int gap) {
        this(gap, Alignment.START, Alignment.STRETCH);
    }

    public Column(int gap, Alignment verticalAlignment, Alignment horizontalAlignment) {
        if (gap < 0) throw new IllegalArgumentException("gap cannot be negative");
        this.gapOverride = gap;
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
    }

    private Column(Integer gap, Alignment verticalAlignment, Alignment horizontalAlignment) {
        this.gapOverride = gap;
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return measureChildren(font, theme, maxWidth, maxHeight, gap(theme), false);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        int count = children().size();
        if (count == 0) return;
        int gap = gap(theme);
        int innerHeight = Math.max(0, bounds().height() - gap * (count - 1));
        UiSize[] sizes = new UiSize[count];
        int totalHeight = 0;
        int flexibleCount = 0;
        for (int i = 0; i < count; i++) {
            int remainingHeight = Math.max(0, innerHeight - totalHeight);
            sizes[i] = children().get(i).measure(font, theme, bounds().width(), remainingHeight);
            totalHeight += sizes[i].height();
            if (isFlexibleSpacer(children().get(i))) flexibleCount++;
        }
        int flexibleSpace = flexibleCount == 0 ? 0 : Math.max(0, innerHeight - totalHeight);
        int flexibleBase = flexibleCount == 0 ? 0 : flexibleSpace / flexibleCount;
        int flexibleRemainder = flexibleCount == 0 ? 0 : flexibleSpace % flexibleCount;
        int y = alignedStart(bounds().y(), bounds().height(),
                totalHeight + flexibleSpace + gap * (count - 1), verticalAlignment);
        for (int i = 0; i < count; i++) {
            UiComponent child = children().get(i);
            UiSize size = sizes[i];
            if (isFlexibleSpacer(child)) {
                int extra = flexibleBase + (flexibleRemainder > 0 ? 1 : 0);
                if (flexibleRemainder > 0) flexibleRemainder--;
                size = new UiSize(size.width(), size.height() + extra);
            }
            int width = horizontalAlignment == Alignment.STRETCH && child.preferredWidth() == null
                    ? bounds().width() : size.width();
            width = Math.min(width, bounds().width());
            int x = alignedStart(bounds().x(), bounds().width(), width, horizontalAlignment);
            child.layout(font, theme, new Bounds(x, y, width, size.height()));
            y += size.height() + gap;
        }
    }

    private static boolean isFlexibleSpacer(UiComponent component) {
        return component instanceof Spacer spacer && spacer.isFlexible();
    }

    static int alignedStart(int start, int available, int content, Alignment alignment) {
        return switch (alignment) {
            case CENTER -> start + Math.max(0, (available - content) / 2);
            case END -> start + Math.max(0, available - content);
            default -> start;
        };
    }

    private int gap(UiTheme theme) {
        return gapOverride == null ? theme.tokens().mediumGap() : gapOverride;
    }
}
