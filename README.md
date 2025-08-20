# Projeto Task Board em Java

![Java](https://img.shields.io/badge/Java-17-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![Maven](https://img.shields.io/badge/Maven-3.8-red)
![Status](https://img.shields.io/badge/Status-Concluído-brightgreen)
- Projeto para o Bootcamp Santander DIO 2025 -
Um sistema de gerenciamento de tarefas (Task Board) baseado em console, desenvolvido em Java com persistência de dados em um banco de dados MySQL. O projeto permite a criação de múltiplos quadros (boards) customizáveis para acompanhar o fluxo de trabalho de diversas tarefas.

## 🚀 Funcionalidades

### Gerenciamento de Boards
- **Criar Boards**: Crie novos quadros para diferentes projetos ou contextos.
- **Selecionar Boards**: Escolha um board existente para visualizar e manipular suas tarefas.
- **Excluir Boards**: Remova boards que não são mais necessários (esta ação remove também todas as colunas e cards associados).

### Colunas Customizáveis
- **Estrutura Flexível**: Cada board é composto por colunas que representam as etapas de um fluxo de trabalho.
- **Tipos de Colunas**:
  - **`INICIAL`**: Coluna obrigatória onde todas as novas tarefas são criadas.
  - **`PENDENTE`**: Colunas intermediárias para tarefas em andamento. É possível ter várias.
  - **`FINAL`**: Coluna obrigatória para tarefas concluídas.
  - **`CANCELAMENTO`**: Coluna obrigatória para tarefas canceladas.
- **Ordem Definida**: A ordem das colunas é respeitada, garantindo um fluxo de trabalho lógico e sequencial.

### Gerenciamento de Cards (Tarefas)
- **Criar Cards**: Adicione novas tarefas com título e descrição na coluna inicial de um board.
- **Mover Cards**: Avance os cards entre as colunas, seguindo a ordem predefinida.
- **Cancelar Cards**: Mova um card de qualquer coluna (exceto a final) diretamente para a coluna de cancelamento.
- **Bloquear/Desbloquear Cards**: Bloqueie um card com um motivo específico para impedir sua movimentação e desbloqueie-o com uma justificativa.

### Funcionalidades Avançadas (Opcionais Implementadas)
- **Histórico de Movimentação**: O sistema armazena a data e hora em que um card entra e sai de cada coluna.
- **Histórico de Bloqueios**: Registra o motivo e a duração de cada bloqueio de um card.
- **Relatórios**:
  - **Relatório de Tempo**: Gera um relatório detalhado sobre o tempo que cada tarefa levou para ser concluída, incluindo o tempo gasto em cada coluna.
  - **Relatório de Bloqueios**: Mostra o histórico de bloqueios de um board, com justificativas e o tempo total que cada card ficou bloqueado.

## 🛠️ Tecnologias Utilizadas

- **Java 17 (ou superior)**: Linguagem principal do projeto.
- **MySQL 8.0**: Sistema de gerenciamento de banco de dados para persistência dos dados.
- **JDBC (Java Database Connectivity)**: API para conexão e execução de comandos no banco de dados.
- **Maven**: Ferramenta de automação de compilação e gerenciamento de dependências.

## ⚙️ Pré-requisitos

Antes de começar, você vai precisar ter instalado em sua máquina:
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) - Versão 17 ou superior.
- [Apache Maven](https://maven.apache.org/download.cgi)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)

## 🔧 Instalação e Configuração

Siga os passos abaixo para executar o projeto localmente.

**1. Clone o repositório:**
```bash
git clone [https://github.com/seu-usuario/nome-do-repositorio.git](https://github.com/seu-usuario/nome-do-repositorio.git)
cd nome-do-repositorio
```
**2. Configure o Banco de Dados:**

Conecte-se ao seu servidor MySQL.

Crie o banco de dados e as tabelas executando o script SQL abaixo.

<details>
<summary><strong>Clique para ver o Script SQL Completo</strong></summary>

SQL
```
-- Cria o banco de dados, caso não exista.
CREATE DATABASE IF NOT EXISTS task_board_db;

-- Seleciona o banco de dados para uso.
USE task_board_db;

-- Tabela para armazenar os Boards (Quadros).
CREATE TABLE IF NOT EXISTS boards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela para armazenar as Colunas de cada Board.
CREATE TABLE IF NOT EXISTS columns (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    board_id INT NOT NULL,
    column_order INT NOT NULL,
    type ENUM('INICIAL', 'PENDENTE', 'FINAL', 'CANCELAMENTO') NOT NULL,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

-- Tabela para armazenar os Cards (Tarefas).
CREATE TABLE IF NOT EXISTS cards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_blocked BOOLEAN DEFAULT FALSE,
    column_id INT NOT NULL,
    FOREIGN KEY (column_id) REFERENCES columns(id) ON DELETE CASCADE
);

-- Tabela para o Histórico de Movimentação dos Cards
CREATE TABLE IF NOT EXISTS card_movement_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_id INT NOT NULL,
    column_id INT NOT NULL,
    entry_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP NULL,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    FOREIGN KEY (column_id) REFERENCES columns(id) ON DELETE CASCADE
);

-- Tabela para o Histórico de Bloqueio dos Cards
CREATE TABLE IF NOT EXISTS card_block_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_id INT NOT NULL,
    block_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unblock_time TIMESTAMP NULL,
    block_reason TEXT NOT NULL,
    unblock_reason TEXT,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);
```
</details>
<br>

**3. Configure a Conexão:**

Na pasta src/main/resources, renomeie o arquivo db.properties.example para db.properties.

Abra o arquivo db.properties e insira suas credenciais do MySQL:

Properties
```
dburl=jdbc:mysql://localhost:3306/task_board_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
user=SEU_USUARIO_AQUI
password=SUA_SENHA_AQUI
```
Importante: O arquivo db.properties está no .gitignore e não deve ser enviado para o repositório.

**4. Compile o Projeto:**

Use o Maven para compilar o projeto e baixar as dependências.

Bash

mvn clean package
▶️ Como Usar
Após a compilação, você pode executar a aplicação a partir da linha de comando:

Bash
```
java -jar target/nome-do-seu-arquivo.jar
```
(Substitua nome-do-seu-arquivo.jar pelo nome do arquivo JAR gerado na pasta target)


A aplicação iniciará no console, exibindo o menu principal. Siga as instruções na tela para criar e gerenciar seus boards e tarefas.

🎴
