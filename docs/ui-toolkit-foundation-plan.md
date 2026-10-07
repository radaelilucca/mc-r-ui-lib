# Action plan — R-Ui Lib foundation

- **Project:** R-Ui Lib
- **Confirmed platform:** Minecraft 1.21.1, NeoForge 21.1.236, Java 21
- **Status:** phases 1, 2, and 3, the phase 4 visual playground, and the `Toggle`, `Tabs`, `Chip`, `ClipStack`, `ScrollView`, `TextField`, `SearchField`, `Slider`, `Grid`, `Spacer`, `ItemRenderer`, `ItemCard`, `ItemGrid`, `InventoryGrid`, `Toast`, `ToastHost`, `Table`, `ProgressBar`, `RadialMenu`, `Panel`, `Card`, `IconButton`, `Divider`, `Tooltip`, and `Modal` primitives are implemented; visual components can be demonstrated in the playground; manual in-game validation is pending

## Origin and boundaries

The toolkit started as isolated code in this project and is now distributed as the R-Ui Lib library. Logistics Network served as a reference for behavior and visual organization; its local JAR remains in `references/` for reference only.

The `minecraft-ui-toolkit-prd.md` file is treated as a draft requirements document. It defines the client-side direction, the thin layer over Minecraft APIs, composition, themes, and the broad list of future components. The phases and priorities below propose an execution sequence that starts with a verifiable foundation; they are not additional requirements attributed to the PRD.

## Recommended decisions for the first iteration

1. **Keep NeoForge and Minecraft 1.21.1**, aligned with the existing build.
2. **Distribute the library as an independent mod**, in the public package `com.radaeli.ruilib.ui`, with a clear separation between API and internal details.
3. **Keep the toolkit independent of domain logic:** the API contains no business rules, networking, or logic from another consuming mod.
4. **Treat all integration with `Screen`, `GuiGraphics`, input, and widgets as client-side.** The common mod constructor and server initialization must not load client-only classes.
5. **Use vanilla APIs where they already provide the behavior** — fonts, `ItemStack`, item rendering, tooltips, and text editing — and control the custom appearance of panels and controls.
6. **Do not assume the Logistics Network JAR provides reusable code.** It is a local reference for behavior and visual organization; the project page states “All Rights Reserved.” Do not copy code, resources, or visual identity into the mod.

## Work sequence

### Phase 0 — Define the library boundary

- Record the package location, the contract prohibiting domain dependencies, and the client-side boundary.
- Run a short spike against the configured version's APIs to confirm the `Screen` lifecycle, rendering with `GuiGraphics`, GUI coordinates, and clipping before abstracting these areas.
- Keep the scope free of external dependencies and support for other loaders.

**Deliverable:** an installable library that other mods can consume, without initializing UI on the dedicated server.

### Phase 1 — Geometry, themes, and painting

**Status:** implemented in the `com.radaeli.ruilib.ui` package.

`Bounds` uses rectangles in scaled GUI coordinates; themes and tokens are
immutable; `UiContext`, `UiPaint`, and `UiText` use vanilla `GuiGraphics` and `Font`.

Implement only the foundation used by all components:

- `Bounds` and `Insets`, with hit testing, edges, center, and insets;
- `UiPalette`, `UiTokens`, and `UiTheme`, initially with a Dark palette;
- `UiContext`, combining `GuiGraphics`, `Font`, theme, pointer, and relevant screen dimensions;
- `UiPaint` for rectangles, outlines, panel/card backgrounds, dividers, and hover/focus states;
- a thin text helper over `Font`, without replacing Minecraft's text measurement and drawing.

**Deliverable:** a demonstration screen can draw a title, panel, card, divider, and focus using the central theme; consuming components do not need to draw shapes directly.

### Phase 2 — Component tree and a small layout system

**Status:** implemented. `UiComponent` defines measurement/layout/rendering/input;
`UiContainer` maintains render order and hit-tests the topmost child first.
`Column`, `Row`, `Stack`, and `Label` compose the tree; `UiScreen` recalculates
layout when the `Screen` lifecycle initializes or resizes.

- Define `UiComponent` with an explicit cycle for measurement, layout, rendering, and required input.
- Define `UiContainer` and consistent traversal order for rendering and hit testing.
- Create `UiScreen` as a thin bridge to the `Screen` lifecycle.
- Implement `Column`, `Row`, and `Stack` first, with fixed alignment and spacing.
- Make final bounds the layout's responsibility; consumers do not position every child with absolute coordinates.

**Deliverable:** build a nested screen with a title, panel, content, and action without repeating manual position calculations in the host screen.

### Phase 3 — First interactive control

**Status:** implemented for `Button`. Focus moves through enabled controls in
tree order; Tab/Shift+Tab navigate, and Enter, Numpad Enter, or Space activate
the focused button. Clicking captures the button until release.

- Create `Button` with normal, hover, pressed, focused, and disabled states.
- Centralize hit testing and simple dispatch in the component/tree: topmost child first; stop when the event is consumed.
- Add basic mouse and keyboard focus, including Tab/Shift+Tab and activation of the focused control.
- Delegate text, fonts, and tooltip behavior to Minecraft when possible.

**Deliverable:** a visually consistent button usable with mouse and keyboard, without duplicated hit-testing logic in `Screen`.

### Available initial API

```java
Column content = new Column(8);
content.add(new Label(Component.literal("Title")));
content.add(new Button(Component.literal("Confirm"), this::confirm));
UiScreen screen = new UiScreen(Component.literal("Example"), content);
```

Every class integrating `Screen`, `Font`, or `GuiGraphics` belongs to the library's
client-side package. The common `RUiLib` class does not import that package.
Layouts use measured intrinsic sizes and fixed alignment; this iteration
does not include flex-grow or scrolling.

### Additional interactive primitives

**Status:** `Toggle`, `Tabs`, and `Chip` have been added to the initial set.

- `Toggle` exposes binary selection and notifies a `Consumer<Boolean>` when toggled.
- `Chip` provides the same binary selection in a compact form for filters/options.
- `Tabs` contains named pages, renders only the selected page, and supports
  switching by clicking or using left/right arrows while focused.
- All three are available in the playground palette. Selection states are
  included in JSON; Java callbacks remain local and are never serialized.

### Clipping, scrolling, and text input

**Status:** `ClipStack`, `ScrollView`, `TextField`, and `SearchField` have been added.

- `ClipStack` positions overlapping children within the same rectangle and uses
  `GuiGraphics` scissoring to prevent drawing beyond its bounds.
- `ScrollView` arranges children vertically, clips content, supports mouse-wheel
  scrolling, and provides a draggable scrollbar.
- `TextField` delegates editing to vanilla `EditBox`, preserving the cursor,
  selection, clipboard, and IME. The toolkit tree forwards keyboard, character,
  and drag events.
- `SearchField` specializes `TextField` with a default hint, magnifying-glass
  icon, and space reserved for the prefix; both respond to value changes.
- Text is vertically centered by vanilla `EditBox`. The base field keeps at
  least 8 px of left padding, plus the icon space in the search field.
- All four types are in the playground palette. Field values are persisted
  as plain text in JSON.

### Range slider

**Status:** `Slider` has been added to the API and playground palette.

- The range accepts configurable minimum, maximum, and step values; a step of
  zero represents a continuous range. Values are clamped to the range and
  snapped to the step when it is positive.
- An optional second value enables two handles with `lower < upper`. The range
  between them is highlighted; the handles stop before crossing.
- Mouse, drag, scroll, and keyboard input adjust the value; arrows change one
  step (or 1% of a continuous range), Page Up/Down change ten steps, and Home/End
  move to the limits. Space switches the active handle in range-selection mode.
- The inspector edits minimum, maximum, and step as `min, max, step`, along with
  `Value` and optional `Upper`. The range and both values are persisted in the
  playground JSON; Java callbacks are not saved.

### Grid and Spacer

**Status:** implemented as public toolkit layouts.

- `Grid(columns, gap)` uses a fixed number of columns, equal-width cells,
  and a uniform height per row. The last row can be incomplete.
- `Spacer.fixed(width, height)` creates space without drawing; `Spacer.fixed(size)`
  creates a square spacer.
- `Spacer.flex()` evenly divides remaining space along the main axis of a
  `Row` or `Column`; it measures as empty in other containers.

Example:

```java
Row actions = new Row(8);
actions.add(new Button(Component.literal("Back"), this::back));
actions.add(Spacer.flex());
actions.add(new Button(Component.literal("Save"), this::save));

Grid palette = new Grid(6, 6);
```

### Vanilla item integration

**Status:** `ItemRenderer`, `ItemCard`, and `ItemGrid` are implemented.

- `ItemRenderer` delegates `ItemStack` drawing, counts, and tooltips to vanilla
  `GuiGraphics` APIs.
- `ItemCard` is a focusable, selectable card that stores a copy of the stack,
  displays its item name and count, and supports activation by mouse, Enter,
  or Space.
- `ItemGrid(columns, gap)` provides `addItem(stack)` and `addItem(stack, label)`;
  its callback receives the index and a copy of the clicked item.
- `ItemCard.stack()` also returns a copy so consumers cannot accidentally modify
  the component's internal state.

Example:

```java
ItemGrid items = new ItemGrid(4, 6, (index, stack) -> selectItem(index, stack));
items.addItem(new ItemStack(Items.CHEST));
items.addItem(new ItemStack(Items.HOPPER), Component.literal("Input"));
```

The toolkit still does not import classes from the mod's domain. The API receives
stacks already created by the consumer and keeps Java callbacks only in memory.

`InventoryGrid` also integrates vanilla containers. The player convenience API
shows the 27 main slots and a hotbar separated by a subtle highlight; generic
`Container` grids remain in row-major order. `PICKER` copies stacks to the
`UiScreen` visual cursor and allows dropping them onto `ItemSlot` without
changing the source. `CUSTOM` emits interaction snapshots, leaving mutation
and synchronization to the consuming mod.

### Toast notifications

**Status:** `Toast` and `ToastHost` are implemented and attached to each `UiScreen`.

- `ToastHost` provides `show(Toast)` and level shortcuts. After the configured
  number of ticks, it starts exiting with the theme's duration and removes
  the notification when the fade ends. Background, text, and accent strip share
  the same opacity; the strip stays inside the rounded border.
- Up to four notifications are stacked in the upper-right corner; messages
  wrap to at most three lines, and layout recalculates after resizing/expiration.
- Left-clicking inside a notification dismisses it and consumes the event;
  clicks outside continue to reach the main tree.
- The playground has a Toast button that cycles through INFO, SUCCESS,
  WARNING, and ERROR.

### Table

**Status:** `Table<T>` and `TableColumn<T>` are implemented and available in the
playground palette.

- Columns define a title, fixed width or width measured from content, alignment,
  and a `Component` renderer; rows and selection belong to the consumer.
- A fixed header, uniform rows, hover, single selection, click callbacks, and
  vertical wheel scrolling are supported. Cells are clipped to respect bounds.
- The playground demonstrates three columns and sample rows; the inspector
  accepts comma-separated headers and a configurable number of columns.

### ProgressBar

**Status:** implemented and available in the playground palette.

- Determinate progress over any finite range; values are clamped to the range,
  and invalid/reversed ranges render empty.
- Bar height and track/fill colors are configurable; an optional formatter
  receives the current value and provides a centered `Component`.
- `setPercentage(true)` displays normalized progress as an integer percentage
  from `0%` to `100%`; the playground's default mode displays the rounded
  integer value, and the inspector switches between the two formats.
- Does not capture mouse or keyboard input and has no timer or animation of its own.
- In the playground, the inspector accepts `minimum, maximum, value`, and
  Preview displays the current formatted value.

### Phase 4 — Validate with a small showcase

**Status:** implemented as an editable playground, accessible with F7 in the
development environment. Layouts can be saved as JSON in
`config/r_ui_lib/ui-playground/`; usage details are in
[UI Toolkit playground](ui-toolkit-playground.md). Visual validation in client
and dedicated-server runs remains pending.

- Create a temporary development screen using only the public API: title, panels/cards, text, layout, and buttons.
- Validate resizing, GUI scales 1–4, a small window, and common resolutions; confirm that focus, hover, and clicks remain aligned after resizing.
- Confirm that the mod's main class does not reference client-only types and that the dedicated server initializes without loading the UI.
- Remove or restrict the showcase entry point before treating it as a player feature.

**Exit criterion:** the screen demonstrates primitives without manual positioning in the application, preserves behavior after resizing/GUI scale changes, and does not introduce library classes into the server initialization path.

## Roadmap after the first checkpoint

`ItemRenderer`, `ItemCard`, and `ItemGrid` are in the API; `Grid`, `Spacer`,
`ItemCard`, and `ItemGrid` are also available in the palette, JSON model, and
playground factory. Toast and ToastHost are integrated into `UiScreen`, with a
demonstration button in the playground. Table, ProgressBar, and RadialMenu are
available in the palette. The prioritized order and minimum technical plan for
the remaining items are in [Minecraft UI Toolkit PRD](minecraft-ui-toolkit-prd.md),
section 63. Radial menu was completed as the fourth milestone; Dropdown / Select
is next. Implement and review one milestone at a time, and start speculative
abstractions only when there is a concrete consumer.

## Outside this iteration

- Flexbox/CSS, a style parser, or declarative layouts;
- virtualization, general drag-and-drop, complex animations, or state management;
- full integration with `AbstractContainerScreen`;
- Fabric, Forge 1.20.1, or cross-loader abstraction;
- a runtime dependency on a third-party UI framework;
- publishing to a mod platform or a specific remote Maven repository; the project prepares release artifacts and accepts a Maven destination configured through the environment.

## Risks and decisions to revisit

- **Input and focus lifecycle:** reconcile the custom tree with the `Screen` lifecycle without duplicating events or losing compatibility with vanilla widgets.
- **Scaling and clipping:** validate the coordinate convention before implementing nested clipping or a scrollbar.
- **Public API boundary:** keep the exported set small and move internal renderers/helpers into `internal`.
- **Production usage:** mods using the API declare R-Ui Lib as a client-side dependency and distribute its installation separately.
- **Remote publishing:** the Maven coordinate is defined as `com.radaeli.ruilib:r-ui-lib-neoforge`; the publishing endpoint is configured by the repository consumer.
