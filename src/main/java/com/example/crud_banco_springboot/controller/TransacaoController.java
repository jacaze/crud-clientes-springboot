package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.dto.TransacaoValorDTO;
import com.example.crud_banco_springboot.dto.TransferenciaRequestDTO;
import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.model.TipoTransacao;
import com.example.crud_banco_springboot.model.Transacao;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import com.example.crud_banco_springboot.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @GetMapping
    public List<Transacao> listarTodas() {
        return transacaoRepository.findAll();
    }

    @GetMapping("/conta/{id}")
    public ResponseEntity<?> listarContaPorId(@PathVariable Long id) {
        if (!contaBancariaRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Conta não encontrada");
        }

        List<Transacao> extrato = transacaoRepository.findByContaId(id);
        return ResponseEntity.ok(extrato);
    }

    @PostMapping("/deposito/{id}")
    @Transactional
    public ResponseEntity<?> depositar(@PathVariable Long id, @Valid @RequestBody TransacaoValorDTO dto) {
        if (dto == null || dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(400).body("O valor do depósito deve ser maior que zero");
        }

        BigDecimal valor = dto.valor();

        Optional<ContaBancaria> contaBancariaOptional = contaBancariaRepository.findById(id);

        if (contaBancariaOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Conta não encontrada");
        }

        ContaBancaria conta = contaBancariaOptional.get();
        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        conta.setSaldo(novoSaldo);

        contaBancariaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setTipoTransacao(TipoTransacao.DEPOSITO);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setConta(conta);

        transacaoRepository.save(transacao);
        return ResponseEntity.ok(conta);
    }

    @PostMapping("/saque/{id}")
    @Transactional
    public ResponseEntity<?> saque(@PathVariable Long id, @Valid @RequestBody TransacaoValorDTO dto) {
        if (dto == null || dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(400).body("O valor do saque deve ser maior que zero");
        }

        BigDecimal valor = dto.valor();

        Optional<ContaBancaria> contaOptional = contaBancariaRepository.findById(id);

        if (contaOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Conta não encontrada");
        }

        ContaBancaria conta = contaOptional.get();
        BigDecimal saldoDisponivel = conta.getSaldo();

        if (valor.compareTo(saldoDisponivel) > 0) {
            return ResponseEntity.status(400).body("O valor de saque é maior que o saldo em conta");
        }

        BigDecimal saldoFinal = saldoDisponivel.subtract(valor);
        conta.setSaldo(saldoFinal);

        contaBancariaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setTipoTransacao(TipoTransacao.SAQUE);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setConta(conta);

        transacaoRepository.save(transacao);
        return ResponseEntity.ok(conta);
    }

    @PostMapping("/transferencia")
    @Transactional
    public ResponseEntity<?> transferir(@Valid @RequestBody TransferenciaRequestDTO dto) {
        if (dto == null || dto.contaOrigemId() == null || dto.contaDestinoId() == null) {
            return ResponseEntity.status(400).body("As contas de origem e destino são obrigatórias");
        }

        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(400).body("O valor da transferência deve ser maior que zero");
        }

        Long contaOrigemId = dto.contaOrigemId();
        Long contaDestinoId = dto.contaDestinoId();
        BigDecimal valor = dto.valor();

        if (contaOrigemId.equals(contaDestinoId)) {
            return ResponseEntity.status(400).body("A conta de origem não pode ser a mesma que a de destino");
        }

        Optional<ContaBancaria> origemOptional = contaBancariaRepository.findById(contaOrigemId);
        Optional<ContaBancaria> destinoOptional = contaBancariaRepository.findById(contaDestinoId);

        if (origemOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Conta de origem não foi encontrada");
        }

        if (destinoOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Conta de destino não foi encontrada");
        }

        ContaBancaria contaOrigem = origemOptional.get();
        ContaBancaria contaDestino = destinoOptional.get();

        if (valor.compareTo(contaOrigem.getSaldo()) > 0) {
            return ResponseEntity.status(400).body("Saldo insuficiente na conta de origem");
        }

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        Transacao transacaoOrigem = new Transacao();
        transacaoOrigem.setTipoTransacao(TipoTransacao.TRANSFERENCIA_ENVIADA);
        transacaoOrigem.setValor(valor);
        transacaoOrigem.setDataHora(LocalDateTime.now());
        transacaoOrigem.setConta(contaOrigem);
        transacaoOrigem.setContaRelacionada(contaDestino);
        transacaoRepository.save(transacaoOrigem);

        Transacao transacaoDestino = new Transacao();
        transacaoDestino.setTipoTransacao(TipoTransacao.TRANSFERENCIA_RECEBIDA);
        transacaoDestino.setValor(valor);
        transacaoDestino.setDataHora(LocalDateTime.now());
        transacaoDestino.setConta(contaDestino);
        transacaoDestino.setContaRelacionada(contaOrigem);
        transacaoRepository.save(transacaoDestino);

        return ResponseEntity.ok("Transferencia realizada com sucesso");
    }
}
