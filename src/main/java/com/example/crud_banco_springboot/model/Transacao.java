package com.example.crud_banco_springboot.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tb_transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipoTransacao;

    private BigDecimal valor;
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private ContaBancaria conta;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public TipoTransacao getTipoTransacao() {return tipoTransacao;}
    public void setTipoTransacao(TipoTransacao tipoTransacao) {this.tipoTransacao = tipoTransacao;}

    public BigDecimal getValor() {return valor;}
    public void setValor(BigDecimal valor) {this.valor = valor;}

    public LocalDateTime getDataHora() {return dataHora;}
    public void setDataHora(LocalDateTime dataHora) {this.dataHora = dataHora;}

    public ContaBancaria getConta() {return conta;}
    public void setConta(ContaBancaria conta) {this.conta = conta;}
}
