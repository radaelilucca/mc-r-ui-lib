package com.radaeli.ruilib.client.example;

import com.radaeli.ruilib.ui.Alignment;
import com.radaeli.ruilib.ui.Bounds;
import com.radaeli.ruilib.ui.Column;
import com.radaeli.ruilib.ui.InventoryGrid;
import com.radaeli.ruilib.ui.ItemSlot;
import com.radaeli.ruilib.ui.Label;
import com.radaeli.ruilib.ui.Panel;
import com.radaeli.ruilib.ui.Row;
import com.radaeli.ruilib.ui.UiComponent;
import com.radaeli.ruilib.ui.UiContext;
import com.radaeli.ruilib.ui.UiPaint;
import com.radaeli.ruilib.ui.UiScreen;
import com.radaeli.ruilib.ui.UiSize;
import com.radaeli.ruilib.ui.UiTheme;
import com.radaeli.ruilib.ui.UiText;
import com.radaeli.ruilib.ui.UiTextStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Development-only screen for manually trying ghost-slot drops from the player inventory and JEI. */
public final class ItemSlotExampleScreen extends UiScreen {
    private static final int INVENTORY_WIDTH = 9 * 20;
    private static final int PANEL_MAX_WIDTH = INVENTORY_WIDTH + 32;

    private final Panel panel;

    public ItemSlotExampleScreen() {
        this(createPanel());
    }

    private ItemSlotExampleScreen(Panel panel) {
        super(Component.translatable("screen.r_ui_lib.item_slot_example"), panel);
        this.panel = panel;
    }

    private static Panel createPanel() {
        Column content = new Column(8);
        content.add(new Label(Component.translatable("screen.r_ui_lib.item_slot_example.title")));
        content.add(new WrappedLabel(Component.translatable("screen.r_ui_lib.item_slot_example.instructions")));

        Row filters = new Row(18, Alignment.CENTER, Alignment.START);
        Column firstFilter = new Column(4, Alignment.START, Alignment.CENTER);
        firstFilter.add(new Label(Component.translatable("screen.r_ui_lib.item_slot_example.filter_a")));
        firstFilter.add(new ItemSlot());
        Column secondFilter = new Column(4, Alignment.START, Alignment.CENTER);
        secondFilter.add(new Label(Component.translatable("screen.r_ui_lib.item_slot_example.filter_b")));
        secondFilter.add(new ItemSlot());
        filters.add(firstFilter);
        filters.add(secondFilter);
        content.add(filters);

        content.add(new Label(Component.translatable("screen.r_ui_lib.item_slot_example.inventory")));
        content.add(InventoryGrid.playerInventory());
        return new Panel(content);
    }

    @Override
    protected void layoutRoot(UiTheme theme) {
        UiSize size = panel.measure(font, theme, Math.min(PANEL_MAX_WIDTH, Math.max(0, width - 24)),
                Math.max(0, height - 24));
        panel.layout(font, theme, new Bounds((width - size.width()) / 2, (height - size.height()) / 2,
                size.width(), size.height()));
    }

    /** Bounds used by JEI to reserve the area beside this custom screen. */
    public Bounds jeiGuiBounds() {
        return panel.bounds();
    }

    private static final class WrappedLabel extends UiComponent {
        private final Component text;

        private WrappedLabel(Component text) {
            this.text = text;
        }

        @Override
        protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
            List<FormattedCharSequence> lines = lines(font, theme, maxWidth);
            float scale = theme.tokens().typography().scale(UiTextStyle.BODY);
            int width = Math.round(lines.stream().mapToInt(font::width).max().orElse(0) * scale);
            int height = lines.size() * UiText.height(font, theme);
            return new UiSize(width, height).constrain(maxWidth, maxHeight);
        }

        @Override
        public void render(UiContext context) {
            List<FormattedCharSequence> lines = lines(context.font(), context.theme(), bounds().width());
            int lineHeight = UiText.height(context.font(), context.theme());
            for (int i = 0; i < lines.size(); i++) {
                UiPaint.text(context, lines.get(i), bounds().x(), bounds().y() + i * lineHeight,
                        context.theme().palette().text(), UiTextStyle.BODY);
            }
        }

        private List<FormattedCharSequence> lines(Font font, UiTheme theme, int availableWidth) {
            float scale = theme.tokens().typography().scale(UiTextStyle.BODY);
            int maxFontWidth = Math.max(1, (int) Math.floor(availableWidth / scale));
            List<FormattedCharSequence> wrapped = font.split(text, maxFontWidth);
            return wrapped.isEmpty() ? List.of(FormattedCharSequence.EMPTY) : wrapped;
        }
    }
}
