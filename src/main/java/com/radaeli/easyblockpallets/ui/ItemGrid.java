package com.radaeli.easyblockpallets.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.BiConsumer;

/** Fixed-column grid with convenience methods for composing item cards. */
public final class ItemGrid extends Grid {
    private final BiConsumer<Integer, ItemStack> onItemClicked;

    public ItemGrid(int columns) {
        this(columns, 0, (index, stack) -> {});
    }

    public ItemGrid(int columns, int gap) {
        this(columns, gap, (index, stack) -> {});
    }

    public ItemGrid(int columns, int gap, BiConsumer<Integer, ItemStack> onItemClicked) {
        super(columns, gap);
        this.onItemClicked = Objects.requireNonNull(onItemClicked, "onItemClicked");
    }

    public ItemCard addItem(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        int index = children().size();
        ItemStack snapshot = stack.copy();
        ItemCard card = new ItemCard(snapshot, snapshot.getHoverName(),
                () -> onItemClicked.accept(index, snapshot.copy()));
        return add(card);
    }

    public ItemCard addItem(ItemStack stack, Component label) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(label, "label");
        int index = children().size();
        ItemStack snapshot = stack.copy();
        ItemCard card = new ItemCard(snapshot, label, () -> onItemClicked.accept(index, snapshot.copy()));
        return add(card);
    }
}
