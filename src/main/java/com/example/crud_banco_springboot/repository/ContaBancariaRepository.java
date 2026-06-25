package com.example.crud_banco_springboot.repository;

import com.example.crud_banco_springboot.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
}
