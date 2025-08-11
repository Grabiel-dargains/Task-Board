package com.taskboard;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

import com.taskboard.dao.BoardDAO;
import com.taskboard.dao.CardDAO;
import com.taskboard.dao.ColumnDAO;
import com.taskboard.db.DB;
import com.taskboard.db.DBException;
import com.taskboard.model.Board;
import com.taskboard.model.Column;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connection conn = DB.getConnection();
        BoardDAO boardDAO = new BoardDAO(conn);
        CardDAO cardDAO = new CardDAO(conn);
        ColumnDAO columnDAO = new ColumnDAO(conn);

        int choice;
        do {
            showMainMenu();
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    createNewBoard(sc, boardDAO);
                    break;
                case 2:
                    selectBoard(sc, boardDAO);
                    break;
                case 3:
                    deleteBoard(sc, boardDAO);
                    break;
                case 4:
                    System.out.println("Obrigado por usar o Task Board!");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        } while (choice != 4);

        sc.close();
        DB.closeConnection();
    }

    private static void showMainMenu() {
        System.out.println("\n--- TASK BOARD - MENU PRINCIPAL ---");
        System.out.println("1 - Criar novo board");
        System.out.println("2 - Selecionar board");
        System.out.println("3 - Excluir board");
        System.out.println("4 - Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void createNewBoard(Scanner sc, BoardDAO boardDAO) {
        System.out.print("Digite o nome do novo board: ");
        String boardName = sc.nextLine();
        
        Board newBoard = new Board();
        newBoard.setName(boardName);
        boardDAO.create(newBoard);
        
        System.out.println("Board '" + newBoard.getName() + "' criado com ID: " + newBoard.getId());
        
        // **AÇÃO NECESSÁRIA:**
        // Aqui você deve chamar um método para criar as 3 colunas obrigatórias:
        // 1. Pedir o nome da coluna INICIAL (ordem 1)
        // 2. Pedir o nome da coluna FINAL (ordem 2)
        // 3. Pedir o nome da coluna CANCELAMENTO (ordem 3)
        // E salvá-las no banco usando o ColumnDAO.
    }

    private static void selectBoard(Scanner sc, BoardDAO boardDAO) {
        List<Board> boards = boardDAO.findAll();
        if (boards.isEmpty()) {
            System.out.println("Nenhum board para selecionar. Crie um primeiro.");
            return;
        }

        System.out.println("\n--- Boards Disponíveis ---");
        boards.forEach(System.out::println);
        System.out.print("Digite o ID do board que deseja gerenciar: ");
        int boardId = sc.nextInt();
        sc.nextLine();

        // **AÇÃO NECESSÁRIA:**
        // Aqui você deve criar e chamar o `showBoardMenu(boardId)`,
        // que conterá a lógica de manipulação do board selecionado
        // (mover card, criar card, etc.).
    }

    private static void deleteBoard(Scanner sc, BoardDAO boardDAO) {
        List<Board> boards = boardDAO.findAll();
        if (boards.isEmpty()) {
            System.out.println("Nenhum board para excluir.");
            return;
        }

        System.out.println("\n--- Boards Disponíveis para Exclusão ---");
        boards.forEach(System.out::println);
        System.out.print("Digite o ID do board que deseja excluir: ");
        int boardId = sc.nextInt();
        sc.nextLine();
        
        boardDAO.deleteById(boardId);
        System.out.println("Board com ID " + boardId + " e seus dados foram excluídos (se existirem).");
    }

    private static void reorderColumns(Scanner sc, int boardId, ColumnDAO columnDAO) {
    // 1. Carregar as colunas pendentes atuais
    List<Column> pendentColumns = columnDAO.findPendentByBoardId(boardId);

    if (pendentColumns.size() < 2) {
        System.out.println("Não há colunas suficientes para reordenar.");
        return;
    }

    // 2. Mostrar as colunas atuais para o usuário
    System.out.println("\n--- Colunas Pendentes Atuais ---");
    for (int i = 0; i < pendentColumns.size(); i++) {
        Column col = pendentColumns.get(i);
        // Mostra o índice da lista (0, 1, 2...) e os detalhes da coluna
        System.out.printf("[%d] - %s (ID: %d, Ordem Atual: %d)\n", i, col.getName(), col.getId(), col.getColumnOrder());
    }
    
    // 3. Obter a entrada do usuário para reordenar
    System.out.print("\nDigite o índice da coluna que você quer mover: ");
    int fromIndex = sc.nextInt();

    System.out.print("Digite o novo índice para onde ela deve ir: ");
    int toIndex = sc.nextInt();
    sc.nextLine(); // Limpar o buffer

    // Validação dos índices
    if (fromIndex < 0 || fromIndex >= pendentColumns.size() || toIndex < 0 || toIndex >= pendentColumns.size()) {
        System.out.println("Índice inválido. Operação cancelada.");
        return;
    }
    
    // 4. Reordenar a lista em memória (Java)
    Column columnToMove = pendentColumns.remove(fromIndex);
    pendentColumns.add(toIndex, columnToMove);

    // 5. Chamar o DAO para persistir a nova ordem no banco
    try {
        columnDAO.updateOrder(pendentColumns);
        System.out.println("Colunas reordenadas com sucesso!");
    } catch (DBException e) {
        System.out.println("ERRO: Não foi possível reordenar as colunas.");
        e.printStackTrace();
    }
    
    // Opcional: Mostrar a nova ordem
    System.out.println("\n--- Nova Ordem das Colunas ---");
    for (int i = 0; i < pendentColumns.size(); i++) {
        System.out.printf("[%d] - %s\n", i, pendentColumns.get(i).getName());
    }
}
}