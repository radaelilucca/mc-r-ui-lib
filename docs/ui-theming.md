# Temas e tokens visuais

R-Ui Lib fornece os temas `UiTheme.DARK` e `UiTheme.LIGHT`. Um mod consumidor
pode escolher o padrão global uma vez durante a inicialização client-side:

```java
UiThemes.setGlobalTheme(UiTheme.LIGHT);
```

Telas criadas sem tema explícito e todos os componentes que recebem o contexto
dessas telas passam a consumir essa configuração. O valor global pode ser
atualizado em runtime; telas abertas atualizam o layout na próxima renderização.
Uma tela pode manter um tema próprio pelo construtor `UiScreen(title, root,
theme)`. Esse tema substitui o padrão global só naquela tela.

## Definição manual

`UiPalette` é um record público com um argumento para cada cor semântica, em
ordem ARGB:

| Token | Uso |
| --- | --- |
| `screen` | Fundo de tela |
| `panel`, `card` | Superfícies de painéis e cartões |
| `border`, `divider` | Contornos e separadores |
| `text`, `mutedText` | Texto principal e secundário |
| `accent` | Seleção, progresso e destaque |
| `button`, `buttonHover`, `buttonPressed` | Estados de botão |
| `disabled`, `focus` | Estado desabilitado e foco |
| `overlay` | Fundo atrás de modais |
| `success`, `warning`, `error` | Estados de toast |

Os outros tokens de aparência são agrupados em `UiTokens`:

- Espaçamento: `panelPadding`, `cardPadding`, `buttonPadding` e os gaps pequeno,
  médio e grande.
- Tamanho: largura e altura mínimas dos botões.
- Tipografia: escalas `small`, `body`, `heading` e `title`.
- Bordas: raios pequeno, médio, grande e pill.
- Animação: duração padrão e easing (`LINEAR`, `EASE_IN_OUT` ou `EASE_OUT`).
  Duração zero desativa as transições.

Exemplo que mantém as cores escuras e troca os demais tokens:

```java
UiTokens customTokens = new UiTokens(
        Insets.all(12), Insets.all(8), 4, 8, 16,
        Insets.symmetric(12, 6), 72, 22,
        new UiTypography(0.9f, 1.0f, 1.2f, 1.35f),
        new UiRadii(2, 4, 8, 999),
        new UiMotion(180, UiEasing.EASE_OUT));
UiThemes.setGlobalTheme(new UiTheme(UiTheme.DARK.palette(), customTokens));
```

Para definir todas as cores manualmente, passe os 17 valores ao construtor de
`UiPalette` e use esse record na criação do tema:

```java
UiPalette colors = new UiPalette(
        screen, panel, card, border, divider, text, mutedText, accent,
        button, buttonHover, buttonPressed, disabled, focus,
        overlay, success, warning, error);
UiThemes.setGlobalTheme(new UiTheme(colors, customTokens));
```

## Customização pontual

Passe outro `UiTheme` ao construtor de uma tela para isolá-la do padrão global.
Os componentes também mantêm seus overrides locais, como cor em `Label` e
`Divider`, cores em `ProgressBar` e padding explícito em `Panel`/`Card`. Sem override, seus
valores padrão vêm do tema ativo, inclusive quando a tela usa um tema global
customizado.
