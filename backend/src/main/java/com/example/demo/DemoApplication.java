package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
/*
[Front-End] 
     ↓ (HTTP GET /api/usuarios)
[Controller] 
     ↓ (chama método do serviço)
[Service] 
     ↓ (chama método do repositório)
[Repository] 
     ↓ (executa consulta no banco usando o Model)
[Model + Banco de Dados]
     ↑ (retorna entidade/entidades)
[Repository]
     ↑ (retorna lista de entidades)
[Service]
     ↑ (processa se necessário)
[Controller]
     ↑ (converte para DTO ou retorna diretamente)
[Front-End] ← (resposta JSON)
 * 
 */