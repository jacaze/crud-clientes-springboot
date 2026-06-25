package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.Cliente;
import com.example.crud_banco_springboot.model.Endereco;
import com.example.crud_banco_springboot.repository.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // Avisa ao Spring que esta classe é uma API Web
@RequestMapping("/clientes") // Define a URL base (ex: http://localhost:8080/clientes)
public class ClienteController {

    @Autowired // Injeção de Dependência: o Spring gerencia e entrega o repository pronto para uso
    private ClienteRepository repository;

    // Rota para CADASTRAR um cliente
    @PostMapping
    public Cliente criar(@Valid @RequestBody Cliente cliente) {
        return repository.save(cliente);
    }

    // Rota para LISTAR todos os clientes
    @GetMapping
    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    // Rota de atualização
    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizarParcial(@PathVariable Long id, @RequestBody Cliente dadosAtualizados){
        //Verifica se o cliente já possui cadastro no banco de dados
        Optional<Cliente> clienteOptional = repository.findById(id);

        //Se não existir, impede a requisição
        if(clienteOptional.isEmpty()){
            return ResponseEntity.status(404).body("Erro: Cliente com o ID "+id+" não possui cadastro");
        }

        //Caso o cliente exista, pegamos o cliente antigo que vio do banco
        Cliente clienteExistente = clienteOptional.get();

        //Atualizamos os campos permitidos
        if (dadosAtualizados.getNome() != null && !dadosAtualizados.getNome().isBlank()) {
            clienteExistente.setNome(dadosAtualizados.getNome());
        }

        if (dadosAtualizados.getTelefone() != null && !dadosAtualizados.getTelefone().isBlank()) {
            clienteExistente.setTelefone(dadosAtualizados.getTelefone());
        }

        // Atualização do endereço protegendo os dados existentes
        if (dadosAtualizados.getEndereco() != null && clienteExistente.getEndereco() != null) {
            // Pegamos o endereço que já está salvo no banco de dados
            Endereco enderecoDoBanco = clienteExistente.getEndereco();
            // Pegamos os novos dados enviados no JSON
            Endereco enderecoEnviado = dadosAtualizados.getEndereco();

            // Atualizamos apenas o que não veio nulo/vazio
            if (enderecoEnviado.getLogradouro() != null && !enderecoEnviado.getLogradouro().isBlank()) {
                enderecoDoBanco.setLogradouro(enderecoEnviado.getLogradouro());
            }
            if (enderecoEnviado.getNumero() != 0) {
                enderecoDoBanco.setNumero(enderecoEnviado.getNumero());
            }
            if (enderecoEnviado.getCep() != null && !enderecoEnviado.getCep().isBlank()) {
                enderecoDoBanco.setCep(enderecoEnviado.getCep());
            }

        }

        //Salvamos o cliente com os dados atualizados
        Cliente clienteSalvo = repository.save(clienteExistente);

        return ResponseEntity.ok(clienteSalvo);
    }
}