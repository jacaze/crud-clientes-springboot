package com.example.crud_banco_springboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logradouro;
    private Integer numero;
    private String bairro;
    private String cep;
    private String municipio;

    public Endereco() {
    }

    public Endereco(Long id, String logradouro, int numero, String bairro, String cep, String municipio) {
        this.id = id;
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.municipio = municipio;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getLogradouro() {return logradouro;}
    public void setLogradouro(String logradouro) {this.logradouro = logradouro;}

    public int getNumero() {return numero;}
    public void setNumero(int numero) {this.numero = numero;}

    public String getBairro() {return bairro;}
    public void setBairro(String bairro) {this.bairro = bairro;}

    public String getCep() {return cep;}
    public void setCep(String cep) {this.cep = cep;}

    public String getMunicipio() {return municipio;}
    public void setMunicipio(String municipio) {this.municipio = municipio;}
}
