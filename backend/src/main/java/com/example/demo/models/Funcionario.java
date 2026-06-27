package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionarios")
public class Funcionario extends Usuario {

    private String departamento;
    private String cargo;

    public Funcionario() {
        super();
    }

    public Funcionario(String nome, String cpf, String email, String senha, String departamento, String cargo) {
        super(nome, cpf, email, senha);
        this.departamento = departamento;
        this.cargo = cargo;
    }

    // Getters and Setters
    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
