package com.radaeli.easyblockpallets.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Per-screen overlay that stacks, expires, and dismisses toast notifications. */
public final class ToastHost extends UiComponent {
    private static final int MAX_TOASTS = 4;
    private static final int MAX_WIDTH = 240;
    private static final int MIN_WIDTH = 96;
    private static final int MAX_LINES = 3;
    private static final int EDGE = 8;
    private static final int PADDING_X = 9;
    private static final int PADDING_Y = 6;
    private static final int GAP = 5;
    private static final AtomicLong NEXT_TOAST_ID = new AtomicLong();

    private final List<ActiveToast> activeToasts = new ArrayList<>();
    private Font layoutFont;
    private int transitionDurationMillis = 140;

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return UiSize.ZERO;
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        layoutFont = font;
        transitionDurationMillis = theme.tokens().transitionDurationMillis();
        layoutToasts();
    }

    public void show(Toast toast) {
        activeToasts.add(0, new ActiveToast(Objects.requireNonNull(toast, "toast")));
        while (activeToasts.size() > MAX_TOASTS) {
            ActiveToast removed = activeToasts.remove(activeToasts.size() - 1);
            clearAnimation("toast-opacity-" + removed.id);
        }
        layoutToasts();
    }

    public void info(Component message) {
        show(Toast.info(message));
    }

    public void success(Component message) {
        show(Toast.success(message));
    }

    public void warning(Component message) {
        show(Toast.warning(message));
    }

    public void error(Component message) {
        show(Toast.error(message));
    }

    /** Called from the owning screen's tick; inactive screens do not age their toasts. */
    public void tick() {
        if (activeToasts.isEmpty()) return;
        for (int index = activeToasts.size() - 1; index >= 0; index--) {
            ActiveToast active = activeToasts.get(index);
            if (active.remainingTicks > 0) {
                active.remainingTicks--;
                if (active.remainingTicks == 0) active.exitTicks = exitDurationTicks();
            } else if (active.exitTicks > 0) {
                active.exitTicks--;
            }
            if (active.remainingTicks <= 0 && active.exitTicks <= 0) {
                activeToasts.remove(index);
                clearAnimation("toast-opacity-" + active.id);
            }
        }
        layoutToasts();
    }

    public int size() {
        return activeToasts.size();
    }

    @Override
    public void render(UiContext context) {
        if (layoutFont != context.font()) {
            layoutFont = context.font();
            layoutToasts();
        }
        for (ActiveToast active : activeToasts) {
            if (active.bounds.width() == 0 || active.lines.isEmpty()) continue;
            double opacity = animateFrom(context, "toast-opacity-" + active.id,
                    active.remainingTicks > 0 ? 1 : 0, 0);
            if (opacity <= 0) continue;
            int slide = (int) Math.round((1 - opacity) * 14);
            Bounds visualBounds = new Bounds(active.bounds.x() + slide, active.bounds.y(),
                    active.bounds.width(), active.bounds.height());
            UiPaint.panel(context, visualBounds, opacity);
            UiPaint.rect(context, new Bounds(visualBounds.x(), visualBounds.y(), 3, visualBounds.height()),
                    UiPaint.withAlpha(accentColor(active.toast.level(), context), opacity));
            int textY = active.bounds.y() + PADDING_Y;
            for (FormattedCharSequence line : active.lines) {
                context.graphics().drawString(context.font(), line,
                        visualBounds.x() + PADDING_X, textY,
                        UiPaint.withAlpha(context.theme().palette().text(), opacity), false);
                textY += context.font().lineHeight;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        for (int index = 0; index < activeToasts.size(); index++) {
            ActiveToast active = activeToasts.get(index);
            if (!active.dismissing && active.bounds.contains(mouseX, mouseY)) {
                active.dismissing = true;
                active.remainingTicks = 0;
                active.exitTicks = exitDurationTicks();
                return true;
            }
        }
        return false;
    }

    private void layoutToasts() {
        if (layoutFont == null) return;
        int availableWidth = Math.max(0, Math.min(MAX_WIDTH, bounds().width() - EDGE * 2));
        int textWidth = Math.max(1, availableWidth - PADDING_X * 2 - 2);
        int y = bounds().y() + EDGE;
        int bottom = bounds().bottom() - EDGE;

        for (ActiveToast active : activeToasts) {
            List<FormattedCharSequence> wrapped = layoutFont.split(active.toast.message(), textWidth);
            if (wrapped.isEmpty()) wrapped = List.of(FormattedCharSequence.EMPTY);
            int linesShown = Math.min(MAX_LINES, wrapped.size());
            active.lines = List.copyOf(wrapped.subList(0, linesShown));
            int contentWidth = 0;
            for (FormattedCharSequence line : active.lines) {
                contentWidth = Math.max(contentWidth, layoutFont.width(line));
            }
            int width = Math.min(availableWidth,
                    Math.max(Math.min(MIN_WIDTH, availableWidth), contentWidth + PADDING_X * 2));
            int height = active.lines.size() * layoutFont.lineHeight + PADDING_Y * 2;
            if (availableWidth == 0 || y + height > bottom) {
                active.bounds = Bounds.EMPTY;
                continue;
            }
            active.bounds = new Bounds(bounds().right() - EDGE - width, y, width, height);
            y += height + GAP;
        }
    }

    private int exitDurationTicks() {
        return Math.max(1, (transitionDurationMillis + 49) / 50);
    }

    private static int accentColor(Toast.Level level, UiContext context) {
        return switch (level) {
            case INFO -> context.theme().palette().accent();
            case SUCCESS -> 0xFF63C78D;
            case WARNING -> 0xFFE8B64F;
            case ERROR -> 0xFFE56B73;
        };
    }

    private static final class ActiveToast {
        private final long id = NEXT_TOAST_ID.incrementAndGet();
        private final Toast toast;
        private int remainingTicks;
        private int exitTicks;
        private boolean dismissing;
        private Bounds bounds = Bounds.EMPTY;
        private List<FormattedCharSequence> lines = List.of();

        private ActiveToast(Toast toast) {
            this.toast = toast;
            this.remainingTicks = toast.durationTicks();
        }
    }
}
