# Discord 2

App de mensagens com sistema de amigos (pedido/aceitar/recusar) e chat em tempo real,
usando Firebase Authentication + Firestore.

## Passo obrigatório antes de compilar

Esse projeto **não vai compilar** sem um arquivo `google-services.json` próprio, porque
ele contém as chaves do SEU projeto Firebase (não dá pra gerar isso sem você criar a conta).

1. Acesse https://console.firebase.google.com e crie um projeto novo (gratuito).
2. Em **Build > Authentication**, ative o provedor **E-mail/senha**.
3. Em **Build > Firestore Database**, crie o banco (modo produção ou teste, tanto faz pra começar).
4. Em **Configurações do projeto > Seus apps**, adicione um app Android com o pacote:
   `com.discord2.app`
5. Baixe o arquivo `google-services.json` gerado e coloque em `app/google-services.json`
   (na raiz da pasta `app`, do lado do `build.gradle` do app).
6. Nas regras do Firestore (aba "Regras"), pra testar rápido, pode usar temporariamente:
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
   (Isso libera leitura/escrita pra qualquer usuário logado — dá pra refinar depois.)

## Como gerar o .apk
- **Android Studio**: abra a pasta do projeto, sincronize o Gradle, `Build > Build APK(s)`.
- **GitHub Actions**: suba num repositório (o `google-services.json` também precisa estar
  lá, ou o CI vai falhar) e o workflow já configurado compila sozinho a cada push.

## Estrutura
- `LoginActivity` — login e cadastro (Firebase Auth)
- `MainActivity` — 3 abas: Amigos / Pedidos / Adicionar
- `ChatActivity` — conversa em tempo real com um amigo
- Firestore: `users`, `friendRequests`, `friends/{uid}/list`, `chats/{chatId}/messages`
