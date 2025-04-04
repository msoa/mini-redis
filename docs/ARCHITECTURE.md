# Arquitetura e Decisões de Design do Mini-Redis em Kotlin

Este documento descreve a arquitetura geral e as principais decisões de design tomadas durante o desenvolvimento do Mini-Redis em Kotlin, com base na estrutura de código fornecida. O objetivo é fornecer uma visão de alto nível de como o sistema funciona e as razões por trás de escolhas importantes.

## Arquitetura Geral

O Mini-Redis adota uma arquitetura cliente-servidor síncrona, utilizando um modelo de threading para gerenciar múltiplas conexões de clientes. Os principais componentes da arquitetura são:

* **TcpServer:** Responsável por iniciar e gerenciar o ciclo de vida do servidor TCP. Ele configura um `ServerSocket` para aceitar conexões na porta especificada.
* **ConnectionAcceptor:** Executado em uma thread separada, este componente aceita novas conexões de clientes no `ServerSocket` e as delega ao `ClientManager`.
* **ClientManager:** Gerencia um pool de threads (`ExecutorService`) e é responsável por atribuir o tratamento de cada nova conexão a um `ClientProcessor`. Ele controla o ciclo de vida dos processadores de clientes.
* **ClientProcessor:** Representa a conexão individual com um cliente. Executado em uma thread do pool, ele lê as mensagens do cliente, as encaminha para o `ProtocolHandler` para processamento e envia as respostas de volta ao cliente.
* **ProtocolHandler (RedisProtocolHandler):** Implementa a lógica para entender e gerar mensagens seguindo o protocolo Redis (RESP). Ele recebe strings brutas, as interpreta como comandos Redis e formata as respostas de acordo com o protocolo.
* **Command Handlers (StringCommand, ListCommand, ExpireCommand):** Classes específicas que implementam a lógica de negócios para cada grupo de comandos Redis suportado (strings, listas, expiração). Eles interagem com a camada de `Storage`.
* **Storage (InMemoryStorage):** Implementa o armazenamento de dados em memória. Utiliza `ConcurrentHashMap` para garantir a thread-safety das operações de leitura e escrita.
* **ExpireManager (SchedulerExpireManager):** Responsável por gerenciar a expiração de chaves. Utiliza um `ScheduledExecutorService` para executar periodicamente a limpeza de chaves expiradas no `InMemoryStorage`.

## Decisões de Design

As seguintes decisões de design foram tomadas com o objetivo de criar uma implementação simples, didática e funcional do Redis, focando na clareza e na concorrência básica:

**1. Modelo de Concorrência Baseado em Pool de Threads:**

* **Decisão:** Utilizar um `ExecutorService` com um número fixo de threads (determinado pelo `threadPoolSize` ou pelo número de processadores disponíveis) para processar as conexões dos clientes.
* **Justificativa:** Permite o tratamento simultâneo de múltiplas conexões sem a sobrecarga de criar uma nova thread para cada conexão. O pool de threads limita o consumo de recursos e melhora a capacidade de resposta sob carga.
* **Alternativas Consideradas:** Modelo de I/O não bloqueante com NIO (Java NIO).
* **Razão para a Escolha:** A complexidade do NIO foi considerada excessiva para um projeto inicial. O modelo de pool de threads oferece um bom equilíbrio entre desempenho e simplicidade de implementação e compreensão.

**2. Separação de Responsabilidades:**

* **Decisão:** Dividir as responsabilidades em classes distintas (TcpServer, ConnectionAcceptor, ClientManager, ClientProcessor, ProtocolHandler, Command Handlers, Storage, ExpireManager).
* **Justificativa:** Promove a modularidade, facilita a manutenção e o teste de cada componente individualmente, e torna o código mais organizado e compreensível. Por exemplo, o `TcpServer` se preocupa apenas com a aceitação de conexões, enquanto o `ClientProcessor` lida com a comunicação com um cliente específico.

**3. Tratamento do Protocolo Redis no `RedisProtocolHandler`:**

* **Decisão:** Implementar a lógica de parsing de comandos e formatação de respostas seguindo o protocolo RESP em uma classe dedicada (`RedisProtocolHandler`).
* **Justificativa:** Isola a lógica específica do protocolo Redis do restante da aplicação, tornando mais fácil adicionar suporte a novos comandos ou modificar o protocolo no futuro.

**4. Handlers de Comando Dedicados:**

* **Decisão:** Criar classes separadas para agrupar a lógica de comandos relacionados (ex: `StringCommand` para comandos como `GET` e `SET`, `ListCommand` para comandos de lista, `ExpireCommand` para comandos de expiração).
* **Justificativa:** Melhora a organização do código e facilita a extensão com novos comandos. Cada handler contém a lógica específica para os comandos que ele suporta e interage com a camada de armazenamento.

**5. Armazenamento de Dados em Memória com `ConcurrentHashMap`:**

* **Decisão:** Utilizar `ConcurrentHashMap` para armazenar os dados e os metadados de expiração.
* **Justificativa:** `ConcurrentHashMap` é thread-safe e oferece bom desempenho para operações simultâneas de leitura e escrita, o que é crucial em um servidor que atende múltiplos clientes.
* **Considerações:** A escolha de `MutableList<String>` como valor no mapa de dados sugere um tratamento uniforme de diferentes tipos de dados do Redis (strings são armazenadas em listas de um elemento, listas são armazenadas diretamente