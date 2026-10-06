# Usar R-Ui Lib em outro mod

R-Ui Lib é uma dependência NeoForge instalada como um mod separado. O mod que
usa seus componentes compila contra o JAR da biblioteca e declara a dependência
para que o cliente carregue os dois JARs. O JAR da biblioteca não é embutido no
JAR do consumidor.

## Baixar e instalar

Cada release fornece dois arquivos:

- `r_ui_lib-1.21.1-neoforge-<versão>.jar`: biblioteca para instalar no cliente.
- `r_ui_lib-1.21.1-neoforge-<versão>-sources.jar`: fontes para consulta no IDE.

Instale somente o primeiro arquivo na pasta `mods` da instância Minecraft. O
JAR de fontes não é carregado pelo jogo.

## Compilar um mod consumidor

Copie o JAR de runtime para uma pasta `libs/` no projeto consumidor e adicione-o
ao classpath do Gradle:

```groovy
dependencies {
    implementation files("libs/r_ui_lib-1.21.1-neoforge-0.1.0.jar")
}
```

O JAR contém `META-INF/neoforge.mods.toml`, então o ModDevGradle também o carrega
nas execuções de desenvolvimento. O jogador instala R-Ui Lib separadamente do
mod consumidor.

Declare no `META-INF/neoforge.mods.toml` do mod consumidor que a biblioteca é
necessária no cliente:

```toml
[[dependencies.${mod_id}]]
modId="r_ui_lib"
type="required"
versionRange="[0.1.0,)"
ordering="AFTER"
side="CLIENT"
```

As classes públicas ficam em `com.radaeli.ruilib.ui`. Componentes que criam ou
usam telas devem ser referenciados apenas pelo código client-side do mod
consumidor.

Escolha a aparência global uma vez no inicializador client-side do mod:

```java
UiThemes.setGlobalTheme(UiTheme.DARK); // ou UiTheme.LIGHT
```

Veja [Temas e tokens visuais](ui-theming.md) para a API de paletas e tokens,
além dos overrides de tela e componente.

## Slot fantasma de item

`ItemSlot` renderiza uma referência de `ItemStack` para filtros e usos similares;
ele não coloca itens em inventário. Com JEI instalado, os `ItemSlot`s dentro de
uma tela `UiScreen` são automaticamente alvos de arraste. A integração de JEI é
opcional e o restante da biblioteca continua funcionando sem JEI.

```java
ItemSlot itemSlot = new ItemSlot().setOnItemChanged(stack -> {
    if (stack.isEmpty()) {
        filter.clear();
    } else {
        filter.set(stack);
    }
});
```

O callback recebe uma cópia defensiva com quantidade normalizada para um item;
`ItemStack.EMPTY` representa slot vazio. Também é possível atribuir ou limpar a
referência com `setItemStack(stack)` e `clear()`. O usuário pode limpar o slot
com clique direito, Delete ou Backspace quando ele está focado.

## Maven local ou repositório Maven

Para consumir pelo Maven local nesta máquina, publique o projeto com:

```powershell
.\gradlew.bat publishToMavenLocal
```

No projeto consumidor, adicione `mavenLocal()` aos repositórios e declare:

```groovy
dependencies {
    implementation "com.radaeli.ruilib:r-ui-lib-neoforge:0.1.0"
}
```

Também é possível publicar em qualquer repositório Maven que aceite uploads.
Configure `MAVEN_RELEASE_URL` e, se necessário, `MAVEN_USERNAME` e
`MAVEN_PASSWORD` no ambiente, então execute `.\gradlew.bat publish`. Os mesmos
valores podem ser passados por propriedades Gradle `maven_release_url`,
`maven_username` e `maven_password`. Nenhum endpoint remoto ou credencial fica
gravado no repositório.

## Build dos artefatos

`.\gradlew.bat build` cria o JAR de runtime e o JAR de fontes em `build/libs/`.
O workflow de release anexa ambos ao release do GitHub quando uma tag de versão
é criada. Para distribuição em um repositório Maven público, configure o endpoint
descrito acima.
