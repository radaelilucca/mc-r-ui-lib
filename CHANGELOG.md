# Changelog

## [0.3.0] - 2026-10-07

### Added

- Added optional two-handle slider selection for ordered ranges, with mouse, scroll, keyboard, and playground editing support.
- Added optional interactive actions in modal headers.

### Changed

- Added more spacing between toast content and the left accent strip.
- Translated all documentation into US English and added a project README that clearly identifies the library as alpha and under construction.

### Fixed

- Fixed flickering at the end of toast and text fades, and kept toast accent strips inside rounded borders.
- Fixed text-field vertical alignment and clicks within field padding.
- Fixed internal tab corners so adjacent tabs form a continuous strip.
- Fixed themed tooltip layering over rendered items.
- Fixed row and column alignment and allowed screens to reserve their visible panel bounds for JEI.

## [0.2.0] - 2026-10-06

### Added

- Added `InventoryGrid` for rendering generic containers or the local player's main inventory and hotbar, with a picker mode that copies items into `ItemSlot` without changing the source inventory.
- Added custom interaction callbacks, configurable grid dimensions, and subtle hotbar highlighting for the player inventory layout.
- Added configurable `ItemSlot` backgrounds with named theme colors and arbitrary ARGB values, including playground inspector controls.
- Added a development example screen for testing inventory and JEI item drops into ghost slots.
