# Documentação de Comandos Suportados do Mini-Redis

Esta página detalha todos os comandos Redis atualmente suportados por esta implementação simplificada (Mini-Redis em Kotlin), incluindo sua sintaxe, parâmetros, descrição e o tipo de retorno esperado.

## Comandos de Strings

* **`GET <key>`**
    * **Descrição:** Retorna o valor da chave especificada.
    * **Sintaxe:** `GET key`
    * **Parâmetros:**
        * `<key>`: A chave da string a ser recuperada.
    * **Retorno:**
        * **Simple String:** O valor da chave, se existir.
        * **Bulk String:** `$-1\r\n` (representando `nil`) se a chave não existir.

* **`SET <key> <value> [NX|XX] [GET]`**
    * **Descrição:** Define o valor da chave especificada.
    * **Sintaxe:** `SET key value [NX|XX] [GET]`
    * **Parâmetros:**
        * `<key>`: A chave a ser definida.
        * `<value>`: O valor a ser associado à chave (uma string).
        * `NX` (opcional): Define a chave somente se ela não existir.
        * `XX` (opcional): Define a chave somente se ela já existir.
        * `GET` (opcional): Retorna o valor antigo da chave após a definição.
    * **Retorno:**
        * Sem a opção `GET`:
            * **Simple String:** `+OK\r\n` se a chave foi definida com sucesso.
            * **Bulk String:** `$-1\r\n` (representando `nil`) se a chave não pôde ser definida devido às condições `NX` ou `XX`.
        * Com a opção `GET`:
            * **Bulk String:** O valor antigo da chave (formato: `$<length>\r\n<value>\r\n`).
            * **Bulk String:** `$-1\r\n` (representando `nil`) se a chave não existia com `NX` ou existia com `XX` e a operação não foi realizada.

## Comandos de Listas

* **`LPUSH <key> <value> [value ...]`**
    * **Descrição:** Insere um ou mais valores no início da lista armazenada na chave. Se a chave não existir, uma nova lista vazia é criada antes da operação de push.
    * **Sintaxe:** `LPUSH key value [value ...]`
    * **Parâmetros:**
        * `<key>`: A chave da lista.
        * `<value>`: Um ou mais valores a serem inseridos no início da lista (strings).
    * **Retorno:**
        * **Integer:** `:<new length>\r\n`, o novo comprimento da lista após a operação de push.

* **`RPUSH <key> <value> [value ...]`**
    * **Descrição:** Insere um ou mais valores no final da lista armazenada na chave. Se a chave não existir, uma nova lista vazia é criada antes da operação de push.
    * **Sintaxe:** `RPUSH key value [value ...]`
    * **Parâmetros:**
        * `<key>`: A chave da lista.
        * `<value>`: Um ou mais valores a serem inseridos no final da lista (strings).
    * **Retorno:**
        * **Integer:** `:<new length>\r\n`, o novo comprimento da lista após a operação de push.

* **`LPOP <key> [count]`**
    * **Descrição:** Remove e retorna o primeiro elemento (ou os primeiros `count` elementos) da lista armazenada na chave.
    * **Sintaxe:** `LPOP key [count]`
    * **Parâmetros:**
        * `<key>`: A chave da lista.
        * `count` (opcional): O número de elementos a serem removidos e retornados. O padrão é `1`.
    * **Retorno:**
        * Se `count` for omitido ou for `1`:
            * **Bulk String:** O valor do primeiro elemento da lista (formato: `$<length>\r\n<value>\r\n`).
            * **Bulk String:** `$-1\r\n` (representando `nil`) se a lista estiver vazia.
        * Se `count` for maior que `1`:
            * **Array:** Um array de Bulk Strings contendo os elementos removidos na ordem em que foram removidos. Retorna um array vazio se a lista estiver vazia.

* **`RPOP <key> [count]`**
    * **Descrição:** Remove e retorna o último elemento (ou os últimos `count` elementos) da lista armazenada na chave.
    * **Sintaxe:** `RPOP key [count]`
    * **Parâmetros:**
        * `<key>`: A chave da lista.
        * `count` (opcional): O número de elementos a serem removidos e retornados. O padrão é `1`.
    * **Retorno:**
        * Se `count` for omitido ou for `1`:
            * **Bulk String:** O valor do último elemento da lista (formato: `$<length>\r\n<value>\r\n`).
            * **Bulk String:** `$-1\r\n` (representando `nil`) se a lista estiver vazia.
        * Se `count` for maior que `1`:
            * **Array:** Um array de Bulk Strings contendo os elementos removidos na ordem inversa em que foram removidos (o último elemento removido será o primeiro no array). Retorna um array vazio se a lista estiver vazia.

* **`LRANGE <key> <start> <stop>`**
    * **Descrição:** Retorna os elementos no intervalo especificado da lista armazenada na chave. Os índices `start` e `stop` são baseados em zero, com `0` sendo o primeiro elemento e `-1` sendo o último elemento.
    * **Sintaxe:** `LRANGE key start stop`
    * **Parâmetros:**
        * `<key>`: A chave da lista.
        * `<start>`: O índice inicial do intervalo (inclusive).
        * `<stop>`: O índice final do intervalo (inclusive).
    * **Retorno:**
        * **Array:** Um array de Bulk Strings contendo os elementos dentro do intervalo especificado. Retorna um array vazio se a chave não existir ou se o intervalo for inválido (por exemplo, `start` maior que o tamanho da lista).

## Comandos de Expiração

* **`EXPIRE <key> <seconds>`**
    * **Descrição:** Define um tempo de vida (TTL) em segundos para a chave especificada. Após o TTL expirar, a chave será automaticamente removida.
    * **Sintaxe:** `EXPIRE key seconds`
    * **Parâmetros:**
        * `<key>`: A chave à qual o tempo de vida será associado.
        * `<seconds>`: O número de segundos até a chave expirar (um inteiro positivo).
    * **Retorno:**
        * **Integer:** `:1\r\n` se o TTL foi definido com sucesso.
        * **Integer:** `:0\r\n` se a chave não existir.

* **`TTL <key>`**
    * **Descrição:** Retorna o tempo restante de vida (TTL) em segundos da chave especificada.
    * **Sintaxe:** `TTL key`
    * **Parâmetros:**
        * `<key>`: A chave para verificar o tempo de vida.
    * **Retorno:**
        * **Integer:** O tempo restante em segundos (um inteiro positivo).
        * **Integer:** `:-1\r\n` se a chave existir, mas não tiver um TTL associado (é persistente).
        * **Integer:** `:-2\r\n` se a chave não existir.

## Formato de Resposta Redis (Simplificado)

Esta implementação do Mini-Redis utiliza um subconjunto do Redis Serialization Protocol (RESP) para comunicar com os clientes:

* **Simple Strings:** Para respostas curtas e não binárias, iniciam com `+` seguido pela string e `\r\n`. Exemplo: `+OK\r\n`.
* **Errors:** Para indicar erros, iniciam com `-` seguido pela mensagem de erro e `\r\n`. Exemplo: `-ERR unknown command 'BLAH'\r\n`.
* **Integers:** Para retornar valores inteiros, iniciam com `:` seguido pelo número e `\r\n`. Exemplo: `:1\r\n`.
* **Bulk Strings:** Para retornar strings binárias ou strings de maior comprimento, iniciam com `$` seguido pelo comprimento da string, `\r\n`, e então a string propriamente dita seguida por `\r\n`. Se a string for `nil` (ausente), é representado como `$-1\r\n`. Exemplo de "valor": `$5\r\nvalor\r\n`.
* **Arrays:** Para retornar uma lista de elementos (outros tipos RESP), iniciam com `*` seguido pelo número de elementos no array, `\r\n`, e então cada elemento do array no seu próprio formato RESP. Um array `nil` (vazio ou quando a chave não existe para comandos como `LRANGE`) pode ser representado como `*-1\r\n`.

Lembre-se que este é um Mini-Redis e nem todos os comandos ou opções do Redis completo estão implementados. Consulte o código fonte para obter a informação mais precisa sobre a funcionalidade suportada.