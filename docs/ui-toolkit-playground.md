# Playground do UI Toolkit

O playground é uma ferramenta de desenvolvimento para experimentar os componentes do toolkit dentro do Minecraft.

## Abrir

- Inicie o cliente pelo `runClient` e pressione **F7**.
- A tecla pode ser alterada em **Opções → Controles → Easy Block Pallets**.
- A tela só é registrada em execução de desenvolvimento. Ela não fica disponível no mod instalado em produção.
- **Toast** percorre os quatro níveis de notificação. Avisos no canto superior direito somem após quatro segundos; clique esquerdo sobre um aviso o dispensa.
- **Table** traz linhas de exemplo, seleção e rolagem vertical. No inspector, edite os cabeçalhos separados por vírgula e o número de colunas.
- **ProgressBar** mostra progresso determinado. No inspetor, ajuste o range como `mínimo, máximo, valor`; a API permite altura, cores do trilho/preenchimento e formatter opcionais.
- **RadialMenu** começa com quatro segmentos. No inspetor, edite os rótulos separados por vírgulas; no Preview, pressione, arraste até um setor e solte para disparar a ação de demonstração. A área central cancela a seleção.

## Montar um layout

- Arraste um tipo da paleta à esquerda para o canvas. Ela contém `Label`, `Button`, `Toggle`, `Chip`, `TextField`, `SearchField`, `Slider`, `ProgressBar`, `RadialMenu`, `ItemCard`, `Tabs`, `ScrollView`, `ClipStack`, `Row`, `Column`, `Stack`, `Grid`, `ItemGrid`, `Table`, `Spacer` e `AbsoluteLayout`. A lista rola com a roda do mouse em janelas baixas. `ItemRenderer` é um helper exercitado por `ItemCard`, não um nó separado.
- Selecione um nó para editar seu texto, cor, posição, dimensões, espaçamento e alinhamento no inspetor; pressione **Apply** para aplicar os campos. Arraste o canto inferior direito para redimensionar; **Delete** ou **Remove** exclui o nó.
- Solte um componente sobre `Row`, `Column` ou `Stack` para aninhá-lo. O contêiner passa a definir a posição dos filhos; X/Y ficam inativos e largura/altura continuam ajustáveis.
- Solte `ItemCard` sobre `Grid` ou `ItemGrid` para compor uma grade. O inspetor edita as colunas e o espaçamento da grade, e o ID namespaced e a quantidade do item no cartão. `Spacer` pode ser fixo (ajuste Width/Height) ou flexível (botão Flex no inspetor), preenchendo o espaço livre em `Row`/`Column`.
- Arraste um filho para uma área vazia do canvas para colocá-lo de volta no `AbsoluteLayout`.

## Testar e salvar

- Alterne entre **Preview** e **Edit** na barra superior. No Preview, a paleta e o inspetor desaparecem e o canvas ocupa a largura disponível; a barra superior continua acessível para voltar à edição ou fechar. Botões, toggles e chips respondem ao mouse, Tab/Shift+Tab, Enter e Espaço. `Tabs` troca de página por clique e pelas setas esquerda/direita quando focado. As interações atualizam o status/contador visível.
- Solte filhos dentro de `Tabs` para criar páginas; o texto do primeiro filho funciona como título da aba. Adicione duas ou mais páginas para navegar entre elas.
- Campos de texto aceitam edição no Preview; `TextField` e `SearchField` mantêm o valor salvo no layout. O campo de busca exibe uma lupa, e ambos centralizam o texto verticalmente com pelo menos 8 px de padding à esquerda. `ScrollView` empilha os filhos verticalmente, rola pela roda do mouse e permite arrastar a scrollbar. `ClipStack` sobrepõe os filhos e recorta qualquer desenho fora do contêiner.
- `Slider` pode ser ajustado por arraste ou teclado. No inspetor, edite a faixa como `mínimo, máximo, passo`; use passo `0` para valores contínuos.
- `ProgressBar` é somente visual e mostra o valor atual formatado; seus valores podem ser editados como `mínimo, máximo, valor`.
- Digite um nome na caixa superior e use **Save** para gravar em `config/easy_block_pallets/ui-playground/` dentro da pasta de jogo. **Load** abre uma lista dos layouts salvos; selecione um item e confirme.
- Ao abrir o playground, o último layout salvo é carregado automaticamente. Se layouts antigos não tiverem registro de último salvamento, o arquivo JSON modificado mais recentemente é usado.
- **New** cria um documento vazio. Os callbacks de demonstração são recriados localmente e nunca são gravados no JSON.
- Erros de arquivo ou de formato aparecem na barra inferior da tela.

Os documentos usam `version: 1`; o playground rejeita versões desconhecidas e layouts estruturalmente inválidos sem fechar o jogo.
