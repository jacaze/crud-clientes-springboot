package com.example.crud_banco_springboot.repository;

import com.example.crud_banco_springboot.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao,Long> {

    List<Transacao> findByContaId(Long contaId);
}
