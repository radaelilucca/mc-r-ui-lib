package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Thin bridge from a component tree to Minecraft's client Screen lifecycle. */
public class UiScreen extends Screen {
    private final UiComponent root;
    private final UiTheme themeOverride;
    private UiTheme layoutTheme;
    private final ToastHost toastHost = new ToastHost();
    private UiComponent focusedComponent;
    private UiComponent pressedComponent;
    private Modal modal;
    private Modal closingModal;

    public UiScreen(Component title, UiComponent root) {
        this(title, root, null);
    }

    public UiScreen(Component title, UiComponent root, UiTheme theme) {
        super(title);
        this.root = Objects.requireNonNull(root, "root");
        this.themeOverride = theme;
    }

    @Override
    protected void init() {
        UiTheme activeTheme = theme();
        toastHost.layout(font, activeTheme, new Bounds(0, 0, width, height));
        layoutRoot(activeTheme);
        if (modal != null) modal.layoutInScreen(font, activeTheme, width, height);
        if (closingModal != null) closingModal.layoutInScreen(font, activeTheme, width, height);
        layoutTheme = activeTheme;
        clearToolkitFocus();
    }

    @Override
    public void tick() {
        super.tick();
        toastHost.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ensureThemeLayout();
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        UiContext context = new UiContext(graphics, font, theme(), mouseX, mouseY, width, height);
        UiPaint.rect(context, new Bounds(0, 0, width, height), context.theme().palette().screen());
        root.render(context);
        renderToastOverlay(graphics, mouseX, mouseY);
        renderables.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
        renderClosingModal(context);
        if (modal != null) modal.render(context);
        UiComponent hovered = closingModal == null ? activeRoot().hitTest(mouseX, mouseY) : null;
        if (hovered != null && hovered.tooltip() != null) {
            UiPaint.tooltip(context, hovered.tooltip(), mouseX, mouseY);
        }
    }

    protected void renderToastOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        toastHost.render(new UiContext(graphics, font, theme(), mouseX, mouseY, width, height));
    }

    protected void layoutRoot(UiTheme theme) {
        root.layout(font, theme, new Bounds(0, 0, width, height));
    }

    protected void renderClosingModal(UiContext context) {
        if (closingModal == null) return;
        closingModal.render(context);
        if (closingModal.closeTransitionComplete()) {
            closingModal.clearContentFocus();
            closingModal = null;
        }
    }

    protected boolean handleToastClick(double mouseX, double mouseY, int button) {
        if (!toastHost.mouseClicked(mouseX, mouseY, button)) return false;
        clearToolkitFocus();
        pressedComponent = null;
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ensureThemeLayout();
        if (modal != null) {
            if (modal.shouldCloseFromOutsideClick(mouseX, mouseY)) {
                closeModal();
                return true;
            }
            UiComponent target = modal.hitTest(mouseX, mouseY);
            clearToolkitFocus();
            if (target != null && target != modal && target.isFocusable()) focus(target);
            pressedComponent = target != null && target.mouseClicked(mouseX, mouseY, button) ? target : null;
            return true;
        }
        if (closingModal != null) return true;
        if (handleToastClick(mouseX, mouseY, button)) return true;
        UiComponent target = root.hitTest(mouseX, mouseY);
        clearToolkitFocus();
        if (target == null) {
            pressedComponent = null;
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (target.isFocusable()) {
            focus(target);
        }
        pressedComponent = target.mouseClicked(mouseX, mouseY, button) ? target : null;
        return pressedComponent != null || target.isFocusable();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (closingModal != null) return true;
        UiComponent target = pressedComponent;
        pressedComponent = null;
        return target != null && target.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (closingModal != null) return true;
        return pressedComponent != null
                && pressedComponent.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ensureThemeLayout();
        if (modal != null) {
            modal.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return true;
        }
        if (closingModal != null) return true;
        return root.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ensureThemeLayout();
        if (closingModal != null) return true;
        if (modal != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeModal();
            return true;
        }
        if (focusedComponent != null && !focusedComponent.isEnabled()) {
            clearToolkitFocus();
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            moveFocus((modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
            return true;
        }
        if (focusedComponent != null && focusedComponent.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (modal != null) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (closingModal != null) return true;
        return focusedComponent != null && focusedComponent.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    private void moveFocus(boolean backwards) {
        List<UiComponent> focusable = new ArrayList<>();
        activeRoot().visitFocusable(focusable::add);
        if (focusable.isEmpty()) {
            clearToolkitFocus();
            return;
        }
        int current = focusable.indexOf(focusedComponent);
        int next = backwards
                ? (current <= 0 ? focusable.size() - 1 : current - 1)
                : (current + 1) % focusable.size();
        focus(focusable.get(next));
    }

    private void focus(UiComponent component) {
        clearToolkitFocus();
        if (component.isFocusable() && component.isEnabled()) {
            focusedComponent = component;
            focusedComponent.setFocused(true);
        }
    }

    private void clearToolkitFocus() {
        if (focusedComponent != null) {
            focusedComponent.setFocused(false);
            focusedComponent = null;
        }
    }

    private UiComponent activeRoot() {
        return modal == null ? root : modal;
    }

    public void showModal(Modal modal) {
        ensureThemeLayout();
        Objects.requireNonNull(modal, "modal");
        closeModal();
        this.modal = modal;
        closingModal = null;
        modal.beginOpen();
        modal.layoutInScreen(font, theme(), width, height);
        pressedComponent = null;
        clearToolkitFocus();
        List<UiComponent> focusable = new ArrayList<>();
        modal.visitFocusable(focusable::add);
        if (!focusable.isEmpty()) focus(focusable.get(0));
    }

    public void closeModal() {
        Modal closing = modal;
        if (closing == null) return;
        modal = null;
        closing.beginClose();
        closingModal = closing;
        pressedComponent = null;
        focusedComponent = null;
        closing.closed();
    }

    public Modal modal() {
        return modal;
    }

    public UiComponent root() {
        return root;
    }

    public UiTheme theme() {
        return themeOverride == null ? UiThemes.globalTheme() : themeOverride;
    }

    public ToastHost toastHost() {
        return toastHost;
    }

    protected final void ensureThemeLayout() {
        UiTheme activeTheme = theme();
        if (activeTheme == layoutTheme || font == null) return;
        layoutRoot(activeTheme);
        toastHost.layout(font, activeTheme, new Bounds(0, 0, width, height));
        if (modal != null) modal.layoutInScreen(font, activeTheme, width, height);
        if (closingModal != null) closingModal.layoutInScreen(font, activeTheme, width, height);
        layoutTheme = activeTheme;
    }
}
