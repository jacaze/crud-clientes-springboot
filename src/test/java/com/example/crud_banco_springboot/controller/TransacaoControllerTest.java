package com.example.crud_banco_springboot.controller;

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
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    void deveDepositarComSucesso(){
        // 1. Arrange - Preparação dos dados
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("100.00"));

        //Ensinando o mockito para quando buscarem a conta por id 1, retornar a conta simulada
        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        //Payload de deposito
        Map<String, BigDecimal> payload = Map.of("valor", new BigDecimal("50.00"));

        // 2. Act - Executando o teste
        ResponseEntity<?> resposta = transacaoController.depositar(contaId,payload);

        // 3. Assert - Verificação dos resultados
        assertEquals(200,resposta.getStatusCode().value());

        ContaBancaria contaAtualizada = (ContaBancaria) resposta.getBody();
        assertNotNull(contaAtualizada);
        assertEquals(new BigDecimal("150.00"),contaAtualizada.getSaldo());

        //Verificando se o repositorio chamou o save
        verify(contaBancariaRepository, times(1)).save(contaSimulada);
        verify(transacaoRepository,times(1)).save(any());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar depositar em um conta inexsitente ")
    void deveRetornarErroQuandoContaNaoExistir(){
        // Arrange - preparação dos dados
        Long contaIdInexsitente = 99L;

        //Ensinando o mockito a buscar
        when(contaBancariaRepository.findById(contaIdInexsitente)).thenReturn(Optional.empty());

        //criando o payload
        Map<String,BigDecimal> payload = Map.of("valor",new BigDecimal("50.00"));

        // Act - executando o teste
        ResponseEntity<?> resposta = transacaoController.depositar(contaIdInexsitente,payload);

        // Assert
        //Validando se o stats foi reportado
        assertEquals(404,resposta.getStatusCode().value());

        //Validando se a mensagem de erro retornada no corpo esta correta
        assertEquals("Conta não encontrada", resposta.getBody());

        //Verifica que nenhum salvamento foi feito nos repositorios
        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());

    }

    //------- Saque
    @Test
    @DisplayName("Deve realizar saque com sucesso quando houver saldo suficiente")
    void deveSacarComSucesso(){
        //Arrange
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("100.00"));

        //Treinando o mockito para retornar a resposta correta
        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        //payload do saque
        Map<String, BigDecimal> payload = Map.of("valor", new BigDecimal("40.00"));

        // Act
        ResponseEntity<?> resposta = transacaoController.saque(contaId,payload);

        //Assert
        assertEquals(200,resposta.getStatusCode().value());

        ContaBancaria contaAtualizada = (ContaBancaria) resposta.getBody();
        assertNotNull(contaAtualizada);
        assertEquals(new BigDecimal("60.00"), contaAtualizada.getSaldo());

        verify(contaBancariaRepository, times(1)).save(contaSimulada);
        verify(transacaoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve recusar saque quando o saldo for insuficiente")
    void deveBarrarQuandoSaldoInsuficiente(){
        //Arrange
        Long contaId = 1L;

        ContaBancaria contaSimulada = new ContaBancaria();
        contaSimulada.setId(contaId);
        contaSimulada.setSaldo(new BigDecimal("20.00"));

        when(contaBancariaRepository.findById(contaId)).thenReturn(Optional.of(contaSimulada));

        //Tentando sacar R$ 50.00 tendo apenas R$ 20.00
        Map<String, BigDecimal> payload = Map.of("valor",new BigDecimal("50.00"));

        //ACT
        ResponseEntity<?> resposta = transacaoController.saque(contaId,payload);

        //Assert
        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("O valor de saque é maior que o saldo em conta", resposta.getBody());

        //Garante que o saldo permeceu sem ser alterado
        assertEquals(new BigDecimal("20.00"), contaSimulada.getSaldo());

        //Verifica que nenhuma ação goi gravada nos repositórios
        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar sacar em uma conta inexistente")
    void deveRetornarErroAoSacarContaInexistente(){
        //Arrange
        Long contaIdInexistente = 99L;

        when(contaBancariaRepository.findById(contaIdInexistente)).thenReturn(Optional.empty());

        Map<String,BigDecimal> payload = Map.of("valor", new BigDecimal("50.00"));

        //ACT
        ResponseEntity<?> resposta = transacaoController.saque(contaIdInexistente,payload);

        //ASSERT
        assertEquals(404,resposta.getStatusCode().value());
        assertEquals("Conta não encontrada",resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    //------ Transferência
    @Test
    @DisplayName("Deve transferir com sucesso")
    void deveTransferirComSucesso(){
        //Arrange
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

        Map<String, Object> payload = Map.of(
                "contaOrigemId",idOrigem,
                "contaDestinoId", idDestino,
                "valor", new BigDecimal("100.00")
        );

        //ACT
        ResponseEntity<?> resposta = transacaoController.transferir(payload);

        //Assert
        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(new BigDecimal("100.00"), contaOrigem.getSaldo());
        assertEquals(new BigDecimal("150.00"), contaDestino.getSaldo());

        verify(contaBancariaRepository, times(1)).save(contaOrigem);
        verify(contaBancariaRepository, times(1)).save(contaDestino);
        verify(transacaoRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta de destino não existir")
    void deveBarrarTransferenciaContaDestinoInexistente(){
        //Arrange
        Long idOrigem = 1L;
        Long idDestinoInexistente = 99L;

        ContaBancaria contaOrigem = new ContaBancaria();
        contaOrigem.setId(idOrigem);
        contaOrigem.setSaldo(new BigDecimal("200.00"));

        when(contaBancariaRepository.findById(idOrigem)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(idDestinoInexistente)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
                "contaOrigemId",idOrigem,
                "contaDestinoId", idDestinoInexistente,
                "valor", new BigDecimal("100.00")
        );
        
        //ACT 
        ResponseEntity<?> resposta = transacaoController.transferir(payload);
        
        //Assert 
        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta de destino não foi encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta de origem não existir")
    void deveBarrarTransferenciaContaOrigemInexistente(){
        //Arrange
        Long idOrigemInexistente = 99L;
        Long idDestino = 1L;

        ContaBancaria contaDestino = new ContaBancaria();
        contaDestino.setId(idDestino);
        contaDestino.setSaldo(new BigDecimal("50.00"));

        when(contaBancariaRepository.findById(idOrigemInexistente)).thenReturn(Optional.empty());
        when(contaBancariaRepository.findById(idDestino)).thenReturn(Optional.of(contaDestino));

        Map<String, Object> payload = Map.of(
                "contaOrigemId",idOrigemInexistente,
                "contaDestinoId", idDestino,
                "valor", new BigDecimal("100.00")
        );

        //ACT
        ResponseEntity<?> resposta = transacaoController.transferir(payload);

        //Assert
        assertEquals(404,resposta.getStatusCode().value());
        assertEquals("Conta de origem não foi encontrada", resposta.getBody());

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());

    }

    @Test
    @DisplayName("Deve recusar transferência quando a conta de origem não tiver saldo suficiente")
    void deveBarrarTransferenciaComSaldoInsuficiente(){
        // Arrange
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

        Map<String, Object> payload = Map.of(
            "contaOrigemId", idOrigem,
            "contaDestinoId", idDestino,
            "valor", new BigDecimal("100.00")
        );

        // ACT
        ResponseEntity<?> resposta = transacaoController.transferir(payload);

        //Assert
        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("Saldo insuficiente na conta de origem", resposta.getBody());

        //Garante que os saldos não foram alterados
        assertEquals(new BigDecimal("20.00"), contaOrigem.getSaldo());
        assertEquals(new BigDecimal("40.00"), contaDestino.getSaldo());

        //Garante que nada foi salvo no banco de dados
        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todas as transações com sucesso (GET sem ID)")
    void deveListarTodasAsTransacoes() {
        // --- 1. ARRANGE ---
        List<Transacao> listaSimulada = List.of(new Transacao(), new Transacao());
        when(transacaoRepository.findAll()).thenReturn(listaSimulada);

        // --- 2. ACT ---
        List<Transacao> listaRetornada = transacaoController.listarTodas();

        // --- 3. ASSERT ---
        assertNotNull(listaRetornada);
        assertEquals(2, listaRetornada.size());
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso (GET com ID)")
    void deveBuscarTransacaoPorIdComSucesso() {
        //ARRANGE
        Long contaId = 1L;
        Transacao transacaoSimulada = new Transacao();
        transacaoSimulada.setId(100L);

        // Mock faz a verificação de existência da conta
        when(contaBancariaRepository.existsById(contaId)).thenReturn(true);

        // Mock faz busca das transações pelo ID da conta
        when(transacaoRepository.findByContaId(contaId)).thenReturn(List.of(transacaoSimulada));

        //ACT
        ResponseEntity<?> resposta = transacaoController.listarContaPorId(contaId);

        //ASSERT
        assertEquals(200, resposta.getStatusCode().value());

        List<?> extrato = (List<?>) resposta.getBody();
        assertNotNull(extrato);
        assertEquals(1, extrato.size());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar transação por ID inexistente")
    void deveRetornar404AoBuscarTransacaoInexistente() {
        //ARRANGE
        Long contaIdInexistente = 99L;

        when(contaBancariaRepository.existsById(contaIdInexistente)).thenReturn(false);

        //ACT
        ResponseEntity<?> resposta = transacaoController.listarContaPorId(contaIdInexistente);

        //ASSERT
        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta não encontrada", resposta.getBody());
    }
}

