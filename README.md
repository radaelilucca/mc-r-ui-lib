# Easy Block Pallets

Minimal NeoForge foundation for Minecraft 1.21.1 and NeoForge 21.1.236.

## Development

- Build: `gradlew.bat build`
- Run the client: `gradlew.bat runClient`
- VS Code: use `Easy Block Pallets: Build` or `Easy Block Pallets: Run NeoForge Client`.

The metadata in `src/main/resources/META-INF/neoforge.mods.toml` and
`src/main/resources/pack.mcmeta` is expanded from `gradle.properties` during
the build. GitHub Actions run PR builds, create semantic version tags from
Conventional Commits merged to `main`, and publish a release for each tag.

## Documentation

- [Documentation index](docs/README.md)
- [UI Toolkit foundation action plan](docs/ui-toolkit-foundation-plan.md)
- [Local reference JAR notes](references/README.md)
