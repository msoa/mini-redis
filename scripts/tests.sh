#!/bin/bash

MINIREDIS_HOST="localhost"
MINIREDIS_PORT="6379"

echo "Iniciando testes funcionais do Mini-Redis..."

# Função para enviar comando e receber resposta via netcat
send_command() {
  local command="$1"
  echo -e "$command\r" | nc -w 1 "$MINIREDIS_HOST" "$MINIREDIS_PORT"
}

# Função para verificar se a resposta corresponde ao esperado
verify_response() {
  local expected="$1"
  local actual="$2"
  expected_length=$(echo -n "$expected" | wc -c)
  actual_length=$(echo -n "$actual" | wc -c)
  if [[ "$actual" == "$expected" ]]; then
    echo "  [OK] Esperado ($expected_length): '$expected'"
  else
    echo "  [FALHOU]"
    echo "Esperado ($expected_length): '$expected'"
    echo "Obtido ($actual_length): '$actual'"
  fi
}

# Iniciar o servidor Mini-Redis em segundo plano
echo "Iniciando o servidor Mini-Redis..."
java -jar build/libs/mini-redis-*-all.jar &
SERVER_PID=$!
sleep 1 # Espera um pouco para o servidor iniciar

echo
echo "Executando testes funcionais:"

#echo "Testando SET..."
response=$(send_command "SET test_key test_value")
verify_response $'+OK\r' "$response"

# Teste GET
echo "Testando GET..."
response=$(send_command "GET test_key")
verify_response $'+test_value\r' "$response"

# Teste GET chave inexistente
echo "Testando GET chave inexistente..."
response=$(send_command "GET non_existent_key")
verify_response $'-ERR no result\r' "$response"

# Teste LPUSH
echo "Testando LPUSH..."
response=$(send_command "LPUSH test_list item1")
verify_response $'*1\r\n$1\r\n1\r' "$response"
response=$(send_command "LPUSH test_list item2")
verify_response $'*1\r\n$1\r\n2\r' "$response"

# Teste LRANGE
echo "Testando LRANGE..."
response=$(send_command "LRANGE test_list 0 1")
verify_response $'*2\r\n$5\r\nitem2\r\n$5\r\nitem1\r' "$response"

# Teste LPOP
echo "Testando LPOP..."
response=$(send_command "LPOP test_list")
verify_response $'*1\r\n$5\r\nitem2\r' "$response"
response=$(send_command "LRANGE test_list 0 5")
verify_response $'*2\r\n$5\r\nitem2\r\n$5\r\nitem1\r' "$response"

# Teste RPUSH
echo "Testando RPUSH..."
response=$(send_command "RPUSH another_list last1")
verify_response $'*1\r\n$1\r\n1\r' "$response"
response=$(send_command "RPUSH another_list last2")
verify_response $'*1\r\n$1\r\n2\r' "$response"

# Teste RPOP
echo "Testando RPOP..."
response=$(send_command "RPOP another_list")
verify_response $'*1\r\n$5\r\nlast2\r' "$response"
response=$(send_command "LRANGE another_list 0 1")
verify_response $'*2\r\n$5\r\nlast1\r\n$5\r\nlast2\r' "$response"

# Teste EXPIRE
echo "Testando EXPIRE..."
response=$(send_command "EXPIRE test_key 5")
verify_response $':1\r' "$response"

# Teste TTL
echo "Testando TTL..."
response=$(send_command "TTL test_key")
if [[ "$response" =~ ^:[0-9]+'\r\n'$ ]] || [[ "$response" == ":-1\r\n" ]]; then
  echo "  [FALHOU] Esperado um TTL válido, obtido: '$response'"
else
  echo "  [OK] $response (TTL)"
fi

# Teste GET após expiração (pode falhar dependendo do timing exato)
echo "Testando GET após possível expiração (aguardando 6 segundos)..."
sleep 6
response=$(send_command "GET test_key")
verify_response $'-ERR no result\r' "$response"

echo
echo "Testes funcionais concluídos."

# Parar o servidor Mini-Redis
echo "Parando o servidor Mini-Redis..."
kill "$SERVER_PID"