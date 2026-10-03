# R-Ui Lib

R-Ui Lib is a reusable UI library for Minecraft mods, with a compositional set
of client-side components.

| Minecraft | Loader | Java |
| --- | --- | --- |
| 1.21.1 | NeoForge 21.1.236+ | 21 |

Install the library JAR in the client `mods` folder alongside mods that use it.
Releases include a source JAR for developers. See the [consumer guide](docs/using-r-ui-lib.md)
for adding the library to another mod project.

## Development

- Build: `gradlew.bat build`
- Run the client: `gradlew.bat runClient`
- Publish to the local Maven repository: `gradlew.bat publishToMavenLocal`

## Documentation

- [Documentation index](docs/README.md)
- [Using R-Ui Lib in another mod](docs/using-r-ui-lib.md)
- [Toolkit foundation and API plan](docs/ui-toolkit-foundation-plan.md)
- [Playground guide](docs/ui-toolkit-playground.md)
