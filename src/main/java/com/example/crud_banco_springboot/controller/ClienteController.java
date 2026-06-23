package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.Cliente;
import com.example.crud_banco_springboot.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Avisa ao Spring que esta classe é uma API Web
@RequestMapping("/clientes") // Define a URL base (ex: http://localhost:8080/clientes)
public class ClienteController {

    @Autowired // Injeção de Dependência: o Spring gerencia e entrega o repository pronto para uso
    private ClienteRepository repository;

    // 1. Rota para CADASTRAR um cliente
    @PostMapping
    public Cliente criar(@RequestBody Cliente cliente) {
        return repository.save(cliente);
    }

    // 2. Rota para LISTAR todos os clientes
    @GetMapping
    public List<Cliente> listarTodos() {
        return repository.findAll();
    }
}