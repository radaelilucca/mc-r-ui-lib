package com.radaeli.ruilib.ui;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Vanilla-backed item icon and tooltip rendering for toolkit components. */
public final class ItemRenderer {
    private ItemRenderer() {
    }

    public static void render(UiContext context, ItemStack stack, int x, int y) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) return;
        context.graphics().renderItem(stack, x, y);
        context.graphics().renderItemDecorations(context.font(), stack, x, y);
    }

    public static void renderTooltip(UiContext context, ItemStack stack) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(stack, "stack");
        if (!stack.isEmpty()) {
            context.graphics().renderTooltip(context.font(), stack,
                    (int) context.mouseX(), (int) context.mouseY());
        }
    }
}
