# R-UI Library Agent Instructions

## Playground component examples

- Keep `UiPlaygroundScreen.PALETTE_CATEGORIES` and the built-in playground layout catalog aligned. Every category in the left component palette must have one bundled example layout in `src/main/resources/assets/r_ui_lib/playground/layouts/`.
- The catalog is declared by `UiPlaygroundScreen.BUILT_IN_LAYOUTS`. Each entry must point to a JSON file with the same ID and a valid `PlaygroundDocument` schema. Built-in layouts are loaded directly from mod resources; never require first-run generation or overwrite a user's saved layouts to make them available.
- Whenever a palette category or component is added, moved, renamed, or its visible behavior/API changes, update the corresponding bundled layout so the category example exercises the current component set and representative states. Include new components in their category layout as part of the same change.
- Preserve stable layout IDs where possible. If an ID must change, update the catalog and resource path together. Keep labels understandable in the playground Load picker.
- Shared playground code and bundled JSON belong in root `src/main`; Forge 1.20.1 and NeoForge 1.21.1 must load the same examples. Do not duplicate loader-specific copies.
- When changing a component or its example, keep the preset JSON within the limits enforced by `PlaygroundDocument.normalize()` and use item IDs available in vanilla unless the example explicitly tests an optional integration.
