package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Read-only, single-selection table with uniform rows and vertical wheel scrolling. */
public final class Table<T> extends UiComponent {
    private static final int CELL_PADDING_X = 6;
    private static final int CELL_PADDING_Y = 4;
    private static final int MIN_ROW_HEIGHT = 20;

    private final List<TableColumn<T>> columns = new ArrayList<>();
    private List<T> rows = List.of();
    private BiConsumer<Integer, T> onRowClicked = (index, row) -> {};
    private int selectedIndex = -1;
    private int scrollOffset;
    private double visualScrollOffset;
    private int rowHeight = MIN_ROW_HEIGHT;
    private int headerHeight = MIN_ROW_HEIGHT;
    private int contentHeight;
    private int[] columnWidths = new int[0];
    private Font lastFont;

    public Table<T> addColumn(TableColumn<T> column) {
        columns.add(Objects.requireNonNull(column, "column"));
        calculateMetrics(lastFont, bounds().width());
        return this;
    }

    public Table<T> setRows(List<? extends T> rows) {
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        if (selectedIndex >= this.rows.size()) selectedIndex = -1;
        clampScroll();
        calculateMetrics(lastFont, bounds().width());
        return this;
    }

    public Table<T> setOnRowClicked(BiConsumer<Integer, T> callback) {
        onRowClicked = Objects.requireNonNull(callback, "callback");
        return this;
    }

    public List<TableColumn<T>> columns() {
        return List.copyOf(columns);
    }

    public List<T> rows() {
        return rows;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public T selectedRow() {
        return selectedIndex >= 0 && selectedIndex < rows.size() ? rows.get(selectedIndex) : null;
    }

    public int scrollOffset() {
        return scrollOffset;
    }

    public void clearSelection() {
        selectedIndex = -1;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        calculateMetrics(font, maxWidth);
        int width = 0;
        for (int columnWidth : columnWidths) width += columnWidth;
        int height = headerHeight + rows.size() * rowHeight;
        return new UiSize(width, height).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        lastFont = font;
        calculateMetrics(font, bounds().width());
        clampScroll();
    }

    @Override
    public void render(UiContext context) {
        if (lastFont != context.font()) {
            lastFont = context.font();
            calculateMetrics(lastFont, bounds().width());
            clampScroll();
        }
        Bounds tableBounds = bounds();
        UiPaint.panel(context, tableBounds);
        if (tableBounds.width() == 0 || tableBounds.height() == 0 || columns.isEmpty()) return;

        int headerBottom = Math.min(tableBounds.bottom(), tableBounds.y() + headerHeight);
        UiPaint.rect(context, new Bounds(tableBounds.x(), tableBounds.y(), tableBounds.width(),
                headerBottom - tableBounds.y()), context.theme().palette().card());
        context.graphics().enableScissor(tableBounds.x(), tableBounds.y(), tableBounds.right(), headerBottom);
        try {
            drawCells(context, -1, tableBounds.y(), context.theme().palette().text());
        } finally {
            context.graphics().disableScissor();
        }
        UiPaint.divider(context, new Bounds(tableBounds.x(), headerBottom - 1, tableBounds.width(), 1));

        int viewportY = headerBottom;
        int viewportHeight = Math.max(0, tableBounds.bottom() - viewportY);
        if (viewportHeight == 0) return;
        visualScrollOffset = animate(context, "scroll-offset", scrollOffset);
        context.graphics().enableScissor(tableBounds.x(), viewportY, tableBounds.right(), tableBounds.bottom());
        try {
            for (int index = firstVisibleRow(visualScrollOffset); index < rows.size(); index++) {
                int y = (int) Math.round(viewportY + index * rowHeight - visualScrollOffset);
                if (y >= tableBounds.bottom()) break;
                Bounds rowBounds = new Bounds(tableBounds.x(), y, tableBounds.width(), rowHeight);
                boolean hovered = rowBounds.contains(context.mouseX(), context.mouseY());
                UiPalette palette = context.theme().palette();
                double hoverProgress = animate(context, "row-hover-" + index, hovered ? 1 : 0);
                double selectedProgress = animate(context, "row-selected-" + index,
                        index == selectedIndex ? 1 : 0);
                int base = (index & 1) == 1 ? palette.card() : palette.panel();
                int fill = UiPaint.mixColor(UiPaint.mixColor(base, palette.buttonHover(), hoverProgress),
                        palette.accent(), selectedProgress);
                UiPaint.rect(context, rowBounds, fill);
                drawCells(context, index, y, context.theme().palette().text());
                UiPaint.divider(context, new Bounds(tableBounds.x(), y + rowHeight - 1,
                        tableBounds.width(), 1));
            }
        } finally {
            context.graphics().disableScissor();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || button != 0 || !bounds().contains(mouseX, mouseY)
                || mouseY < bounds().y() + headerHeight || columns.isEmpty()) return false;
        int index = (int) ((mouseY - bounds().y() - headerHeight + visualScrollOffset) / rowHeight);
        if (index < 0 || index >= rows.size()) return true;
        selectedIndex = index;
        onRowClicked.accept(index, rows.get(index));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isEnabled() || !bounds().contains(mouseX, mouseY) || maxScrollOffset() == 0 || scrollY == 0) {
            return false;
        }
        int nextOffset = Math.max(0, Math.min(maxScrollOffset(),
                scrollOffset - (int) Math.round(scrollY * rowHeight)));
        if (nextOffset != scrollOffset) scrollOffset = nextOffset;
        return true;
    }

    private void calculateMetrics(Font font, int availableWidth) {
        if (font == null) return;
        headerHeight = Math.max(MIN_ROW_HEIGHT, font.lineHeight + CELL_PADDING_Y * 2);
        rowHeight = Math.max(MIN_ROW_HEIGHT, font.lineHeight + CELL_PADDING_Y * 2);
        columnWidths = new int[columns.size()];
        contentHeight = rows.size() * rowHeight;
        if (columns.isEmpty()) return;

        int naturalTotal = 0;
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            TableColumn<T> column = columns.get(columnIndex);
            int width = column.width();
            if (width == 0) {
                width = font.width(column.title()) + CELL_PADDING_X * 2;
                for (T row : rows) {
                    Component value = Objects.requireNonNull(column.renderer().apply(row),
                            "Table column renderer returned null");
                    width = Math.max(width, font.width(value) + CELL_PADDING_X * 2);
                }
            }
            columnWidths[columnIndex] = Math.max(1, width);
            naturalTotal += columnWidths[columnIndex];
        }
        if (naturalTotal > availableWidth && availableWidth > 0) {
            int assigned = 0;
            for (int i = 0; i < columnWidths.length; i++) {
                int scaled = i == columnWidths.length - 1
                        ? availableWidth - assigned
                        : (int) ((long) columnWidths[i] * availableWidth / naturalTotal);
                columnWidths[i] = Math.max(1, scaled);
                assigned += columnWidths[i];
            }
        }
    }

    private void drawCells(UiContext context, int rowIndex, int y, int color) {
        int x = bounds().x();
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            TableColumn<T> column = columns.get(columnIndex);
            int cellWidth = columnWidths[columnIndex];
            Component value = rowIndex < 0 ? column.title()
                    : Objects.requireNonNull(column.renderer().apply(rows.get(rowIndex)),
                    "Table column renderer returned null");
            int textWidth = context.font().width(value);
            int textX = switch (column.alignment()) {
                case CENTER -> x + Math.max(CELL_PADDING_X, (cellWidth - textWidth) / 2);
                case END -> x + Math.max(CELL_PADDING_X, cellWidth - textWidth - CELL_PADDING_X);
                case START, STRETCH -> x + CELL_PADDING_X;
            };
            int textY = y + Math.max(0, (rowHeight - context.font().lineHeight) / 2);
            context.graphics().enableScissor(x, y, x + cellWidth, y + rowHeight);
            try {
                context.graphics().drawString(context.font(), value, textX, textY,
                        UiPaint.withAlpha(color, context.opacity()), false);
            } finally {
                context.graphics().disableScissor();
            }
            x += cellWidth;
        }
    }

    private int firstVisibleRow(double offset) {
        return Math.max(0, (int) (offset / rowHeight));
    }

    private int maxScrollOffset() {
        return Math.max(0, contentHeight - Math.max(0, bounds().height() - headerHeight));
    }

    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset()));
    }
}
