# Diretrizes de Atuação dos Agentes de IA

Este documento define o padrão operacional obrigatório para qualquer agente ou desenvolvedor que atue no repositório `crud_banco_springboot`.

---

## 1. Fluxo de Trabalho (Git Workflow)

1. **Uma Branch por Feature / Tarefa:**
   - Nunca desenvolva ou faça commit diretamente na `main`.
   - Crie sempre uma branch com nomenclatura semântica a partir da `main` atualizada:
     - `feature/<nome-da-funcionalidade>` (ex: `feature/security-cors-ratelimit`)
     - `fix/<nome-do-bug>` (ex: `fix/saldo-negativo-deposito`)
     - `refactor/<escopo>` (ex: `refactor/service-layer-contas`)
     - `test/<escopo>` (ex: `test/integracao-transacoes`)
2. **Convenção de Commits (Conventional Commits):**
   - Estrutura: `<tipo>(<escopo-opcional>): <descrição em minúsculas>`
   - Tipos válidos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `perf`, `security`.
   - Exemplo: `feat(security): configura cors e headers de seguranca basicos`
3. **Testes Automatizados Obrigatórios:**
   - Toda alteração em regras de negócio ou endpoints deve ser acompanhada de testes unitários e/ou de integração.
   - Os testes devem ser executados e validados (`mvn test`) antes de qualquer commit.
4. **Pull Requests (PR):**
   - Após testes validados localmente, suba a branch para o repositório remoto.
   - Abra um Pull Request via `gh pr create` detalhando:
     - Resumo das mudanças
     - Critérios de segurança atendidos
     - Evidência de testes executados
   - **Aguarde a aprovação do usuário antes de realizar merge na branch principal.**

---

## 2. Padrões de Código e Boas Práticas

- **Arquitetura em Camadas:** Controller -> Service -> Repository. O controller não deve ter regras de negócio nem cálculos de saldo.
- **DTOs e Validação:** Não expor entidades JPA diretamente em requisições/respostas. Utilizar DTOs com Bean Validation (`@NotNull`, `@Positive`, etc.).
- **Imutabilidade e Injeção:** Preferir injeção via construtor a `@Autowired` direto em campos privados.
- **Tratamento Global de Exceções:** Usar `@RestControllerAdvice` padronizado em vez de retornos soltos com `ResponseEntity.status(400).body("texto")`.
