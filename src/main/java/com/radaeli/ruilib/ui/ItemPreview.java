package com.radaeli.ruilib.ui;

import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

/** Scalable vanilla item preview. Tooltip is rendered at the pointer in unscaled coordinates. */
public final class ItemPreview extends UiComponent {
  private ItemStack stack;
  private final int size;

  public ItemPreview(ItemStack stack, int size) {
    if (size < 1 || size > 512) throw new IllegalArgumentException("Preview size must be 1–512");
    this.stack = Objects.requireNonNull(stack).copy();
    this.size = size;
  }

  public void setItem(ItemStack stack) {
    this.stack = Objects.requireNonNull(stack).copy();
  }

  @Override
  protected UiSize measureIntrinsic(Font font, UiTheme theme, int w, int h) {
    return new UiSize(size, size).constrain(w, h);
  }

  @Override
  public void render(UiContext context) {
    int actual = Math.min(bounds().width(), bounds().height());
    if (actual <= 0) return;
    var pose = context.graphics().pose();
    pose.pushPose();
    try {
      pose.translate(bounds().centerX() - actual / 2.0, bounds().centerY() - actual / 2.0, 0);
      pose.scale(actual / 16f, actual / 16f, 1);
      ItemRenderer.render(context, stack, 0, 0);
    } finally {
      pose.popPose();
    }
  }

  /** UiScreen invokes this after containers have restored their scissor state. */
  public void renderTooltip(UiContext context) {
    ItemRenderer.renderTooltip(context, stack);
  }
}
