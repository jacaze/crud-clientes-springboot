package com.example.crud_banco_springboot.controller;

import com.example.crud_banco_springboot.model.ContaBancaria;
import com.example.crud_banco_springboot.repository.ContaBancariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/contas")
public class ContaBancariaController {

    @Autowired
    private ContaBancariaRepository repository;

    //Rota de cadastro para a conta
    @PostMapping
    public ContaBancaria criar(@RequestBody ContaBancaria conta){
        //definindo o saldo como inicial como zero
        conta.setSaldo(BigDecimal.ZERO);

        //Definindo uma agencia padrão para o banco
        conta.setAgencia("0001");

        //Gerando um número aleatório de 5 digitos para a conta
        Random random = new Random();
        int numero = 10000 + random.nextInt(90000);
        conta.setNumeroConta(String.valueOf(numero));

        return repository.save(conta);
    }

    //Rota de Listagem
    @GetMapping
    public List<ContaBancaria> listar(){
        return repository.findAll();
    }
}
