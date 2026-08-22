# CardFlow - Gerenciador de Cartões

Projeto Android feito em Kotlin com Jetpack Compose, Navigation Compose, MVVM, Firebase Authentication e Cloud Firestore. Cartões, compras, limite e bloqueio são persistidos por usuário.

O visual segue o protótipo de alta fidelidade fornecido: azul profundo no login, botões azul/roxo, cartões com degradê, superfícies claras, ações rápidas e navegação inferior com quatro itens.

## Como abrir

1. Extraia o ZIP e abra a pasta raiz no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Use um emulador ou aparelho com Android 7.0 (API 24) ou superior.
4. No Firebase Console, crie um app Android com o pacote `com.treinamento.gerenciadordecartoes`.
5. Ative **Authentication > Sign-in method > E-mail/senha**.
6. Crie um banco **Cloud Firestore** e publique as regras do arquivo `firestore.rules`.
7. Baixe `google-services.json` e coloque o arquivo dentro da pasta `app/`.
8. Execute a configuração `app`.

Requisitos de ambiente: Android Studio atual, JDK 17 e Android SDK 36. Cadastro e login usam contas reais do Firebase Authentication; a senha precisa ter ao menos 6 caracteres.

## Telas

1. **Login** - autenticação real por e-mail/senha com Firebase e estado de carregamento.
2. **Meus Cartões** - lista reutilizando `CardItem` e navegação inferior.
3. **Detalhes do Cartão** - resumo e aba **Compras**, sem criar uma sexta tela.
4. **Solicitar Cartão** - formulário e envio simulado.
5. **Gerenciar Cartão** - bloqueio/desbloqueio e alteração de limite.
6. **Perfil** - nome e e-mail do usuário autenticado, com encerramento da sessão.

## Arquitetura

```text
com.treinamento.gerenciadordecartoes
├── data/          MockCardRepository e contrato de fonte local
├── model/         Card, Purchase e CardRequest
├── navigation/    rotas e NavHost
├── repository/    contrato CardRepository
├── state/         estados imutáveis da interface
├── view/
│   ├── components/ componentes reutilizáveis
│   ├── screens/    cinco telas Compose
│   └── theme/      cores e tema
├── util/          formatação monetária
└── viewmodel/      regras de apresentação e StateFlow
```

Fluxo de dados: a tela envia uma ação ao `CardViewModel`; o ViewModel chama `CardRepository`; o repositório atualiza os dados; os `StateFlow`s expõem um novo estado imutável para o Compose.

Componentes reaproveitados: `AppButton`, `CardItem`, `TopBar` e `BottomNavigation`. O estado é observado com `collectAsStateWithLifecycle`.

## Dados desta versão

Na primeira autenticação de cada usuário, `FirebaseCardDataSource` carrega no Firestore os cartões e compras definidos em `MockCardData`. A partir daí, alterações de limite, bloqueio e novos cartões são persistidas em `users/{uid}/cards`. Os dados são didáticos e não representam transações financeiras reais.

## Próximos passos

### Persistência local

- Adicionar Room e criar entidades `CardEntity` e `PurchaseEntity`.
- Criar DAOs e uma implementação de `CardDataSource`.
- Substituir `MockCardRepository` por um repositório que combine a fonte local e os mapeadores.
- Opcionalmente usar DataStore para sessão e preferências simples.

### API

- Criar uma interface de serviço HTTP e DTOs separados dos modelos de domínio.
- Implementar uma fonte remota e injetá-la no repositório.
- Tratar carregamento, erro, cache e sincronização.
- Nunca armazenar número completo, CVV ou senha sem requisitos reais de segurança.

### Evoluções simples

- Adicionar injeção de dependência (Hilt/Koin) apenas quando o projeto crescer.
- Criar testes unitários para o ViewModel e repositório.
- Adicionar acessibilidade, máscaras de campo e validações mais completas.

## Tecnologias

- Kotlin 2.2.20
- Android Gradle Plugin 8.12.2 e Gradle 8.13
- Jetpack Compose (BOM 2026.04.01) e Material 3
- Navigation Compose
- ViewModel, StateFlow e Lifecycle Compose
- Firebase Authentication (e-mail e senha)
- Cloud Firestore (cartões, compras, limite e bloqueio)

O projeto segue o requisito acadêmico de Compose + navegação, MVVM visível, cinco telas e preparação para futura persistência/API, mantendo a primeira entrega propositalmente pequena.
