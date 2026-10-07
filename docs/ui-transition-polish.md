# UI Toolkit visual transitions

## Global foundation

Animations use `UiTokens.motion`, with a default duration of **140 ms** and `EASE_IN_OUT` easing. A theme can configure the duration and choose `LINEAR`, `EASE_IN_OUT`, or `EASE_OUT`; a duration of zero disables transitions. The `UiTokens.transitionDurationMillis()` accessor remains available for compatibility. Transitions use the monotonic clock shared through each frame's `UiContext`.

Interaction states start at the component's initial value. Disabling a control takes effect immediately; hover, focus, and selection changes use the theme's duration.

## Per-component decisions

| Component | Transition |
|---|---|
| `AbsoluteLayout` | None: positions children without its own visual state. |
| `Button` | Color transitions between rest, hover, and pressed states; focus outline. |
| `Card` | Background transitions between rest, hover, and selected states; focus outline. |
| `Chip` | Hover and selection backgrounds; focus outline. |
| `ClipStack` | None: clips children; each child animates its own state. |
| `Column` | None: static layout. |
| `Divider` | None: static separator. |
| `Grid` | None: static layout. |
| `IconButton` | Same as `Button`, preserving the native icon and tooltip. |
| `ItemCard` | Hover, pressed, and selection backgrounds. |
| `ItemGrid` | None of its own: child `ItemCard` components animate individually. |
| `Label` | None: static informational text. |
| `Modal` | Dimmed backdrop, frame, and inner content enter and exit with the same opacity; focus and input remain stable until the exit fade ends. |
| `Panel` | None: structural surface. |
| `ProgressBar` | Fill interpolates toward the new progress value. |
| `RadialMenu` | Highlight for the segment under the pointer and a fade when opening or closing. |
| `Row` | None: static layout. |
| `ScrollView` | Content scrolls smoothly; the indicator becomes more visible during interaction. |
| `SearchField` | Inherits hover and focus transitions from `TextField`; the magnifying glass remains steady. |
| `Slider` | Indicator and readout follow programmatic changes; dragging follows the pointer without delay. Hover and focus also transition. |
| `Spacer` | None: only reserves space. |
| `Stack` | None: static layout. |
| `Table` | Per-row hover and selection highlights, with smooth scrolling. |
| `Tabs` | Background, text, outline, and selection indicator transition; the active page changes immediately. |
| `TextField` | Background and outline transition on hover and focus; cursor, selection, and IME use vanilla `EditBox` behavior. |
| `Toast` | Only the notification descriptor; the host animates its presentation. |
| `ToastHost` | Entry and exit fades with a slight horizontal slide. |
| `Toggle` | The thumb slides between positions; track and focus transition. |
| `Tooltip` | Keeps the vanilla tooltip pipeline, without its own animation at this stage. |

## Infrastructure

`UiComponent` stores animation progress per instance. `UiPaint` interpolates colors and opacity without duplicating easing logic in components. Components without their own visual state remain immediate; their interactive children continue to use the same global token.
