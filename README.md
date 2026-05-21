# RSS Bubble

RSS Bubble é um aplicativo Android desenvolvido em Kotlin que permite assinar feeds RSS e exibir notificações de notícias flutuantes na tela do seu dispositivo, mesmo que o aplicativo não esteja aberto.

## 🚀 Funcionalidades

* **Assinatura de Feeds RSS**: Adicione qualquer URL de feed RSS para acompanhar suas notícias.
* **Sugestões Embutidas**: Assine com um toque feeds famosos e pré-selecionados de tecnologia e notícias locais (Brasil) como G1, UOL, Jovem Nerd, além de fontes globais em inglês (BBC, The Verge, NYT).
* **Resumos Inteligentes**: Integração com a API do Google Gemini para compactar e resumir notícias antes da exibição, facilitando a leitura de tópicos extensos.
* **Balões Flutuantes (Overlay)**: Quando uma nova notícia do feed é identificada em segundo plano, um balão interativo é exibido sobre outros aplicativos na sua tela, contendo o título e o resumo da notícia.
* **Sincronização em Segundo Plano**: O app vasculha ativamente seus feeds subscritos usando `WorkManager` do Android, consumindo um mínimo de energia.
* **Botão de Teste Rápido**: Praticidade para verificar se as permissões de Tela Flutuante estão corretas antes mesmo da sua primeira notícia chegar.

## 🛠 Tecnologias Utilizadas

* **Linguagem**: Kotlin
* **UI**: Jetpack Compose com Material Design 3
* **Persistência Local**: Room Database para manter os feeds assinados e um histórico local das notícias geradas.
* **Comunicação Web**: Configuração com `Retrofit` e OkHttp para puxar o XML do RSS.
* **Parsers em XML**: Trabalhador nativo para processar os nós `<item>`, `<title>`, `<description>` e afins do protocolo XML/RSS.
* **Inteligência Artificial**: Consumo rest via `GeminiApiService` da LLM Gemini-1.5-Flash para gerar sumarização sem ocupar memória do celular.
* **Tarefas Agendadas**: `WorkManager` com políticas consistentes de rede conectada e tempo fixo de execução para puxar os conteúdos em _background_.

## ⚙️ Configuração Adicional Necessária

Para funcionar plenamente, existem duas exigências:

1. **Permissão de Sobreposição**: Na primeira inicialização, o aplicativo pedirá permissão de "Desenhar sobre outros aplicativos" (System Alert Window). Siga os menus na tela para concedê-la.
2. **Chave de API do Google Gemini**:
    O aplicativo usa a variável de ambiente `GEMINI_API_KEY`. Certifique-se de configurar essa chave usando o painel **Secrets** no AI Studio para injetar a chave segura e gerar os resumos das notícias.

## 📱 Como Usar

1. Abra o aplicativo e concorde em ceder a permissão para Sobreposição no botão em vermelho destacado no começo.
2. Nas **"Sugestões Famosas"**, toque em qualquer _chip_ para subscrever a um canal conhecido.
3. Se desejar, adicione suas próprias URLs de RSS.
4. Pressione "Sincronizar" para buscar imediatamente, ou apenas aguarde: o aplicativo faz isto sozinho em intervalos de aproximadamente 15 minutos em plano de fundo.
5. Quando os balões abrirem, sinta-se à vontade para movê-los na tela arrastando o ícone, ou tocando para abrir o Card de detalhes com os textos e opção de fechá-lo!
