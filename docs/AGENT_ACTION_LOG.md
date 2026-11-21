# Histórico de Ações e Auditoria dos Agentes

Este arquivo registra todas as tarefas e implementações realizadas pelos agentes no projeto, servindo para rastreabilidade de decisões e histórico de branches.

---

## Template de Registro de Ação

```markdown
### [YYYY-MM-DD] - <Título da Ação>
- **Branch:** `<nome-da-branch>`
- **Tipo:** `feat` | `fix` | `refactor` | `security` | `test`
- **Descrição:** <Breve resumo do que foi feito>
- **Itens de Segurança Tratados:** <IDs do SECURITY_CHECKLIST.md>
- **Testes Implementados/Executados:** <Classes e métodos de teste>
- **Pull Request:** `<link ou número do PR>`
- **Status:** Em Revisão / Aprovado / Merged
```

---

## Log de Ações

### [2026-11-18] - Padronização e Governança dos Agentes
- **Branch:** `main` (inicialização de documentação de governança)
- **Tipo:** `docs`
- **Descrição:** Criação da estrutura de documentação contendo diretrizes operacionais de agentes, checklist de segurança básica e log de auditoria.
- **Itens de Segurança Tratados:** Mapeamento de SEC-01 a SEC-08 em [SECURITY_CHECKLIST.md](file:///c:/Users/Guilherme/Documents/Faculdade/Projetos/crud_banco_springboot/docs/SECURITY_CHECKLIST.md).
- **Testes Implementados/Executados:** N/A (documentação).
- **Pull Request:** N/A (preparação de base).
- **Status:** Concluído.

### [2026-11-18] - Implementação de CORS, Rate Limiting e Security Headers
- **Branch:** `feature/security-cors-ratelimit`
- **Tipo:** `feat`
- **Descrição:** Implementação de configuração de CORS com controle granular de origens, filtro de limitação de requisições (Rate Limiting) por IP com respostas HTTP 429 e cabeçalhos informativos (`X-RateLimit-*`), e filtro de headers HTTP essenciais de segurança OWASP (MIME nosniff, Frame DENY, XSS protection, Referrer Policy e CSP). Além disso, parametrização do banco de dados para evitar senhas hardcoded.
- **Itens de Segurança Tratados:** SEC-02 (CORS), SEC-03 (Rate Limiting), SEC-06 (Credenciais em Env Vars) e SEC-07 (Security Headers).
- **Testes Implementados/Executados:** 
  - `RateLimitingFilterTest` (4 testes): cenários de requisições dentro do limite, bloqueio com status 429 ao exceder quota, parsing de `X-Forwarded-For` e bypass quando desabilitado.
  - `SecurityHeadersFilterTest` (1 teste): validação da injeção de todos os headers de segurança OWASP.
  - `CorsConfigTest` (1 teste): validação do registro e mapeamento de origens autorizadas.
  - Suíte total do projeto: 31 testes executados e 100% aprovados.
- **Pull Request:** [PR #1](https://github.com/jacaze/crud-clientes-springboot/pull/1)
- **Status:** Merged.

### [2026-11-21] - Blindagem e Validação de Transações Financeiras (DTOs + Bean Validation + Exception Handler)
- **Branch:** `feature/dto-validation-transacoes`
- **Tipo:** `feat`
- **Descrição:** Substituição de `Map<String, Object>` e `Map<String, BigDecimal>` nos endpoints de depósito, saque e transferência por DTOs fortemente tipados (`TransacaoValorDTO` e `TransferenciaRequestDTO`) com Bean Validation (`@NotNull`, `@Positive`) e validação defensiva em profundidade contra valores negativos, nulos ou zero. Adição de `@Transactional` em depósito e saque para atomicidade, e criação do `GlobalExceptionHandler` (`@RestControllerAdvice`) para formatação limpa de erros HTTP 400 sem vazamento de stack traces.
- **Itens de Segurança Tratados:** SEC-04 (Validação de Entrada & Zero Trust) e SEC-08 (Prevenção de Vazamento de Stack Traces).
- **Testes Implementados/Executados:**
  - `TransacaoControllerTest` (15 testes): cenários de sucesso e bloqueio de depósito/saque/transferência com valores negativos, zero ou campos nulos.
  - `TransacaoDtoValidationTest` (4 testes): validação das constraints `@NotNull` e `@Positive` via `jakarta.validation.Validator`.
  - `GlobalExceptionHandlerTest` (2 testes): formatação de erros `MethodArgumentNotValidException` e `HttpMessageNotReadableException`.
  - Suíte total do projeto: 40 testes executados e 100% aprovados.
- **Pull Request:** Submetido via GitHub CLI para aprovação do usuário.
- **Status:** Em Revisão.
