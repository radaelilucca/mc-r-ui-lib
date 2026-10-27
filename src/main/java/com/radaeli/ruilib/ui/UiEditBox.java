package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

/** Native text editing with shared selection, viewport and cursor rendering across game versions. */
final class UiEditBox extends EditBox {
    private final Font editorFont;
    private int anchor;
    private int displayStart;
    private int limit = 32;
    private int color = 0xFFE0E0E0;
    private int disabledColor = 0xFF707070;
    private boolean editable = true;
    private long focusedAt = Long.MIN_VALUE;

    UiEditBox(Font font, int x, int y, int width, int height, Component hint) {
        super(font, x, y, width, height, hint);
        editorFont = font;
    }
    void resizeEditor(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override public void setValue(String value) {
        super.setValue(value);
        anchor = getCursorPosition();
        displayStart = 0;
    }
    @Override public void setMaxLength(int length) { super.setMaxLength(length); limit = length; }
    @Override public void setTextColor(int color) { super.setTextColor(color); this.color = color; }
    @Override public void setTextColorUneditable(int color) { super.setTextColorUneditable(color); disabledColor = color; }
    @Override public void setEditable(boolean editable) { super.setEditable(editable); this.editable = editable; }
    @Override public void setFocused(boolean focused) {
        if (focused != isFocused()) focusedAt = Long.MIN_VALUE;
        super.setFocused(focused);
    }

    @Override public void onClick(double x, double y) {
        int position = positionAt(x);
        if (!Screen.hasShiftDown()) anchor = position;
        selectTo(position);
    }

    @Override protected void onDrag(double x, double y, double dragX, double dragY) {
        if (!editable || !isFocused()) return;
        int position;
        if (x < getX()) position = getValue().offsetByCodePoints(getCursorPosition(), getCursorPosition() > 0 ? -1 : 0);
        else if (x >= getX() + getWidth()) position = getValue().offsetByCodePoints(getCursorPosition(), getCursorPosition() < getValue().length() ? 1 : 0);
        else position = positionAt(x);
        selectTo(position);
    }

    private int positionAt(double x) {
        keepCursorVisible();
        String visible = editorFont.plainSubstrByWidth(getValue().substring(displayStart), getInnerWidth());
        return displayStart + editorFont.plainSubstrByWidth(visible, Math.max(0, (int) x - getX())).length();
    }

    private void selectTo(int position) {
        setCursorPosition(position);
        setHighlightPos(anchor);
        keepCursorVisible();
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        boolean consumed = super.keyPressed(key, scanCode, modifiers);
        if (Screen.isSelectAll(key)) anchor = 0;
        else if (getHighlighted().isEmpty()) anchor = getCursorPosition();
        keepCursorVisible();
        return consumed;
    }

    @Override public boolean charTyped(char character, int modifiers) {
        boolean consumed = super.charTyped(character, modifiers);
        if (consumed) anchor = getCursorPosition();
        keepCursorVisible();
        return consumed;
    }

    private void keepCursorVisible() {
        String value = getValue();
        int cursor = getCursorPosition();
        displayStart = Math.max(0, Math.min(displayStart, Math.min(cursor, value.length())));
        if (editorFont.width(value) <= getInnerWidth()) displayStart = 0;
        while (displayStart < cursor && editorFont.width(value.substring(displayStart, cursor)) > getInnerWidth()) {
            displayStart = value.offsetByCodePoints(displayStart, 1);
        }
    }

    /** Uses the same frame clock as component transitions, including deterministic validation frames. */
    void renderEditor(GuiGraphics graphics, long frameTime) {
        if (!isVisible()) return;
        keepCursorVisible();
        String value = getValue();
        String visible = editorFont.plainSubstrByWidth(value.substring(displayStart), getInnerWidth());
        int cursor = Math.max(0, Math.min(visible.length(), getCursorPosition() - displayStart));
        int highlight = Math.max(0, Math.min(visible.length(), anchor - displayStart));
        int textColor = editable ? color : disabledColor;
        String prefix = visible.substring(0, cursor);
        graphics.drawString(editorFont, prefix, getX(), getY(), textColor, true);
        int cursorX = getX() + editorFont.width(prefix);
        boolean insertCursor = getCursorPosition() < value.length() || value.length() >= limit;
        if (insertCursor) cursorX--;
        if (cursor < visible.length()) graphics.drawString(editorFont, visible.substring(cursor), cursorX, getY(), textColor, true);
        if (isFocused()) {
            if (focusedAt == Long.MIN_VALUE) focusedAt = frameTime;
            if (Math.max(0, frameTime - focusedAt) / 300_000_000L % 2 == 0) {
                if (insertCursor) graphics.fill(RenderType.guiOverlay(), cursorX, getY() - 1, cursorX + 1, getY() + 10, 0xFFD0D0D0);
                else graphics.drawString(editorFont, "_", cursorX, getY(), textColor, true);
            }
        }
        if (highlight != cursor) {
            int endX = getX() + editorFont.width(visible.substring(0, highlight)) - 1;
            graphics.fill(RenderType.guiTextHighlight(), Math.min(cursorX, endX), getY() - 1,
                    Math.max(cursorX, endX), getY() + 10, 0xFF0000FF);
        }
    }
}
