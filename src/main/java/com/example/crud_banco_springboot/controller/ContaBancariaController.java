package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.Cliente;
import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.repository.ClienteRepository;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/contas")
public class ContaBancariaController {

    @Autowired
    private ContaBancariaRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    //Rota de cadastro para a conta
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ContaBancaria conta){

        //verificando se o cliente usado na abertura da conta existe
        Optional<Cliente> clienteOptional = clienteRepository.findById(conta.getCliente().getId());

        //se clienteOptional vazio n existe um cliente
        if (clienteOptional.isEmpty()){
            return  ResponseEntity.status(404).body("Cliente não encontrado");
        }

        //se o cliente existir associamos ele a conta
        conta.setCliente(clienteOptional.get());

        //definindo o saldo como inicial como zero
        conta.setSaldo(BigDecimal.ZERO);

        //Definindo uma agencia padrão para o banco
        conta.setAgencia("0001");

        //Gerando um número aleatório de 5 digitos para a conta
        Random random = new Random();
        int numero = 10000 + random.nextInt(90000);
        conta.setNumeroConta(String.valueOf(numero));

        ContaBancaria contaSalva = repository.save(conta);
        return ResponseEntity.status(201).body(contaSalva);
    }

    //Rota de Listagem
    @GetMapping
    public List<ContaBancaria> listar(){
        return repository.findAll();
    }
}
