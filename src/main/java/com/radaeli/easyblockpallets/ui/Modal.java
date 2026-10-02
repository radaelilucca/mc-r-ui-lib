package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import java.util.Objects;
import java.util.function.Consumer;

/** Centered modal content rendered as an overlay by {@link UiScreen}. */
public final class Modal extends UiComponent {
    private final Component title;
    private final UiComponent content;
    private final int preferredDialogWidth;
    private final int preferredDialogHeight;
    private boolean closeOnOutsideClick;
    private boolean closing;
    private double transitionOpacity;
    private Runnable onClose = () -> { };

    public Modal(Component title, UiComponent content) {
        this(title, content, 240, 140);
    }

    public Modal(Component title, UiComponent content, int width, int height) {
        this.title = Objects.requireNonNull(title, "title");
        this.content = Objects.requireNonNull(content, "content");
        if (width < 80 || height < 48) throw new IllegalArgumentException("Modal dimensions are too small");
        this.preferredDialogWidth = width;
        this.preferredDialogHeight = height;
    }

    public Modal setCloseOnOutsideClick(boolean close) {
        closeOnOutsideClick = close;
        return this;
    }

    public Modal setOnClose(Runnable onClose) {
        this.onClose = Objects.requireNonNull(onClose, "onClose");
        return this;
    }

    public Component title() { return title; }
    public UiComponent content() { return content; }
    public boolean closeOnOutsideClick() { return closeOnOutsideClick; }
    public void closed() { onClose.run(); }
    void beginOpen() {
        closing = false;
        transitionOpacity = 0;
        clearAnimation("modal-opacity");
        clearContentFocus();
    }
    void beginClose() { closing = true; }
    boolean closeTransitionComplete() { return closing && transitionOpacity <= 0.001; }

    void clearContentFocus() {
        content.visitFocusable(component -> component.setFocused(false));
    }

    public void layoutInScreen(Font font, UiTheme theme, int screenWidth, int screenHeight) {
        int dialogWidth = Math.min(preferredDialogWidth, Math.max(1, screenWidth - 16));
        int dialogHeight = Math.min(preferredDialogHeight, Math.max(1, screenHeight - 16));
        layout(font, theme, new Bounds((screenWidth - dialogWidth) / 2,
                (screenHeight - dialogHeight) / 2, dialogWidth, dialogHeight));
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return new UiSize(Math.min(preferredDialogWidth, maxWidth),
                Math.min(preferredDialogHeight, maxHeight));
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        Insets padding = theme.tokens().panelPadding();
        int titleHeight = font.lineHeight + theme.tokens().mediumGap();
        content.layout(font, theme, new Bounds(bounds().x() + padding.left(),
                bounds().y() + padding.top() + titleHeight,
                Math.max(0, bounds().width() - padding.left() - padding.right()),
                Math.max(0, bounds().height() - padding.top() - padding.bottom() - titleHeight)));
    }

    @Override
    public void render(UiContext context) {
        double opacity = animateFrom(context, "modal-opacity", closing ? 0 : 1, closing ? 1 : 0);
        transitionOpacity = opacity;
        if (opacity <= 0) return;
        UiPaint.rect(context, new Bounds(0, 0, context.screenWidth(), context.screenHeight()),
                UiPaint.withAlpha(0x99000000, opacity));
        UiPaint.panel(context, bounds(), opacity);
        Insets padding = context.theme().tokens().panelPadding();
        UiPaint.text(context, title, bounds().x() + padding.left(), bounds().y() + padding.top(),
                UiPaint.withAlpha(context.theme().palette().text(), opacity));
        int dividerY = bounds().y() + padding.top() + context.font().lineHeight + 3;
        UiPaint.rect(context, new Bounds(bounds().x() + padding.left(), dividerY,
                Math.max(0, bounds().width() - padding.left() - padding.right()), 1),
                UiPaint.withAlpha(context.theme().palette().divider(), opacity));
        content.render(context.withOpacity(opacity));
    }

    @Override
    public UiComponent hitTest(double mouseX, double mouseY) {
        if (!bounds().contains(mouseX, mouseY)) return null;
        UiComponent target = content.hitTest(mouseX, mouseY);
        return target == null ? this : target;
    }

    @Override
    public void visitFocusable(Consumer<UiComponent> visitor) {
        if (isEnabled()) content.visitFocusable(visitor);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return bounds().contains(mouseX, mouseY) && content.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return content.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return content.charTyped(codePoint, modifiers);
    }

    public boolean shouldCloseFromOutsideClick(double mouseX, double mouseY) {
        return closeOnOutsideClick && !bounds().contains(mouseX, mouseY);
    }
}
