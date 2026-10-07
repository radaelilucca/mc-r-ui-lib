# PRD — Minecraft UI Toolkit
## Client-side UI library for Minecraft mods

- **Status:** Evolving
- **Initial target:** Minecraft 1.21.1 + NeoForge
- **Initial distribution:** independent R-Ui Lib library mod
- **Next objective:** stabilize the API and provide runtime and sources artifacts for consuming projects

**Architectural reference:** the approach used by Logistics Network — custom primitives and a design system built on vanilla Minecraft `Screen`, `GuiGraphics`, and widget APIs.

---

# 1. Vision

Build a reusable UI library for Minecraft mods that supports visually modern, consistent interfaces independent of the traditional vanilla inventory/furnace style.

The library should abstract the repetitive, cumbersome parts of Minecraft's UI API without trying to recreate a complete web framework.

The goal is not to compete with React, CSS, or complete declarative frameworks.

The goal is to create a small, predictable, idiomatic layer over:

```text
Minecraft Screen API
        ↓
GuiGraphics
        ↓
Input events
        ↓
Widgets / Item rendering
```

providing:

```text
Design tokens
Theme
Layout primitives
Reusable components
Input/focus handling
Scroll/clipping
Tooltips
Common rendering helpers
```

# 2. Problem

The vanilla API can build almost any interface, but it requires substantial repetitive code.

Common problems:

- manual element positioning;
- manual padding/gap calculations;
- duplicated colors and styles;
- repeated hover/focus implementations;
- manually implemented scrolling;
- cumbersome clipping/scissoring;
- repeated card, panel, button, and tab logic;
- limited consistency across screens;
- tight coupling between layout, rendering, and input;
- vanilla widgets have aesthetics and behavior that do not always fit custom interfaces.

A mod with several screens naturally tends to create its own small design system.

Logistics Network is an example of this approach, with structures equivalent to:

```text
Theme
ThemePaint
custom EditBox
screen abstractions
reusable paint helpers
```

This library formalizes that approach from the start.

# 3. Goals

## 3.1. Main goals

The library should support building UIs such as:

```text
dashboard
settings screen
palette browser
graph editor
resource picker
searchable lists
cards
tabs
modal dialogs
scrollable grids
tooltips
radial overlays
```

without visually depending on the vanilla style.

## 3.2. Ergonomics

Usage should conceptually resemble:

```java
Panel panel = ui.panel()
    .padding(12)
    .gap(8);

panel.add(
    ui.text("Palette")
);

panel.add(
    ui.blockGrid(blocks)
);
```

instead of scattering:

```java
graphics.fill(...);
graphics.drawString(...);
graphics.renderItem(...);

if (mouseX >= ...) {
    ...
}
```

throughout every screen.

The library does not need to be declarative in the MVP, but it should support component composition.

## 3.3. Visual independence

No component should require:

- vanilla button textures;
- furnace/chest sprites;
- vanilla slots;
- a fixed background PNG.

Appearance should primarily come from:

- shapes;
- colors;
- borders;
- text;
- gradients;
- optional sprites;
- item rendering.

## 3.4. Decoupling

The library will initially be created inside another project, but it should be developed as if it were already an external dependency.

Rule:

> No library class may import domain classes from the host mod.

Allowed:

```text
net.minecraft.*
net.neoforged.*
org.joml.*
ui toolkit internal packages
```

Not allowed:

```text
com.my_mod.features.*
com.my_mod.blocks.*
com.my_mod.network.*
com.my_mod.domain.*
```

# 4. Non-goals

The MVP should not attempt to implement:

- HTML/CSS;
- complete Flexbox;
- complete CSS Grid;
- a stylesheet parser;
- declarative XML/JSON layouts;
- UI hot reload;
- a WYSIWYG editor;
- a reactive state framework;
- complex animations;
- a full accessibility framework;
- cross-loader support;
- Fabric support;
- Forge 1.20.1;
- server-side UI abstraction;
- networking abstraction;
- a complete inventory menu abstraction.

These areas can be evaluated later.

# 5. Philosophy

## 5.1. Vanilla first

Reuse Minecraft behavior whenever it already solves a problem well.

Examples:

```text
Font
ItemStack rendering
tooltips
text input behavior
Screen lifecycle
keyboard/mouse events
narration where applicable
```

Do not recreate item rendering.

Do not recreate basic text layout.

## 5.2. Own the visuals

For custom appearance:

```text
panel
card
button
chip
tabs
toggle
search field chrome
modal
scrollbar
```

prefer custom rendering.

## 5.3. Composition over inheritance

Avoid a deep class hierarchy such as:

```text
BaseWidget
  ↓
StyledWidget
  ↓
ClickableStyledWidget
  ↓
ContainerStyledWidget
  ↓
...
```

Prefer:

```text
Component
Behavior
Layout
Renderer
```

composition.

## 5.4. Stateful only where necessary

Components should keep only the state they need.

Examples:

```text
hovered
focused
pressed
scrollOffset
selected
expanded
```

Domain state belongs to the consumer.

# 6. Module organization

Suggested initial structure:

```text
src/main/java/.../ui/
│
├── core/
│   ├── UiContext
│   ├── UiComponent
│   ├── UiContainer
│   ├── UiScreen
│   └── Bounds
│
├── theme/
│   ├── UiTheme
│   ├── UiTokens
│   ├── UiPalette
│   └── UiThemeRegistry
│
├── paint/
│   ├── UiPaint
│   ├── ShapeRenderer
│   ├── TextRenderer
│   └── ItemRenderer
│
├── layout/
│   ├── Row
│   ├── Column
│   ├── Stack
│   ├── Spacer
│   ├── Insets
│   └── Alignment
│
├── input/
│   ├── UiInputRouter
│   ├── FocusManager
│   ├── HoverState
│   └── DragState
│
├── clipping/
│   └── ClipStack
│
├── components/
│   ├── Text
│   ├── Panel
│   ├── Card
│   ├── Button
│   ├── IconButton
│   ├── Toggle
│   ├── Tabs
│   ├── Chip
│   ├── Divider
│   ├── Tooltip
│   ├── TextField
│   ├── SearchField
│   ├── ScrollView
│   ├── Grid
│   ├── ItemCard
│   ├── ItemGrid
│   └── Modal
│
├── animation/
│   ├── Tween
│   └── Easing
│
└── debug/
    └── UiDebugOverlay
```

The structure can evolve, but the module should remain self-contained.

# 7. Core layer

## 7.1. `UiComponent`

Conceptual base interface:

```java
interface UiComponent {
    void measure(UiMeasureContext context);
    void layout(Bounds bounds);
    void render(UiContext context);
}
```

Responsibilities:

- receive bounds;
- render;
- participate in layout;
- optionally receive input.

## 7.2. `Bounds`

Value object:

```java
record Bounds(
    int x,
    int y,
    int width,
    int height
) {}
```

Helpers:

```text
contains(x, y)
inset(...)
centerX()
centerY()
right()
bottom()
```

## 7.3. `UiContext`

Should encapsulate frequently accessed values:

```text
GuiGraphics
Font
Minecraft
mouseX
mouseY
partialTick
theme
screen size
```

Conceptual example:

```java
record UiContext(
    GuiGraphics graphics,
    Font font,
    UiTheme theme,
    int mouseX,
    int mouseY,
    float partialTick
) {}
```

# 8. Theme system

Inspired directly by the Logistics Network pattern.

## 8.1. `UiTokens`

Structural tokens:

```text
spacingXs
spacingSm
spacingMd
spacingLg

radiusSm
radiusMd
radiusLg

borderThin
borderStrong

controlHeightSm
controlHeightMd
controlHeightLg
```

## 8.2. `UiPalette`

Color tokens:

```text
background

surface
surfaceElevated
surfaceSunken

border
borderStrong

text
textMuted
textSubtle

accent
accentSoft

success
successSoft

warning
warningSoft

danger
dangerSoft

info
infoSoft

shadow
overlay
```

## 8.3. `UiTheme`

```java
record UiTheme(
    String id,
    UiPalette palette,
    UiTokens tokens
) {}
```

The toolkit should support multiple themes.

Initial example:

```text
Dark
Light
```

Do not prioritize a theme editor in the MVP.

# 9. Paint primitives

`UiPaint` will be equivalent to the `ThemePaint` observed in Logistics Network.

It should centralize repetitive rendering.

First set:

```text
rect
outline
roundedRect
roundedOutline

panel
card
sunkenPanel

divider

shadow
overlay

badge
pill
chip

focusRing
hoverOverlay
```

# 10. Layout engine

Do not implement complete Flexbox.

Implement a small subset that handles most screens.

## 10.1. Column

```text
Column
├── component
├── component
├── component
└── component
```

Configuration:

```text
gap
padding
horizontal alignment
```

## 10.2. Row

```text
Row
├── component
├── component
└── component
```

Configuration:

```text
gap
vertical alignment
```

## 10.3. Stack

Overlapping elements:

```text
Stack
├── background
├── content
└── overlay
```

Useful for:

- badges;
- hover;
- selection;
- modals.

## 10.4. Grid

Needed early for mods with:

- items;
- blocks;
- resource browsers;
- palettes.

Conceptual API:

```java
Grid grid = Grid.builder()
    .columns(6)
    .gap(6)
    .build();
```

Initially:

```text
fixed column count
fixed/known item size
```

Auto-fit can be supported later.

## 10.5. Spacer

```java
Spacer.fixed(8);
Spacer.flex();
```

`flex()` can be postponed if it complicates the first layout engine.

# 11. Sizing

MVP:

```text
FIXED
CONTENT
FILL
```

Example:

```java
component.width(FILL);
component.height(CONTENT);
```

Do not initially implement:

```text
min-content
max-content
percentage
calc()
complex constraints
```

# 12. Priority primitives

## 12.1. Text

Support:

```text
Component
String
color
alignment
wrap
max width
```

## 12.2. Panel

Neutral visual container.

Props:

```text
background
border
padding
radius
```

## 12.3. Card

Panel with visual grouping semantics.

Props:

```text
optional title
optional hover
optional selected state
```

## 12.4. Button

Custom button without a visual dependency on the vanilla button.

States:

```text
default
hover
pressed
focused
disabled
```

## 12.5. IconButton

Same behavior as Button, with compact visual content.

Useful for:

```text
close
back
search
settings
delete
favorite
```

## 12.6. Toggle

Boolean primitive.

```text
label
state
onChange
```

## 12.7. Tabs

```text
TabBar
├── tab
├── tab
└── tab
```

States:

```text
active
hover
disabled
```

## 12.8. Chip / Pill

Useful for:

```text
mod names
filters
statuses
tags
categories
```

## 12.9. Divider

Initially horizontal.

# 13. Inputs

Inputs are an area where vanilla behavior is worth reusing.

## 13.1. TextField

Can wrap `EditBox`.

The library controls:

```text
layout
theme
border
focus chrome
placeholder
```

and lets Minecraft control:

```text
cursor
selection
keyboard
clipboard
IME behavior where possible
```

## 13.2. SearchField

Specialized component:

```text
search icon
placeholder
clear action
onChange
```

# 14. Scrolling

`ScrollView` is a required MVP component.

Responsibilities:

- viewport;
- clipping;
- scroll offset;
- mouse wheel;
- optional scrollbar;
- scrollbar dragging;
- clamping;
- content larger than the viewport.

Conceptual API:

```java
ScrollView view = new ScrollView(content)
    .showScrollbar(true);
```

# 15. Clipping

Create `ClipStack`.

Goal:

```text
push(bounds)
render children
pop()
```

Use the graphics API's scissoring internally.

This should prevent clipping code from being scattered across screens.

# 16. Item rendering

Very important for mods.

Create a reusable helper:

```java
ItemRenderer.render(
    context,
    stack,
    bounds
);
```

Use Minecraft's standard renderer internally.

Support:

```text
ItemStack
tooltip
optional count overlay
optional durability overlay
scale
```

Do not implement a custom 3D renderer.

# 17. ItemCard

An important component for several future mods.

Structure:

```text
┌───────────────┐
│   ItemStack   │
│               │
│ Item name     │
│ Mod name      │
└───────────────┘
```

Props:

```text
stack
title
subtitle
selected
disabled
onClick
```

# 18. ItemGrid

Composition of:

```text
Grid
+
ItemCard
+
ScrollView
```

Use cases:

- block palette;
- resource browser;
- recipe browser;
- selection dialog;
- filters.

This component should help validate the toolkit in the host mod.

# 19. Tooltip

Create a thin abstraction over vanilla tooltips.

API:

```java
component.tooltip(Component...)
```

The library should continue to use native tooltip rendering whenever possible.

# 20. Modal

The MVP should support a basic modal.

```text
Screen
 ↓
Overlay
 ↓
Centered panel
```

Features:

```text
backdrop
Esc to close
optional outside click close
simple focus containment
```

# 21. Input routing

Create a clear input layer.

Events:

```text
mouseMove
mouseDown
mouseUp
mouseScroll
keyDown
keyUp
charTyped
```

Rule:

> The visual component should declare its bounds and behavior. The Screen should not need to repeat hit testing for each child.

# 22. Event propagation

Simple model:

```text
topmost component first
        ↓
handled?
 ├─ yes → stop
 └─ no  → parent / next
```

Do not implement full DOM-style capture/bubble in the MVP.

# 23. Focus management

`FocusManager` should manage:

```text
focused component
focus()
blur()
next()
previous()
```

MVP:

```text
mouse focus
Tab
Shift+Tab
Esc
```

Do not attempt to solve complete accessibility initially.

# 24. Hover

Hover should be derived automatically from bounds.

Do not require:

```java
boolean hovered =
    mouseX >= x &&
    mouseX <= x + width ...
```

in every component.

# 25. Drag behavior

Create a minimal reusable abstraction.

Use cases:

```text
scrollbar
future slider
future graph editor
future movable panels
```

Do not create a complex drag/drop system in the MVP.

# 26. Screen integration

Create a base class:

```java
abstract class UiScreen extends Screen
```

Responsible for:

- creating the root component;
- recalculating layout in `init`;
- creating `UiContext`;
- rendering;
- input routing;
- focus;
- resizing;
- lifecycle.

Usage:

```java
class PaletteScreen extends UiScreen {

    @Override
    protected UiComponent buildUi() {
        return Column.create(...)
            .add(...)
            .add(...);
    }
}
```

# 27. Container screen integration

Inventories need:

```text
AbstractContainerScreen
```

Do not build a complete abstraction in the first milestone.

Create a bridge later:

```java
UiContainerScreen<T extends AbstractContainerMenu>
```

It should allow combining:

```text
Minecraft slots
+
custom UI toolkit
```

without reimplementing inventory synchronization.

# 28. State

The library should not impose state management.

Example:

```java
class PaletteScreen {
    Block selectedBlock;
    String query;
    List<Block> results;
}
```

The toolkit only renders and triggers callbacks.

Possible future additions:

```text
ObservableValue
State<T>
```

but they are not required for the MVP.

# 29. Rebuild vs. mutation

The MVP can accept a simple approach:

```text
state changes
   ↓
rebuild relevant component tree
```

or direct mutation.

Do not optimize prematurely.

Minecraft UIs usually have at most a few hundred components.

# 30. Animations

Animations should be optional and subtle.

MVP:

```text
Tween
Easing
```

Use cases:

```text
hover fade
modal fade
tab indicator
panel slide
```

Do not create a complex timeline engine.

# 31. Accessibility / narration

Not a priority for the first spike, but the architecture should not prevent narration integration.

Interactive components should have:

```text
label
conceptual role
state
```

Reuse vanilla mechanisms when possible.

# 32. Debug tooling

Create `UiDebugOverlay`.

Development toggle to visualize:

```text
component bounds
component id
hover target
focus target
layout boxes
clipping areas
```

Example:

```text
┌──────────────┐
│ Card         │
│ ┌──────────┐ │
│ │ ItemCard │ │
│ └──────────┘ │
└──────────────┘
```

This should significantly speed up development.


# 33. Component IDs

Every component can optionally have a:

```text
debugId
```

Example:

```java
new Card()
    .debugId("palette-reference-card");
```

Do not use IDs for styling in the MVP.

# 34. Public API boundary

Everything consumers should use belongs in:

```text
ui.api
```

or packages explicitly documented as public.

Internals:

```text
ui.internal
```

This will make future library extraction easier without breaking consumers.

# 35. Dependencies

The library should initially avoid external dependencies.

Allowed:

```text
Minecraft
NeoForge
JOML
```

Avoid:

```text
YACL
owo-ui
Cloth Config
third-party UI framework
```

The goal is to control the stack and learn the internals.

# 36. No dependency on the host mod

Desired build rule:

```text
ui toolkit
   ↓
does not know about
   ↓
host mod
```

But:

```text
host mod
   ↓
depends on
   ↓
ui toolkit
```

Ideally, even within the same repository:

```text
modules/
├── ui-toolkit/
└── mod/
```

If the current Gradle structure cannot immediately support multiple modules, use an isolated package:

```text
src/main/java/<org>/uitoolkit/
```

and prohibit imports from the host.

# 37. Resource namespace

To simplify future migration, custom UI assets should use a separate namespace when possible.

Example:

```text
assets/minecraft_ui_toolkit/
```

or a planned neutral namespace.

Avoid putting fundamental library assets in the host mod's semantic namespace.

# 38. Configuration

The library should not have a global configuration screen in the MVP.

Themes and behavior should be configurable through code:

```java
UiToolkit.init(
    UiTheme.DARK
);
```

# 39. First consumer

The first consumer should be a real UI in the host mod.

Recommendation:

**Dynamic Block Palette UI**

Because it requires:

```text
custom screen
cards
item rendering
grid
scroll
tabs/filters
tooltips
search
selected state
```

This tests many primitives without requiring an inventory container.

# 40. Library MVP

The first version needs to include:

## Core

- `UiScreen`
- `UiComponent`
- `UiContainer`
- `UiContext`
- `Bounds`

## Theme

- `UiTheme`
- `UiPalette`
- `UiTokens`

## Layout

- `Row`
- `Column`
- `Grid`
- `Stack`
- `Spacer`

## Paint

- rectangle
- rounded rectangle
- outline
- panel
- card
- divider
- focus ring
- overlay

## Components

- `Text`
- `Panel`
- `Card`
- `Button`
- `Toggle`
- `Tabs`
- `Chip`
- `TextField`
- `SearchField`
- `ScrollView`
- `ItemCard`
- `ItemGrid`
- `Modal`

## Input

- hover
- click
- scroll
- keyboard focus
- basic drag

## Debug

- bounds overlay
- focus/hover inspection

# 41. Component API style

Prefer fluent composition.

Example:

```java
return Column.create()
    .padding(theme.tokens().spacingLg())
    .gap(theme.tokens().spacingMd())
    .add(
        Text.title("Block Palette")
    )
    .add(
        Tabs.of(
            "Similar",
            "Shades",
            "Complementary"
        )
    )
    .add(
        ScrollView.of(
            ItemGrid.of(blocks)
                .columns(6)
                .gap(6)
        )
    );
```

This is only a direction for API ergonomics.

It does not need to be the final API.

# 42. Rendering lifecycle

Conceptual flow:

```text
Screen.init()
   ↓
build component tree
   ↓
measure
   ↓
layout
   ↓
render
```

On every resize:

```text
measure
layout
```

On every frame:

```text
derive hover
render
```

Input:

```text
event
  ↓
InputRouter
  ↓
target component
```

# 43. Layout lifecycle

## Measure

The component reports its desired size.

## Layout

The parent defines the final bounds.

## Render

The component draws within the bounds.

This separation prevents each component from calculating layout during rendering.

# 44. Performance

Targets:

- no excessive per-frame allocations;
- avoid rebuilding large lists during rendering;
- recalculate layouts only when necessary;
- item rendering reuses the vanilla pipeline;
- controlled nested clipping;
- no reflection in the render loop.

The library should work well on screens with hundreds of item cards.

# 45. Testing

## Unit tests

Parts that can be tested without the Minecraft runtime:

```text
Bounds
Insets
layout math
Row/Column sizing
Grid calculations
scroll clamp
focus ordering
```

## In-game tests

Required for:

```text
rendering
input
tooltips
item rendering
scissor
GUI scale
fullscreen/window resize
```

# 46. GUI scale

Testing is required at:

```text
GUI Scale 1
GUI Scale 2
GUI Scale 3
GUI Scale Auto
```

Layout must never depend on physical screen pixels.

# 47. Resolutions

Validate at least:

```text
1280×720
1920×1080
2560×1440
ultrawide
small window
```

# 48. Acceptance criteria — MVP

The first library version will be considered valid when it can implement a real screen that:

- uses `UiScreen`;
- does not manually position every element in the screen;
- uses a central theme;
- has Row/Column/Grid;
- has ScrollView;
- has SearchField;
- renders ItemStacks;
- has selectable cards;
- has a tooltip;
- works after resizing;
- works with different GUI scales;
- supports mouse and basic keyboard input;
- does not import any domain classes from the host mod.

# 49. Milestones

## Milestone 0 — Rendering primitives

Create:

```text
UiTheme
UiTokens
Bounds
UiContext
UiPaint
```

Demonstrate:

```text
panel
card
text
button visual
```

## Milestone 1 — Component tree

Create:

```text
UiComponent
UiContainer
UiScreen
```

Demonstrate:

```text
nested components
render traversal
```

## Milestone 2 — Layout

Create:

```text
Row
Column
Stack
Grid
Spacer
Insets
```

Demonstrate a responsive screen without hardcoded coordinates in the application.

## Milestone 3 — Input

Create:

```text
hover
click
focus
Tab navigation
scroll routing
```

## Milestone 4 — Core components

Create:

```text
Button
Toggle
Tabs
Chip
TextField
SearchField
```

## Milestone 5 — Scroll + clipping

Create:

```text
ClipStack
ScrollView
Scrollbar
```

## Milestone 6 — Mod-oriented components

Create:

```text
ItemRenderer
ItemCard
ItemGrid
Tooltip
Modal
```

## Milestone 7 — Real consumer

Implement a real screen in the host mod using only the toolkit's public API.

Recommended:

```text
Dynamic Block Palette
```

# 50. Future migration to a standalone project

Extraction should require only:

```text
move module
change Maven coordinates
change package root if desired
publish artifact
```

It should not require:

```text
remove domain dependencies
rewrite components
migrate coupled assets
change business callbacks
```

# 51. Future publishing strategy

Once the library proves its value:

```text
group:
<org>.minecraft

artifact:
ui-toolkit-neoforge

version:
semver
```

Possible distribution:

```text
Maven
GitHub Packages
CurseForge/Modrinth only if it becomes a runtime dependency
```

Initial preference:

> embedded/Jar-in-Jar library dependency or compile dependency, depending on the distribution strategy.

Do not decide yet.

# 52. Criteria for extraction from the host project

Extract when:

- at least two internal mods use the library;
- the public API is reasonably stable;
- there are no domain imports from the first mod;
- the theme/component system has been proven in production;
- migration can happen without structural refactoring.

# 53. Future backlog (status and order updated in section 63)

After the MVP:

```text
dropdown/select
context menu
tree view
virtualized lists
virtualized grids
drag-and-drop
graph/canvas components
radial menu
toast
notification center
split panes
resizable panels
table
progress bar
color picker
keybind input
rich text
markdown subset
animation primitives
responsive breakpoints
declarative builders
data binding
accessibility improvements
UiContainerScreen
```

# 54. Radial menu

Plan radial menus as a future extension because other planned mods already need them.

Do not include them in the MVP.

Future architecture:

```text
RadialMenu
RadialSegment
RadialGeometry
RadialInput
```

Can reuse:

```text
theme
paint
input
text
animation
```

# 55. Config screens

The library itself should be able to build polished configuration pages without depending on YACL.

This does not mean recreating YACL immediately.

The MVP primitives should already make it possible to build:

```text
tabs
toggles
text inputs
buttons
cards
sections
```

Config-specific abstractions can come later:

```text
ConfigSection
BooleanSetting
EnumSetting
NumberSetting
```

# 56. Differences from existing frameworks

The toolkit does not aim to beat owo-ui/YACL in feature count.

Its distinguishing features are:

- complete control over appearance;
- no external dependencies;
- a small API;
- optimized for our mods;
- learning Minecraft internals;
- easy embedding;
- a consistent design system across projects.

# 57. Naming

Temporary internal name:

```text
Minecraft UI Toolkit
```

or:

```text
MUI Toolkit
```

Avoid a final name before deciding whether it will be published.

The package should remain neutral.

# 58. Main technical risk

The biggest risk is not rendering.

Basic rendering is simple.

The real risk is allowing the library to grow into an overly large layout/input framework.

Rule:

> Implement only abstractions that already have at least one concrete consumer.

Avoid building speculative primitives.

# 59. Main architectural risk

Accidental coupling to the first mod.

Review periodically:

```text
Does this class know what a Block Palette is?
Does this component import host domain?
Does this helper assume one screen?
Does this theme contain product-specific names?
```

If so, move the responsibility to the consumer.

# 60. Definition of Done for each component

Every new component should have:

- a small public API;
- theme support;
- hover/focus where applicable;
- layout that remains correct after resizing;
- no domain references;
- a usage example;
- validation at different GUI scales;
- predictable mouse behavior.

# 61. Expected result

At the end of the MVP, building a custom screen should look like:

```text
define state
   ↓
compose components
   ↓
define callbacks
   ↓
let the toolkit handle
layout/render/input/theme
```

rather than:

```text
calculate every x/y
draw every shape
perform manual hit testing
implement scrolling
implement hover
implement focus
```

# 62. Architectural direction

The library should sit between two extremes.

We do not want:

```text
raw GuiGraphics everywhere
```

or:

```text
full web framework inside Minecraft
```

We want:

```text
Minecraft API
      ↓
small reusable UI runtime
      ↓
design system primitives
      ↓
mod screens
```

This should be the main design constraint during development.

# 63. Current status and prioritized technical roadmap

This inventory was compared with `docs/ui-toolkit-foundation-plan.md` and the
current API. This section updates the historical list in section 53. Milestones
should be implemented and reviewed one at a time; speculative items only move
forward when there is a concrete consumer.

## 63.1. API and playground status

Implemented in the API: `Label` (text), `Button`, `Toggle`, `Tabs`, `Chip`,
`TextField`, `SearchField`, `Slider`, `ScrollView`, `ClipStack`, `Row`,
`Column`, `Stack`, `Grid`, `Spacer`, `ItemRenderer`, `ItemCard`, `ItemGrid`,
`Toast`, `ToastHost`, and `Table`.

`Panel`, `Card`, `IconButton`, `Divider`, `Tooltip`, and `Modal` now have public
APIs. Panel/Card compose content with padding and themed frames; the consumer
chooses the inner layout using Row/Column/Grid. Tooltip uses the vanilla renderer,
and Modal provides a centered overlay, backdrop, focus containment, Escape,
and optional closing through an outside click.

In the playground, Panel, Card, Divider, and IconButton are persistable nodes;
the Dialog button opens a temporary Modal to test overlays and focus without
saving callbacks or dialog state in JSON.

The playground exposes `Grid`, `Spacer`, `ItemCard`, and `ItemGrid` through the
palette. `ItemCard` stores the item ID and count in the document and can be
nested in `Grid`/`ItemGrid`. `Spacer` can be fixed or flexible, and the palette
scrolls to show all types in short windows. `ItemRenderer` is a helper, not a
layout node; its rendering path is exercised by `ItemCard`.

## 63.2. Order of upcoming milestones

| Order | Component | First-iteration result |
|---:|---|---|
| 1 | Toast | Implemented; transient overlay notifications |
| 2 | Table | Implemented; data in columns and rows, with selection and scrolling |
| 3 | ProgressBar | Implemented; determinate progress, optional indeterminate mode |
| 4 | Radial menu | Implemented; geometry, segments, hit testing, and gesture selection |
| 5 | Dropdown / Select | Single selection, overlay, and keyboard navigation |
| 6 | Context menu | Anchored actions, keyboard support, and predictable closing |
| 7 | Remaining items | Implement in small batches according to sections 63.4–63.6 |

## 63.3. Technical plan for the first six

**Status:** the Toast (`Toast`/`ToastHost`), Table, and ProgressBar milestones
are implemented and can be demonstrated through the API and playground;
RadialMenu is also implemented, and Dropdown / Select is the next phase.

### Toast

- Use a `ToastHost` per screen and dispatch through `toastHost.show(Toast)`
  (with `info`, `success`, `warning`, and `error` shortcuts); avoid a global
  singleton or dedicated threads.
- Stack a limited number, measure vanilla text, expire using the UI clock,
  pause when the screen is inactive, and dismiss by clicking.
- Start with `INFO`, `SUCCESS`, `WARNING`, and `ERROR`, without persistence
  across screens or custom actions. Validate that toasts do not capture clicks
  outside their bounds and reposition after resizing.

### Table

- `Table<T>` accepts columns with a title, fixed or preferred width, alignment,
  and a renderer; each consumer supplies its own rows.
- Headers, uniform rows, hover, single selection, click callbacks, and vertical
  scrolling are implemented. Sorting, editing, column resizing, and virtualization
  remain outside the first milestone.

### ProgressBar

- **Status:** implemented as a noninteractive determinate component, with
  clamped range/value, configurable height and colors, and an optional formatter.
  Invalid or reversed ranges render empty; the playground exposes
  `minimum, maximum, value` in the inspector.
- Expose `minimum`, `maximum`, `value`, height/style, and an optional formatter;
  define clamping behavior for invalid ranges as well.
- Paint the track/fill with `UiPaint`; the determinate variant is required.
- Indeterminate mode and animation are optional and use the UI lifecycle's
  clock, without timers/threads. It is not interactive and does not assume
  a percentage range of 0–100.

### Radial menu

- **Status:** `RadialGeometry`, `RadialSegment`, and `RadialMenu` are implemented
  and available in the playground. The first segment starts at north and the
  rest follow clockwise; mouse down + drag + release chooses a sector, and
  the central dead zone or releasing outside the radius cancels the action.
  The consumer opens and closes the menu; callbacks remain in memory.
- `RadialGeometry`: pure functions for center, radii, angles, sectors, and hit
  testing; mathematically cover boundaries and angle wrapping.
- `RadialSegment`: label, optional vanilla icon, enabled state, and in-memory
  callback. `RadialMenu` draws sectors, hover, selection, and empty/single-item states.
- `RadialInput`: mouse capture, central dead zone, release, and cancellation;
  the first version uses the mouse while allowing later keyboard/gamepad extensions.
- The consumer controls opening/closing; no domain or networking dependencies.
  Reuse existing themes, overlays, text, and painting.

### Dropdown / Select

- Options with stable identity, label, value, and enabled state; single selection
  and a local callback. Persistence belongs to the consumer.
- Open the list as a field overlay, positioning it above/below to fit on the
  screen and applying clipping/scrolling if it exceeds the available space.
- Support mouse, arrows, Enter, Escape, focus, and disabled options. Search and
  multiple selection are outside the first iteration.

### Context menu

- Model actions, separators, enabled state, and labels; open at coordinates
  or at the source component's bounds.
- Adjust the overlay position to screen limits; close with Escape, an outside
  click, or after executing an action. Support arrows/Enter and focus the first
  valid action.
- Postpone submenus, global shortcuts, and callbacks loaded from JSON.

## 63.4. Presentation and navigation components

| Component | Minimum technical plan |
|---|---|
| `Panel` | **Implemented.** Themed frame with configurable padding; inner content receives bounds and is composed with an explicit layout such as Row/Column/Grid. |
| `Card` | **Implemented.** Frame with an optional title and hover/selected states; the selected state belongs to the consumer. |
| `IconButton` | **Implemented.** Mouse/keyboard activation, focus, and accessible name/tooltip; the icon is supplied as a vanilla `Component`. |
| `Divider` | **Implemented.** Horizontal by default, with configurable orientation, thickness, and color; no input. |
| `Tooltip` | **Implemented.** `UiComponent.tooltip(Component...)` renders through the vanilla pipeline, without its own delay or animation. |
| `Modal` | **Implemented.** Centered overlay through `UiScreen.showModal`, backdrop, focus containment, Escape, and optional outside-click closing; does not create another Screen. |
| `TreeView` | Stable IDs, hierarchy, expand/collapse, selection, and arrow navigation; draw only visible nodes. |
| `SplitPane` | Two children, a draggable divider, ratio, and minimum limits; update bounds on resize and allow keyboard adjustment. |
| `ResizablePanel` | Edge/corner handles, drag capture, and clamping to minimum/maximum dimensions; persistence belongs to the consumer. |

## 63.5. Lists, data, and Minecraft integration

| Component | Minimum technical plan |
|---|---|
| `VirtualizedList` | Data source and row renderer; calculate the visible range from the viewport/offset and create/reuse only visible rows, with stable indices. |
| `VirtualizedGrid` | Calculate visible rows/columns from cell size, gap, and bounds; build only cells in the viewport and preserve identity during resizing/scrolling. |
| `NotificationCenter` | Persistent model separate from Toast, with read/unread state, timestamp, and dismissal; lifecycle and storage belong to the consumer. |
| `DragAndDrop` | Typed payload, source, target accept/reject, visual ghost, and start/move/drop/cancel events; no inventory transfer or networking. |
| `Graph/Canvas` | Pan/zoom transform, clipping, world coordinates, and hit testing; start with generic nodes/edges, without a domain solver. |
| `UiContainerScreen` | Bridge to `AbstractContainerScreen` that integrates components and slots while preserving render/input order and vanilla synchronization; no menu/network abstraction. |
| `UiDebugOverlay` | Development only: bounds, debug IDs, hover/focus, and clipping regions; configurable toggle and no cost when disabled. |
| `debugId` | Optional `UiComponent` metadata for diagnostics; does not affect layout, style, serialization, or business logic. |

## 63.6. Inputs, content, configuration, and extensions

| Component | Minimum technical plan |
|---|---|
| `ColorPicker` | Color value object, validated hex input, and HSV controls or a palette; supports keyboard input without imposing a color model on the consumer. |
| `KeybindInput` | Capture keys/modifiers through vanilla events and display waiting/cancel/clear states; do not register a global `KeyMapping` for each edit. |
| `RichText` | Render styled `Component` text and wrap lines using vanilla APIs; separate measurement/layout, without a custom parser. |
| `Markdown subset` | Convert headings, paragraphs, emphasis, lists, and links to `Component`; use an allowlist, without HTML, execution, or fetching during rendering. |
| `Tween` / `Easing` | Pure math with time supplied by the UI lifecycle and deterministic cancellation; no timeline engine/thread. Introduce only with a concrete consumer. |
| `Responsive breakpoints` | Resolve width/height ranges and choose a variant on init/resize; no CSS, percentages, or per-frame recalculation. |
| `Declarative builders` | Typed Java builders compiled into the current tree, with validation; no JSON/XML parser or style language. |
| `Data binding` | Optional `ObservableValue<T>` with a subscription disposed of when the screen closes; does not replace callbacks or define domain state. |
| `Accessibility improvements` | Labels, states, focus order, keyboard, and narration through vanilla APIs; validate a flow without the mouse and do not create a parallel framework. |
| `ConfigSection` | Group a title, description, and settings by composing `Panel`/`Column`; no persistence of its own. |
| `BooleanSetting` | Visual adapter for `Toggle`; value, persistence, and validation belong to the consuming config system. |
| `EnumSetting` | Adapt a known enum to `Dropdown`/`Select` with explicit labels; no automatic reflection-based discovery. |
| `NumberSetting` | Adapt limits/step to a `Slider` or numeric field; formatter, unit, and persistence come from the consumer. |

## 63.7. Initial sequence after the first six

Implement as reviewable batches: `NotificationCenter` and `TreeView`; virtualization;
`SplitPane` and `ResizablePanel`; configuration adapters; `ColorPicker`/`KeybindInput`;
rich text/Markdown; drag-and-drop/Graph; builders/binding/responsiveness;
animation; accessibility/narration; `UiContainerScreen` and debug tooling.
Reorder based on real consumers, and do not start all of milestone 7 at once.

## 63.8. Future infrastructure

- Extract `UiInputRouter` only if concrete overlays and screens repeat the logic
  that currently belongs to `UiScreen`.
- Add `UiThemeRegistry` only when multiple consumers need to resolve named themes;
  for now, the theme is passed directly.
- Implement narration alongside interactive controls instead of postponing all
  accessibility work.
- The historical list in section 53 does not determine priority; this section is
  the current reference. `Slider`, already implemented, is not part of the future backlog.
