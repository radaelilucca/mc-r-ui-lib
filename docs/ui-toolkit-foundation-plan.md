# Plano de ação — fundação da R-Ui Lib

- **Projeto:** R-Ui Lib
- **Base confirmada:** Minecraft 1.21.1, NeoForge 21.1.236, Java 21
- **Status:** etapas 1, 2 e 3, playground visual da etapa 4 e primitives `Toggle`, `Tabs`, `Chip`, `ClipStack`, `ScrollView`, `TextField`, `SearchField`, `Slider`, `Grid`, `Spacer`, `ItemRenderer`, `ItemCard`, `ItemGrid`, `Toast`, `ToastHost`, `Table`, `ProgressBar`, `RadialMenu`, `Panel`, `Card`, `IconButton`, `Divider`, `Tooltip` e `Modal` implementados; componentes visuais demonstráveis no playground; validação manual in-game pendente

## Origem e limites

O toolkit começou como código isolado neste projeto e agora é distribuído como a biblioteca R-Ui Lib. Logistics Network serviu como referência de comportamento e organização visual; seu JAR local permanece em `references/` apenas para consulta.

O arquivo `minecraft-ui-toolkit-prd.md` é tratado como documento de requisitos em rascunho. Ele define a direção client-side, a camada pequena sobre as APIs do Minecraft, composição, temas e a lista ampla de componentes futuros. As fases e prioridades abaixo são uma proposta de execução para começar com uma base verificável; não são requisitos adicionais atribuídos ao PRD.

## Decisões recomendadas para o primeiro corte

1. **Manter NeoForge e Minecraft 1.21.1**, alinhados ao build existente.
2. **Distribuir a biblioteca como mod independente**, no pacote público `com.radaeli.ruilib.ui`, com separação clara entre API e detalhes internos.
3. **Manter o toolkit independente do domínio:** a API não contém regras de negócio, networking ou lógica de outro mod consumidor.
4. **Tratar toda integração com `Screen`, `GuiGraphics`, entrada e widgets como client-side.** O construtor comum do mod e a inicialização do servidor não devem carregar classes exclusivas do cliente.
5. **Usar as APIs vanilla onde já resolvem o comportamento** — fonte, `ItemStack`, renderer de itens, tooltips e edição de texto — e controlar a aparência própria dos painéis e controles.
6. **Não assumir que o JAR de Logistics Network fornece código reutilizável.** Ele é uma referência local de comportamento e organização visual; a página do projeto indica “All Rights Reserved”. Não copiar código, recursos ou identidade visual para o mod.

## Sequência de trabalho

### Etapa 0 — Fechar a fronteira da biblioteca

- Registrar a localização do pacote, o contrato de ausência de dependências do domínio e a fronteira client-side.
- Fazer um spike curto nas APIs da versão configurada para confirmar lifecycle de `Screen`, render com `GuiGraphics`, coordenadas de GUI e clipping antes de abstrair essas áreas.
- Manter o escopo sem dependências externas e sem suporte a outros loaders.

**Saída:** uma biblioteca instalável e consumível por outros mods, sem inicialização de UI no servidor dedicado.

### Etapa 1 — Geometria, tema e pintura

**Estado:** implementada no pacote `com.radaeli.ruilib.ui`.

`Bounds` usa retângulos em coordenadas escaladas de GUI; tema e tokens são
imutáveis; `UiContext`, `UiPaint` e `UiText` usam `GuiGraphics` e `Font` vanilla.

Implementar apenas a base usada por todos os componentes:

- `Bounds` e `Insets`, com hit test, bordas, centro e inset;
- `UiPalette`, `UiTokens` e `UiTheme`, inicialmente com uma paleta escura;
- `UiContext`, reunindo `GuiGraphics`, `Font`, tema, ponteiro e dimensões relevantes da tela;
- `UiPaint` para retângulo, contorno, fundo de painel/cartão, divisor e estados de hover/foco;
- helper de texto fino sobre `Font`, sem substituir medição e desenho de texto do Minecraft.

**Saída:** uma tela de demonstração consegue desenhar título, painel, cartão, divisor e foco usando o tema central; nenhum componente consumidor precisa desenhar shapes diretamente.

### Etapa 2 — Árvore de componentes e layout pequeno

**Estado:** implementada. `UiComponent` define medida/layout/render/input;
`UiContainer` mantém a ordem de render e faz hit testing do filho superior
primeiro. `Column`, `Row`, `Stack` e `Label` compõem a árvore; `UiScreen`
refaz o layout quando o lifecycle da `Screen` inicializa ou redimensiona.

- Definir `UiComponent` com ciclo explícito de medida, layout, render e input necessário.
- Definir `UiContainer` e travessia em ordem consistente para render e hit testing.
- Criar `UiScreen` como ponte fina para o lifecycle de `Screen`.
- Implementar primeiro `Column`, `Row` e `Stack`, mais alinhamento e espaçamento fixos.
- Fazer os bounds finais serem responsabilidade do layout; consumidores não posicionam cada filho com coordenadas absolutas.

**Saída:** montar uma tela aninhada com título, painel, conteúdo e ação sem repetir cálculo manual de posição na screen hospedeira.

### Etapa 3 — Primeiro controle interativo

**Estado:** implementada para o controle `Button`. O foco percorre controles
habilitados em ordem de árvore; Tab/Shift+Tab navegam e Enter, Numpad Enter ou
Espaço ativam o botão focado. O clique captura o botão até o release.

- Criar `Button` com estados normal, hover, pressionado, focado e desabilitado.
- Centralizar hit testing e despacho simples no componente/árvore: filho superior primeiro; parar quando o evento for consumido.
- Adicionar foco por mouse e teclado básico, incluindo Tab/Shift+Tab e ativação do controle focado.
- Delegar texto, fonte e comportamento de tooltip ao Minecraft quando possível.

**Saída:** botão consistente visualmente e utilizável por mouse e teclado, sem lógica de hit test duplicada na `Screen`.

### API inicial disponível

```java
Column content = new Column(8);
content.add(new Label(Component.literal("Título")));
content.add(new Button(Component.literal("Confirmar"), this::confirm));
UiScreen screen = new UiScreen(Component.literal("Exemplo"), content);
```

Toda classe que integra `Screen`, `Font` ou `GuiGraphics` fica no pacote client-side
da biblioteca. A classe comum `RUiLib` não importa esse pacote. Layouts
usam tamanhos intrínsecos medidos e alinhamentos fixos; não há flex-grow ou
rolagem neste corte.

### Primitives interativos adicionais

**Estado:** `Toggle`, `Tabs` e `Chip` adicionados ao conjunto inicial.

- `Toggle` expõe seleção binária e notifica `Consumer<Boolean>` quando alternado.
- `Chip` oferece a mesma seleção binária em formato compacto para filtros/opções.
- `Tabs` contém páginas nomeadas, renderiza apenas a página selecionada e permite
  trocar por clique ou pelas setas esquerda/direita quando focado.
- Os três estão disponíveis na paleta do playground. Estados de seleção fazem
  parte do JSON; callbacks Java continuam locais e nunca são serializados.

### Clipping, rolagem e entrada textual

**Estado:** `ClipStack`, `ScrollView`, `TextField` e `SearchField` adicionados.

- `ClipStack` posiciona filhos sobrepostos dentro do mesmo retângulo e usa o
  scissor de `GuiGraphics` para impedir que o desenho ultrapasse seus limites.
- `ScrollView` organiza os filhos verticalmente, recorta o conteúdo, oferece
  rolagem pela roda do mouse e uma scrollbar que pode ser arrastada.
- `TextField` delega edição a `EditBox` vanilla, preservando cursor, seleção,
  clipboard e IME. A árvore do toolkit encaminha teclado, caracteres e arraste.
- `SearchField` é uma especialização de `TextField` com hint padrão, ícone de
  lupa e espaço reservado para o prefixo; ambos respondem a mudanças de valor.
- Os textos ficam centralizados verticalmente pelo `EditBox` vanilla. O campo
  base mantém 8 px de padding mínimo à esquerda, além do espaço do ícone na busca.
- Os quatro tipos estão na paleta do playground. O valor de campos é persistido
  como texto comum no JSON.

### Slider de faixa

**Estado:** `Slider` adicionado à API e à paleta do playground.

- O range recebe mínimo, máximo e passo configuráveis; passo zero representa
  uma faixa contínua. O valor é limitado ao range e alinhado ao passo quando
  ele é positivo.
- Mouse, arraste e teclado ajustam o valor; setas alteram um passo (ou 1% do
  range contínuo), Page Up/Down alteram dez passos, e Home/End vão aos limites.
- O inspetor edita mínimo, máximo e passo como `min, max, step`. O valor atual e
  o range ficam persistidos no JSON do playground; callbacks Java não são salvos.

### Grid e Spacer

**Estado:** implementados como layouts públicos do toolkit.

- `Grid(columns, gap)` usa um número fixo de colunas, células de largura igual
  e altura uniforme por linha. A última linha pode ficar incompleta.
- `Spacer.fixed(width, height)` cria espaço sem desenho; `Spacer.fixed(size)`
  cria um espaçador quadrado.
- `Spacer.flex()` divide igualmente o espaço restante no eixo principal de um
  `Row` ou `Column`; em outros contêineres mede como vazio.

Exemplo:

```java
Row actions = new Row(8);
actions.add(new Button(Component.literal("Voltar"), this::back));
actions.add(Spacer.flex());
actions.add(new Button(Component.literal("Salvar"), this::save));

Grid palette = new Grid(6, 6);
```

### Integração com itens vanilla

**Estado:** `ItemRenderer`, `ItemCard` e `ItemGrid` implementados.

- `ItemRenderer` delega desenho de `ItemStack`, contagem e tooltip às APIs
  vanilla de `GuiGraphics`.
- `ItemCard` é um cartão focável e selecionável que guarda uma cópia do stack,
  mostra o nome e a contagem do item e recebe ativação por mouse, Enter ou
  Espaço.
- `ItemGrid(columns, gap)` oferece `addItem(stack)` e `addItem(stack, label)`;
  seu callback recebe o índice e uma cópia do item clicado.
- `ItemCard.stack()` também devolve uma cópia para que consumidores não alterem
  acidentalmente o estado interno do componente.

Exemplo:

```java
ItemGrid items = new ItemGrid(4, 6, (index, stack) -> selectItem(index, stack));
items.addItem(new ItemStack(Items.CHEST));
items.addItem(new ItemStack(Items.HOPPER), Component.literal("Entrada"));
```

O toolkit continua sem importar classes do domínio do mod. A API recebe stacks
já construídos pelo consumidor e mantém callbacks Java somente em memória.

### Toast notifications

**Estado:** `Toast` e `ToastHost` implementados e anexados a cada `UiScreen`.

- `ToastHost` oferece `show(Toast)`, atalhos por nível e remove avisos após o
  número configurado de ticks enquanto a tela está ativa.
- Até quatro avisos são empilhados no canto superior direito; mensagens são
  quebradas em até três linhas e o layout se recalcula após resize/expiração.
- Clique esquerdo dentro de um aviso o dispensa e é consumido; cliques fora
  continuam chegando à árvore principal.
- O playground tem um botão Toast que percorre INFO, SUCCESS, WARNING e ERROR.

### Table

**Estado:** `Table<T>` e `TableColumn<T>` implementados e disponíveis na paleta
do playground.

- Colunas definem título, largura fixa ou medida pelo conteúdo, alinhamento e
  renderer de `Component`; linhas e seleção pertencem ao consumidor.
- Cabeçalho fixo, linhas uniformes, hover, seleção única, callback de clique e
  rolagem vertical pelo wheel. Células são recortadas para respeitar os limites.
- O playground demonstra três colunas e linhas de exemplo; o inspector aceita
  cabeçalhos separados por vírgula e quantidade de colunas configurável.

### ProgressBar

**Estado:** implementado e disponível na paleta do playground.

- Progresso determinado em qualquer faixa finita; valores são limitados ao
  intervalo e faixas inválidas/invertidas renderizam vazias.
- Altura da barra e cores do trilho/preenchimento são configuráveis; formatter
  opcional recebe o valor atual e fornece um `Component` centralizado.
- `setPercentage(true)` exibe o progresso normalizado como porcentagem inteira
  de `0%` a `100%`; no playground, o modo padrão exibe o valor inteiro
  arredondado, e o inspetor alterna entre os dois formatos.
- Não captura mouse nem teclado e não usa timer ou animação própria.
- No playground, o inspetor aceita `mínimo, máximo, valor` e o preview mostra
  o valor atual formatado.

### Etapa 4 — Validar com um showcase pequeno

**Estado:** implementada como playground editável, acessível por F7 no ambiente
de desenvolvimento. Layouts podem ser salvos como JSON em
`config/r_ui_lib/ui-playground/`; os detalhes de uso estão em
[Playground do UI Toolkit](ui-toolkit-playground.md). A validação visual em
execução do cliente e do servidor dedicado continua pendente.

- Criar uma tela de desenvolvimento temporária usando apenas a API pública: título, painéis/cartões, texto, layout e botões.
- Validar resize, GUI scales 1–4, janela pequena e resolução comum; conferir que foco, hover e clique continuam alinhados após resize.
- Confirmar que a classe principal do mod não referencia tipos client-only e que o servidor dedicado inicializa sem carregar a UI.
- Remover ou restringir o ponto de entrada do showcase antes de tratá-lo como recurso de jogador.

**Critério de saída:** a tela demonstra os primitives sem posicionamento manual na aplicação, conserva comportamento após resize/GUI scale e não introduz classes da biblioteca no caminho de inicialização do servidor.

## Roadmap depois do primeiro checkpoint

`ItemRenderer`, `ItemCard` e `ItemGrid` estão na API; `Grid`, `Spacer`,
`ItemCard` e `ItemGrid` também estão disponíveis na paleta, no modelo JSON e na
fábrica do playground. Toast e ToastHost estão integrados ao `UiScreen` e há
um botão de demonstração no playground. Table, ProgressBar e RadialMenu estão
disponíveis na paleta. A ordem priorizada e o plano técnico mínimo dos demais itens estão em
[Minecraft UI Toolkit PRD](minecraft-ui-toolkit-prd.md), seção 63. Radial menu
foi concluído como o quarto marco; o próximo é Dropdown / Select. Implementar e
revisar um marco por vez e começar abstrações especulativas somente quando
houver consumidor concreto.

## Fora deste corte

- Flexbox/CSS, parser de estilos ou layouts declarativos;
- virtualização, drag-and-drop geral, animações complexas ou state management;
- integração completa com `AbstractContainerScreen`;
- Fabric, Forge 1.20.1 ou abstração cross-loader;
- dependência runtime de framework de UI de terceiros;
- publicação em uma plataforma de mods ou em um repositório Maven remoto específico; o projeto deixa os artefatos de release prontos e aceita um destino Maven configurado por ambiente.

## Riscos e decisões a revisitar

- **Ciclo de input e foco:** conciliar a árvore própria com o ciclo de `Screen` sem duplicar eventos nem perder compatibilidade com widgets vanilla.
- **Escala e clipping:** validar a convenção de coordenadas antes de implementar clipping aninhado ou scrollbar.
- **Limite da API pública:** manter pequeno o conjunto exportado e mover renderers/helpers internos para `internal`.
- **Consumo em produção:** mods que usam a API declaram R-Ui Lib como dependência client-side e distribuem sua instalação separadamente.
- **Publicação remota:** a coordenada Maven está definida como `com.radaeli.ruilib:r-ui-lib-neoforge`; o endpoint de publicação é configurado pelo consumidor do repositório.

