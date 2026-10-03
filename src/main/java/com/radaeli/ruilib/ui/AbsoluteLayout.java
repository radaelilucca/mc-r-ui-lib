package com.radaeli.ruilib.ui;

import net.minecraft.client.gui.Font;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/** Places children at explicit GUI coordinates relative to this container. */
public final class AbsoluteLayout extends UiContainer {
    private final Map<UiComponent, Bounds> placements = new IdentityHashMap<>();

    @Override
    public <T extends UiComponent> T add(T child) {
        throw new UnsupportedOperationException("Use addAt(component, bounds) with AbsoluteLayout");
    }

    /** Adds a child at coordinates relative to this container. */
    public <T extends UiComponent> T addAt(T child, Bounds placement) {
        Objects.requireNonNull(placement, "placement");
        super.add(child);
        placements.put(child, placement);
        return child;
    }

    public void setPlacement(UiComponent child, Bounds placement) {
        if (!placements.containsKey(child)) {
            throw new IllegalArgumentException("Component is not a child of this AbsoluteLayout");
        }
        placements.put(child, Objects.requireNonNull(placement, "placement"));
    }

    public Bounds placementOf(UiComponent child) {
        return placements.get(child);
    }

    @Override
    public boolean remove(UiComponent child) {
        placements.remove(child);
        return super.remove(child);
    }

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return UiSize.ZERO;
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        for (UiComponent child : children()) {
            Bounds placement = placements.get(child);
            if (placement == null) continue;
            child.layout(font, theme, new Bounds(
                    bounds().x() + placement.x(), bounds().y() + placement.y(),
                    placement.width(), placement.height()));
        }
    }
}
