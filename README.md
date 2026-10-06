# 🏦 Banking API - CRUD Spring Boot & Docker

API RESTful desenvolvida em **Spring Boot** para gerenciamento de clientes, contas bancárias e transações financeiras. O projeto utiliza **PostgreSQL** rodando via **Docker Compose** e possui cobertura completa de testes unitários nas controllers utilizando **JUnit 5** e **Mockito**.

---

## 🚀 Tecnologias Utilizadas

* **Java 17+**
* **Spring Boot 3**
* **Spring Data JPA** (Hibernate)
* **PostgreSQL**
* **Docker & Docker Compose**
* **JUnit 5 & Mockito** (Testes Unitários)
* **Maven**

---

## 🛠️ Regras de Negócio e Decisões de Arquitetura

* **Abertura de Conta**: Ao registrar uma nova conta bancária, o sistema atribui automaticamente a agência padrão (`0001`), inicia o saldo em `0.00` e gera um número aleatório de conta de 5 dígitos.
* **Integridade de Dados (Soft Delete)**: Por compliance e integridade do histórico financeiro, a API não disponibiliza remoção física (`DELETE`) para a entidade `ContaBancaria`.
* **Validação de Vínculos**: Operações de conta dependem da existência prévia de um `Cliente` válido cadastrado no sistema.

---

## 🗄️ Estrutura do Banco de Dados

O modelo de dados é composto por 4 entidades principais:

* **`Cliente`**: Dados cadastrais do titular.
* **`Endereco`**: Informações do endereço do cliente.
* **`ContaBancaria`**: Vínculo entre cliente, agência, número da conta e saldo atual.
* **`Transacao`**: Registro de operações (`TipoTransacao`: Depósito, Saque, Transferência).

---

## 📌 Endpoints da API

### 👤 Clientes (`/clientes`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/clientes` | Cadastra um novo cliente |
| `GET` | `/clientes` | Lista todos os clientes |
| `GET` | `/clientes/{id}` | Busca cliente por ID |
| `PUT` | `/clientes/{id}` | Atualiza dados do cliente |
| `DELETE` | `/clientes/{id}` | Remove um cliente |

### 💳 Contas Bancárias (`/contas`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/contas` | Abre uma nova conta para um cliente existente |
| `GET` | `/contas` | Lista todas as contas bancárias |
| `GET` | `/contas/{id}` | Busca conta bancária por ID |

### 💸 Transações (`/transacoes`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/transacoes` | Realiza um saque, depósito ou transferência |
| `GET` | `/transacoes` | Lista o histórico de transações |

---

## 🧪 Suíte de Testes Unitários

O projeto possui cobertura de testes unitários para a camada web (`Controllers`), garantindo o comportamento correto de fluxos de sucesso e exceções HTTP (ex: `404 Not Found`).

Para rodar os testes da aplicação via terminal:

```bash
./mvnw test
