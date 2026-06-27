-- 1. Administrador Geral (Cargo: Administrador, Departamento: COPER)
-- Senha original: admin123
INSERT INTO usuarios (nome, cpf, email, senha, tentativas_falhas, bloqueado_ate) 
VALUES ('Administrador Geral', '000.000.000-00', 'admin@fmpsc.edu.br', '$2a$10$msgShVeDL9KCBr89ToEeke3ZvxYw2Ul6QsC.5H0thD4Y7UemI.zBS', 0, NULL);

INSERT INTO funcionarios (id, departamento, cargo) 
VALUES (LAST_INSERT_ID(), 'COPER', 'Administrador');

-- 2. Aluno Davi
-- Senha original: davi123
INSERT INTO usuarios (nome, cpf, email, senha, tentativas_falhas, bloqueado_ate) 
VALUES ('Davi', '111.111.111-11', 'davi@aluno.fmpsc.edu.br', '$2a$10$yDSG8/NNll0eOOXXBujQUO/beqHb1tU1UUTbjpVp9XxmoI8aXTnYG', 0, NULL);

INSERT INTO alunos (id, matricula, curso, saldo_horas) 
VALUES (LAST_INSERT_ID(), '19875', 'Análise e Desenvolvimento de Sistemas', 0.0);

-- 3. Aluno Eduardo
-- Senha original: eduardo123
INSERT INTO usuarios (nome, cpf, email, senha, tentativas_falhas, bloqueado_ate) 
VALUES ('Eduardo', '222.222.222-22', 'eduardo@aluno.fmpsc.edu.br', '$2a$10$VgD61OxdV8GwQsT0nAkyZedcD15vARjM4QIbUWd29EGqjBNPBnJW2', 0, NULL);

INSERT INTO alunos (id, matricula, curso, saldo_horas) 
VALUES (LAST_INSERT_ID(), '19876', 'Análise e Desenvolvimento de Sistemas', 0.0);

-- 4. Aluno Lucas Schutz
-- Senha original: lucas123
INSERT INTO usuarios (nome, cpf, email, senha, tentativas_falhas, bloqueado_ate) 
VALUES ('Lucas Schutz', '333.333.333-33', 'lucas@aluno.fmpsc.edu.br', '$2a$10$M6UkKvKDq3n8HGYv96ei/.EuXbz8sX1Ro1n7RSvgMLRRKUC.1zqNC', 0, NULL);

INSERT INTO alunos (id, matricula, curso, saldo_horas) 
VALUES (LAST_INSERT_ID(), '19877', 'Análise e Desenvolvimento de Sistemas', 0.0);

-- 5. Aluno Vitor
-- Senha original: vitor123
INSERT INTO usuarios (nome, cpf, email, senha, tentativas_falhas, bloqueado_ate) 
VALUES ('Vitor', '444.444.444-44', 'vitor@aluno.fmpsc.edu.br', '$2a$10$uoGXR34Z6Td97IagAxamKeqTnP05xmO6S7I65FMqYTSyy9vGJnKui', 0, NULL);

INSERT INTO alunos (id, matricula, curso, saldo_horas) 
VALUES (LAST_INSERT_ID(), '19878', 'Análise e Desenvolvimento de Sistemas', 0.0);
