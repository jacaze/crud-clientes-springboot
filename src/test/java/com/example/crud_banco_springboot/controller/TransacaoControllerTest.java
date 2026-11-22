package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.dto.TransacaoValorDTO;
import com.example.crud_banco_springboot.dto.TransferenciaRequestDTO;
import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.model.Transacao;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import com.example.crud_banco_springboot.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransacaoControllerTest {

    @Mock
    private ContaBancariaRepository contaBancariaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private TransacaoController transacaoController;

    // ----- Deposito
    @Test
    @DisplayName("Deve realizar o deposito com sucesso quando a conta existir")
    void deveDepositarComSucesso() {
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("100.00"));

        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("50.00"));

        ResponseEntity<?> resposta = transacaoController.depositar(contaId, dto);

        assertEquals(200, resposta.getStatusCode().value());

        ContaBancaria contaAtualizada = (ContaBancaria) resposta.getBody();
        assertNotNull(contaAtualizada);
        assertEquals(new BigDecimal("150.00"), contaAtualizada.getSaldo());

        verify(contaBancariaRepository, times(1)).save(contaSimulada);
        verify(transacaoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar depositar em uma conta inexistente")
    void deveRetornarErroQuandoContaNaoExistir() {
        Long contaIdInexistente = 99L;

        when(contaBancariaRepository.findById(contaIdInexistente)).thenReturn(Optional.empty());

        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("50.00"));

        ResponseEntity<?> resposta = transacaoController.depositar(contaIdInexistente, dto);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta não encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve bloquear depósito com valor negativo ou zero (proteção de integridade de saldo)")
    void deveBloquearDepositoComValorNegativoOuZero() {
        Long contaId = 1L;

        ResponseEntity<?> respostaNegativa = transacaoController.depositar(contaId, new TransacaoValorDTO(new BigDecimal("-50.00")));
        ResponseEntity<?> respostaZero = transacaoController.depositar(contaId, new TransacaoValorDTO(BigDecimal.ZERO));
        ResponseEntity<?> respostaNula = transacaoController.depositar(contaId, new TransacaoValorDTO(null));

        assertEquals(400, respostaNegativa.getStatusCode().value());
        assertEquals("O valor do depósito deve ser maior que zero", respostaNegativa.getBody());

        assertEquals(400, respostaZero.getStatusCode().value());
        assertEquals("O valor do depósito deve ser maior que zero", respostaZero.getBody());

        assertEquals(400, respostaNula.getStatusCode().value());

        verify(contaBancariaRepository, never()).findById(any());
        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    // ------- Saque
    @Test
    @DisplayName("Deve realizar saque com sucesso quando houver saldo suficiente")
    void deveSacarComSucesso() {
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("100.00"));

        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("40.00"));

        ResponseEntity<?> resposta = transacaoController.saque(contaId, dto);

        assertEquals(200, resposta.getStatusCode().value());

        ContaBancaria contaAtualizada = (ContaBancaria) resposta.getBody();
        assertNotNull(contaAtualizada);
        assertEquals(new BigDecimal("60.00"), contaAtualizada.getSaldo());

        verify(contaBancariaRepository, times(1)).save(contaSimulada);
        verify(transacaoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve recusar saque quando o saldo for insuficiente")
    void deveBarrarQuandoSaldoInsuficiente() {
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("20.00"));

        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("50.00"));

        ResponseEntity<?> resposta = transacaoController.saque(contaId, dto);

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("O valor de saque é maior que o saldo em conta", resposta.getBody());
        assertEquals(new BigDecimal("20.00"), contaSimulada.getSaldo());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar sacar em uma conta inexistente")
    void deveRetornarErroAoSacarContaInexistente() {
        Long contaIdInexistente = 99L;

        when(contaBancariaRepository.findById(contaIdInexistente)).thenReturn(Optional.empty());

        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("50.00"));

        ResponseEntity<?> resposta = transacaoController.saque(contaIdInexistente, dto);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta não encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve bloquear saque com valor negativo ou zero (prevenção de adição fraudulenta de saldo)")
    void deveBloquearSaqueComValorNegativoOuZero() {
        Long contaId = 1L;

        ResponseEntity<?> respostaNegativa = transacaoController.saque(contaId, new TransacaoValorDTO(new BigDecimal("-100.00")));
        ResponseEntity<?> respostaZero = transacaoController.saque(contaId, new TransacaoValorDTO(BigDecimal.ZERO));

        assertEquals(400, respostaNegativa.getStatusCode().value());
        assertEquals("O valor do saque deve ser maior que zero", respostaNegativa.getBody());

        assertEquals(400, respostaZero.getStatusCode().value());
        assertEquals("O valor do saque deve ser maior que zero", respostaZero.getBody());

        verify(contaBancariaRepository, never()).findById(any());
        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    // ------ Transferência
    @Test
    @DisplayName("Deve transferir com sucesso")
    void deveTransferirComSucesso() {
        Long idOrigem = 1L;
        Long idDestino = 2L;

        ContaBancaria contaOrigem = new ContaBancaria();
        contaOrigem.setId(idOrigem);
        contaOrigem.setSaldo(new BigDecimal("200.00"));

        ContaBancaria contaDestino = new ContaBancaria();
        contaDestino.setId(idDestino);
        contaDestino.setSaldo(new BigDecimal("50.00"));

        when(contaBancariaRepository.findById(idOrigem)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(idDestino)).thenReturn(Optional.of(contaDestino));

        TransferenciaRequestDTO dto = new TransferenciaRequestDTO(idOrigem, idDestino, new BigDecimal("100.00"));

        ResponseEntity<?> resposta = transacaoController.transferir(dto);

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(new BigDecimal("100.00"), contaOrigem.getSaldo());
        assertEquals(new BigDecimal("150.00"), contaDestino.getSaldo());

        verify(contaBancariaRepository, times(1)).save(contaOrigem);
        verify(contaBancariaRepository, times(1)).save(contaDestino);
        verify(transacaoRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta de destino não existir")
    void deveBarrarTransferenciaContaDestinoInexistente() {
        Long idOrigem = 1L;
        Long idDestinoInexistente = 99L;

        ContaBancaria contaOrigem = new ContaBancaria();
        contaOrigem.setId(idOrigem);
        contaOrigem.setSaldo(new BigDecimal("200.00"));

        when(contaBancariaRepository.findById(idOrigem)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(idDestinoInexistente)).thenReturn(Optional.empty());

        TransferenciaRequestDTO dto = new TransferenciaRequestDTO(idOrigem, idDestinoInexistente, new BigDecimal("100.00"));

        ResponseEntity<?> resposta = transacaoController.transferir(dto);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta de destino não foi encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta de origem não existir")
    void deveBarrarTransferenciaContaOrigemInexistente() {
        Long idOrigemInexistente = 99L;
        Long idDestino = 1L;

        ContaBancaria contaDestino = new ContaBancaria();
        contaDestino.setId(idDestino);
        contaDestino.setSaldo(new BigDecimal("50.00"));

        when(contaBancariaRepository.findById(idOrigemInexistente)).thenReturn(Optional.empty());
        when(contaBancariaRepository.findById(idDestino)).thenReturn(Optional.of(contaDestino));

        TransferenciaRequestDTO dto = new TransferenciaRequestDTO(idOrigemInexistente, idDestino, new BigDecimal("100.00"));

        ResponseEntity<?> resposta = transacaoController.transferir(dto);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta de origem não foi encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar transferência quando a conta de origem não tiver saldo suficiente")
    void deveBarrarTransferenciaComSaldoInsuficiente() {
        Long idOrigem = 1L;
        Long idDestino = 2L;

        ContaBancaria contaOrigem = new ContaBancaria();
        contaOrigem.setId(idOrigem);
        contaOrigem.setSaldo(new BigDecimal("20.00"));

        ContaBancaria contaDestino = new ContaBancaria();
        contaDestino.setId(idDestino);
        contaDestino.setSaldo(new BigDecimal("40.00"));

        when(contaBancariaRepository.findById(idOrigem)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(idDestino)).thenReturn(Optional.of(contaDestino));

        TransferenciaRequestDTO dto = new TransferenciaRequestDTO(idOrigem, idDestino, new BigDecimal("100.00"));

        ResponseEntity<?> resposta = transacaoController.transferir(dto);

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("Saldo insuficiente na conta de origem", resposta.getBody());

        assertEquals(new BigDecimal("20.00"), contaOrigem.getSaldo());
        assertEquals(new BigDecimal("40.00"), contaDestino.getSaldo());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve bloquear transferência com valor negativo, zero ou contas iguais")
    void deveBloquearTransferenciaInvalidaOuComValorNegativo() {
        ResponseEntity<?> mesmaConta = transacaoController.transferir(
                new TransferenciaRequestDTO(1L, 1L, new BigDecimal("50.00")));
        assertEquals(400, mesmaConta.getStatusCode().value());
        assertEquals("A conta de origem não pode ser a mesma que a de destino", mesmaConta.getBody());

        ResponseEntity<?> valorNegativo = transacaoController.transferir(
                new TransferenciaRequestDTO(1L, 2L, new BigDecimal("-100.00")));
        assertEquals(400, valorNegativo.getStatusCode().value());
        assertEquals("O valor da transferência deve ser maior que zero", valorNegativo.getBody());

        ResponseEntity<?> valorZero = transacaoController.transferir(
                new TransferenciaRequestDTO(1L, 2L, BigDecimal.ZERO));
        assertEquals(400, valorZero.getStatusCode().value());
        assertEquals("O valor da transferência deve ser maior que zero", valorZero.getBody());

        ResponseEntity<?> contasNulas = transacaoController.transferir(
                new TransferenciaRequestDTO(null, 2L, new BigDecimal("50.00")));
        assertEquals(400, contasNulas.getStatusCode().value());
        assertEquals("As contas de origem e destino são obrigatórias", contasNulas.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todas as transações com sucesso (GET sem ID)")
    void deveListarTodasAsTransacoes() {
        List<Transacao> listaSimulada = List.of(new Transacao(), new Transacao());
        when(transacaoRepository.findAll()).thenReturn(listaSimulada);

        List<Transacao> listaRetornada = transacaoController.listarTodas();

        assertNotNull(listaRetornada);
        assertEquals(2, listaRetornada.size());
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso (GET com ID)")
    void deveBuscarTransacaoPorIdComSucesso() {
        Long contaId = 1L;
        Transacao transacaoSimulada = new Transacao();
        transacaoSimulada.setId(100L);

        when(contaBancariaRepository.existsById(contaId)).thenReturn(true);
        when(transacaoRepository.findByContaId(contaId)).thenReturn(List.of(transacaoSimulada));

        ResponseEntity<?> resposta = transacaoController.listarContaPorId(contaId);

        assertEquals(200, resposta.getStatusCode().value());

        List<?> extrato = (List<?>) resposta.getBody();
        assertNotNull(extrato);
        assertEquals(1, extrato.size());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar transação por ID inexistente")
    void deveRetornar404AoBuscarTransacaoInexistente() {
        Long contaIdInexistente = 99L;

        when(contaBancariaRepository.existsById(contaIdInexistente)).thenReturn(false);

        ResponseEntity<?> resposta = transacaoController.listarContaPorId(contaIdInexistente);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta não encontrada", resposta.getBody());
    }
}
