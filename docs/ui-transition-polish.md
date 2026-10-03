# Transições visuais do UI Toolkit

## Base global

As animações usam `UiTokens.motion`, com duração padrão de **140 ms** e easing `EASE_IN_OUT`. Um tema pode configurar a duração e escolher `LINEAR`, `EASE_IN_OUT` ou `EASE_OUT`; duração zero desativa as transições. O acessor `UiTokens.transitionDurationMillis()` permanece disponível por compatibilidade. As transições usam o relógio monotônico compartilhado pelo `UiContext` de cada frame.

Estados de interação começam no valor inicial do componente. Desabilitar um controle mantém resposta imediata; hover, foco e mudanças de seleção usam a duração do tema.

## Decisão por componente

| Componente | Transição |
|---|---|
| `AbsoluteLayout` | Nenhuma: posiciona filhos sem estado visual próprio. |
| `Button` | Cor entre repouso, hover e pressionado; contorno de foco. |
| `Card` | Fundo entre repouso, hover e selecionado; contorno de foco. |
| `Chip` | Fundo de hover e seleção; contorno de foco. |
| `ClipStack` | Nenhuma: recorta os filhos; cada filho anima seu próprio estado. |
| `Column` | Nenhuma: layout estático. |
| `Divider` | Nenhuma: separador estático. |
| `Grid` | Nenhuma: layout estático. |
| `IconButton` | Igual ao `Button`, preservando ícone e tooltip nativos. |
| `ItemCard` | Fundo de hover, pressão e seleção. |
| `ItemGrid` | Nenhuma própria: os `ItemCard` filhos animam individualmente. |
| `Label` | Nenhuma: texto informativo estático. |
| `Modal` | Fundo escurecido, moldura e conteúdo interno entram e saem com a mesma opacidade; foco e input ficam estáveis até o fade de saída terminar. |
| `Panel` | Nenhuma: superfície estrutural. |
| `ProgressBar` | Preenchimento interpola até o novo progresso. |
| `RadialMenu` | Realce do segmento sob o ponteiro e fade ao abrir/fechar. |
| `Row` | Nenhuma: layout estático. |
| `ScrollView` | Conteúdo acompanha a rolagem suavemente; indicador fica mais visível ao interagir. |
| `SearchField` | Herda transições de hover e foco de `TextField`; lupa permanece estável. |
| `Slider` | Indicador e leitura acompanham mudanças programáticas; arraste segue o ponteiro sem atraso. Hover e foco também transitam. |
| `Spacer` | Nenhuma: apenas reserva espaço. |
| `Stack` | Nenhuma: layout estático. |
| `Table` | Realce de hover/seleção por linha e rolagem suave. |
| `Tabs` | Fundo, texto, contorno e indicador de seleção transitam; a página ativa troca imediatamente. |
| `TextField` | Fundo e contorno transitam em hover/foco; cursor, seleção e IME seguem o `EditBox` vanilla. |
| `Toast` | É apenas o descritor da notificação; a apresentação é animada pelo host. |
| `ToastHost` | Entrada e saída com fade e pequeno deslocamento lateral. |
| `Toggle` | A alavanca desliza entre posições; trilha e foco transitam. |
| `Tooltip` | Mantém o pipeline de tooltip vanilla, sem animação própria nesta etapa. |

## Infraestrutura

`UiComponent` guarda o progresso dos estados animados por instância. `UiPaint` interpola cores e opacidade sem duplicar lógica de easing nos componentes. Componentes sem estado visual próprio permanecem imediatos; seus filhos interativos continuam usando o mesmo token global.
