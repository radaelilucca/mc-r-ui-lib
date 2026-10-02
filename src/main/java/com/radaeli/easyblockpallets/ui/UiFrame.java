package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

/** Shared layout and input behavior for framed presentation components. */
abstract class UiFrame extends UiComponent {
    private final UiComponent content;
    private final Insets padding;

    UiFrame(UiComponent content, Insets padding) {
        this.content = Objects.requireNonNull(content, "content");
        this.padding = Objects.requireNonNull(padding, "padding");
    }

    protected UiComponent content() {
        return content;
    }

    protected Insets padding() {
        return padding;
    }

    protected int contentTopOffset(Font font, UiTheme theme) {
        return 0;
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        int top = padding.top() + contentTopOffset(font, theme);
        UiSize child = content.measure(font, theme,
                Math.max(0, maxWidth - padding.left() - padding.right()),
                Math.max(0, maxHeight - top - padding.bottom()));
        return new UiSize(child.width() + padding.left() + padding.right(),
                child.height() + top + padding.bottom()).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        int top = padding.top() + contentTopOffset(font, theme);
        Bounds inner = new Bounds(bounds().x() + padding.left(), bounds().y() + top,
                Math.max(0, bounds().width() - padding.left() - padding.right()),
                Math.max(0, bounds().height() - top - padding.bottom()));
        content.layout(font, theme, inner);
    }

    @Override
    public UiComponent hitTest(double mouseX, double mouseY) {
        if (!isEnabled() || !bounds().contains(mouseX, mouseY)) return null;
        UiComponent hit = content.hitTest(mouseX, mouseY);
        return hit == null ? this : hit;
    }

    @Override
    public void visitFocusable(Consumer<UiComponent> visitor) {
        if (isEnabled()) content.visitFocusable(visitor);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return isEnabled() && bounds().contains(mouseX, mouseY)
                && content.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return content.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return content.charTyped(codePoint, modifiers);
    }
}
