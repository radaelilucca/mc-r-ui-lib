package com.radaeli.easyblockpallets.client.playground;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.radaeli.easyblockpallets.ui.AbsoluteLayout;
import com.radaeli.easyblockpallets.ui.Alignment;
import com.radaeli.easyblockpallets.ui.Bounds;
import com.radaeli.easyblockpallets.ui.Button;
import com.radaeli.easyblockpallets.ui.UiComponent;
import com.radaeli.easyblockpallets.ui.UiContext;
import com.radaeli.easyblockpallets.ui.UiPaint;
import com.radaeli.easyblockpallets.ui.UiScreen;
import com.radaeli.easyblockpallets.ui.UiTheme;
import com.radaeli.easyblockpallets.ui.Toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Development-only visual editor for composing and exercising toolkit components. */
public final class UiPlaygroundScreen extends UiScreen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final UiTheme THEME = UiTheme.DARK;
    private static final int SIDEBAR_WIDTH = 98;
    private static final int INSPECTOR_WIDTH = 168;
    private static final int TOOLBAR_HEIGHT = 28;
    private static final String[] PALETTE_TYPES = {
            "label", "button", "toggle", "chip", "textfield", "searchfield",
            "slider", "itemcard", "tabs", "scroll", "clip", "row", "column",
            "stack", "grid", "itemgrid", "table", "spacer", "absolute"
    };

    private PlaygroundDocument document = PlaygroundDocument.empty("untitled");
    private PlaygroundNode selected;
    private final AbsoluteLayout canvas;
    private Map<PlaygroundNode, UiComponent> componentMap = PlaygroundComponentFactory.newComponentMap();
    private UiComponent focusedPreviewComponent;
    private UiComponent pressedPreviewComponent;
    private String status = "Arraste componentes da paleta para o canvas.";
    private int clickCount;
    private int toastDemoIndex;
    private boolean previewMode;
    private int paletteScrollOffset;

    private int sidebarWidth;
    private int inspectorWidth;
    private int canvasX;
    private int canvasY;
    private int canvasWidth;
    private int canvasHeight;

    private String paletteDragType;
    private PlaygroundNode draggingNode;
    private int dragOffsetX;
    private int dragOffsetY;
    private int lastMouseX;
    private int lastMouseY;
    private boolean hasDragged;
    private boolean resizing;

    private EditBox nameField;
    private EditBox textField;
    private EditBox colorField;
    private EditBox xField;
    private EditBox yField;
    private EditBox widthField;
    private EditBox heightField;
    private EditBox gapField;
    private EditBox rangeField;
    private final List<AbstractWidget> inspectorWidgets = new ArrayList<>();
    private net.minecraft.client.gui.components.Button horizontalAlignmentButton;
    private net.minecraft.client.gui.components.Button verticalAlignmentButton;
    private net.minecraft.client.gui.components.Button enabledButton;

    public UiPlaygroundScreen() {
        this(new AbsoluteLayout());
    }

    private UiPlaygroundScreen(AbsoluteLayout canvas) {
        super(Component.literal("UI Toolkit Playground"), canvas, THEME);
        this.canvas = canvas;
        loadLastSavedDocument();
        rebuildCanvas();
    }

    @Override
    protected void init() {
        super.init();
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        updateCanvasLayout(width, height);

        boolean compactToolbar = width < 300;
        int nameWidth = Math.min(78, Math.max(compactToolbar ? 36 : 62, width / 5));
        int cursorX = 6;
        nameField = addRenderableWidget(new EditBox(font, cursorX, 5, nameWidth, 20, Component.literal("Layout name")));
        nameField.setMaxLength(40);
        nameField.setValue(document.name);
        cursorX += nameWidth + 3;
        int topButtonWidth = Math.max(20, (width - cursorX - 21) / 6);
        String newLabel = compactToolbar ? "N" : "New";
        String saveLabel = compactToolbar ? "S" : "Save";
        String loadLabel = compactToolbar ? "L" : "Load";
        String modeLabel = compactToolbar ? (previewMode ? "E" : "P") : (previewMode ? "Edit" : "Preview");
        String closeLabel = compactToolbar ? "X" : "Close";
        cursorX = addTopButton(cursorX, newLabel, topButtonWidth, this::newDocument);
        cursorX = addTopButton(cursorX, saveLabel, topButtonWidth, this::saveDocument);
        cursorX = addTopButton(cursorX, loadLabel, topButtonWidth, this::openLoadPicker);
        cursorX = addTopButton(cursorX, modeLabel, topButtonWidth, this::toggleMode);
        String toastLabel = compactToolbar ? "T" : "Toast";
        cursorX = addTopButton(cursorX, toastLabel, topButtonWidth, this::showToastDemo);
        addTopButton(cursorX, closeLabel, topButtonWidth, this::onClose);

        inspectorWidgets.clear();
        createInspectorWidgets(width);
        refreshInspector();
    }

    private void updateCanvasLayout(int screenWidth, int screenHeight) {
        if (previewMode) {
            sidebarWidth = 0;
            inspectorWidth = 0;
            canvasX = 8;
            canvasWidth = Math.max(28, screenWidth - 16);
        } else {
            sidebarWidth = screenWidth < 470 ? 76 : SIDEBAR_WIDTH;
            inspectorWidth = screenWidth < 470 ? 118 : INSPECTOR_WIDTH;
            if (screenWidth < 300) {
                sidebarWidth = 58;
                inspectorWidth = 94;
            }
            canvasX = sidebarWidth + 8;
            canvasWidth = Math.max(28, screenWidth - sidebarWidth - inspectorWidth - 24);
        }
        canvasY = TOOLBAR_HEIGHT + 8;
        canvasHeight = Math.max(40, screenHeight - TOOLBAR_HEIGHT - 24);
        paletteScrollOffset = Math.min(paletteScrollOffset, maxPaletteScrollOffset());
        root().layout(font, THEME, new Bounds(canvasX, canvasY, canvasWidth, canvasHeight));
    }

    private int addTopButton(int x, String label, int buttonWidth, Runnable action) {
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.literal(label), button -> action.run())
                .bounds(x, 5, buttonWidth, 20).build());
        return x + buttonWidth + 3;
    }

    private void showToastDemo() {
        int level = toastDemoIndex++ % 4;
        switch (level) {
            case 0 -> toastHost().show(Toast.info(Component.literal("Info toast preview")));
            case 1 -> toastHost().show(Toast.success(Component.literal("Success toast preview")));
            case 2 -> toastHost().show(Toast.warning(Component.literal("Warning toast preview")));
            default -> toastHost().show(Toast.error(Component.literal("Error toast preview")));
        }
    }

    private void createInspectorWidgets(int screenWidth) {
        int editorInspectorWidth = screenWidth < 300 ? 94 : screenWidth < 470 ? 118 : INSPECTOR_WIDTH;
        int panelX = screenWidth - editorInspectorWidth + 5;
        int fieldWidth = Math.max(34, editorInspectorWidth - 11);
        textField = createField(panelX, 58, fieldWidth, "Component text");
        colorField = createField(panelX, 78, fieldWidth, "ARGB color");
        int halfWidth = Math.max(32, (fieldWidth - 5) / 2);
        xField = createField(panelX, 98, halfWidth, "X");
        yField = createField(panelX + halfWidth + 5, 98, halfWidth, "Y");
        widthField = createField(panelX, 118, halfWidth, "Width");
        heightField = createField(panelX + halfWidth + 5, 118, halfWidth, "Height");
        gapField = createField(panelX, 138, fieldWidth, "Gap");
        rangeField = createField(panelX, 160, fieldWidth, "Min, max, step");
        int alignmentWidth = Math.max(32, (fieldWidth - 4) / 2);
        horizontalAlignmentButton = addInspectorButton(net.minecraft.client.gui.components.Button.builder(
                Component.literal("H"), button -> cycleAlignment(true))
                .bounds(panelX, 160, alignmentWidth, 18).build());
        verticalAlignmentButton = addInspectorButton(net.minecraft.client.gui.components.Button.builder(
                Component.literal("V"), button -> cycleAlignment(false))
                .bounds(panelX + alignmentWidth + 4, 160, alignmentWidth, 18).build());
        enabledButton = addInspectorButton(net.minecraft.client.gui.components.Button.builder(
                Component.literal("Enabled"), button -> toggleEnabled())
                .bounds(panelX, 182, fieldWidth, 18).build());
        int actionY = Math.max(204, height - 38);
        addInspectorButton(net.minecraft.client.gui.components.Button.builder(
                Component.literal("Apply"), button -> applyInspector())
                .bounds(panelX, actionY, alignmentWidth, 18).build());
        addInspectorButton(net.minecraft.client.gui.components.Button.builder(
                Component.literal("Remove"), button -> removeSelected())
                .bounds(panelX + alignmentWidth + 4, actionY, alignmentWidth, 18).build());
    }

    private net.minecraft.client.gui.components.Button addInspectorButton(
            net.minecraft.client.gui.components.Button button) {
        inspectorWidgets.add(button);
        return addRenderableWidget(button);
    }

    private EditBox createField(int x, int y, int fieldWidth, String hint) {
        EditBox field = new EditBox(font, x, y, fieldWidth, 18, Component.literal(hint));
        field.setMaxLength(128);
        inspectorWidgets.add(field);
        return addRenderableWidget(field);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        UiContext context = new UiContext(graphics, font, THEME, mouseX, mouseY, width, height);
        drawEditorPanels(context);
        graphics.enableScissor(canvasX, canvasY, canvasX + canvasWidth, canvasY + canvasHeight);
        drawCanvasGrid(graphics);
        root().render(context);
        drawCanvasDebug(context);
        if (paletteDragType != null && inCanvas(mouseX, mouseY)) {
            graphics.renderOutline(mouseX - 18, mouseY - 10, 72, 22, 0xFF8BCBFF);
            graphics.drawString(font, paletteDragType, mouseX - 14, mouseY - 5, 0xFFFFFFFF, false);
        }
        if (previewMode) {
            graphics.drawString(font, "Preview · clicks: " + clickCount, canvasX + 4, canvasY + 4,
                    THEME.palette().mutedText(), false);
        }
        graphics.disableScissor();
        renderToastOverlay(graphics, mouseX, mouseY);
        graphics.drawString(font, status, 8, height - 12, 0xFFD3DCE8, false);
        renderables.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
    }

    private void drawCanvasGrid(GuiGraphics graphics) {
        for (int x = canvasX + 16; x < canvasX + canvasWidth; x += 16) {
            graphics.fill(x, canvasY, x + 1, canvasY + canvasHeight, 0x182F3C4A);
        }
        for (int y = canvasY + 16; y < canvasY + canvasHeight; y += 16) {
            graphics.fill(canvasX, y, canvasX + canvasWidth, y + 1, 0x182F3C4A);
        }
    }

    private void drawEditorPanels(UiContext context) {
        GuiGraphics graphics = context.graphics();
        UiPaint.rect(context, new Bounds(0, 0, width, height), THEME.palette().screen());
        UiPaint.rect(context, new Bounds(0, 0, width, TOOLBAR_HEIGHT), THEME.palette().panel());
        if (previewMode) {
            UiPaint.panel(context, new Bounds(canvasX, canvasY, canvasWidth, canvasHeight));
            return;
        }
        UiPaint.panel(context, new Bounds(4, canvasY, sidebarWidth, canvasHeight));
        UiPaint.panel(context, new Bounds(canvasX, canvasY, canvasWidth, canvasHeight));
        UiPaint.panel(context, new Bounds(width - inspectorWidth + 1, canvasY, inspectorWidth - 5, canvasHeight));
        UiPaint.text(context, "COMPONENTS", 8, canvasY + 7, THEME.palette().mutedText());
        UiPaint.text(context, "INSPECTOR", width - inspectorWidth + 7, canvasY + 7, THEME.palette().mutedText());
        int paletteTop = canvasY + 22;
        int paletteBottom = canvasY + canvasHeight;
        graphics.enableScissor(8, paletteTop, sidebarWidth, paletteBottom);
        int first = paletteScrollOffset;
        int last = Math.min(PALETTE_TYPES.length,
                first + Math.max(1, (canvasHeight - 22 + 19) / 20));
        for (int i = first; i < last; i++) {
            int y = paletteTop + (i - first) * 20;
            int color = paletteDragType != null && paletteDragType.equals(PALETTE_TYPES[i])
                    ? THEME.palette().buttonHover() : THEME.palette().button();
            UiPaint.rect(context, new Bounds(8, y, sidebarWidth - 8, 17), color);
            UiPaint.outline(context, new Bounds(8, y, sidebarWidth - 8, 17), THEME.palette().border());
            graphics.drawString(font, PALETTE_TYPES[i], 12, y + 5, THEME.palette().text(), false);
        }
        graphics.disableScissor();
        drawInspectorLabels(graphics, width - inspectorWidth + 7);
    }

    private void drawInspectorLabels(GuiGraphics graphics, int x) {
        graphics.drawString(font, selected != null && selected.type.equals("itemcard")
                ? "Item label" : selected != null && selected.type.equals("table")
                ? "Headers (CSV)" : "Text / label", x, 48, 0xFFADB7C5, false);
        graphics.drawString(font, selected != null && selected.type.equals("itemcard")
                ? "Item ID" : "Color ARGB", x, 68, 0xFFADB7C5, false);
        graphics.drawString(font, "X", x, 88, 0xFFADB7C5, false);
        graphics.drawString(font, "Y", x + Math.max(36, (inspectorWidth - 16) / 2) + 5, 88, 0xFFADB7C5, false);
        graphics.drawString(font, "Width", x, 108, 0xFFADB7C5, false);
        graphics.drawString(font, "Height", x + Math.max(36, (inspectorWidth - 16) / 2) + 5, 108, 0xFFADB7C5, false);
        graphics.drawString(font, "Gap", x, 128, 0xFFADB7C5, false);
        if (selected != null) {
            String rangeLabel = selected.type.equals("slider") ? "Min, max, step"
                    : selected.usesItemStack() ? "Stack count"
                    : selected.type.equals("table") ? "Table columns"
                    : selected.supportsColumns() ? "Grid columns" : null;
            if (rangeLabel != null) graphics.drawString(font, rangeLabel, x, 150, 0xFFADB7C5, false);
        }
        if (selected != null) {
            graphics.drawString(font, selected.type.toUpperCase(), x, 32, THEME.palette().accent(), false);
        } else {
            graphics.drawString(font, "No selection", x, 32, THEME.palette().mutedText(), false);
        }
    }

    private void drawCanvasDebug(UiContext context) {
        if (previewMode) return;
        UiComponent rootComponent = root();
        UiPaint.outline(context, rootComponent.bounds(), THEME.palette().border());
        for (Map.Entry<PlaygroundNode, UiComponent> entry : componentMap.entrySet()) {
            UiComponent component = entry.getValue();
            Bounds bounds = component.bounds();
            UiPaint.outline(context, bounds, entry.getKey() == selected
                    ? THEME.palette().focus() : 0x886E7D8E);
        }
        UiComponent selectedComponent = selected == null ? null : componentMap.get(selected);
        if (!previewMode && selectedComponent != null) {
            Bounds bounds = selectedComponent.bounds();
            UiPaint.rect(context, new Bounds(bounds.right() - 5, bounds.bottom() - 5, 8, 8),
                    THEME.palette().focus());
            String info = (bounds.x() - canvasX) + ", " + (bounds.y() - canvasY)
                    + "  " + bounds.width() + "×" + bounds.height();
            context.graphics().drawString(font, info, bounds.x(), Math.max(canvasY, bounds.y() - 10),
                    THEME.palette().focus(), true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleToastClick(mouseX, mouseY, button)) {
            clearPreviewFocus();
            pressedPreviewComponent = null;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !previewMode && inPalette(mouseX, mouseY)) {
            setFocused(null);
            setDragging(false);
            int paletteIndex = paletteScrollOffset + (int) ((mouseY - canvasY - 22) / 20);
            if (paletteIndex >= 0 && paletteIndex < PALETTE_TYPES.length) {
                paletteDragType = PALETTE_TYPES[paletteIndex];
                lastMouseX = (int) mouseX;
                lastMouseY = (int) mouseY;
                return true;
            }
        }
        if (inCanvas(mouseX, mouseY)) {
            setFocused(null);
            setDragging(false);
            if (previewMode) return previewMouseClicked(mouseX, mouseY, button);
            return editorMouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean editorMouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
        PlaygroundNode hit = nodeAt(mouseX, mouseY);
        if (hit == null) {
            selected = null;
            refreshInspector();
            return true;
        }
        selected = hit;
        UiComponent component = componentMap.get(hit);
        Bounds bounds = component.bounds();
        resizing = mouseX >= bounds.right() - 7 && mouseY >= bounds.bottom() - 7;
        draggingNode = hit;
        hasDragged = false;
        dragOffsetX = (int) mouseX - bounds.x();
        dragOffsetY = (int) mouseY - bounds.y();
        lastMouseX = (int) mouseX;
        lastMouseY = (int) mouseY;
        refreshInspector();
        return true;
    }

    private boolean previewMouseClicked(double mouseX, double mouseY, int button) {
        UiComponent target = canvas.hitTest(mouseX, mouseY);
        clearPreviewFocus();
        if (target == null) {
            pressedPreviewComponent = null;
            return true;
        }
        if (target.isFocusable()) {
            focusedPreviewComponent = target;
            focusedPreviewComponent.setFocused(true);
        }
        pressedPreviewComponent = target.mouseClicked(mouseX, mouseY, button) ? target : null;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (previewMode && pressedPreviewComponent != null
                && pressedPreviewComponent.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (paletteDragType != null || draggingNode != null) {
            if (Math.abs((int) mouseX - lastMouseX) + Math.abs((int) mouseY - lastMouseY) > 1) {
                hasDragged = true;
            }
            if (paletteDragType == null && selected != null) {
                if (resizing) {
                    int deltaWidth = (int) mouseX - lastMouseX;
                    int deltaHeight = (int) mouseY - lastMouseY;
                    selected.width = Math.max(12, Math.min(8192, selected.width + deltaWidth));
                    selected.height = Math.max(12, Math.min(8192, selected.height + deltaHeight));
                } else if (selected.parent != null && selected.parent.isAbsolute()) {
                    Bounds parentBounds = componentMap.get(selected.parent).bounds();
                    selected.x = clamp((int) mouseX - parentBounds.x() - dragOffsetX, -100_000, 100_000);
                    selected.y = clamp((int) mouseY - parentBounds.y() - dragOffsetY, -100_000, 100_000);
                }
                rebuildCanvas();
            }
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (paletteDragType != null) {
            if (inCanvas(mouseX, mouseY)) {
                addNode(paletteDragType, (int) mouseX, (int) mouseY);
            }
            paletteDragType = null;
            return true;
        }
        if (draggingNode != null) {
            if (!resizing && hasDragged) {
                PlaygroundNode destination = dropContainerAt(mouseX, mouseY, draggingNode);
                if (destination == null && inCanvas(mouseX, mouseY)) destination = document.root;
                if (destination != null && destination != draggingNode && !draggingNode.contains(destination)) {
                    reparent(draggingNode, destination, (int) mouseX, (int) mouseY);
                }
            }
            draggingNode = null;
            resizing = false;
            hasDragged = false;
            refreshInspector();
            return true;
        }
        if (pressedPreviewComponent != null) {
            UiComponent target = pressedPreviewComponent;
            pressedPreviewComponent = null;
            target.mouseReleased(mouseX, mouseY, button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!previewMode && inPalette(mouseX, mouseY) && scrollY != 0) {
            paletteScrollOffset = clamp(paletteScrollOffset - (int) Math.signum(scrollY),
                    0, maxPaletteScrollOffset());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (previewMode) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                movePreviewFocus((modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
                return true;
            }
            if (focusedPreviewComponent != null
                    && focusedPreviewComponent.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE && !(getFocused() instanceof EditBox)
                && !previewMode && selected != null) {
            removeSelected();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (previewMode && focusedPreviewComponent != null
                && focusedPreviewComponent.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    private void movePreviewFocus(boolean backwards) {
        List<UiComponent> focusable = new ArrayList<>();
        canvas.visitFocusable(focusable::add);
        if (focusable.isEmpty()) {
            clearPreviewFocus();
            return;
        }
        int current = focusable.indexOf(focusedPreviewComponent);
        int next = backwards ? (current <= 0 ? focusable.size() - 1 : current - 1)
                : (current + 1) % focusable.size();
        clearPreviewFocus();
        focusedPreviewComponent = focusable.get(next);
        focusedPreviewComponent.setFocused(true);
    }

    private void clearPreviewFocus() {
        if (focusedPreviewComponent != null) focusedPreviewComponent.setFocused(false);
        focusedPreviewComponent = null;
    }

    private void addNode(String type, int mouseX, int mouseY) {
        PlaygroundNode parent = dropContainerAt(mouseX, mouseY, null);
        if (parent == null) parent = document.root;
        Bounds parentBounds = componentMap.get(parent).bounds();
        PlaygroundNode node = PlaygroundNode.create(type,
                mouseX - parentBounds.x() - 24, mouseY - parentBounds.y() - 12);
        if (!parent.isAbsolute()) {
            node.x = 0;
            node.y = 0;
        }
        parent.addChild(node, parent.children.size());
        selected = node;
        rebuildCanvas();
        refreshInspector();
        status = "Adicionado: " + node.type;
    }

    private void reparent(PlaygroundNode node, PlaygroundNode newParent, int mouseX, int mouseY) {
        if (!newParent.isContainer() || node == document.root) return;
        PlaygroundNode oldParent = node.parent;
        if (oldParent == newParent && newParent.isAbsolute()) return;
        if (oldParent == null || !oldParent.removeChild(node)) return;
        if (newParent.isAbsolute()) {
            Bounds targetBounds = componentMap.get(newParent).bounds();
            node.x = mouseX - targetBounds.x() - componentMap.get(node).bounds().width() / 2;
            node.y = mouseY - targetBounds.y() - componentMap.get(node).bounds().height() / 2;
        } else {
            node.x = 0;
            node.y = 0;
        }
        newParent.addChild(node, newParent.children.size());
        selected = node;
        rebuildCanvas();
    }

    private PlaygroundNode dropContainerAt(double mouseX, double mouseY, PlaygroundNode ignored) {
        PlaygroundNode hit = nodeAt(document.root, mouseX, mouseY, ignored);
        while (hit != null && !hit.isContainer()) hit = hit.parent;
        return hit;
    }

    private PlaygroundNode nodeAt(double mouseX, double mouseY) {
        return nodeAt(document.root, mouseX, mouseY, null);
    }

    private PlaygroundNode nodeAt(PlaygroundNode node, double mouseX, double mouseY, PlaygroundNode ignored) {
        UiComponent component = componentMap.get(node);
        if (component == null || !component.bounds().contains(mouseX, mouseY)) return null;
        for (int index = node.children.size() - 1; index >= 0; index--) {
            PlaygroundNode childHit = nodeAt(node.children.get(index), mouseX, mouseY, ignored);
            if (childHit != null) return childHit;
        }
        if (node == ignored) return null;
        return node == document.root ? null : node;
    }

    private boolean inCanvas(double mouseX, double mouseY) {
        return mouseX >= canvasX && mouseX < canvasX + canvasWidth
                && mouseY >= canvasY && mouseY < canvasY + canvasHeight;
    }

    private boolean inPalette(double mouseX, double mouseY) {
        return mouseX >= 8 && mouseX < sidebarWidth && mouseY >= canvasY + 22
                && mouseY < canvasY + canvasHeight;
    }

    private int maxPaletteScrollOffset() {
        int visibleSlots = Math.max(1, (canvasHeight - 22 + 19) / 20);
        return Math.max(0, PALETTE_TYPES.length - visibleSlots);
    }

    private void rebuildCanvas() {
        document.normalize();
        componentMap = rebuildIntoStableRoot();
        canvas.layout(font == null ? Minecraft.getInstance().font : font, THEME,
                new Bounds(canvasX, canvasY, Math.max(1, canvasWidth), Math.max(1, canvasHeight)));
    }

    private Map<PlaygroundNode, UiComponent> rebuildIntoStableRoot() {
        Map<PlaygroundNode, UiComponent> rebuiltMap = PlaygroundComponentFactory.newComponentMap();
        AbsoluteLayout rebuiltRoot = (AbsoluteLayout) PlaygroundComponentFactory.build(
                document.root, rebuiltMap, node -> {
                    clickCount++;
                    if (node.type.equals("itemcard")) {
                        if (node.parent != null && node.parent.type.equals("itemgrid")) {
                            for (PlaygroundNode sibling : node.parent.children) {
                                if (sibling.type.equals("itemcard")) sibling.selected = sibling == node;
                            }
                        } else {
                            node.selected = !node.selected;
                        }
                        status = "Item selecionado (" + clickCount + "): " + node.itemId;
                        clearPreviewFocus();
                        rebuildCanvas();
                        refreshInspector();
                    } else {
                        status = "Ação acionada (" + clickCount + "): " + node.text;
                    }
                }, (node, selected) -> {
                    node.selected = selected;
                    clickCount++;
                    status = node.type.equals("toggle")
                            ? "Toggle " + node.text + (selected ? " ativado" : " desativado")
                            : "Chip " + node.text + (selected ? " selecionado" : " desmarcado");
                });
        List<UiComponent> newChildren = new ArrayList<>(rebuiltRoot.children());
        Map<UiComponent, Bounds> newPlacements = new IdentityHashMap<>();
        for (UiComponent child : newChildren) newPlacements.put(child, rebuiltRoot.placementOf(child));
        for (UiComponent child : new ArrayList<>(canvas.children())) canvas.remove(child);
        for (UiComponent child : newChildren) {
            canvas.addAt(child, newPlacements.get(child));
        }
        rebuiltMap.put(document.root, canvas);
        return rebuiltMap;
    }

    private void newDocument() {
        document = PlaygroundDocument.empty("untitled");
        selected = null;
        clickCount = 0;
        nameField.setValue(document.name);
        rebuildCanvas();
        refreshInspector();
        status = "Novo layout.";
    }

    private void saveDocument() {
        try {
            document.name = safeName(nameField.getValue());
            document.normalize();
            Path directory = playgroundDirectory();
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(directory.resolve(document.name + ".json"))) {
                GSON.toJson(document, writer);
            }
            try {
                Files.writeString(directory.resolve("last-layout.txt"), document.name, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                status = "Salvo, mas não atualizei o layout padrão: " + shortMessage(exception);
                return;
            }
            status = "Salvo: " + document.name + ".json";
        } catch (IOException | IllegalArgumentException exception) {
            status = "Falha ao salvar: " + shortMessage(exception);
        }
    }

    private void openLoadPicker() {
        Minecraft.getInstance().setScreen(new UiPlaygroundLoadScreen(this, nameField.getValue()));
    }

    boolean loadSavedLayout(String requestedName) {
        try {
            String name = safeName(requestedName);
            Path path = playgroundDirectory().resolve(name + ".json");
            if (!Files.isRegularFile(path)) {
                status = "Arquivo não encontrado: " + name + ".json";
                return false;
            }
            document = readDocument(path);
            document.name = name;
            nameField.setValue(document.name);
            selected = null;
            clickCount = 0;
            rebuildCanvas();
            refreshInspector();
            status = "Carregado: " + document.name + ".json";
            return true;
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            status = "Falha ao carregar: " + shortMessage(exception);
            return false;
        }
    }

    String loadStatus() {
        return status;
    }

    private void loadLastSavedDocument() {
        Path directory = playgroundDirectory();
        try {
            if (!Files.isDirectory(directory)) return;
            List<String> names = savedLayoutNames();
            if (names.isEmpty()) return;

            String lastSaved = null;
            Path pointer = directory.resolve("last-layout.txt");
            if (Files.isRegularFile(pointer)) {
                try {
                    lastSaved = safeName(Files.readString(pointer, StandardCharsets.UTF_8));
                } catch (IOException | IllegalArgumentException ignored) {
                    // A stale or malformed pointer falls back to the most recently modified layout.
                }
            }
            if (lastSaved == null || !names.contains(lastSaved)) {
                lastSaved = names.get(0);
                long newest = Long.MIN_VALUE;
                for (String name : names) {
                    long modified = Files.getLastModifiedTime(directory.resolve(name + ".json")).toMillis();
                    if (modified > newest) {
                        newest = modified;
                        lastSaved = name;
                    }
                }
            }

            document = readDocument(directory.resolve(lastSaved + ".json"));
            document.name = lastSaved;
            status = "Último layout salvo carregado: " + lastSaved + ".json";
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            document = PlaygroundDocument.empty("untitled");
            status = "Falha ao carregar último layout: " + shortMessage(exception);
        }
    }

    static List<String> savedLayoutNames() throws IOException {
        Path directory = playgroundDirectory();
        if (!Files.isDirectory(directory)) return List.of();
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".json"))
                    .map(fileName -> fileName.substring(0, fileName.length() - 5))
                    .filter(name -> name.matches("[A-Za-z0-9_-]+"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
    }

    private static PlaygroundDocument readDocument(Path path) throws IOException {
        if (Files.size(path) > 2_000_000) throw new IllegalArgumentException("Arquivo maior que 2 MB");
        try (Reader reader = Files.newBufferedReader(path)) {
            PlaygroundDocument loaded = GSON.fromJson(reader, PlaygroundDocument.class);
            if (loaded == null) throw new JsonParseException("Documento vazio");
            loaded.normalize();
            return loaded;
        }
    }

    private static String safeName(String value) {
        String name = value == null ? "" : value.trim();
        name = name.replaceAll("[^A-Za-z0-9_-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Use um nome simples para o layout");
        }
        return name;
    }

    private static String shortMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message;
    }

    private static Path playgroundDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config/easy_block_pallets/ui-playground");
    }

    private void toggleMode() {
        previewMode = !previewMode;
        setFocused(null);
        clearPreviewFocus();
        pressedPreviewComponent = null;
        paletteDragType = null;
        updateCanvasLayout(width, height);
        refreshInspector();
        modeButtonUpdate();
        for (var renderable : renderables) {
            if (renderable instanceof net.minecraft.client.gui.components.Button button) {
                String label = button.getMessage().getString();
                if (label.equals("Apply") || label.equals("Remove")) button.active = !previewMode;
            }
        }
        status = previewMode ? "Preview: componentes recebem mouse e teclado." : "Edição: arraste e ajuste os componentes.";
    }

    private void modeButtonUpdate() {
        // The mode button is rebuilt with the screen controls after a resize; keep its label in sync now.
        for (var renderable : renderables) {
            if (renderable instanceof net.minecraft.client.gui.components.Button button
                    && (button.getMessage().getString().equals("Preview")
                    || button.getMessage().getString().equals("Edit")
                    || button.getMessage().getString().equals("P")
                    || button.getMessage().getString().equals("E"))) {
                boolean abbreviated = button.getMessage().getString().length() == 1;
                button.setMessage(Component.literal(previewMode
                        ? abbreviated ? "E" : "Edit"
                        : abbreviated ? "P" : "Preview"));
            }
        }
    }

    private void refreshInspector() {
        if (textField == null) return;
        for (AbstractWidget widget : inspectorWidgets) {
            widget.visible = !previewMode;
        }
        boolean hasSelection = selected != null;
        nameField.setEditable(!previewMode);
        textField.setValue(hasSelection && (selected.type.equals("label") || selected.type.equals("button")
                || selected.type.equals("toggle") || selected.type.equals("chip")
                || selected.type.equals("textfield") || selected.type.equals("searchfield")
                || selected.usesItemStack() || selected.type.equals("table"))
                ? selected.text : "");
        colorField.setValue(!hasSelection ? "FFFFFFFF"
                : selected.usesItemStack() ? selected.itemId : String.format("%08X", selected.color));
        xField.setValue(hasSelection ? Integer.toString(selected.x) : "0");
        yField.setValue(hasSelection ? Integer.toString(selected.y) : "0");
        widthField.setValue(hasSelection ? Integer.toString(selected.width) : "0");
        heightField.setValue(hasSelection ? Integer.toString(selected.height) : "0");
        gapField.setValue(hasSelection ? Integer.toString(selected.gap) : "0");
        rangeField.setValue(!hasSelection ? "0, 100, 1"
                : selected.type.equals("slider")
                    ? formatNumber(selected.minimum) + ", " + formatNumber(selected.maximum) + ", "
                        + formatNumber(selected.step)
                    : selected.usesItemStack() ? Integer.toString(selected.itemCount)
                    : selected.supportsColumns() ? Integer.toString(selected.columns) : "0, 100, 1");
        boolean rangeEditable = hasSelection && (selected.type.equals("slider")
                || selected.usesItemStack() || selected.supportsColumns());
        rangeField.setEditable(rangeEditable);
        textField.setEditable(hasSelection && (selected.type.equals("label") || selected.type.equals("button")
                || selected.type.equals("toggle") || selected.type.equals("chip")
                || selected.type.equals("textfield") || selected.type.equals("searchfield")
                || selected.usesItemStack() || selected.type.equals("table")));
        colorField.setEditable(hasSelection && (selected.type.equals("label") || selected.usesItemStack()));
        gapField.setEditable(hasSelection && selected.isContainer() && !selected.isAbsolute());
        xField.setEditable(hasSelection && selected.parent != null && selected.parent.isAbsolute());
        yField.setEditable(hasSelection && selected.parent != null && selected.parent.isAbsolute());
        widthField.setEditable(hasSelection);
        heightField.setEditable(hasSelection);
        if (horizontalAlignmentButton != null) {
            Alignment horizontal = hasSelection ? selected.horizontalAlignment : Alignment.START;
            Alignment vertical = hasSelection ? selected.verticalAlignment : Alignment.STRETCH;
            horizontalAlignmentButton.setMessage(Component.literal(alignmentLabel("H", horizontal)));
            verticalAlignmentButton.setMessage(Component.literal(alignmentLabel("V", vertical)));
            boolean selectable = hasSelection && (selected.type.equals("toggle") || selected.type.equals("chip"));
            enabledButton.setMessage(Component.literal(selected != null && selected.type.equals("spacer")
                    ? "Flex: " + (selected.flexible ? "Yes" : "No")
                    : selectable ? "Selected: " + (selected.selected ? "Yes" : "No")
                    : "Enabled: " + (hasSelection && selected.enabled ? "Yes" : "No")));
            boolean alignedContainer = hasSelection && selected.supportsAlignment();
            boolean gapContainer = hasSelection && selected.isContainer() && !selected.isAbsolute();
            horizontalAlignmentButton.visible = alignedContainer && !previewMode;
            verticalAlignmentButton.visible = alignedContainer && !previewMode;
            horizontalAlignmentButton.active = alignedContainer && !previewMode;
            verticalAlignmentButton.active = alignedContainer && !previewMode;
            gapField.visible = gapContainer && !previewMode;
            rangeField.visible = rangeEditable && !previewMode;
            enabledButton.visible = hasSelection && (selected.type.equals("button")
                    || selected.type.equals("slider") || selected.usesItemStack()
                    || selected.type.equals("spacer") || selectable) && !previewMode;
            enabledButton.active = enabledButton.visible && !previewMode;
        }
        if (previewMode) {
            nameField.setEditable(false);
            textField.setEditable(false);
            colorField.setEditable(false);
            xField.setEditable(false);
            yField.setEditable(false);
            widthField.setEditable(false);
            heightField.setEditable(false);
            gapField.setEditable(false);
            if (horizontalAlignmentButton != null) {
                horizontalAlignmentButton.active = false;
                verticalAlignmentButton.active = false;
                enabledButton.active = false;
            }
        }
        for (var renderable : renderables) {
            if (renderable instanceof net.minecraft.client.gui.components.Button button) {
                String label = button.getMessage().getString();
                if (label.equals("Apply") || label.equals("Remove")) button.active = !previewMode;
            }
        }
    }

    private String alignmentLabel(String axis, Alignment alignment) {
        return inspectorWidth < 140 ? axis + ":" + alignment.name().charAt(0)
                : axis + ": " + alignment;
    }

    private void applyInspector() {
        if (selected == null) return;
        try {
            String nextText = textField.getValue();
            int nextColor = selected.color;
            String nextItemId = selected.itemId;
            if (selected.usesItemStack()) {
                nextItemId = colorField.getValue().trim();
                if (!PlaygroundComponentFactory.itemExists(nextItemId)) {
                    throw new IllegalArgumentException("Unknown item ID: " + nextItemId);
                }
            } else if (selected.type.equals("label") && !colorField.getValue().isBlank()) {
                String color = colorField.getValue().replace("#", "");
                if (color.length() > 8) throw new IllegalArgumentException("Use até oito dígitos ARGB");
                nextColor = (int) Long.parseLong(color, 16);
            }
            int nextX = selected.x;
            int nextY = selected.y;
            if (selected.parent != null && selected.parent.isAbsolute()) {
                nextX = parseBoundedInt(xField.getValue(), -100_000, 100_000);
                nextY = parseBoundedInt(yField.getValue(), -100_000, 100_000);
            }
            int nextWidth = parseBoundedInt(widthField.getValue(), 12, 8192);
            int nextHeight = parseBoundedInt(heightField.getValue(), 12, 8192);
            int nextGap = selected.gap;
            int nextColumns = selected.columns;
            int nextItemCount = selected.itemCount;
            double nextMinimum = selected.minimum;
            double nextMaximum = selected.maximum;
            double nextStep = selected.step;
            if (selected.isContainer() && !selected.isAbsolute()) {
                nextGap = parseBoundedInt(gapField.getValue(), 0, 4096);
            }
            if (selected.type.equals("slider")) {
                String[] rangeParts = rangeField.getValue().split(",", -1);
                if (rangeParts.length != 3) throw new IllegalArgumentException("Use min, max, step");
                nextMinimum = parseFiniteDouble(rangeParts[0]);
                nextMaximum = parseFiniteDouble(rangeParts[1]);
                nextStep = parseFiniteDouble(rangeParts[2]);
                if (nextMaximum <= nextMinimum || nextStep < 0) {
                    throw new IllegalArgumentException("Min deve ser menor que max; step não pode ser negativo");
                }
            }
            if (selected.supportsColumns()) {
                nextColumns = parseBoundedInt(rangeField.getValue(), 1, 64);
            }
            if (selected.usesItemStack()) {
                nextItemCount = parseBoundedInt(rangeField.getValue(), 1, 64);
            }
            if (selected.type.equals("label") || selected.type.equals("button")
                    || selected.type.equals("toggle") || selected.type.equals("chip")
                    || selected.type.equals("textfield") || selected.type.equals("searchfield")
                    || selected.usesItemStack() || selected.type.equals("table")) {
                selected.text = nextText;
            }
            selected.itemId = nextItemId;
            selected.itemCount = nextItemCount;
            selected.columns = nextColumns;
            selected.color = nextColor;
            selected.x = nextX;
            selected.y = nextY;
            selected.width = nextWidth;
            selected.height = nextHeight;
            selected.gap = nextGap;
            if (selected.type.equals("slider")) {
                selected.minimum = nextMinimum;
                selected.maximum = nextMaximum;
                selected.step = nextStep;
                selected.value = normalizeSliderValue(selected.value, nextMinimum, nextMaximum, nextStep);
            }
            rebuildCanvas();
            refreshInspector();
            status = "Propriedades aplicadas.";
        } catch (IllegalArgumentException exception) {
            status = "Valor inválido: " + shortMessage(exception);
        }
    }

    private static int parseBoundedInt(String value, int minimum, int maximum) {
        int parsed = Integer.parseInt(value.trim());
        if (parsed < minimum || parsed > maximum) {
            throw new IllegalArgumentException("Valor permitido: " + minimum + " a " + maximum);
        }
        return parsed;
    }

    private static double parseFiniteDouble(String value) {
        double parsed = Double.parseDouble(value.trim());
        if (!Double.isFinite(parsed)) throw new IllegalArgumentException("Use valores numéricos finitos");
        return parsed;
    }

    private static double normalizeSliderValue(double value, double minimum, double maximum, double step) {
        double normalized = Math.max(minimum, Math.min(maximum, value));
        double steps = step == 0 ? 0 : (normalized - minimum) / step;
        if (step > 0 && Double.isFinite(steps) && steps <= Long.MAX_VALUE) {
            normalized = minimum + Math.round(steps) * step;
        }
        return Math.max(minimum, Math.min(maximum, normalized));
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private void cycleAlignment(boolean horizontal) {
        if (selected == null) return;
        Alignment current = horizontal ? selected.horizontalAlignment : selected.verticalAlignment;
        Alignment[] values = Alignment.values();
        Alignment next = values[(current.ordinal() + 1) % values.length];
        if (horizontal) selected.horizontalAlignment = next;
        else selected.verticalAlignment = next;
        rebuildCanvas();
        refreshInspector();
    }

    private void toggleEnabled() {
        if (selected == null) return;
        if (selected.type.equals("spacer")) {
            selected.flexible = !selected.flexible;
        } else if (selected.type.equals("toggle") || selected.type.equals("chip")) {
            selected.selected = !selected.selected;
        } else {
            selected.enabled = !selected.enabled;
        }
        rebuildCanvas();
        refreshInspector();
    }

    private void removeSelected() {
        if (selected == null || selected == document.root || selected.parent == null) return;
        selected.parent.removeChild(selected);
        selected = null;
        rebuildCanvas();
        refreshInspector();
        status = "Componente removido.";
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
