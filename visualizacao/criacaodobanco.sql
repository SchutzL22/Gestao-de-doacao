CREATE DATABASE IF NOT EXISTS db_gestaodedoacao;
USE db_gestaodedoacao;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(14) UNIQUE,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tentativas_falhas INT DEFAULT 0,
    bloqueado_ate DATETIME NULL
);

CREATE TABLE funcionarios (
    id INT PRIMARY KEY,
    departamento VARCHAR(50),
    cargo VARCHAR(50),
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE alunos (
    id INT PRIMARY KEY,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    curso VARCHAR(100),
    saldo_horas DECIMAL(10,2) DEFAULT 0.0,
    FOREIGN KEY (id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE campanhas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_limite DATE,
    status VARCHAR(20),
    regras_conversao TEXT,
    limite_horas DECIMAL(10,2)
);

CREATE TABLE itens_prioritarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    campanha_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    meta INT,
    urgencia VARCHAR(20),
    FOREIGN KEY (campanha_id) REFERENCES campanhas(id) ON DELETE CASCADE
);

CREATE TABLE doacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT NOT NULL,
    campanha_id INT,
    func_registro_id INT,
    func_validacao_id INT,
    data DATE,
    quantidade INT,
    tipo_item VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Pendente',
    foto_evidencia BLOB,
    motivo_reprovacao TEXT,
    horas_concedidas DECIMAL(10,2),
    horario_doacao TIME,
    retencao_anos INT,
    FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    FOREIGN KEY (campanha_id) REFERENCES campanhas(id),
    FOREIGN KEY (func_registro_id) REFERENCES funcionarios(id),
    FOREIGN KEY (func_validacao_id) REFERENCES funcionarios(id)
);

CREATE TABLE anexos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doacao_id INT NOT NULL,
    nome_arquivo VARCHAR(255),
    caminho VARCHAR(255),
    FOREIGN KEY (doacao_id) REFERENCES doacoes(id) ON DELETE CASCADE
);

CREATE TABLE certificados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doacao_id INT NOT NULL UNIQUE,
    token VARCHAR(255) UNIQUE NOT NULL,
    caminho_arquivo VARCHAR(255),
    data_emissao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doacao_id) REFERENCES doacoes(id) ON DELETE CASCADE
);

CREATE TABLE notificacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT NOT NULL,
    doacao_id INT,
    mensagem TEXT,
    data_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    canal VARCHAR(50),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    FOREIGN KEY (doacao_id) REFERENCES doacoes(id)
);

CREATE TABLE registros_edicao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doacao_id INT NOT NULL,
    funcionario_id INT NOT NULL,
    data DATETIME DEFAULT CURRENT_TIMESTAMP,
    campo_alterado VARCHAR(100),
    valor_antigo TEXT,
    valor_novo TEXT,
    FOREIGN KEY (doacao_id) REFERENCES doacoes(id),
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
);
<<<<<<< HEAD


ALTER TABLE doacoes ADD COLUMN local_entrega VARCHAR(255);
=======
>>>>>>> 06548224ef54506879a4dd7f88273b8e56b07eb7
