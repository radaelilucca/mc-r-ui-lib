# PRD — Minecraft UI Toolkit
## Biblioteca client-side de UI para mods Minecraft

**Status:** Draft  
**Target inicial:** Minecraft 1.21.1 + NeoForge  
**Distribuição inicial:** módulo interno de um mod hospedeiro  
**Objetivo futuro:** extração para projeto/biblioteca independente  
**Referência arquitetural:** abordagem utilizada pelo Logistics Network — primitives e design system próprios construídos sobre as APIs vanilla de `Screen`, `GuiGraphics` e widgets do Minecraft.

---

# 1. Visão

Construir uma biblioteca de UI reutilizável para mods Minecraft que permita criar interfaces visualmente modernas, consistentes e desacopladas do estilo vanilla tradicional de inventário/furnace.

A biblioteca deve abstrair as partes repetitivas e pouco ergonômicas da API de UI do Minecraft sem tentar recriar um framework web completo.

O objetivo não é competir com React, CSS ou frameworks declarativos completos.

O objetivo é criar uma camada pequena, previsível e idiomática sobre:

```text
Minecraft Screen API
        ↓
GuiGraphics
        ↓
Input events
        ↓
Widgets / Item rendering
```

fornecendo:

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

# 2. Problema

A API vanilla permite construir praticamente qualquer interface, mas exige muito código repetitivo.

Problemas comuns:

- posicionamento manual de elementos;
- cálculo manual de padding/gap;
- cores e estilos duplicados;
- hover/focus implementados várias vezes;
- scroll implementado manualmente;
- clipping/scissor pouco ergonômico;
- necessidade de repetir lógica de cards, panels, buttons e tabs;
- pouca consistência entre diferentes telas;
- grande acoplamento entre layout, rendering e input;
- widgets vanilla possuem estética e comportamento que nem sempre combinam com interfaces customizadas.

Um mod com várias telas tende naturalmente a criar seu próprio mini design system.

O Logistics Network é um exemplo dessa abordagem, com estruturas equivalentes a:

```text
Theme
ThemePaint
custom EditBox
screen abstractions
reusable paint helpers
```

Esta biblioteca formaliza esse approach desde o início.

# 3. Objetivos

## 3.1. Objetivos principais

A biblioteca deve permitir construir UIs como:

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

sem depender visualmente do estilo vanilla.

## 3.2. Ergonomia

O uso deve se aproximar conceitualmente de:

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

em vez de espalhar:

```java
graphics.fill(...);
graphics.drawString(...);
graphics.renderItem(...);

if (mouseX >= ...) {
    ...
}
```

por cada screen.

A biblioteca não precisa ser declarativa no MVP, mas deve permitir composição de componentes.

## 3.3. Independência visual

Nenhum componente deve depender obrigatoriamente de:

- texturas vanilla de botão;
- sprites de furnace/chest;
- slots vanilla;
- background PNG fixo.

A aparência deve ser derivada principalmente de:

- shapes;
- cores;
- borders;
- text;
- gradients;
- sprites opcionais;
- item rendering.

## 3.4. Desacoplamento

A biblioteca será criada inicialmente dentro de outro projeto, mas deve ser desenvolvida como se já fosse uma dependência externa.

Regra:

> Nenhuma classe da biblioteca pode importar classes de domínio do mod hospedeiro.

Permitido:

```text
net.minecraft.*
net.neoforged.*
org.joml.*
ui toolkit internal packages
```

Não permitido:

```text
com.my_mod.features.*
com.my_mod.blocks.*
com.my_mod.network.*
com.my_mod.domain.*
```

# 4. Não objetivos

O MVP não deve tentar implementar:

- HTML/CSS;
- Flexbox completo;
- CSS Grid completo;
- stylesheet parser;
- XML/JSON layout declarativo;
- hot reload de UI;
- WYSIWYG editor;
- reactive state framework;
- animations complexas;
- full accessibility framework;
- cross-loader support;
- Fabric support;
- Forge 1.20.1;
- server-side UI abstraction;
- networking abstraction;
- inventory menu abstraction completa.

Esses pontos podem ser avaliados depois.

# 5. Filosofia

## 5.1. Vanilla first

Sempre que Minecraft já resolve bem alguma coisa, reutilizar.

Exemplos:

```text
Font
ItemStack rendering
tooltips
text input behavior
Screen lifecycle
keyboard/mouse events
narration quando aplicável
```

Não recriar item rendering.

Não recriar text layout básico.

## 5.2. Own the visuals

Para aparência customizada:

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

preferir rendering próprio.

## 5.3. Composition over inheritance

Evitar uma árvore profunda de classes como:

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

Preferir:

```text
Component
Behavior
Layout
Renderer
```

composição.

## 5.4. Stateful only where necessary

Componentes devem manter apenas o estado necessário.

Exemplos:

```text
hovered
focused
pressed
scrollOffset
selected
expanded
```

Estado de domínio pertence ao consumidor.

# 6. Organização do módulo

Estrutura inicial sugerida:

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

A estrutura pode evoluir, mas o módulo deve permanecer autocontido.

# 7. Camada core

## 7.1. `UiComponent`

Interface base conceitual:

```java
interface UiComponent {
    void measure(UiMeasureContext context);
    void layout(Bounds bounds);
    void render(UiContext context);
}
```

Responsabilidades:

- receber bounds;
- renderizar;
- participar de layout;
- opcionalmente receber input.

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

Deve encapsular acesso frequente a:

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

Exemplo conceitual:

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

Inspirado diretamente no pattern do Logistics Network.

## 8.1. `UiTokens`

Tokens estruturais:

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

Tokens de cor:

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

O toolkit deve permitir múltiplos themes.

Exemplo inicial:

```text
Dark
Light
```

Não priorizar theme editor no MVP.

# 9. Paint primitives

`UiPaint` será equivalente ao `ThemePaint` observado no Logistics Network.

Deve centralizar rendering repetitivo.

Primeiro set:

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

Não implementar Flexbox completo.

Implementar um subset pequeno que resolva a maioria das screens.

## 10.1. Column

```text
Column
├── component
├── component
├── component
└── component
```

Configuração:

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

Configuração:

```text
gap
vertical alignment
```

## 10.3. Stack

Elementos sobrepostos:

```text
Stack
├── background
├── content
└── overlay
```

Útil para:

- badges;
- hover;
- selection;
- modals.

## 10.4. Grid

Necessário desde cedo para mods com:

- items;
- blocks;
- resource browsers;
- palettes.

API conceitual:

```java
Grid grid = Grid.builder()
    .columns(6)
    .gap(6)
    .build();
```

Inicialmente:

```text
fixed column count
fixed/known item size
```

Depois pode suportar auto-fit.

## 10.5. Spacer

```java
Spacer.fixed(8);
Spacer.flex();
```

`flex()` pode ser adiado se complicar o primeiro layout engine.

# 11. Sizing

MVP:

```text
FIXED
CONTENT
FILL
```

Exemplo:

```java
component.width(FILL);
component.height(CONTENT);
```

Não implementar inicialmente:

```text
min-content
max-content
percentage
calc()
complex constraints
```

# 12. Primitives prioritários

## 12.1. Text

Suportar:

```text
Component
String
color
alignment
wrap
max width
```

## 12.2. Panel

Container visual neutro.

Props:

```text
background
border
padding
radius
```

## 12.3. Card

Panel com semantics visuais de agrupamento.

Props:

```text
title opcional
hover opcional
selected opcional
```

## 12.4. Button

Button próprio, sem depender visualmente do botão vanilla.

Estados:

```text
default
hover
pressed
focused
disabled
```

## 12.5. IconButton

Mesmo comportamento de Button, conteúdo visual compacto.

Útil para:

```text
close
back
search
settings
delete
favorite
```

## 12.6. Toggle

Primitivo de boolean.

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

Estados:

```text
active
hover
disabled
```

## 12.8. Chip / Pill

Útil para:

```text
mod names
filters
statuses
tags
categories
```

## 12.9. Divider

Horizontal inicialmente.

# 13. Inputs

Inputs são uma área onde vale reutilizar comportamento vanilla.

## 13.1. TextField

Pode encapsular `EditBox`.

A biblioteca controla:

```text
layout
theme
border
focus chrome
placeholder
```

e deixa o Minecraft controlar:

```text
cursor
selection
keyboard
clipboard
IME behavior quando possível
```

## 13.2. SearchField

Componente especializado:

```text
search icon
placeholder
clear action
onChange
```

# 14. Scrolling

`ScrollView` é componente obrigatório do MVP.

Responsabilidades:

- viewport;
- clipping;
- scroll offset;
- mouse wheel;
- scrollbar opcional;
- drag de scrollbar;
- clamp;
- conteúdo maior que viewport.

API conceitual:

```java
ScrollView view = new ScrollView(content)
    .showScrollbar(true);
```

# 15. Clipping

Criar `ClipStack`.

Objetivo:

```text
push(bounds)
render children
pop()
```

Internamente utilizar scissor da API gráfica.

Isso deve evitar código de clipping espalhado pelas screens.

# 16. Item rendering

Muito importante para mods.

Criar helper reutilizável:

```java
ItemRenderer.render(
    context,
    stack,
    bounds
);
```

Internamente usar o renderer padrão do Minecraft.

Suportar:

```text
ItemStack
tooltip
count overlay opcional
durability overlay opcional
scale
```

Não implementar renderer 3D próprio.

# 17. ItemCard

Componente importante para vários mods futuros.

Estrutura:

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

Composição de:

```text
Grid
+
ItemCard
+
ScrollView
```

Casos de uso:

- block palette;
- resource browser;
- recipe browser;
- selection dialog;
- filters.

Esse componente deve ser importante para validar o toolkit no mod hospedeiro.

# 19. Tooltip

Criar abstraction fina sobre tooltip vanilla.

API:

```java
component.tooltip(Component...)
```

A library deve continuar utilizando rendering nativo de tooltip sempre que possível.

# 20. Modal

MVP deve suportar modal básico.

```text
Screen
 ↓
Overlay
 ↓
Centered panel
```

Recursos:

```text
backdrop
Esc to close
optional outside click close
focus containment simples
```

# 21. Input routing

Criar uma camada clara de input.

Eventos:

```text
mouseMove
mouseDown
mouseUp
mouseScroll
keyDown
keyUp
charTyped
```

Regra:

> O componente visual deve declarar bounds e comportamento. A Screen não deve precisar repetir hit testing de cada filho.

# 22. Event propagation

Modelo simples:

```text
topmost component first
        ↓
handled?
 ├─ yes → stop
 └─ no  → parent / next
```

Sem implementar DOM-style capture/bubble completo no MVP.

# 23. Focus management

`FocusManager` deve gerenciar:

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

Não tentar resolver acessibilidade completa inicialmente.

# 24. Hover

Hover deve ser derivado automaticamente dos bounds.

Não exigir:

```java
boolean hovered =
    mouseX >= x &&
    mouseX <= x + width ...
```

em cada componente.

# 25. Drag behavior

Criar abstração mínima reutilizável.

Casos:

```text
scrollbar
slider futuro
graph editor futuro
movable panels futuro
```

Não criar sistema complexo de drag/drop no MVP.

# 26. Screen integration

Criar uma base:

```java
abstract class UiScreen extends Screen
```

Responsável por:

- criar root component;
- recalcular layout em `init`;
- criar `UiContext`;
- render;
- input routing;
- focus;
- resize;
- lifecycle.

Uso:

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

Inventários precisam de:

```text
AbstractContainerScreen
```

Não construir uma abstração completa no primeiro milestone.

Criar depois uma ponte:

```java
UiContainerScreen<T extends AbstractContainerMenu>
```

Ela deve permitir combinar:

```text
Minecraft slots
+
custom UI toolkit
```

sem reimplementar sincronização de inventory.

# 28. Estado

A biblioteca não deve impor state management.

Exemplo:

```java
class PaletteScreen {
    Block selectedBlock;
    String query;
    List<Block> results;
}
```

O toolkit apenas renderiza e dispara callbacks.

Pode existir no futuro:

```text
ObservableValue
State<T>
```

mas não é necessário para o MVP.

# 29. Rebuild vs mutation

MVP pode aceitar abordagem simples:

```text
state changes
   ↓
rebuild relevant component tree
```

ou mutation direta.

Não otimizar prematuramente.

Minecraft UIs normalmente possuem poucas centenas de componentes no máximo.

# 30. Animations

Animations devem ser opcionais e discretas.

MVP:

```text
Tween
Easing
```

Casos:

```text
hover fade
modal fade
tab indicator
panel slide
```

Não criar timeline engine complexa.

# 31. Accessibility / narration

Não é prioridade do primeiro spike, mas a arquitetura não deve impedir integração com narration.

Componentes interativos devem ter:

```text
label
role conceitual
state
```

Quando possível, reutilizar mecanismos vanilla.

# 32. Debug tooling

Criar `UiDebugOverlay`.

Toggle de desenvolvimento para visualizar:

```text
component bounds
component id
hover target
focus target
layout boxes
clipping areas
```

Exemplo:

```text
┌──────────────┐
│ Card         │
│ ┌──────────┐ │
│ │ ItemCard │ │
│ └──────────┘ │
└──────────────┘
```

Isso deve acelerar muito o desenvolvimento.

# 33. IDs de componentes

Todo componente pode opcionalmente ter:

```text
debugId
```

Exemplo:

```java
new Card()
    .debugId("palette-reference-card");
```

Não usar IDs para styling no MVP.

# 34. Public API boundary

Tudo que consumidores devem usar fica em:

```text
ui.api
```

ou packages explicitamente documentados como públicos.

Internals:

```text
ui.internal
```

Isso facilitará a futura extração da biblioteca sem quebrar consumidores.

# 35. Dependências

A biblioteca deve evitar dependências externas inicialmente.

Permitidas:

```text
Minecraft
NeoForge
JOML
```

Evitar:

```text
YACL
owo-ui
Cloth Config
third-party UI framework
```

Objetivo é controlar a stack e aprender as internals.

# 36. Nenhuma dependência do mod hospedeiro

Regra de build desejada:

```text
ui toolkit
   ↓
não conhece
   ↓
host mod
```

Mas:

```text
host mod
   ↓
depende de
   ↓
ui toolkit
```

Idealmente mesmo dentro do mesmo repository:

```text
modules/
├── ui-toolkit/
└── mod/
```

Se a estrutura Gradle atual não comportar multi-module imediatamente, usar package isolado:

```text
src/main/java/<org>/uitoolkit/
```

e proibir imports do host.

# 37. Resource namespace

Para facilitar migração futura, assets próprios da UI devem utilizar namespace separado quando possível.

Exemplo:

```text
assets/minecraft_ui_toolkit/
```

ou um namespace neutro planejado.

Evitar colocar assets fundamentais da library no namespace semântico do mod hospedeiro.

# 38. Configuração

A biblioteca não deve possuir screen de configuração global no MVP.

Themes e comportamento devem ser configuráveis por código:

```java
UiToolkit.init(
    UiTheme.DARK
);
```

# 39. Primeiro consumidor

O primeiro consumidor deve ser uma UI real do mod hospedeiro.

Recomendação:

**Dynamic Block Palette UI**

Porque exige:

```text
screen custom
cards
item rendering
grid
scroll
tabs/filters
tooltips
search
selected state
```

Ou seja, testa boa parte dos primitives sem precisar de inventory container.

# 40. MVP da library

Primeira versão precisa conter:

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

Exemplo:

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

Isso é apenas direção de ergonomia.

Não precisa ser API final.

# 42. Rendering lifecycle

Fluxo conceitual:

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

A cada resize:

```text
measure
layout
```

A cada frame:

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

Componente informa tamanho desejado.

## Layout

Parent define bounds finais.

## Render

Componente desenha dentro dos bounds.

Essa separação é importante para impedir que cada componente calcule layout durante render.

# 44. Performance

Meta:

- zero allocation excessiva por frame;
- evitar reconstruir listas grandes durante render;
- layouts recalculados apenas quando necessário;
- item rendering reaproveita pipeline vanilla;
- clipping aninhado controlado;
- nenhuma reflection no render loop.

A library deve funcionar bem em telas com centenas de item cards.

# 45. Testing

## Unit tests

Partes possíveis sem Minecraft runtime:

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

Necessários para:

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

Testar obrigatoriamente em:

```text
GUI Scale 1
GUI Scale 2
GUI Scale 3
GUI Scale Auto
```

Layout nunca deve depender de pixels físicos da tela.

# 47. Resolutions

Validar ao menos:

```text
1280×720
1920×1080
2560×1440
ultrawide
window pequeno
```

# 48. Acceptance criteria — MVP

A primeira versão da biblioteca será considerada válida quando for possível implementar uma tela real que:

- utiliza `UiScreen`;
- não realiza positioning manual de cada elemento na screen;
- utiliza theme central;
- possui Row/Column/Grid;
- possui ScrollView;
- possui SearchField;
- renderiza ItemStacks;
- possui cards selecionáveis;
- possui tooltip;
- funciona com resize;
- funciona com GUI scale diferente;
- suporta mouse e teclado básico;
- não importa nenhuma classe de domínio do mod hospedeiro.

# 49. Milestones

## Milestone 0 — Rendering primitives

Criar:

```text
UiTheme
UiTokens
Bounds
UiContext
UiPaint
```

Provar:

```text
panel
card
text
button visual
```

## Milestone 1 — Component tree

Criar:

```text
UiComponent
UiContainer
UiScreen
```

Provar:

```text
nested components
render traversal
```

## Milestone 2 — Layout

Criar:

```text
Row
Column
Stack
Grid
Spacer
Insets
```

Provar screen responsiva sem coordenadas hardcoded na aplicação.

## Milestone 3 — Input

Criar:

```text
hover
click
focus
Tab navigation
scroll routing
```

## Milestone 4 — Core components

Criar:

```text
Button
Toggle
Tabs
Chip
TextField
SearchField
```

## Milestone 5 — Scroll + clipping

Criar:

```text
ClipStack
ScrollView
Scrollbar
```

## Milestone 6 — Mod-oriented components

Criar:

```text
ItemRenderer
ItemCard
ItemGrid
Tooltip
Modal
```

## Milestone 7 — Real consumer

Implementar uma tela real do mod hospedeiro usando somente API pública do toolkit.

Recomendado:

```text
Dynamic Block Palette
```

# 50. Migração futura para projeto próprio

A extração deve exigir apenas:

```text
move module
change Maven coordinates
change package root se desejado
publish artifact
```

Não deve exigir:

```text
remover dependências do domínio
reescrever components
migrar assets acoplados
alterar callbacks de negócio
```

# 51. Estratégia de publicação futura

Quando a biblioteca provar valor:

```text
group:
<org>.minecraft

artifact:
ui-toolkit-neoforge

version:
semver
```

Distribuição possível:

```text
Maven
GitHub Packages
CurseForge/Modrinth apenas se virar runtime dependency
```

Preferência inicial:

> library dependency embutida/Jar-in-Jar ou compile dependency, dependendo da estratégia de distribuição.

Ainda não decidir.

# 52. Critério para extrair do projeto hospedeiro

Extrair quando:

- pelo menos dois mods internos usam a library;
- API pública estiver razoavelmente estável;
- não houver imports do domínio do primeiro mod;
- theme/component system estiver provado em produção;
- migração puder acontecer sem refactor estrutural.

# 53. Future backlog (status and order updated in section 63)

Após MVP:

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

Vale prever o radial como extensão futura porque já existe demanda em outros mods planejados.

Não incluir no MVP.

Arquitetura futura:

```text
RadialMenu
RadialSegment
RadialGeometry
RadialInput
```

Pode reutilizar:

```text
theme
paint
input
text
animation
```

# 55. Config screens

A própria library deve ser capaz de construir páginas de configuração refinadas sem depender de YACL.

Isso não significa recriar YACL imediatamente.

Os primitives do MVP já devem tornar possível construir:

```text
tabs
toggles
text inputs
buttons
cards
sections
```

Config-specific abstractions podem vir depois:

```text
ConfigSection
BooleanSetting
EnumSetting
NumberSetting
```

# 56. Diferença para frameworks existentes

O toolkit não busca vencer owo-ui/YACL em quantidade de features.

O diferencial é:

- controle completo da aparência;
- zero dependência externa;
- API pequena;
- otimizada para nossos mods;
- aprendizado das internals do Minecraft;
- facilidade de embutir;
- design system consistente entre projetos.

# 57. Naming

Nome provisório interno:

```text
Minecraft UI Toolkit
```

ou:

```text
MUI Toolkit
```

Evitar nome definitivo antes de decidir se será publicado.

O package deve permanecer neutro.

# 58. Principal risco técnico

O maior risco não é rendering.

Rendering básico é simples.

O risco real é deixar a library crescer até virar um framework de layout/input grande demais.

Regra:

> Implementar apenas abstrações que já possuam pelo menos um consumidor concreto.

Evitar construir primitives especulativos.

# 59. Principal risco arquitetural

Acoplamento acidental ao primeiro mod.

Revisar periodicamente:

```text
Does this class know what a Block Palette is?
Does this component import host domain?
Does this helper assume one screen?
Does this theme contain product-specific names?
```

Se sim, mover a responsabilidade para o consumidor.

# 60. Definition of Done para cada component

Todo component novo deve possuir:

- API pública pequena;
- theme support;
- hover/focus quando aplicável;
- resize-safe layout;
- nenhuma referência ao domínio;
- exemplo de uso;
- validação em GUI scales diferentes;
- comportamento previsível com mouse.

# 61. Resultado esperado

Ao final do MVP, construir uma tela custom deve parecer:

```text
definir state
   ↓
compor components
   ↓
definir callbacks
   ↓
deixar toolkit cuidar de
layout/render/input/theme
```

e não:

```text
calcular todos os x/y
desenhar cada shape
fazer hit test manual
implementar scroll
implementar hover
implementar focus
```

# 62. Norte arquitetural

A biblioteca deve ficar entre dois extremos.

Não queremos:

```text
raw GuiGraphics everywhere
```

nem:

```text
full web framework inside Minecraft
```

Queremos:

```text
Minecraft API
      ↓
small reusable UI runtime
      ↓
design system primitives
      ↓
mod screens
```

Essa deve ser a principal restrição de design durante o desenvolvimento.

# 63. Estado atual e roadmap técnico priorizado

Este inventário foi comparado com `docs/ui-toolkit-foundation-plan.md` e com a
API atual. Esta seção atualiza a lista histórica da seção 53. Os marcos devem
ser feitos e revisados um por vez; itens especulativos só avançam quando houver
consumidor concreto.

## 63.1. Estado da API e do playground

Implementados na API: `Label` (texto), `Button`, `Toggle`, `Tabs`, `Chip`,
`TextField`, `SearchField`, `Slider`, `ScrollView`, `ClipStack`, `Row`,
`Column`, `Stack`, `Grid`, `Spacer`, `ItemRenderer`, `ItemCard`, `ItemGrid`,
`Toast` e `ToastHost`.

Ainda sem componente público próprio: `Panel`, `Card`, `IconButton`, `Divider`,
`Tooltip` e `Modal`; helpers de pintura existentes não equivalem às APIs de
composição.

O playground disponibiliza `Grid`, `Spacer`, `ItemCard` e `ItemGrid` pela
paleta. `ItemCard` guarda o ID e a quantidade do item no documento, e pode ser
aninhado em `Grid`/`ItemGrid`. `Spacer` pode ser fixo ou flexível e a paleta tem
rolagem para exibir todos os tipos em janelas baixas. `ItemRenderer` é helper,
não nó de layout; seu caminho é exercitado por `ItemCard`.

## 63.2. Ordem dos próximos marcos

| Ordem | Componente | Resultado do primeiro corte |
|---:|---|---|
| 1 | Toast | Implementado; avisos efêmeros em overlay |
| 2 | Table | Dados em colunas e linhas, com seleção básica |
| 3 | ProgressBar | Progresso determinado, indeterminado opcional |
| 4 | Radial menu | Geometria, segmentos, hit test e seleção |
| 5 | Dropdown / Select | Seleção única, overlay e navegação por teclado |
| 6 | Context menu | Ações ancoradas, teclado e fechamento previsível |
| 7 | Demais itens | Implementar em lotes pequenos conforme seção 63.4–63.6 |

## 63.3. Plano técnico dos seis primeiros

**Estado:** marco Toast concluído na API `Toast`/`ToastHost`, integrado ao
`UiScreen` e demonstrável no playground pelo botão Toast. A próxima etapa da
sequência é Table.

### Toast

- Usar `ToastHost` por tela e publicar por `toastHost.show(Toast)` (com atalhos
  `info`, `success`, `warning` e `error`); evitar singleton global ou threads
  próprias.
- Empilhar quantidade limitada, medir texto vanilla, expirar pelo relógio da UI,
  pausar quando a tela não estiver ativa e dispensar por clique.
- Começar com `INFO`, `SUCCESS`, `WARNING` e `ERROR`, sem persistência entre
  telas nem ações customizadas. Validar que toast não captura clique fora dos
  próprios bounds e que reposiciona em resize.

### Table

- Definir colunas com título, largura fixa/preferida, alinhamento e renderer;
  dados e identidade das linhas vêm do consumidor.
- Implementar cabeçalho, linhas uniformes, hover, seleção única e callback de
  clique; ordenação simples por coluna pode ser opcional.
- Reusar clipping e `ScrollView` vertical. Edição, redimensionamento de coluna e
  virtualização ficam fora do primeiro marco.

### ProgressBar

- Expor `minimum`, `maximum`, `value`, altura/estilo e formatter opcional; clamp
  definido também para range inválido.
- Pintar trilho/preenchimento com `UiPaint`; a variante determinada é obrigatória.
- Indeterminada e animação são opcionais e usam o tempo do ciclo da UI, sem
  timer/thread. Não é interativa e não assume intervalo percentual 0–100.

### Radial menu

- `RadialGeometry`: funções puras para centro, raios, ângulos, setores e hit
  test; cobrir matematicamente fronteiras e wrap do ângulo.
- `RadialSegment`: label, ícone vanilla opcional, habilitado e callback em
  memória. `RadialMenu` desenha setor, hover, seleção e vazio/um item.
- `RadialInput`: captura do mouse, dead zone central, release e cancelamento;
  primeira versão usa mouse, preservando extensão posterior a teclado/gamepad.
- Abrir/fechar é controlado pelo consumidor; sem dependência de domínio ou
  networking. Reutilizar theme, overlay, texto e pintura existentes.

### Dropdown / Select

- Opções com identidade estável, label, valor e habilitado; seleção única e
  callback local. Persistência fica com o consumidor.
- Abrir lista como overlay do campo, posicionando acima/abaixo para caber na
  tela e aplicando clipping/scroll se exceder o espaço.
- Suportar mouse, setas, Enter, Escape, foco e opções desabilitadas. Busca e
  multi-select não entram no primeiro corte.

### Context menu

- Modelar ações, separadores, habilitado e label; abrir por coordenada ou bounds
  do componente de origem.
- Ajustar a posição do overlay aos limites da tela; fechar com Escape, clique
  fora ou após executar ação. Permitir setas/Enter e focar primeira ação válida.
- Adiar submenus, atalhos globais e callbacks carregados de JSON.

## 63.4. Componentes de apresentação e navegação ainda ausentes

| Componente | Plano técnico mínimo |
|---|---|
| `Panel` | `UiContainer` com fundo/borda temáticos, padding e filhos; usar estratégia explícita de layout, sem duplicar `UiPaint.panel`. |
| `Card` | Compor título opcional e conteúdo, com hover/selecionado opcionais; estado de domínio fica com o consumidor. |
| `IconButton` | Reusar ativação/foco de `Button`, desenhar ícone vanilla/sprite e exigir tooltip ou nome narrável. |
| `Divider` | Componente sem input com orientação, espessura e cor temática; medir só a espessura. |
| `Tooltip` | Wrapper fino das APIs vanilla ligado ao componente e com conteúdo `Component`; sem atraso/animação própria no primeiro corte. |
| `Modal` | Overlay no host, backdrop, painel central, foco contido, Escape e opção de fechar por clique fora; não criar uma `Screen` por diálogo. |
| `TreeView` | IDs estáveis, hierarquia, expand/collapse, seleção e navegação por setas; desenhar apenas nós visíveis. |
| `SplitPane` | Dois filhos, divisor arrastável, proporção e limites mínimos; atualizar bounds em resize e permitir ajuste por teclado. |
| `ResizablePanel` | Handles nas bordas/cantos, captura de drag e clamp por dimensões mínima/máxima; persistência fica com consumidor. |

## 63.5. Listas, dados e integração com Minecraft

| Componente | Plano técnico mínimo |
|---|---|
| `VirtualizedList` | Fonte de dados e renderer de linha; calcular intervalo visível pelo viewport/offset e criar/reutilizar apenas linhas visíveis, com índices estáveis. |
| `VirtualizedGrid` | Calcular linhas/colunas visíveis por célula, gap e bounds; construir só células no viewport e preservar identidade em resize/scroll. |
| `NotificationCenter` | Modelo persistente separado do Toast, com lida/não lida, horário e dismiss; ciclo de vida e armazenamento são do consumidor. |
| `DragAndDrop` | Payload tipado, origem, target accept/reject, ghost visual e eventos start/move/drop/cancel; sem transferir inventário ou fazer networking. |
| `Graph/Canvas` | Transform pan/zoom, clipping, coordenadas de mundo e hit testing; iniciar com nós/arestas genéricos, sem solver de domínio. |
| `UiContainerScreen` | Ponte para `AbstractContainerScreen` que integra componentes e slots preservando ordem de render/input e sincronização vanilla; sem abstrair menu/rede. |
| `UiDebugOverlay` | Development-only: bounds, debug IDs, hover/foco e regiões de clipping; toggle configurável e custo nulo quando desligado. |
| `debugId` | Metadado opcional em `UiComponent` para diagnóstico; não afeta layout, estilo, serialização ou lógica de negócio. |

## 63.6. Inputs, conteúdo, configuração e extensões

| Componente | Plano técnico mínimo |
|---|---|
| `ColorPicker` | Value object de cor, hex validado e controles HSV ou paleta; suporta teclado sem impor modelo de cor ao consumidor. |
| `KeybindInput` | Capturar tecla/modificadores pelos eventos vanilla e exibir aguardando/cancelar/limpar; não registrar `KeyMapping` global por edição. |
| `RichText` | Renderizar `Component` estilizado e quebrar linha com APIs vanilla; separar medição/layout, sem parser próprio. |
| `Markdown subset` | Converter títulos, parágrafos, ênfase, listas e links a `Component`; whitelist, sem HTML, execução ou fetch durante render. |
| `Tween` / `Easing` | Matemática pura com tempo fornecido pelo ciclo UI e cancelamento determinístico; sem timeline engine/thread. Só introduzir com consumidor concreto. |
| `Responsive breakpoints` | Resolver faixas de largura/altura e escolher variante no init/resize; sem CSS, porcentagens ou recálculo por frame. |
| `Declarative builders` | Builders Java tipados compilados para a árvore atual, com validação; sem parser JSON/XML ou linguagem de estilo. |
| `Data binding` | `ObservableValue<T>` opcional com subscription descartável ao fechar tela; não substitui callbacks nem define estado de domínio. |
| `Accessibility improvements` | Labels, estados, ordem de foco, teclado e narration via APIs vanilla; validar fluxo sem mouse e não criar framework paralelo. |
| `ConfigSection` | Agrupar título, descrição e settings por composição de `Panel`/`Column`; sem persistência própria. |
| `BooleanSetting` | Adaptador visual para `Toggle`; valor, persistência e validação pertencem ao sistema de config consumidor. |
| `EnumSetting` | Adaptar enum conhecida a `Dropdown`/`Select` com labels explícitos; sem descoberta reflexiva automática. |
| `NumberSetting` | Adaptar limites/step a `Slider` ou campo numérico; formatter, unidade e persistência vêm do consumidor. |

## 63.7. Sequência inicial depois dos seis primeiros

Implementar como lotes revisáveis: `Panel`/`Card`/`Divider`/`IconButton`/
`Tooltip`/`Modal`; `NotificationCenter` e `TreeView`; virtualização; `SplitPane`
e `ResizablePanel`; adaptadores de configuração; `ColorPicker`/`KeybindInput`;
texto rico/Markdown; drag-and-drop/Graph; builders/binding/responsividade;
animação; acessibilidade/narration; `UiContainerScreen` e debug tooling.
Reordenar por consumidor real e não iniciar todo o marco 7 de uma vez.

## 63.8. Infraestrutura futura

- Extrair `UiInputRouter` somente se overlays e screens concretos repetirem a
  lógica que hoje pertence a `UiScreen`.
- `UiThemeRegistry` só quando múltiplos consumidores precisarem resolver themes
  nomeados; por ora o theme é passado diretamente.
- Implementar narration junto aos controles interativos em vez de postergar
  todo o trabalho de acessibilidade.
- A lista histórica da seção 53 não é a fonte de prioridade; esta seção é a
  referência atual. `Slider`, já implementado, não faz parte do backlog futuro.
