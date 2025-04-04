# Guia de Instalação e Execução do Mini-Redis em Kotlin

Este guia detalha os passos necessários para instalar, compilar e executar o Mini-Redis em Kotlin, tanto diretamente no seu sistema quanto utilizando Docker.

## Requisitos

Antes de começar, certifique-se de ter os seguintes softwares instalados em seu sistema:

* **JDK (Java Development Kit) 11 ou superior:** Necessário para compilar e executar o projeto Kotlin. Você pode baixar uma versão adequada em [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) ou [OpenJDK](https://openjdk.java.net/install/).
* **Gradle:** Utilizado para construir e gerenciar as dependências do projeto. As instruções de instalação podem ser encontradas em [Gradle Installation](https://gradle.org/install/).
* **Docker (Opcional):** Necessário apenas se você pretende executar o Mini-Redis em um container Docker. Você pode baixar e instalar o Docker em [Docker Desktop](https://www.docker.com/products/docker-desktop/).

## Como Compilar

1.  **Clone o repositório:**

    Primeiro, clone o repositório do Mini-Redis para a sua máquina local utilizando o Git:

    ```bash
    git clone <URL_DO_SEU_REPOSITÓRIO>
    cd mini-redis
    ```

    Substitua `<URL_DO_SEU_REPOSITÓRIO>` pelo endereço do repositório do seu projeto.

2.  **Construa o projeto com Gradle:**

    Navegue até o diretório raiz do projeto (`mini-redis`) e execute o seguinte comando no seu terminal:

    ```bash
    ./gradlew clean build
    ```

    * `./gradlew clean`: Remove quaisquer arquivos de build anteriores.
    * `./gradlew build`: Compila o código fonte Kotlin, executa os testes unitários e empacota a aplicação.

    Após a conclusão bem-sucedida do build, um arquivo `.jar` "all" (contendo todas as dependências) será gerado na pasta `build/libs`. O nome do arquivo seguirá o padrão `mini-redis-<versão>-all.jar`, onde `<versão>` é a versão do seu projeto (por exemplo, `mini-redis-1.0.0-all.jar`).

## Como Executar

Você pode executar o Mini-Redis de duas maneiras: diretamente via linha de comando (usando o arquivo `.jar` gerado) ou utilizando Docker.

### Execução via Shell

1.  **Execute o arquivo `.jar` "all":**

    Abra um terminal, navegue até a pasta `build/libs` do seu projeto e execute o seguinte comando:

    ```bash
    java -jar mini-redis-<versão>-all.jar
    ```

    Substitua `<versão>` pela versão do seu projeto (por exemplo, `java -jar mini-redis-1.0.0-all.jar`).

    Por padrão, o servidor Mini-Redis será iniciado na porta `6379`.

2.  **Configuração via Variáveis de Ambiente:**

    Você pode personalizar a porta em que o servidor escuta e o tamanho do pool de threads utilizando variáveis de ambiente antes de executar o comando `java -jar`.

    * **`MINIREDIS_PORT`:** Define a porta do servidor (padrão: `6379`).
    * **`MINIREDIS_THREAD_POOL_SIZE`:** Define o número de threads para processar conexões (padrão: determinado pelo sistema).

    **Exemplo:** Para iniciar o servidor na porta `7000` com um pool de `16` threads:

    ```bash
    export MINIREDIS_PORT=7000
    export MINIREDIS_THREAD_POOL_SIZE=16
    java -jar mini-redis-<versão>-all.jar
    ```

3.  **Conectando-se ao Servidor:**

    Com o servidor em execução, você pode se conectar utilizando um cliente Redis ou uma ferramenta simples como `Netcat` (geralmente disponível em sistemas Unix-like).

    **Usando Netcat:**

    Abra outro terminal e execute o seguinte comando, substituindo a porta se você a alterou:

    ```bash
    nc localhost 6379
    ```

    Após a conexão, você pode começar a enviar comandos Redis suportados (consulte o `COMMANDS.md`).

    **Exemplo de interação com Netcat:**

    ```
    SET minha_chave minha_valor
    +OK
    GET minha_chave
    $10
    minha_valor
    ```

### Execução com Docker

Certifique-se de ter o Docker instalado e em execução no seu sistema.

1.  **Construa a imagem Docker:**

    Navegue até a raiz do seu projeto (`mini-redis`) e execute o seguinte comando para construir a imagem Docker. Assumimos que você possui um arquivo `Dockerfile` na raiz do projeto.

    ```bash
    docker build -t miniredis .
    ```

    * `docker build`: Comando para construir uma imagem Docker.
    * `-t miniredis`: Define o nome da imagem como `miniredis`.
    * `.`: Especifica o diretório atual como o contexto da build (onde o `Dockerfile` está localizado).

2.  **Execute o container Docker:**

    Para executar o servidor Mini-Redis em um container Docker, mapeando a porta `6379` do container para a porta `6379` da sua máquina host (você pode alterar a porta da host se desejar), use o seguinte comando:

    ```bash
    docker run -p 6379:6379 miniredis
    ```

    * `docker run`: Comando para executar um container a partir de uma imagem.
    * `-p 6379:6379`: Mapeia a porta `6379` da máquina host para a porta `6379` do container. A primeira parte é a porta da host, a segunda é a porta do container.
    * `miniredis`: O nome da imagem a ser executada.

3.  **Configuração via Variáveis de Ambiente do Docker:**

    Você também pode configurar a porta e o tamanho do pool de threads utilizando variáveis de ambiente do Docker durante a execução do container:

    ```bash
    docker run -p <host_port>:6379 -e MINIREDIS_PORT=<host_port> -e MINIREDIS_THREAD_POOL_SIZE=<num_threads> miniredis
    ```

    Substitua `<host_port>` pela porta desejada na sua máquina host e `<num_threads>` pelo número de threads desejado.

    **Exemplo:** Para executar na porta `7000` da sua máquina host com `8` threads dentro do container:

    ```bash
    docker run -p 7000:6379 -e MINIREDIS_PORT=7000 -e MINIREDIS_THREAD_POOL_SIZE=8 miniredis
    ```

4.  **Conectando-se ao Servidor (via Docker):**

    Se você mapeou a porta `6379` para a porta `6379` da sua máquina host (ou a porta que você especificou), você pode se conectar usando um cliente Redis ou `Netcat` na porta da sua máquina host:

    ```bash
    nc localhost 6379  # Se usou o mapeamento padrão
    # OU
    nc localhost 7000  # Se usou o exemplo com a porta 7000
    ```

Agora você tem o Mini-Redis em Kotlin em execução e pronto para receber comandos! Consulte o arquivo `COMMANDS.md` para ver a lista de comandos suportados.