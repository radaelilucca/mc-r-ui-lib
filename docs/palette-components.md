# Color and preview components (0.6.0)

Shared API for Forge 1.20.1 / Java 17 and NeoForge 1.21.1 / Java 21:

```java
ItemPreview preview = new ItemPreview(stack, 64);
ColorSwatch swatch = new ColorSwatch(0x4e91bd, false, () -> selectColor());
ColorPicker picker = new ColorPicker(0x4e91bd, rgb -> applyColor(rgb), () -> cancel());
HorizontalScrollView columns = new HorizontalScrollView(6);
Panel column = new Panel(content).setBackground(0xff4e91bd);
column.setPreferredSize(140, null);
columns.add(column);
```

`ItemPreview` copies its stack, accepts sizes 1–512 and scales the vanilla item
renderer. `UiScreen` renders its tooltip after clipping containers have finished.
Use `renderTooltip(context)` explicitly when drawing previews outside `UiScreen`.

`ColorSwatch` masks RGB to 24 bits and supports click, Enter and Space. The caller
owns selection state through `setSelected`; a hex tooltip identifies the color.

`ColorPicker` owns uncommitted preview state. RGB sliders and `#RRGGBB` input stay
in sync. Malformed hex disables Apply; only Apply invokes the commit consumer.
Cancel invokes its callback without committing. Host it in a modal at least 250 GUI
pixels high, or a vertically scrolling region when less height is available.

`HorizontalScrollView` lays out fixed-width children, clips rendering and hit tests,
and supports wheel, Left/Right keys and scrollbar dragging. Tab visits the viewport
and its visible child controls. `setScrollOffset` clamps to the content range.

`Panel.setBackground(null)` restores the normal theme panel. Existing constructors
and defaults are unchanged. `Button.label()` returns its current display label. `Label.setText` updates dynamic messages; relayout the parent when its natural size should change.

Playground categories Input, Data, Layout and Scroll include representative examples.
The opt-in `-PuiValidation` client suite exercises color preview/apply/cancel, invalid
hex, keyboard slider changes, horizontal wheel/keyboard/drag, and all bundled layouts.

## Grid composition (0.7.0)

`SelectableCard(content, selected, onSelect)` is a neutral frame around arbitrary
content. Passive content selects the frame; focusable child controls receive input
without bubbling. Tab visits the frame and its children. Selection uses the outer
accent border; keyboard focus uses a separate inner border.

`SegmentedControl<T>(options, value, onChange)` owns one exclusive value. Options
carry a value, label and optional enabled flag. Mouse commits on release; Left/Right
skip disabled options, Home/End select the first/last enabled option. `setValue`
updates without invoking callbacks. Enter/Space keep the current selection.

`IconButton` also accepts `Symbol.LOCK_OPEN` or `Symbol.LOCK_CLOSED`. These are
vector icons and do not depend on the Minecraft font containing emoji.

Screens that rebuild their trees can read `focusedComponent()` and use
`focusComponent(replacement)` after layout to preserve visible keyboard focus.
