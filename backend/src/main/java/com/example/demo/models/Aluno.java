package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "alunos")
public class Aluno extends Usuario {

    private String matricula;
    private String curso;
    private Double saldoHoras;

    public Aluno() {
        super();
    }

    public Aluno(String nome, String cpf, String email, String senha, String matricula, String curso, Double saldoHoras) {
        super(nome, cpf, email, senha);
        this.matricula = matricula;
        this.curso = curso;
        this.saldoHoras = saldoHoras;
    }

    // Getters and Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Double getSaldoHoras() {
        return saldoHoras;
    }

    public void setSaldoHoras(Double saldoHoras) {
        this.saldoHoras = saldoHoras;
    }
}
