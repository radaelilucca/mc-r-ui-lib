package com.radaeli.easyblockpallets.ui;

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
    private final UiTheme theme;
    private UiComponent focusedComponent;
    private UiComponent pressedComponent;

    public UiScreen(Component title, UiComponent root) {
        this(title, root, UiTheme.DARK);
    }

    public UiScreen(Component title, UiComponent root, UiTheme theme) {
        super(title);
        this.root = Objects.requireNonNull(root, "root");
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    @Override
    protected void init() {
        root.layout(font, theme, new Bounds(0, 0, width, height));
        clearToolkitFocus();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        root.render(new UiContext(graphics, font, theme, mouseX, mouseY, width, height));
        renderables.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
        UiComponent target = pressedComponent;
        pressedComponent = null;
        return target != null && target.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return pressedComponent != null
                && pressedComponent.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return root.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return focusedComponent != null && focusedComponent.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    private void moveFocus(boolean backwards) {
        List<UiComponent> focusable = new ArrayList<>();
        root.visitFocusable(focusable::add);
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

    public UiComponent root() {
        return root;
    }

    public UiTheme theme() {
        return theme;
    }
}
