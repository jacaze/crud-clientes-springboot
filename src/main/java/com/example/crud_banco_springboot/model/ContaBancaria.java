package com.example.crud_banco_springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_conta_bancaria")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroConta;
    private String agencia;
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    private TipoConta tipoConta;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties({"endereco", "cpf", "telefone"})
    private Cliente cliente;

    public ContaBancaria() {
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNumeroConta() {return numeroConta;}
    public void setNumeroConta(String numeroConta) {this.numeroConta = numeroConta;}

    public String getAgencia() {return agencia;}
    public void setAgencia(String agencia) {this.agencia = agencia;}

    public BigDecimal getSaldo() {return saldo;}
    public void setSaldo(BigDecimal saldo) {this.saldo = saldo;}

    public TipoConta getTipoConta() {return tipoConta;}
    public void setTipoConta(TipoConta tipoConta) {this.tipoConta = tipoConta;}

    public Cliente getCliente() {return cliente;}
    public void setCliente(Cliente cliente) {this.cliente = cliente;}
}
