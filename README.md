# R-Ui Lib

> **ALPHA — Under construction.** R-Ui Lib is in active development. Expect
> unfinished features and API changes between releases.

> Build Minecraft mod interfaces from reusable, themed components.

**R-Ui Lib** is a UI toolkit for Minecraft mod developers. Compose settings
screens, inventory filters, dashboards, and notifications with shared layouts,
controls, and themes. Players interact with these interfaces through the mods
that use the library.

## What you can build

- **Interactive controls:** Buttons, icon buttons, toggles, chips, text and
  search fields, and sliders with an optional second handle for range selection.
- **Structured screens:** Rows, columns, grids, stacks, tabs, scrollable content,
  and freely positioned layouts, with panels, cards, labels, and dividers.
- **Feedback and navigation:** Toast notifications, tooltips, modal dialogs,
  progress bars, tables, and radial menus.
- **Item interfaces:** Item cards and grids, a player inventory picker with a
  distinct hotbar, and ghost item slots for filters. Players can copy a selection
  from their inventory without moving the original items, or drag items from
  **JEI** into supported filter slots.
- **A shared appearance:** Built-in Dark and Light themes, configurable colors,
  spacing, text sizes, corner radii, and transition settings. Set a global theme
  once, then customize individual screens and supported component properties.

## For developers

The public API lives in `com.radaeli.ruilib.ui`. Compose Java components and
attach callbacks to your mod's logic; `UiScreen` provides mouse and keyboard
input, focus navigation, tooltips, and overlays. Use the UI API from client code.

For example, compose an inventory picker and a filter slot:

```java
Column content = new Column();
content.add(new Label(Component.literal("Choose an item")));
content.add(new ItemSlot());
content.add(InventoryGrid.playerInventory());

UiScreen screen = new UiScreen(
        Component.literal("My mod"), new Panel(content));
```

Choose a theme during client initialization:

```java
UiThemes.setGlobalTheme(UiTheme.DARK); // or UiTheme.LIGHT
```

Custom palettes and design tokens let you match your mod's appearance. Source
JARs are available for browsing the implementation in your IDE.

See the [integration guide](https://github.com/radaelilucca/mc-r-ui-lib/blob/main/docs/using-r-ui-lib.md)
for dependencies and callbacks, and the
[theme guide](https://github.com/radaelilucca/mc-r-ui-lib/blob/main/docs/ui-theming.md)
for customization.

## Try layouts in game

In development runs, press **F7** to open the visual playground. Add components
from the categorized palette, arrange and resize them, edit their properties,
switch between editing and interactive preview, and compare Dark and Light
themes. Save layouts as local JSON files and reopen them later.
Use **Load** to open the bundled reference layout for each palette category or
one of your saved layouts; the examples are available on a fresh run too.

**F6** demonstrates toasts over the game, and **F8** opens the inventory and
filter-slot example. These development shortcuts are configurable in Controls.
See the [playground guide](https://github.com/radaelilucca/mc-r-ui-lib/blob/main/docs/ui-toolkit-playground.md)
for the full workflow.

## Compatibility

| Minecraft | Loader | Java |
| --- | --- | --- |
| 1.21.1 | NeoForge 21.1.236+ | 21 |
| 1.20.1 | Forge 47.4.20+ | 17 |

## Links and license

- [Source code](https://github.com/radaelilucca/mc-r-ui-lib)
- [Report an issue](https://github.com/radaelilucca/mc-r-ui-lib/issues)
- [Documentation](https://github.com/radaelilucca/mc-r-ui-lib/blob/main/docs/README.md)

R-Ui Lib is available under the
[MIT License](https://github.com/radaelilucca/mc-r-ui-lib/blob/main/LICENSE).
