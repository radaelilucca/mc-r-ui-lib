package com.radaeli.easyblockpallets.client.playground;

import com.radaeli.easyblockpallets.ui.AbsoluteLayout;
import com.radaeli.easyblockpallets.ui.Alignment;
import com.radaeli.easyblockpallets.ui.Bounds;
import com.radaeli.easyblockpallets.ui.Button;
import com.radaeli.easyblockpallets.ui.Chip;
import com.radaeli.easyblockpallets.ui.ClipStack;
import com.radaeli.easyblockpallets.ui.Column;
import com.radaeli.easyblockpallets.ui.Label;
import com.radaeli.easyblockpallets.ui.Grid;
import com.radaeli.easyblockpallets.ui.ItemCard;
import com.radaeli.easyblockpallets.ui.ItemGrid;
import com.radaeli.easyblockpallets.ui.ProgressBar;
import com.radaeli.easyblockpallets.ui.RadialMenu;
import com.radaeli.easyblockpallets.ui.RadialSegment;
import com.radaeli.easyblockpallets.ui.Row;
import com.radaeli.easyblockpallets.ui.ScrollView;
import com.radaeli.easyblockpallets.ui.SearchField;
import com.radaeli.easyblockpallets.ui.Slider;
import com.radaeli.easyblockpallets.ui.Stack;
import com.radaeli.easyblockpallets.ui.Table;
import com.radaeli.easyblockpallets.ui.TableColumn;
import com.radaeli.easyblockpallets.ui.Tabs;
import com.radaeli.easyblockpallets.ui.TextField;
import com.radaeli.easyblockpallets.ui.Toggle;
import com.radaeli.easyblockpallets.ui.Spacer;
import com.radaeli.easyblockpallets.ui.UiComponent;
import com.radaeli.easyblockpallets.ui.UiContainer;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class PlaygroundComponentFactory {
    private PlaygroundComponentFactory() {
    }

    static UiComponent build(PlaygroundNode node, Map<PlaygroundNode, UiComponent> components,
                             Consumer<PlaygroundNode> onButtonClick,
                             BiConsumer<PlaygroundNode, Boolean> onSelectionChange) {
        UiComponent component = switch (node.type) {
            case "label" -> new Label(Component.literal(node.text), node.color);
            case "button" -> new Button(Component.literal(node.text), () -> onButtonClick.accept(node));
            case "toggle" -> new Toggle(Component.literal(node.text), node.selected,
                    selected -> onSelectionChange.accept(node, selected));
            case "chip" -> new Chip(Component.literal(node.text), node.selected,
                    selected -> onSelectionChange.accept(node, selected));
            case "textfield" -> textField(node, false);
            case "searchfield" -> textField(node, true);
            case "slider" -> new Slider(node.minimum, node.maximum, node.value, node.step,
                    value -> node.value = value);
            case "progressbar" -> new ProgressBar(node.minimum, node.maximum, node.value)
                    .setFormatter(value -> Component.literal(java.math.BigDecimal.valueOf(value)
                            .stripTrailingZeros().toPlainString()));
            case "radial" -> createRadialMenu(node, onButtonClick);
            case "itemcard" -> createItemCard(node, onButtonClick);
            case "spacer" -> node.flexible ? Spacer.flex() : Spacer.fixed(node.width, node.height);
            case "row" -> new Row(node.gap, safeAlignment(node.horizontalAlignment),
                    safeAlignment(node.verticalAlignment));
            case "column" -> new Column(node.gap, safeAlignment(node.verticalAlignment),
                    safeAlignment(node.horizontalAlignment));
            case "stack" -> new Stack(safeAlignment(node.horizontalAlignment),
                    safeAlignment(node.verticalAlignment));
            case "tabs" -> new Tabs();
            case "scroll" -> new ScrollView(node.gap);
            case "clip" -> new ClipStack();
            case "grid" -> new Grid(node.columns, node.gap);
            case "itemgrid" -> new ItemGrid(node.columns, node.gap,
                    (index, stack) -> onButtonClick.accept(node));
            case "table" -> createTable(node, onButtonClick);
            case "absolute" -> new AbsoluteLayout();
            default -> throw new IllegalArgumentException("Unsupported component type: " + node.type);
        };
        component.setEnabled(node.enabled);
        if (node.type.equals("spacer") && node.flexible) {
            component.setPreferredSize(null, null);
        } else {
            component.setPreferredSize(node.width, node.height);
        }
        components.put(node, component);

        if (component instanceof UiContainer container) {
            for (PlaygroundNode childNode : node.children) {
                UiComponent child = build(childNode, components, onButtonClick, onSelectionChange);
                if (container instanceof AbsoluteLayout absolute) {
                    absolute.addAt(child, new Bounds(childNode.x, childNode.y,
                            childNode.width, childNode.height));
                } else if (container instanceof Tabs tabs) {
                    tabs.addTab(Component.literal(childNode.text), child);
                } else {
                    container.add(child);
                }
            }
            if (container instanceof Tabs tabs && !node.children.isEmpty()) {
                int selectedTab = Math.max(0, Math.min(node.selectedTab, node.children.size() - 1));
                tabs.select(selectedTab);
                tabs.setOnSelectionChanged(index -> node.selectedTab = index);
            }
        }
        return component;
    }

    static Map<PlaygroundNode, UiComponent> newComponentMap() {
        return new IdentityHashMap<>();
    }

    private static TextField textField(PlaygroundNode node, boolean search) {
        TextField field = search ? new SearchField() : new TextField();
        field.setValue(node.text);
        field.setResponder(value -> node.text = value);
        return field;
    }

    private static ItemCard createItemCard(PlaygroundNode node, Consumer<PlaygroundNode> onClick) {
        var item = resolveItem(node.itemId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + node.itemId));
        ItemStack stack = new ItemStack(item, node.itemCount);
        Component label = Component.literal(node.text == null || node.text.isBlank()
                ? stack.getHoverName().getString() : node.text);
        return new ItemCard(stack, label, node.selected, () -> onClick.accept(node));
    }

    private static Table<String[]> createTable(PlaygroundNode node, Consumer<PlaygroundNode> onClick) {
        String[] configuredHeaders = node.text == null ? new String[0] : node.text.split(",", -1);
        int columnCount = Math.max(1, Math.min(16, node.columns));
        Table<String[]> table = new Table<>();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final int index = columnIndex;
            String title = index < configuredHeaders.length ? configuredHeaders[index].trim() : "Column " + (index + 1);
            if (title.isEmpty()) title = "Column " + (index + 1);
            table.addColumn(TableColumn.preferred(Component.literal(title), Alignment.START,
                    row -> Component.literal(row[index])));
        }
        List<String[]> rows = new ArrayList<>();
        String[] sampleNames = {"Copper Ore", "Iron Plate", "Brass Ingot", "Zinc Nugget",
                "Andesite Alloy", "Precision Mechanism", "Train Schedule", "Empty Basin"};
        for (int rowIndex = 0; rowIndex < sampleNames.length; rowIndex++) {
            String[] row = new String[columnCount];
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                row[columnIndex] = switch (columnIndex) {
                    case 0 -> sampleNames[rowIndex];
                    case 1 -> Integer.toString((rowIndex + 1) * 8);
                    case 2 -> rowIndex % 3 == 0 ? "Ready" : "Queued";
                    default -> "Row " + (rowIndex + 1);
                };
            }
            rows.add(row);
        }
        return table.setRows(rows).setOnRowClicked((index, row) -> onClick.accept(node));
    }

    private static RadialMenu createRadialMenu(PlaygroundNode node, Consumer<PlaygroundNode> onClick) {
        RadialMenu menu = new RadialMenu();
        String[] labels = node.text == null ? new String[0] : node.text.split(",", -1);
        for (String label : labels) {
            String cleanLabel = label.trim();
            if (!cleanLabel.isEmpty()) {
                menu.addSegment(new RadialSegment(Component.literal(cleanLabel), true,
                        () -> onClick.accept(node)));
            }
        }
        return menu;
    }

    static boolean itemExists(String itemId) {
        ResourceLocation id = itemId == null ? null : ResourceLocation.tryParse(itemId);
        return id != null && resolveItem(id).isPresent();
    }

    private static java.util.Optional<net.minecraft.world.item.Item> resolveItem(String itemId) {
        ResourceLocation id = itemId == null ? null : ResourceLocation.tryParse(itemId);
        return id == null ? java.util.Optional.empty() : resolveItem(id);
    }

    private static java.util.Optional<net.minecraft.world.item.Item> resolveItem(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id);
    }

    private static Alignment safeAlignment(Alignment alignment) {
        return alignment == null ? Alignment.START : alignment;
    }
}
