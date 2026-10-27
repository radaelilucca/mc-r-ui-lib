package com.radaeli.ruilib.client.playground;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import com.radaeli.ruilib.platform.UiPlatformScreen;
import com.radaeli.ruilib.platform.UiPlatform;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.List;

/** File picker for saved UI playground documents. */
final class UiPlaygroundLoadScreen extends UiPlatformScreen {
    private static final int ROW_HEIGHT = 22;

    private final UiPlaygroundScreen editor;
    private List<LoadEntry> layouts = List.of();
    private LoadEntry selectedLayout;
    private String error;
    private int scrollOffset;
    private int listTop;
    private int listBottom;
    private Button loadButton;

    UiPlaygroundLoadScreen(UiPlaygroundScreen editor, String preferredSelection) {
        super(Component.literal("Load UI Layout"));
        this.editor = editor;
        List<LoadEntry> entries = new java.util.ArrayList<>();
        UiPlaygroundScreen.builtInLayouts().forEach(layout ->
                entries.add(new LoadEntry(layout.id(), layout.label(), true)));
        layouts = List.copyOf(entries);
        if (!layouts.isEmpty()) selectedLayout = layouts.get(0);
        try {
            UiPlaygroundScreen.savedLayoutNames().forEach(name ->
                    entries.add(new LoadEntry(name, "Saved — " + name, false)));
            layouts = List.copyOf(entries);
            selectedLayout = layouts.stream().filter(entry -> !entry.builtIn()
                    && entry.id().equals(preferredSelection)).findFirst()
                    .orElse(selectedLayout);
        } catch (IOException exception) {
            error = "Could not list layouts: " + message(exception);
        }
    }

    @Override
    protected void init() {
        listTop = 46;
        listBottom = Math.max(listTop + ROW_HEIGHT, height - 58);
        loadButton = addRenderableWidget(Button.builder(Component.literal("Load"), button -> loadSelected())
                .bounds(width / 2 - 96, height - 28, 92, 20).build());
        loadButton.active = selectedLayout != null;
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> returnToEditor())
                .bounds(width / 2 + 4, height - 28, 92, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        String heading = "Load UI Layout";
        graphics.drawString(font, heading, width / 2 - font.width(heading) / 2, 18, 0xFFFFFFFF, false);
        graphics.drawString(font, "Select a saved layout", 12, 34, 0xFFADB7C5, false);

        int visibleRows = visibleRows();
        graphics.enableScissor(8, listTop, width - 8, listBottom);
        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= layouts.size()) break;
            int y = listTop + row * ROW_HEIGHT;
            LoadEntry entry = layouts.get(index);
            boolean selected = entry.equals(selectedLayout);
            boolean hovered = mouseX >= 8 && mouseX < width - 8
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;
            int background = selected ? 0xFF344D65 : hovered ? 0xFF2A3542 : 0xFF202832;
            graphics.fill(8, y, width - 8, y + ROW_HEIGHT - 1, background);
            UiPlatform.outline(graphics, 8, y, width - 16, ROW_HEIGHT - 1,
                    selected ? 0xFF67B7F7 : 0xFF35404D);
            graphics.drawString(font, entry.label() + (entry.builtIn() ? "" : ".json"),
                    16, y + 6, 0xFFF0F3F7, false);
        }
        graphics.disableScissor();

        if (layouts.isEmpty()) {
            String message = error == null ? "No saved layouts yet. Save a layout from the editor first." : error;
            graphics.drawString(font, message, 12, listTop + 8, 0xFFFFAA88, false);
        } else if (error != null) {
            graphics.drawString(font, error, 12, height - 44, 0xFFFFAA88, false);
        }
        renderables.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && mouseX >= 8 && mouseX < width - 8
                && mouseY >= listTop && mouseY < listBottom) {
            int row = (int) (mouseY - listTop) / ROW_HEIGHT;
            int index = scrollOffset + row;
            if (index >= 0 && index < layouts.size() && listTop + row * ROW_HEIGHT < listBottom) {
                selectedLayout = layouts.get(index);
                if (loadButton != null) loadButton.active = true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= 8 && mouseX < width - 8 && mouseY >= listTop && mouseY < listBottom) {
            int maxOffset = Math.max(0, layouts.size() - visibleRows());
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            returnToEditor();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && selectedLayout != null) {
            loadSelected();
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) && !layouts.isEmpty()) {
            int index = layouts.indexOf(selectedLayout);
            index += keyCode == GLFW.GLFW_KEY_DOWN ? 1 : -1;
            index = Math.max(0, Math.min(layouts.size() - 1, index));
            selectedLayout = layouts.get(index);
            ensureSelectedVisible(index);
            if (loadButton != null) loadButton.active = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void loadSelected() {
        if (selectedLayout != null && (selectedLayout.builtIn()
                ? editor.loadBuiltInLayout(selectedLayout.id())
                : editor.loadSavedLayout(selectedLayout.id()))) {
            Minecraft.getInstance().setScreen(editor);
        } else if (selectedLayout != null) {
            error = editor.loadStatus();
        }
    }

    private void returnToEditor() {
        Minecraft.getInstance().setScreen(editor);
    }

    private int visibleRows() {
        return Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
    }

    private void ensureSelectedVisible(int index) {
        int visibleRows = visibleRows();
        if (index < scrollOffset) scrollOffset = index;
        else if (index >= scrollOffset + visibleRows) scrollOffset = index - visibleRows + 1;
    }

    private static String message(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private record LoadEntry(String id, String label, boolean builtIn) { }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
