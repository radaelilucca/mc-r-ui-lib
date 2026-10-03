package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;

/** Fixed-column layout that gives every cell in a row the same height. */
public class Grid extends UiContainer {
    private final int columns;
    private final Integer gapOverride;

    public Grid(int columns) {
        this(columns, null);
    }

    public Grid(int columns, int gap) {
        if (columns < 1) throw new IllegalArgumentException("columns must be at least one");
        if (gap < 0) throw new IllegalArgumentException("gap cannot be negative");
        this.columns = columns;
        this.gapOverride = gap;
    }

    private Grid(int columns, Integer gap) {
        if (columns < 1) throw new IllegalArgumentException("columns must be at least one");
        this.columns = columns;
        this.gapOverride = gap;
    }

    public int columns() {
        return columns;
    }

    public int gap() {
        return gapOverride == null ? UiThemes.globalTheme().tokens().smallGap() : gapOverride;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        if (children().isEmpty()) return UiSize.ZERO;
        int gap = gap(theme);
        int cellMaxWidth = Math.max(0, (maxWidth - gap * (columns - 1)) / columns);
        int cellWidth = 0;
        int[] rowHeights = new int[rowCount()];
        for (int index = 0; index < children().size(); index++) {
            UiSize size = children().get(index).measure(font, theme, cellMaxWidth, maxHeight);
            cellWidth = Math.max(cellWidth, size.width());
            int row = index / columns;
            rowHeights[row] = Math.max(rowHeights[row], size.height());
        }
        int width = cellWidth * columns + gap * (columns - 1);
        int height = sum(rowHeights) + gap * Math.max(0, rowHeights.length - 1);
        return new UiSize(width, height).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        if (children().isEmpty()) return;
        int gap = gap(theme);
        int cellWidth = Math.max(0, (bounds().width() - gap * (columns - 1)) / columns);
        int[] rowHeights = new int[rowCount()];
        for (int index = 0; index < children().size(); index++) {
            UiSize size = children().get(index).measure(font, theme, cellWidth, bounds().height());
            int row = index / columns;
            rowHeights[row] = Math.max(rowHeights[row], size.height());
        }
        int y = bounds().y();
        for (int index = 0; index < children().size(); index++) {
            int row = index / columns;
            int column = index % columns;
            int x = bounds().x() + column * (cellWidth + gap);
            children().get(index).layout(font, theme,
                    new Bounds(x, y, cellWidth, rowHeights[row]));
            if (column == columns - 1 || index == children().size() - 1) {
                y += rowHeights[row] + gap;
            }
        }
    }

    private int rowCount() {
        return (children().size() + columns - 1) / columns;
    }

    private int gap(UiTheme theme) {
        return gapOverride == null ? theme.tokens().smallGap() : gapOverride;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) total += value;
        return total;
    }
}
