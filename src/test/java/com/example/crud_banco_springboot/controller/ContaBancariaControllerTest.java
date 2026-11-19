package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.Cliente;
import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.repository.ClienteRepository;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import org.apache.coyote.Response;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContaBancariaControllerTest {

    @Mock
    private ContaBancariaRepository repository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks ContaBancariaController contaBancariaController;

    //Criar conta
    @Test
    @DisplayName("Deve criar uma conta bancaria com sucesso")
    void deveCriarContaComSucesso(){
        Long clienteId = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        ContaBancaria contaBancaria = new ContaBancaria();
        contaBancaria.setCliente(cliente);

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(repository.save(any(ContaBancaria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> resposta = contaBancariaController.criar(contaBancaria);

        assertEquals(201, resposta.getStatusCode().value());
        ContaBancaria contaCriada = (ContaBancaria) resposta.getBody();

        assertNotNull(contaCriada);
        assertEquals(BigDecimal.ZERO, contaCriada.getSaldo());
        assertEquals("0001", contaCriada.getAgencia());
        assertNotNull(contaCriada.getNumeroConta());
        assertEquals(cliente,contaCriada.getCliente());

        verify(repository, times(1)).save(any(ContaBancaria.class));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar criar conta para cliente inexistente")
    void deveRetornar404AoCriarContaParaClienteInexistente(){
        Long clienteIdInexistente = 99L;
        Cliente clienteInexistente = new Cliente();
        clienteInexistente.setId(clienteIdInexistente);

        ContaBancaria conta = new ContaBancaria();
        conta.setCliente(clienteInexistente);

        when(clienteRepository.findById(clienteIdInexistente)).thenReturn(Optional.empty());

        ResponseEntity<?> resposta = contaBancariaController.criar(conta);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Cliente não encontrado", resposta.getBody());

        verify(repository, never()).save(any());
    }

    //GET -> LISTAGEM
    @Test
    @DisplayName("Deve listar todas as contas com sucesso")
    void deveLitarTodasAsContas(){
        List<ContaBancaria> listaSimulada = List.of(new ContaBancaria(), new ContaBancaria());
        when(repository.findAll()).thenReturn(listaSimulada);

        ResponseEntity<?> resposta = contaBancariaController.listarOuBuscarContaPorId(null);

        assertEquals(200, resposta.getStatusCode().value());
        List<?> contasRetornadas = (List<?>) resposta.getBody();
        assertNotNull(contasRetornadas);
        assertEquals(2, contasRetornadas.size());
    }

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    void deveBuscarContaPorIdComSucesso(){
        Long contaId = 1L;
        ContaBancaria conta = new ContaBancaria();
        conta.setId(contaId);

        when(repository.findById(contaId)).thenReturn(Optional.of(conta));

        ResponseEntity<?> resposta = contaBancariaController.listarOuBuscarContaPorId(contaId);

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(conta, resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar conta com ID inexistente")
    void deveRetornar404AoBuscarContaInexistente() {
        Long contaIdInexistente = 99L;
        when(repository.findById(contaIdInexistente)).thenReturn(Optional.empty());

        ResponseEntity<?> resposta = contaBancariaController.listarOuBuscarContaPorId(contaIdInexistente);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Conta não encontrado", resposta.getBody());
    }
}
