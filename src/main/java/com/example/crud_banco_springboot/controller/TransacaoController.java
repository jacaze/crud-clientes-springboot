package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.model.TipoTransacao;
import com.example.crud_banco_springboot.model.Transacao;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import com.example.crud_banco_springboot.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @GetMapping
    public List<Transacao> listarTodas(){
        return transacaoRepository.findAll();
    }

    @PostMapping("/deposito/{id}")
    public ResponseEntity<?> depositar(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload){

        //pegando o valor do deposito dento do payload
        BigDecimal valor = payload.get("valor");

        //buscando se a conta informado existe
        Optional<ContaBancaria> contaBancariaOptional = contaBancariaRepository.findById(id);

        if (contaBancariaOptional.isEmpty()){
            return  ResponseEntity.status(404).body("Conta não encontrada");
        }

        // Pegando a conta de dentro do Optional
        ContaBancaria conta = contaBancariaOptional.get();

        //Calculando o novo saldo (somando o atual com o valor do depósito)
        BigDecimal novoSaldo = conta.getSaldo().add(valor);

        //Atualizando o saldo dentro do objeto da conta
        conta.setSaldo(novoSaldo);

        //Guardando os dados que foram alterados
        contaBancariaRepository.save(conta);

        //Guardando os dados da transacao
        Transacao transacao = new Transacao();
        transacao.setTipoTransacao(TipoTransacao.DEPOSITO);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setConta(conta);

        transacaoRepository.save(transacao);
        return ResponseEntity.ok(conta);
    }

    @PostMapping("/saque/{id}")
    public ResponseEntity<?> saque(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload){

        //pegar o valor de saque do payload
        BigDecimal valor = payload.get("valor");

        //Verificar se a conta bancaria informada existe
        Optional<ContaBancaria> contaOptinional = contaBancariaRepository.findById(id);

        if (contaOptinional.isEmpty()){
            return  ResponseEntity.status(404).body("Conta não encontrada");
        }

        //pegando a conta do optional
        ContaBancaria conta = contaOptinional.get();

        //Verificar se existe limite
        BigDecimal saldoDisponivel = conta.getSaldo();

        if (valor.compareTo(saldoDisponivel) > 0){
            return ResponseEntity.status(400).body("O valor de saque é maior que o saldo em conta");
        }

        BigDecimal saldoFinal = saldoDisponivel.subtract(valor);
        conta.setSaldo(saldoFinal);

        contaBancariaRepository.save(conta);

        //Guardando os dados da transacao
        Transacao transacao = new Transacao();
        transacao.setTipoTransacao(TipoTransacao.SAQUE);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setConta(conta);

        transacaoRepository.save(transacao);
        return ResponseEntity.ok(conta);
    }
}
