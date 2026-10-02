package com.radaeli.easyblockpallets.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** One enabled or disabled action in a {@link RadialMenu}. */
public record RadialSegment(Component label, ItemStack icon, boolean enabled, Runnable onSelect) {
    public RadialSegment {
        Objects.requireNonNull(label, "label");
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        Objects.requireNonNull(onSelect, "onSelect");
    }

    public RadialSegment(Component label, boolean enabled, Runnable onSelect) {
        this(label, ItemStack.EMPTY, enabled, onSelect);
    }

    public RadialSegment(Component label, ItemStack icon, Runnable onSelect) {
        this(label, icon, true, onSelect);
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
