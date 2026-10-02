package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

/** Single-selection page container with a compact, keyboard-navigable tab strip. */
public final class Tabs extends UiContainer {
    private record Tab(Component title, UiComponent content) { }

    private final List<Tab> tabs = new ArrayList<>();
    private int selectedIndex;
    private int stripHeight;
    private int[] tabWidths = new int[0];
    private Font lastFont;
    private UiTheme lastTheme;
    private IntConsumer onSelectionChanged = index -> { };

    public Tabs addTab(Component title, UiComponent content) {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        super.add(content);
        tabs.add(new Tab(title, content));
        return this;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public void setOnSelectionChanged(IntConsumer listener) {
        onSelectionChanged = Objects.requireNonNull(listener, "listener");
    }

    public void select(int index) {
        if (index < 0 || index >= tabs.size()) throw new IndexOutOfBoundsException("tab index: " + index);
        if (selectedIndex == index) return;
        selectedIndex = index;
        if (lastFont != null) layoutChildren(lastFont, lastTheme);
        onSelectionChanged.accept(index);
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int stripWidth = 0;
        int stripHeight = Math.max(font.lineHeight + 8, 16);
        for (Tab tab : tabs) stripWidth += font.width(tab.title()) + 16;
        UiSize contentSize = tabs.isEmpty() ? UiSize.ZERO
                : tabs.get(selectedIndex).content().measure(font, theme, maxWidth, Math.max(0, maxHeight - stripHeight));
        return new UiSize(Math.max(stripWidth, contentSize.width()), stripHeight + contentSize.height())
                .constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        lastFont = font;
        lastTheme = theme;
        if (tabs.isEmpty()) return;
        stripHeight = Math.min(bounds().height(), Math.max(font.lineHeight + 8, 16));
        tabWidths = new int[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) tabWidths[i] = font.width(tabs.get(i).title()) + 16;
        UiComponent active = tabs.get(selectedIndex).content();
        active.layout(font, theme, new Bounds(bounds().x(), bounds().y() + stripHeight,
                bounds().width(), Math.max(0, bounds().height() - stripHeight)));
        for (int i = 0; i < tabs.size(); i++) {
            if (i != selectedIndex) tabs.get(i).content().layout(font, theme, Bounds.EMPTY);
        }
    }

    @Override
    public void render(UiContext context) {
        if (tabs.isEmpty()) return;
        int x = bounds().x();
        for (int i = 0; i < tabs.size(); i++) {
            Component title = tabs.get(i).title();
            int width = Math.min(bounds().right() - x, tabWidths[i]);
            if (width <= 0) break;
            Bounds tabBounds = new Bounds(x, bounds().y(), width, stripHeight);
            boolean selected = i == selectedIndex;
            double selectedProgress = animate(context, "selected-tab-" + i, selected ? 1 : 0);
            double focusProgress = animate(context, "focus-tab-" + i,
                    selected && isFocused() ? 1 : 0);
            UiPaint.tab(context, tabBounds, selectedProgress, focusProgress);
            UiPaint.textCentered(context, title, tabBounds.centerX(),
                    tabBounds.centerY() - context.font().lineHeight / 2,
                    UiPaint.mixColor(context.theme().palette().mutedText(), context.theme().palette().text(),
                            selectedProgress));
            x += width;
        }
        tabs.get(selectedIndex).content().render(context);
    }

    @Override
    public UiComponent hitTest(double mouseX, double mouseY) {
        if (!isEnabled() || !bounds().contains(mouseX, mouseY) || tabs.isEmpty()) return null;
        if (mouseY < bounds().y() + stripHeight) return this;
        return tabs.get(selectedIndex).content().hitTest(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || button != 0 || tabs.isEmpty()
                || mouseY >= bounds().y() + stripHeight) return false;
        int x = bounds().x();
        for (int i = 0; i < tabs.size(); i++) {
            int width = tabWidths[i];
            if (mouseX >= x && mouseX < x + width) {
                select(i);
                setFocused(true);
                return true;
            }
            x += width;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isFocused() || tabs.size() < 2) return false;
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            select((selectedIndex + tabs.size() - 1) % tabs.size());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            select((selectedIndex + 1) % tabs.size());
            return true;
        }
        return false;
    }

    @Override
    public void visitFocusable(java.util.function.Consumer<UiComponent> visitor) {
        if (!isEnabled()) return;
        if (tabs.size() > 1) visitor.accept(this);
        if (!tabs.isEmpty()) tabs.get(selectedIndex).content().visitFocusable(visitor);
    }

    @Override
    public boolean isFocusable() {
        return tabs.size() > 1;
    }
}
