package com.radaeli.ruilib.ui;

/** Theme-aware framed content region. Put an explicit Row/Column/Grid inside for child layout. */
public final class Panel extends UiFrame {
    private Integer background;
    /** Null restores the theme panel; an ARGB value supplies a flat local background. */
    public Panel setBackground(Integer background) { this.background = background; return this; }
    public Panel(UiComponent content) {
        this(content, null);
    }

    public Panel(UiComponent content, Insets padding) {
        super(content, padding);
    }

    public UiComponent contentComponent() {
        return content();
    }

    @Override
    public void render(UiContext context) {
        if (background == null) UiPaint.panel(context, bounds());
        else UiPaint.rect(context, bounds(), background);
        content().render(context);
    }
}
