package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.model.TipoTransacao;
import com.example.crud_banco_springboot.model.Transacao;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import com.example.crud_banco_springboot.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @GetMapping("/conta/{id}")
    public ResponseEntity<?> listarContaPorId(@PathVariable Long id){
       //Verificar se a conta existe
        if (!contaBancariaRepository.existsById(id)){
            return ResponseEntity.status(404).body("Conta não encontrada");
        }

        List<Transacao> extrato = transacaoRepository.findByContaId(id);
        return ResponseEntity.ok(extrato);
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

    @PostMapping("/transferencia")
    @Transactional
    public  ResponseEntity<?> transferir(@RequestBody Map<String, Object> payload){
        //Extraindo os dados do payload
        Long contaOrigemId = ((Number) payload.get("contaOrigemId")).longValue();
        Long contaDestinoId = ((Number)payload.get("contaDestinoId")).longValue();
        BigDecimal valor = new BigDecimal(payload.get("valor").toString());

        //Validar de origem e destino são iguais
        if (contaOrigemId.equals(contaDestinoId)){
            return ResponseEntity.status(400).body("A conta de origem não pode ser a mesma que a de destino");
        }

        //Buscar as duas contas no banco de dados
        Optional<ContaBancaria> origemOptional = contaBancariaRepository.findById(contaOrigemId);
        Optional<ContaBancaria> destinoOptional = contaBancariaRepository.findById(contaDestinoId);

        if(origemOptional.isEmpty()){
            return ResponseEntity.status(404).body("Conta de origem não foi encontrada");
        }

        if (destinoOptional.isEmpty()){
            return ResponseEntity.status(404).body("Conta de destino não foi encontrada");
        }

        ContaBancaria contaOrigem = origemOptional.get();
        ContaBancaria contaDestino = destinoOptional.get();

        //Validar se a conta de origem possui saldo suficiente
        if (valor.compareTo(contaOrigem.getSaldo())>0){
            return ResponseEntity.status(400).body("Saldo insuficiente na conta de origem");
        }

        //Atualizaçãp dos saldos
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
        contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        //Registrando a transação de origem/saida
        Transacao transacaoOrigem = new Transacao();
        transacaoOrigem.setTipoTransacao(TipoTransacao.TRANSFERENCIA_ENVIADA);
        transacaoOrigem.setValor(valor);
        transacaoOrigem.setDataHora(LocalDateTime.now());
        transacaoOrigem.setConta(contaOrigem);
        transacaoOrigem.setContaRelacionada(contaDestino);
        transacaoRepository.save(transacaoOrigem);

        //Registrando a transação de destino/entrada
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
