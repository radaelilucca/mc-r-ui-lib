# Themes and visual tokens

R-Ui Lib provides the `UiTheme.DARK` and `UiTheme.LIGHT` themes. A consuming mod
can choose the global default once during client-side initialization:

```java
UiThemes.setGlobalTheme(UiTheme.LIGHT);
```

Screens created without an explicit theme, and all components that receive
their context, use this configuration. The global value can be updated at
runtime; open screens update their layout on the next render.
A screen can retain its own theme through the `UiScreen(title, root,
theme)` constructor. This theme overrides the global default for that screen only.

## Manual configuration

`UiPalette` is a public record with one argument for each semantic color,
using ARGB values:

| Token | Usage |
| --- | --- |
| `screen` | Screen background |
| `panel`, `card` | Panel and card surfaces |
| `border`, `divider` | Outlines and separators |
| `text`, `mutedText` | Primary and secondary text |
| `accent` | Selection, progress, and highlights |
| `button`, `buttonHover`, `buttonPressed` | Button states |
| `disabled`, `focus` | Disabled state and focus |
| `overlay` | Backdrop behind modals |
| `success`, `warning`, `error` | Toast states |

Other appearance tokens are grouped in `UiTokens`:

- Spacing: `panelPadding`, `cardPadding`, `buttonPadding`, and small,
  medium, and large gaps.
- Size: minimum button width and height.
- Typography: `small`, `body`, `heading`, and `title` scales.
- Corners: small, medium, large, and pill radii.
- Animation: default duration and easing (`LINEAR`, `EASE_IN_OUT`, or `EASE_OUT`).
  A duration of zero disables transitions.

This example keeps the Dark colors and replaces the other tokens:

```java
UiTokens customTokens = new UiTokens(
        Insets.all(12), Insets.all(8), 4, 8, 16,
        Insets.symmetric(12, 6), 72, 22,
        new UiTypography(0.9f, 1.0f, 1.2f, 1.35f),
        new UiRadii(2, 4, 8, 999),
        new UiMotion(180, UiEasing.EASE_OUT));
UiThemes.setGlobalTheme(new UiTheme(UiTheme.DARK.palette(), customTokens));
```

To define all colors manually, pass the 17 values to the `UiPalette`
constructor and use that record when creating the theme:

```java
UiPalette colors = new UiPalette(
        screen, panel, card, border, divider, text, mutedText, accent,
        button, buttonHover, buttonPressed, disabled, focus,
        overlay, success, warning, error);
UiThemes.setGlobalTheme(new UiTheme(colors, customTokens));
```

## Local customization

Pass another `UiTheme` to a screen's constructor to give it a theme independent
of the global default. Components also retain their local overrides, such as
colors in `Label` and `Divider`, colors in `ProgressBar`, and explicit padding
in `Panel`/`Card`. Without an override, their defaults come from the active
theme, including when the screen uses a custom global theme.
