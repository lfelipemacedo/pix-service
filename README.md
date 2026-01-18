# Pix Service
## Como Executar e Testar

### Pré-requisitos
* **Java 21**
* **Maven 3.9+**
* **Docker e Docker Compose**

### 1. Instalação e Execução
Foi utilizado Docker Multi-Stage, permitindo criar imagens menores e mais eficientes.
```bash
# Dar build e subir containers
docker compose up -d --build
```
### 2. Executando os Testes
```bash
# Executar todos os testes (Unitários, Integração e Concorrência)
mvn clean test
```

### Decisões de Design e Requisitos
A arquitetura do projeto segue os princípios da Clean Architecture (Arquitetura Hexagonal), visando o desacoplamento total da lógica de negócio das tecnologias externas.
Atendimento aos Requisitos Funcionais e Não-Funcionais:
- Domínio Rico (DDD): A lógica de movimentação financeira (débito/crédito) está encapsulada na entidade de domínio Wallet. Isso garante que as regras de saldo sejam respeitadas independentemente da camada de persistência.
- Concorrência (Gasto Duplo): Para evitar que uma carteira gaste o mesmo saldo simultaneamente em duas transações, foi implementado Optimistic Locking (Bloqueio Otimista) através da anotação @Version do JPA.
- Auditabilidade (Ledger): O sistema implementa o padrão Ledger. Nenhuma alteração de saldo ocorre de forma isolada; toda e qualquer movimentação gera um registro imutável na tabela ledger_entries.
- Saldo Histórico: Atendendo ao requisito de reconstrução de estado, o saldo histórico é calculado através da agregação (SUM) das entradas no Ledger até o timestamp solicitado, garantindo precisão em auditorias.
- Idempotência: Para garantir resiliência contra falhas de rede e retentativas, foi utilizada a tabela idempotency_keys que armazena o resultado de operações processadas por key_id ou eventId.

### Trade-offs e Compromissos
Foram feitas as seguintes escolhas técnicas:
- Processamento Síncrono: As transferências e webhooks são processados de forma síncrona. Em um cenário de escala massiva, o ideal seria o uso de mensageria (ex: Kafka) para garantir maior vazão e resiliência.
- Segurança Simplificada: A API não implementa camadas de OAuth2/JWT nesta versão. Em produção, os endpoints de Webhook exigiriam validação de assinatura digital para garantir a autenticidade do remetente.
- Banco de Dados para Testes: Os testes de integração utilizam H2 em modo de compatibilidade para agilizar o pipeline de execução, simulando o comportamento do banco de produção.

### Endpoints
|  Método | Endpoint  | Função                                               |
|---|---|------------------------------------------------------|
|  POST |  /wallets | Criação de carteira com saldo inicial.|              
|  POST | /wallets/pix-keys  | Vincula uma chave Pix única a uma carteira.          |
| GET  |  /wallets/{id}/balance | Consulta saldo (atual ou histórico via query param at). |
| POST  | /wallets/{id}/deposit  | Realiza aporte financeiro direto.                    |
| POST  |  /pix/transfers | Inicia transferência entre contas com idempotência.  |
| POST  |  /pix/webhooks | Recebe notificações externas de conclusão de Pix.    |