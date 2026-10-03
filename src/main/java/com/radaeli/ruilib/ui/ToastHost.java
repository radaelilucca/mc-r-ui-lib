package com.radaeli.ruilib.ui;

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
    private static final AtomicLong NEXT_TOAST_ID = new AtomicLong();

    private final List<ActiveToast> activeToasts = new ArrayList<>();
    private Font layoutFont;
    private UiTheme layoutTheme;
    private int transitionDurationMillis = 140;

    @Override
    protected UiSize measureIntrinsic(Font font, UiTheme theme, int maxWidth, int maxHeight) {
        return UiSize.ZERO;
    }

    @Override
    protected void layoutChildren(Font font, UiTheme theme) {
        layoutFont = font;
        layoutTheme = theme;
        transitionDurationMillis = theme.tokens().motion().transitionDurationMillis();
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
        if (layoutFont != context.font() || layoutTheme != context.theme()) {
            layoutFont = context.font();
            layoutTheme = context.theme();
            transitionDurationMillis = context.theme().tokens().motion().transitionDurationMillis();
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
            UiPaint.roundedRect(context, new Bounds(visualBounds.x(), visualBounds.y(), 3, visualBounds.height()),
                    UiPaint.withAlpha(accentColor(active.toast.level(), context), opacity),
                    context.theme().tokens().radii().pill());
            Insets padding = context.theme().tokens().cardPadding();
            int textY = active.bounds.y() + padding.top();
            for (FormattedCharSequence line : active.lines) {
                UiPaint.text(context.withOpacity(opacity), line,
                        visualBounds.x() + padding.left(), textY, context.theme().palette().text(), UiTextStyle.SMALL);
                textY += UiText.height(context.font(), context.theme(), UiTextStyle.SMALL);
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
        if (layoutFont == null || layoutTheme == null) return;
        Insets padding = layoutTheme.tokens().cardPadding();
        int edge = layoutTheme.tokens().largeGap();
        int gap = layoutTheme.tokens().smallGap();
        float textScale = layoutTheme.tokens().typography().smallScale();
        int availableWidth = Math.max(0, Math.min(MAX_WIDTH, bounds().width() - edge * 2));
        int textWidth = Math.max(1, (int) Math.floor((availableWidth - padding.left() - padding.right() - 2) / textScale));
        int y = bounds().y() + edge;
        int bottom = bounds().bottom() - edge;

        for (ActiveToast active : activeToasts) {
            List<FormattedCharSequence> wrapped = layoutFont.split(active.toast.message(), textWidth);
            if (wrapped.isEmpty()) wrapped = List.of(FormattedCharSequence.EMPTY);
            int linesShown = Math.min(MAX_LINES, wrapped.size());
            active.lines = List.copyOf(wrapped.subList(0, linesShown));
            int contentWidth = 0;
            for (FormattedCharSequence line : active.lines) {
                contentWidth = Math.max(contentWidth, Math.round(layoutFont.width(line) * textScale));
            }
            int width = Math.min(availableWidth,
                    Math.max(Math.min(MIN_WIDTH, availableWidth), contentWidth + padding.left() + padding.right()));
            int height = active.lines.size() * UiText.height(layoutFont, layoutTheme, UiTextStyle.SMALL)
                    + padding.top() + padding.bottom();
            if (availableWidth == 0 || y + height > bottom) {
                active.bounds = Bounds.EMPTY;
                continue;
            }
            active.bounds = new Bounds(bounds().right() - edge - width, y, width, height);
            y += height + gap;
        }
    }

    private int exitDurationTicks() {
        return Math.max(1, (transitionDurationMillis + 49) / 50);
    }

    private static int accentColor(Toast.Level level, UiContext context) {
        return switch (level) {
            case INFO -> context.theme().palette().accent();
            case SUCCESS -> context.theme().palette().success();
            case WARNING -> context.theme().palette().warning();
            case ERROR -> context.theme().palette().error();
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
