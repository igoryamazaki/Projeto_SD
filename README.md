# Sistema de Vagas Distribuído

Este projeto é um sistema de vagas distribuído desenvolvido em Java para a disciplina de Sistemas Distribuídos. Ele permite que candidatos e empresas realizem várias operações relacionadas a vagas de emprego.

## Tecnologias Utilizadas
- Java Socket
- JWT (JSON Web Tokens)
- SQLite (para armazenamento de dados)
- JSON Simple (para manipulação de dados em formato JSON)
- Interface gráfica em Swing
- IntelliJ IDEA para desenvolvimento do projeto

## Funcionalidades para Candidato:

### CRUD de Cadastro:
- **Cadastrar Usuário**: Permite que um usuário comum se registre fornecendo nome, e-mail e senha.
- **Atualizar Cadastro**: Permite que o usuário edite suas informações pessoais.
- **Ler Dados do Próprio Cadastro**: Exibe os dados do usuário logado.
- **Apagar Cadastro**: Permite que o usuário exclua sua conta.

### CRUD de Competências:
- **Cadastrar Competências e Experiências**: O usuário pode adicionar suas habilidades e experiências profissionais.
- **Atualizar Competências e Experiências**: Permite que o usuário edite suas competências e experiências.
- **Apagar Competências e Experiências**: Remove competências e experiências do perfil.
- **Login e Logout**: Autenticação no sistema.

### Busca de Vagas:
- **Filtrar Vagas**: O usuário pode buscar vagas usando filtros como competências e experiência.

## Funcionalidades para Empresas:

### CRUD de Cadastro:
- **Cadastrar Empresa**: Permite que uma empresa se registre fornecendo nome, ramo, descrição, e-mail e senha.
- **Atualizar Cadastro**: Permite que a empresa edite suas informações.
- **Ler Dados do Próprio Cadastro**: Exibe os dados da empresa logada.
- **Apagar Cadastro**: Permite que a empresa exclua sua conta.

### CRUD de Vagas:
- **Cadastrar Vagas**: A empresa pode adicionar informações sobre vagas disponíveis no servidor.
- **Atualizar Vagas**: Permite que a empresa atualize os detalhes das vagas.
- **Ler Dados das Próprias Vagas**: Exibe informações sobre as vagas cadastradas pela empresa.
- **Apagar Vagas**: Remove vagas do servidor.

### Oferta de Vagas:
- **Marcar Vaga como Disponível**: Indica que uma vaga está aberta para candidaturas.
- **Marcar Vaga como “Divulgável”**: Permite que a vaga seja divulgada publicamente.
- **Busca por Candidatos**:
  - A empresa envia um perfil desejado para o servidor.
  - O servidor retorna uma lista de candidatos que se encaixam no perfil.
  - É possível filtrar os resultados por competências e experiência.
  - A empresa pode enviar mensagens para os candidatos selecionados.

## Como Executar
1. Clone o repositório:
2. Abra o projeto no IntelliJ IDEA.
3. Compile e execute o código.
