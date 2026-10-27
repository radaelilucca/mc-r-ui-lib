# Using R-Ui Lib in another mod

R-Ui Lib is a Forge 1.20.1 or NeoForge 1.21.1 dependency installed as a separate mod. A mod that uses
its components compiles against the library JAR and declares the dependency
so the client loads both JARs. The library JAR is not embedded in the
consumer's JAR.

## Downloading and installing

Each release provides two files:

- `r_ui_lib-1.21.1-neoforge-<version>.jar`: library to install on the client.
- `r_ui_lib-1.21.1-neoforge-<version>-sources.jar`: source code for browsing in an IDE.

Install only the first file in the Minecraft instance's `mods` directory.
The sources JAR is not loaded by the game.

## Building a consuming mod

Copy the runtime JAR into a `libs/` directory in the consuming project and add
it to the Gradle classpath:

```groovy
dependencies {
    implementation files("libs/r-ui-lib-neoforge-0.5.0.jar")
}
```

The JAR contains `META-INF/neoforge.mods.toml`, so ModDevGradle also loads it
during development runs. Players install R-Ui Lib separately from the
consuming mod.

In the consuming mod's `META-INF/neoforge.mods.toml`, declare that the library
is required on the client:

```toml
[[dependencies.${mod_id}]]
modId="r_ui_lib"
type="required"
versionRange="[0.5.0,)"
ordering="AFTER"
side="CLIENT"
```

Public classes are in `com.radaeli.ruilib.ui`. Components that create or use
screens must be referenced only from the consuming mod's client-side code.

Choose the global appearance once in the mod's client-side initializer:

```java
UiThemes.setGlobalTheme(UiTheme.DARK); // or UiTheme.LIGHT
```

See [Themes and visual tokens](ui-theming.md) for the palette and token API,
as well as screen and component overrides.

## Dropdown selection

`Select<T>` chooses one value from a non-empty list. Each option can include a
tooltip explaining its value. `UiScreen` renders the open menu above the
component tree, including inside scroll views and modals, and keeps it within
the viewport by opening upwards when needed.

```java
Select<String> mode = new Select<>(List.of(
        new Select.Option<>("last", Component.literal("Last mode"),
                Tooltip.of(Component.literal("Resume the mode you last used."))),
        new Select.Option<>("free", Component.literal("Free flow"))
), "last", value -> saveMode(value));
```

Click to open and choose an option. Arrow keys, Home and End move the highlight;
Enter or Space confirms it. The mouse wheel moves through options. Escape or
an outside click dismisses the menu without changing the value; Tab closes it
and moves focus. `setValue(value)` updates selection without firing the callback.
The selected option's tooltip is also available when the menu is closed.
Use the select inside `UiScreen` to receive popup rendering and input routing.

## Slider with an optional range selection

For a percentage readout, use `slider.setValueFormatter(value -> Math.round(value) + "%")`.
This changes only the displayed text; callbacks still receive numeric values.

`Slider` constructors still create a single handle. `setValues(lower, upper)`
enables selection with two handles; `value()` returns the lower value, and
`upperValue()` returns the upper value, or `null` in single-handle mode.

```java
Slider slider = new Slider(0, 1000, 10, 1, lower -> { });
slider.setValues(10, 100);
slider.setOnRangeChange((lower, upper) -> {
    // Called when either endpoint changes; upper is null in single-handle mode.
    updateSelection(lower, upper);
});
slider.setUpperValue(150); // changes the upper value
slider.setUpperValue(null); // returns to a single handle
```

Values are clamped to the range and snapped to the step. `setValues` and
`setUpperValue` reject pairs with `upper <= lower` after snapping. During
dragging, scrolling, and keyboard input, handles stop before crossing.
Changing the range or step through the API adjusts both values and preserves
their order. The constructor callback receives lower-value changes;
`setOnRangeChange` receives changes to either endpoint.

Clicking or scrolling selects the nearest handle. While focused, Space switches
the active handle, and arrows, Home/End, and Page Up/Down adjust its value.

## Ghost item slot

`ItemSlot` renders an `ItemStack` reference for filters and similar uses;
it does not place items in an inventory. With JEI installed, `ItemSlot` components
inside a `UiScreen` automatically become drag targets. JEI integration is
optional, and the rest of the library continues to work without JEI.

The JEI overlay is also registered for all `UiScreen` subclasses.
By default, `jeiGuiBounds()` reserves the root component's bounds. If the root
fills the window solely to center a panel, override the method to return the
panel's bounds, leaving room on the side for the item list:

```java
@Override
public Bounds jeiGuiBounds() {
    return panel.bounds();
}
```

```java
ItemSlot itemSlot = new ItemSlot().setOnItemChanged(stack -> {
    if (stack.isEmpty()) {
        filter.clear();
    } else {
        filter.set(stack);
    }
});
```

The callback receives a defensive copy with its count normalized to one item;
`ItemStack.EMPTY` represents an empty slot. You can also assign or clear the
reference with `setItemStack(stack)` and `clear()`. Users can clear a focused
slot with right-click, Delete, or Backspace.

The background follows the theme by default, but it can use a local named
color or any ARGB value. `UiColor.named(...)` recognizes basic names such as
`white`, `black`, `red`, `green`, `blue`, `yellow`, `cyan`, `magenta`,
`orange`, `purple`, `pink`, `brown`, and `gray`; `UiColor.hex(...)`
accepts `#RRGGBB` and `#AARRGGBB`.

```java
itemSlot.setBackgroundColor(UiColor.named("black"));
itemSlot.setBackgroundColor(UiColor.hex("#804080FF"));
itemSlot.setBackgroundColor(0xFF204060); // arbitrary ARGB
itemSlot.clearBackgroundColor(); // returns to the active theme
```

## Inventory grid

`InventoryGrid.playerInventory()` displays the local player's main inventory
and hotbar. The hotbar has a subtle visual separation. To display another
`Container`, use `new InventoryGrid(container)`; by default, generic containers
are displayed in rows without assuming where a hotbar begins.

```java
InventoryGrid inventory = InventoryGrid.playerInventory()
        .setColumns(9)
        .setSlotSize(18)
        .setGap(2)
        .setOnItemPicked(stack -> selectedFilter.set(stack));
content.add(inventory);
content.add(new ItemSlot().setOnItemChanged(selectedFilter::set));
```

In `PICKER` mode (the default), left-click picks up a copy of the stack, and
right-click picks up one item. Click the source slot again to cancel, or drop
the item onto an `ItemSlot` in the same `UiScreen`. The grid does not remove or
modify items; `ItemSlot` stores a reference with its count normalized to one.

Use `setInteractionMode(InventoryGrid.InteractionMode.CUSTOM)` to receive press,
drag, and release events through `setOnInteraction(...)`. Each event includes
the action, index, button, and a defensive copy of the stack; the consumer
decides how to modify the container and synchronize those operations with the
server. The grid does not write directly to containers in custom mode.

## Local Maven or a Maven repository

To consume the library through local Maven on this machine, publish the project
with:

```powershell
.\gradlew.bat publishToMavenLocal
```

In the consuming project, add `mavenLocal()` to the repositories and declare:

```groovy
dependencies {
    implementation "com.radaeli.ruilib:r-ui-lib-neoforge:0.3.0"
}
```

You can also publish to any Maven repository that accepts uploads.
Set `MAVEN_RELEASE_URL` and, if needed, `MAVEN_USERNAME` and `MAVEN_PASSWORD`
in the environment, then run `.\gradlew.bat publish`. The same values can be
passed through the Gradle properties `maven_release_url`, `maven_username`,
and `maven_password`. No remote endpoint or credentials are stored in the
repository.

## Building artifacts

`.\gradlew.bat buildAll` creates runtime and sources JARs in `forge/build/libs/`
and `neoforge/build/libs/`. See [the multi-version guide](forge-neoforge.md)
for Forge dependencies, separate runs, and parity validation.
The release workflow attaches both to the GitHub release when a version tag
is created. To distribute through a public Maven repository, configure the
endpoint described above.
