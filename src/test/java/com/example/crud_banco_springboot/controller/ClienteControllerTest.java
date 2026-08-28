package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.Cliente;
import com.example.crud_banco_springboot.model.Endereco;
import com.example.crud_banco_springboot.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteControllerTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteController clienteController;


    // Cadastrar
    @Test
    @DisplayName("Deve criar um cliente com sucesso")
    void deveCriarClienteComSucesso(){
        Cliente cliente = new Cliente();
        cliente.setNome("Jose Aldo");

        when(repository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteController.criar(cliente);

        assertNotNull(resultado);
        assertEquals("Jose Aldo", resultado.getNome());
        verify(repository, times(1)).save(cliente);
    }

    // Listar/Buscar

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso (GET sem id)")
    void deveListarTodosOsClientes(){
        //Arrange
        List<Cliente> listaSimulada = List.of(new Cliente(), new Cliente());
        when(repository.findAll()).thenReturn(listaSimulada);

        //ACT
        ResponseEntity<?> resposta = clienteController.listarOuBuscarPorId(null);

        assertEquals(200, resposta.getStatusCode().value());
        List<?> listaRetornada = (List<?>) resposta.getBody();
        assertNotNull(listaRetornada);
        assertEquals(2, listaRetornada.size());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso (GET com ID")
    void deveBuscarClientePorIdComSucesso(){
        Long id = 1L;
        Cliente cliente = new Cliente();
        cliente.setNome("Guilherme");

        when(repository.findById(id)).thenReturn(Optional.of(cliente));

        ResponseEntity<?> resposta = clienteController.listarOuBuscarPorId(id);

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(cliente, resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar cliente inexistente")
    void deveRetornar404AoBuscarClienteInexistente(){
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> resposta = clienteController.listarOuBuscarPorId(id);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("Cliente não encontrado", resposta.getBody());
    }

    // Atualização
    @Test
    @DisplayName("Deve atualizar cliente com sucesso (PATCH)")
    void deveAtualizarClienteComSucesso(){
        Long id = 1L;

        Endereco enderecoBanco = new Endereco();
        enderecoBanco.setLogradouro("Rua Antiga");
        enderecoBanco.setNumero(100);

        Cliente clienteBanco = new Cliente();
        clienteBanco.setNome("Nome antigo");
        clienteBanco.setEndereco(enderecoBanco);

        Endereco enderecoNovo = new Endereco();
        enderecoNovo.setLogradouro("Rua nova");
        enderecoNovo.setNumero(200);

        Cliente dadosAtualizados = new Cliente();
        dadosAtualizados.setNome("Nome novo");
        dadosAtualizados.setEndereco(enderecoNovo);

        when(repository.findById(id)).thenReturn(Optional.of(clienteBanco));
        when(repository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<?> resposta = clienteController.atualizarParcial(id,dadosAtualizados);

        assertEquals(200, resposta.getStatusCode().value());
        Cliente clienteSalvo = (Cliente) resposta.getBody();
        assertNotNull(clienteSalvo);
        assertEquals("Nome novo", clienteSalvo.getNome());
        assertEquals("Rua nova", clienteSalvo.getEndereco().getLogradouro());
        verify(repository, times(1)).save(clienteBanco);
    }
}
