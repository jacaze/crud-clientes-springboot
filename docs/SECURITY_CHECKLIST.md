# Checklist de Verificação de Segurança Básica

Este documento reúne os requisitos mínimos e a matriz de verificação de segurança a ser checada em cada Pull Request do projeto `crud_banco_springboot`.

---

## 1. Matriz de Controles de Segurança

| ID | Área | Requisito / Verificação | Como Validar / Implementar | Status Atual |
| :--- | :--- | :--- | :--- | :--- |
| **SEC-01** | **Injeção de Código (SQL / JPQL)** | Bloqueio contra SQL Injection | Não concatenar strings em consultas SQL/HQL. Uso estrito de queries parametrizadas no Spring Data JPA (`@Query` com `:param` ou Spring Data method names). | Em conformidade (usa Spring Data JPA) |
| **SEC-02** | **CORS (Cross-Origin Resource Sharing)** | Política restrita de origens permitidas | Definir origens autorizadas via `WebMvcConfigurer` ou Spring Security. Evitar `allowOrigins("*")` com credenciais liberadas. | Em conformidade (`CorsConfig`) |
| **SEC-03** | **Rate Limiting / Proteção contra Brute Force / DoS** | Limite de requisições por IP / Cliente | Adicionar filtro de bucket (ex: Bucket4j / Spring Cloud Gateway / Filtro customizado) em rotas críticas (ex: `/transacoes/transferencia`, `/contas`). | Em conformidade (`RateLimitingFilter`) |
| **SEC-04** | **Validação de Entrada & Zero Trust** | Bloqueio de dados inválidos ou maliciosos | Uso rigoroso de Bean Validation (`@Valid`, `@Positive`, `@NotBlank`, `@CPF`, `@Size`). Evitar `Map<String, Object>` sem schema. | Em conformidade (`TransacaoValorDTO`, `TransferenciaRequestDTO` e `Cliente`) |
| **SEC-05** | **Prevenção de Condições de Corrida (Race Conditions)** | Consistência em operações concorrentes | Utilizar bloqueio pessimista (`PESSIMISTIC_WRITE`) ou otimista (`@Version`) ao debitar/creditar saldos. | Pendente de implementação |
| **SEC-06** | **Segredos e Credenciais em Repositório** | Zero credenciais no código-fonte | Senhas de banco e chaves de API devem vir de variáveis de ambiente (`${DB_PASSWORD}`), nunca em texto claro nos arquivos de configuração versionados. | Em conformidade (`application.properties` parametrizado) |
| **SEC-07** | **Headers de Segurança HTTP** | Proteção contra XSS, Clickjacking, MIME sniffing | Configurar headers HTTP (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Content-Security-Policy`). | Em conformidade (`SecurityHeadersFilter`) |
| **SEC-08** | **Vazamento de Stack Traces e Dados Sensíveis** | Tratamento seguro de erros | `@RestControllerAdvice` padronizado. Nunca expor logs de stack trace ou dados confidenciais (CPF completo de terceiros, senhas) em respostas de erro para o cliente. | Em conformidade (`GlobalExceptionHandler`) |

---

## 2. Checklist Pré-PR para o Agente

Antes de abrir qualquer Pull Request, o agente deve auditar:
- [ ] O código introduz consultas SQL nativas sem parâmetros?
- [ ] Foram adicionados DTOs com validações `@Valid` e `@Positive` para valores monetários?
- [ ] Há credenciais novas ou senhas fixas inseridas no código?
- [ ] Foram escritos testes automatizados cobrindo tanto o caso de sucesso quanto os cenários de falha/segurança (ex: envio de valores negativos, IDs nulos)?
- [ ] Todos os testes (`mvn test`) estão passando 100%?
