# Mini-Redis em Kotlin

Este projeto é uma implementação simplificada do Redis, um servidor de armazenamento de dados em memória, desenvolvido em Kotlin.

Para entender a arquitetura e as decisões de design por trás deste projeto, consulte o [Documentação de Arquitetura e Design](docs/ARCHITECTURE.md).

Para instruções detalhadas sobre como compilar e executar o projeto, incluindo requisitos e variáveis de ambiente, veja o [Guia de Instalação e Execução](docs/SETUP.md).

Para uma lista completa dos comandos Redis suportados e seus parâmetros, consulte a [Documentação de Comandos](docs/COMMANDS.md).

## Requisitos

* Consulte o [Guia de Instalação e Execução](docs/SETUP.md) para obter a lista completa de requisitos.

## Como Compilar

* Consulte o [Guia de Instalação e Execução](docs/SETUP.md) para obter instruções detalhadas sobre como compilar o projeto.

## Como Executar

* Consulte o [Guia de Instalação e Execução](docs/SETUP.md) para obter instruções detalhadas sobre como executar o projeto via Shell e Docker, incluindo variáveis de ambiente.

## Comandos Suportados

O Mini-Redis atualmente suporta os seguintes comandos:

* **Strings:** `GET`, `SET`
* **Listas:** `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LRANGE`
* **Expiração:** `EXPIRE`, `TTL`

Para obter detalhes sobre cada comando, incluindo seus parâmetros e retornos, consulte a [Documentação de Comandos](docs/COMMANDS.md).

**Para executar os testes funcionais:**

1.  Navegue até o diretório raiz do projeto no seu terminal.
2.  Certifique-se de que o script de testes `scripts/tests.sh` existe e tem permissão de execução. Se não tiver, você pode dar permissão com o seguinte comando:

    ```bash
    chmod +x scripts/tests.sh
    ```

3.  Execute o script com o seguinte comando:

    ```bash
    ./scripts/tests.sh
    ```

    O script irá mostrar o progresso dos testes e indicar se cada teste passou ou falhou.

## Como Executar Testes Unitários

1.  **Execute os testes com Gradle:**

    ```bash
    ./gradlew test
    ```

    Este comando irá executar todos os testes unitários do projeto e gerar um relatório na pasta `build/reports/tests/test`.

## Estrutura do Projeto

* `src/main/kotlin`: Contém o código fonte do projeto.
    * `infrastructure/handler`: Contém as classes responsáveis por lidar com as conexões de clientes (SocketServer, ConnectionManager, ClientProcessor).
    * `domain/redis`: Contém uma implementação simples, baseado no protocolo Redis (RedisProtocolHandler).
    * `domain/storage`: Contém a implementação do armazenamento de dados em memória (InMemoryStorage, ExpireManager).
    * `domain/command`: Contém a implementação dos comandos mini-redis (StringCommand, ListCommand, ExpireCommand).
    * `application`: Contém as interfaces das camadas de aplicação.
* `src/test/kotlin`: Contém os testes unitários do projeto.
* `build.gradle`: Arquivo de configuração do Gradle.
* `Dockerfile`: Arquivo de configuração para construir a imagem Docker (opcional).
* `README.md`: Este arquivo (visão geral).
* `docs`: Outros documentos do projeto

## Melhorias Futuras

* Implementar persistência de dados.
* Adicionar suporte para mais comandos Redis.

