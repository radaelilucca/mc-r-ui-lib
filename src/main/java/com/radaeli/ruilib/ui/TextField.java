package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

/** Single-line text input backed by Minecraft's EditBox for cursor, clipboard and IME support. */
public class TextField extends UiComponent {
    private final Component hint;
    private String value = "";
    private int maxLength = 256;
    private Consumer<String> responder = ignored -> { };
    private EditBox editBox;
    private float textScale = 1;

    public TextField() {
        this(Component.empty());
    }

    public TextField(Component hint) {
        this.hint = Objects.requireNonNull(hint, "hint");
    }

    public String value() {
        return editBox == null ? value : editBox.getValue();
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
        if (editBox != null) editBox.setValue(this.value);
    }

    public void setMaxLength(int maxLength) {
        if (maxLength < 0) throw new IllegalArgumentException("maxLength cannot be negative");
        this.maxLength = maxLength;
        if (editBox != null) editBox.setMaxLength(maxLength);
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = Objects.requireNonNull(responder, "responder");
        if (editBox != null) editBox.setResponder(this::onValueChanged);
    }

    private void onValueChanged(String value) {
        this.value = value;
        responder.accept(value);
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return new UiSize(160, UiText.height(font, theme) + 10).constrain(maxWidth, maxHeight);
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        textScale = theme.tokens().typography().bodyScale();
        ensureEditBox(font);
        int inset = editorInsetLeft();
        int editorHeight = Math.max(8, bounds().height() - 4);
        editBox.setX(bounds().x() + 4 + inset);
        editBox.setY(bounds().y() + (bounds().height() - editorHeight) / 2);
        editBox.setSize(Math.max(1, bounds().width() - 8 - inset), editorHeight);
        syncEditBoxState();
    }

    protected int editorInsetLeft() {
        return 0;
    }

    private void ensureEditBox(Font font) {
        if (editBox != null) return;
        int editorHeight = Math.max(8, bounds().height() - 4);
        editBox = new EditBox(font, bounds().x() + 4 + editorInsetLeft(),
                bounds().y() + (bounds().height() - editorHeight) / 2,
                Math.max(1, bounds().width() - 8 - editorInsetLeft()), editorHeight, hint);
        editBox.setBordered(false);
        editBox.setMaxLength(maxLength);
        editBox.setValue(value);
        editBox.setResponder(this::onValueChanged);
        syncEditBoxState();
    }

    private void syncEditBoxState() {
        if (editBox == null) return;
        editBox.active = isEnabled();
        editBox.setEditable(isEnabled());
        editBox.setFocused(isFocused());
        editBox.visible = true;
    }

    @Override
    public void render(UiContext context) {
        ensureEditBox(context.font());
        syncEditBoxState();
        boolean hovered = isEnabled() && bounds().contains(context.mouseX(), context.mouseY());
        UiPaint.input(context, bounds(), animate(context, "hover", hovered ? 1 : 0), isEnabled(),
                animate(context, "focus", isFocused() ? 1 : 0));
        editBox.setTextColor(UiPaint.withAlpha(context.theme().palette().text(), context.opacity()));
        editBox.setTextColorUneditable(UiPaint.withAlpha(context.theme().palette().mutedText(), context.opacity()));
        renderPrefix(context);
        var pose = context.graphics().pose();
        pose.pushPose();
        pose.translate(editBox.getX(), editBox.getY(), 0);
        pose.scale(textScale, textScale, 1);
        pose.translate(-editBox.getX(), -editBox.getY(), 0);
        editBox.render(context.graphics(), logicalMouseX(context.mouseX()), logicalMouseY(context.mouseY()), 0);
        pose.popPose();
    }

    protected void renderPrefix(UiContext context) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !bounds().contains(mouseX, mouseY)) return false;
        ensureEditBox(net.minecraft.client.Minecraft.getInstance().font);
        boolean consumed = editBox.mouseClicked(logicalMouseX(mouseX), logicalMouseY(mouseY), button);
        setFocused(consumed);
        return consumed;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return editBox != null && editBox.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return editBox != null && editBox.mouseDragged(logicalMouseX(mouseX), logicalMouseY(mouseY),
                button, dragX / textScale, dragY / textScale);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return isFocused() && isEnabled() && editBox != null && editBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return isFocused() && isEnabled() && editBox != null && editBox.charTyped(codePoint, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        syncEditBoxState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        syncEditBoxState();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    private int logicalMouseX(double mouseX) {
        return (int) Math.round(editBox.getX() + (mouseX - editBox.getX()) / textScale);
    }

    private int logicalMouseY(double mouseY) {
        return (int) Math.round(editBox.getY() + (mouseY - editBox.getY()) / textScale);
    }
}
