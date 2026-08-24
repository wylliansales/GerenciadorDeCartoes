# CardFlow — Gerenciador de Cartões

Aplicativo Android desenvolvido como projeto final do curso de Android. O CardFlow reúne autenticação, cadastro, consulta e gerenciamento de cartões e compras em uma única jornada, com persistência local e sincronização em nuvem.

O projeto utiliza uma arquitetura MVVM offline-first: os dados são gravados primeiro no Room/SQLite e sincronizados com o Cloud Firestore quando existe conexão. Dessa forma, consulta, cadastro de cartão, lançamento de compra, alteração de limite e bloqueio continuam funcionando sem internet.

## Integrantes

- Rogerio Nunes
- Rodrigo Silva
- Wyllian
- Laura

## Solução implementada

O CardFlow permite que cada usuário possua uma conta e acesse somente os próprios dados. A autenticação é realizada pelo Firebase Authentication e os cartões são organizados no Firestore pelo `uid` do usuário.

Principais funcionalidades:

- Cadastro de usuário com nome, e-mail e senha.
- Login real com Firebase Authentication.
- Manutenção da sessão entre execuções do aplicativo.
- Perfil com nome e e-mail do usuário autenticado.
- Logout com limpeza da pilha de navegação.
- Consulta de cartões e respectivos limites.
- Consulta das compras associadas a cada cartão.
- Cadastro de cartão fictício para o usuário autenticado.
- Formulário de cartão com nome do cartão, nome do cliente, número fictício, CVC fictício, vencimento e limite total.
- Lançamento manual de compra com estabelecimento, data, valor e categoria.
- Validação do limite disponível e do status do cartão antes de registrar a compra.
- Dedução automática do valor da compra no limite disponível e atualização da fatura.
- Alteração do limite total.
- Bloqueio temporário, bloqueio definitivo e desbloqueio.
- Atualização das telas por meio de estado reativo.
- Persistência local com Room/SQLite.
- Sincronização dos dados com Cloud Firestore.
- Fila local para operações realizadas sem internet.
- Acesso à tela **Lançar compra** pela opção **Nova compra** nos detalhes do cartão.
- Exibição do bloqueio temporário ou definitivo no cartão e atualização do estado no Room e no Firebase.

Todos os dados de cartão, número e CVC usados no projeto devem ser fictícios. O aplicativo tem finalidade exclusivamente didática, não possui integração com administradoras e não realiza transações financeiras reais.

## Telas

1. **Login** — autenticação por e-mail e senha.
2. **Cadastro** — criação da conta e do perfil do usuário.
3. **Meus Cartões** — lista de cartões, limites e status.
4. **Detalhes do Cartão** — limite total, limite disponível, fatura, compras e acesso às ações de gerenciamento.
5. **Cadastrar Cartão** — cadastro de um cartão fictício com nome, titular, número, CVC, vencimento e limite para o usuário autenticado.
6. **Lançar Compra** — registro manual do estabelecimento, data, valor e categoria para o cartão selecionado.
7. **Gerenciar Cartão** — alteração de limite, bloqueio temporário, bloqueio definitivo e desbloqueio.
8. **Perfil** — dados do usuário autenticado e logout.

## Arquitetura

O projeto segue MVVM com Repository Pattern e separação entre fontes de dados local e remota.

```text
Jetpack Compose
      │ ações / eventos
      ▼
CardViewModel
      │ StateFlow
      ▼
OfflineFirstCardRepository
      ├── RoomCardDataSource
      │     ├── CardDao
      │     ├── CardDatabase
      │     └── pending_operations
      │
      └── FirebaseCardDataSource
            ├── Firebase Authentication
            └── Cloud Firestore
```

### Camada de apresentação

- Telas e componentes construídos com Jetpack Compose e Material 3.
- Navegação centralizada no `CardManagerApp` com Navigation Compose.
- Estados imutáveis expostos pelo `CardViewModel` usando `StateFlow`.
- Observação consciente do ciclo de vida com `collectAsStateWithLifecycle`.

### Camada de domínio

- Modelos `Card`, `Purchase`, `CardRequest` e `AuthenticatedUser`.
- `CardBlockStatus` representa `ACTIVE`, `TEMPORARY_BLOCKED` e `PERMANENTLY_BLOCKED`.
- Interfaces `CardRepository` e `AuthRepository` definem os contratos usados pelo ViewModel.

### Camada de dados local

Room/SQLite é a fonte de verdade da interface. A UI lê os fluxos emitidos pelos DAOs, garantindo resposta imediata mesmo sem conexão.

Tabelas:

- `cards` — cartões separados por usuário.
- `purchases` — compras associadas aos cartões.
- `pending_operations` — fila persistente de sincronização.

Operações offline suportadas:

- `UPSERT_CARD`
- `UPDATE_LIMIT`
- `UPDATE_BLOCK_STATUS`
- `UPSERT_PURCHASE`

### Camada de dados remota

O Firebase Authentication identifica o usuário. O Cloud Firestore armazena os dados na seguinte estrutura:

```text
users/{uid}/cards/{cardId}
users/{uid}/cards/{cardId}/purchases/{purchaseId}
```

As regras de `firestore.rules` permitem que cada usuário leia e altere somente documentos localizados no próprio `uid`.

### Estratégia offline-first

1. Uma alteração é salva primeiro no Room.
2. A interface recebe imediatamente o novo estado pelo `Flow` do DAO.
3. A operação é registrada em `pending_operations`.
4. O repositório tenta enviar a alteração ao Firestore.
5. Se não houver internet, a operação permanece no SQLite.
6. Quando a conexão retorna ou o app é reaberto, a fila é processada novamente.
7. Após a confirmação remota, a operação é removida da fila.

## Organização dos pacotes

```text
com.treinamento.gerenciadordecartoes
├── data
│   ├── local       entidades Room, DAO, banco e fonte local
│   └── remote      entidades Firestore e fonte remota
├── model           modelos de domínio
├── navigation      rotas e NavHost
├── repository      contratos e repositórios
├── state           estados imutáveis da interface
├── util            formatação monetária
├── view
│   ├── components  componentes reutilizáveis
│   ├── screens     telas Compose
│   └── theme       tema, cores e tipografia
└── viewmodel       regras de apresentação e StateFlow
```

## Tecnologias utilizadas

- Kotlin 2.2.20
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1
- JDK 17
- Android SDK 36
- API mínima 24 — Android 7.0
- Jetpack Compose
- Compose BOM 2026.04.01
- Material 3
- Navigation Compose 2.9.5
- ViewModel e StateFlow
- Lifecycle Compose
- Firebase BOM 34.18.0
- Firebase Authentication
- Cloud Firestore
- Room 2.8.4
- SQLite
- Kotlin Coroutines e Flow

## Componentes reutilizáveis

- `AppButton`
- `CardItem`
- `TopBar`
- `BottomNavigation`

## Configuração do projeto

### Requisitos

- Android Studio compatível com AGP 9.2.1.
- JDK 17.
- Android SDK 36.
- Emulador ou aparelho com Android 7.0 ou superior.
- Projeto criado no Firebase.

### Firebase Authentication

1. Crie um aplicativo Android no Firebase usando o pacote:

   ```text
   com.treinamento.gerenciadordecartoes
   ```

2. Ative **Authentication → Método de login → E-mail/senha**.
3. Baixe `google-services.json`.
4. Coloque o arquivo dentro da pasta `app/`.

### Cloud Firestore

1. Crie um banco Cloud Firestore na edição Standard.
2. Abra a aba **Regras**.
3. Copie o conteúdo do arquivo `firestore.rules`.
4. Clique em **Publicar**.

### Execução

1. Abra a pasta raiz no Android Studio.
2. Sincronize os arquivos Gradle.
3. Selecione um emulador ou aparelho.
4. Execute a configuração `app`.

O banco local `cardflow.db` é criado automaticamente pelo Room na primeira execução.

## Teste do funcionamento offline

1. Entre no aplicativo conectado à internet.
2. Ative o modo avião.
3. Altere o limite ou o status de um cartão.
4. Solicite um novo cartão.
5. Feche e abra o aplicativo ainda offline.
6. Confirme que as alterações continuam disponíveis.
7. Desative o modo avião.
8. Confira a sincronização no Cloud Firestore.

## Próximas evoluções

- Executar a fila de sincronização em segundo plano com WorkManager.
- Implementar política explícita de resolução de conflitos entre dispositivos.
- Adicionar testes unitários para ViewModel, DAOs e repositórios.
- Adicionar testes instrumentados do Room e das telas Compose.
- Implementar injeção de dependência com Hilt ou Koin.
- Melhorar acessibilidade e validação dos formulários.
- Adicionar monitoramento de conectividade e indicador visual de sincronização.
