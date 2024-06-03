package com.tcc.aplicacao.entities;

import java.sql.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Pessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    private Date dataNascimento;
    private Double horasMensais;
    private String email;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Usuario usuario;

    public Pessoa() {
    }

    public Pessoa(int id, String nomeCompleto, String cpf, String telefone, Date dataNascimento, Double horasMensais,
            String email, Usuario usuario) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.horasMensais = horasMensais;
        this.email = email;
        this.usuario = usuario;
    }
}
