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
import com.taskboard.model.Card;
import com.taskboard.model.Column;
import com.taskboard.model.ColumnType;

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
                    createNewBoard(sc, boardDAO, columnDAO);
                    break;
                case 2:
                    selectBoard(sc, boardDAO, columnDAO, cardDAO);
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

    private static void createNewBoard(Scanner sc, BoardDAO boardDAO, ColumnDAO columnDAO) {
        System.out.print("Digite o nome do novo board: ");
        String boardName = sc.nextLine();

        if(boardName == null || boardName.trim().isEmpty()) {
            System.out.println("Erro: O nome não pode ser vazio.");
            return;
        }
        try {
            if (boardDAO.findByName(boardName) != null) {
                System.out.println("Erro: Um board com o nome " + boardName + "' já existe.");
                return;
            }
            Board newBoard = new Board();
            newBoard.setName(boardName);
            boardDAO.create(newBoard);

            System.out.println("Board '" + newBoard.getName() + "' criado com ID: " + newBoard.getId());
            createMandatoryColumns(sc, newBoard, columnDAO);

        } catch (DBException e) {
            System.out.println("Ocorreu um erro: " + e.getMessage());
        }
    }

    private static void selectBoard(Scanner sc, BoardDAO boardDAO, ColumnDAO columnDAO, CardDAO cardDAO) {
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

        Board selectedBoard = boards.stream().filter(b -> b.getId() == boardId).findFirst().orElse(null);

        if (selectedBoard != null) {
            showBoardMenu(sc, selectedBoard, columnDAO, cardDAO);
        } else {
            System.out.println("ERRO: ID do board inválido");
        }
    }

    private static void showBoardMenu (Scanner sc, Board selectedBoard, ColumnDAO columnDAO, CardDAO cardDAO) {
        int choice;
        do {
            System.out.println("\n===================");
            System.out.println("      BOARD: "  + selectedBoard.getName());
            System.out.println("====================");

            List<Column> columns = columnDAO.findByBoardId(selectedBoard.getId());
            for (Column column : columns) {
                System.out.println("\n--- Coluna: " + column.getName() + "(ID: " + column.getId() + ") ---");
                List<Card> cards = cardDAO.findByColumnId(column.getId());
                if (cards.isEmpty()){
                    System.out.println("Nenhum card com ID: " + column.getId());
                } else {
                    for (Card card : cards) {
                        String blockedStatus = card.getIsBlocked() ? "[X]" : "[ ]";
                        System.out.printf("%s Card ID: %d - %s\n", blockedStatus, card.getId(), card.getTitle());
                    }
                }
            }

            System.out.println("\n--- Ações do Board ---");
            System.out.println("1 - Criar novo card");
            System.out.println("2 - Mover card");
            System.out.println("3 - Cancelar card");
            System.out.println("4 - Bloquear card");
            System.out.println("5 - Desbloquear card");
            System.out.println("6 - Voltar ao menu principal");
            System.out.println("Escolha uma opção ---");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    createNewCard(sc, selectedBoard.getId(), cardDAO, columns);
                    break;
                case 2:
                    moveCard(sc, cardDAO, columns);
                    break;
                case 3:
                    cancelCard(sc, cardDAO, columns);
                    break;
                case 4:
                    blockCard(sc, cardDAO);
                    break;
                case 5:
                    unblockCard(sc, cardDAO);
                    break;
                case 6:
                    System.out.println("Fechando o board e voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (choice != 6);
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
    List<Column> pendentColumns = columnDAO.findPendentByBoardId(boardId);

    if (pendentColumns.size() < 2) {
        System.out.println("Não há colunas suficientes para reordenar.");
        return;
    }

    System.out.println("\n--- Colunas Pendentes Atuais ---");
    for (int i = 0; i < pendentColumns.size(); i++) {
        Column col = pendentColumns.get(i);
        System.out.printf("[%d] - %s (ID: %d, Ordem Atual: %d)\n", i, col.getName(), col.getId(), col.getColumnOrder());
    }

    System.out.print("\nDigite o índice da coluna que você quer mover: ");
    int fromIndex = sc.nextInt();

    System.out.print("Digite o novo índice para onde ela deve ir: ");
    int toIndex = sc.nextInt();
    sc.nextLine();


    if (fromIndex < 0 || fromIndex >= pendentColumns.size() || toIndex < 0 || toIndex >= pendentColumns.size()) {
        System.out.println("Índice inválido. Operação cancelada.");
        return;
    }

    Column columnToMove = pendentColumns.remove(fromIndex);
    pendentColumns.add(toIndex, columnToMove);

    try {
        columnDAO.updateOrder(pendentColumns);
        System.out.println("Colunas reordenadas com sucesso!");
    } catch (DBException e) {
        System.out.println("ERRO: Não foi possível reordenar as colunas.");
        e.printStackTrace();
    }

    System.out.println("\n--- Nova Ordem das Colunas ---");
    for (int i = 0; i < pendentColumns.size(); i++) {
        System.out.printf("[%d] - %s\n", i, pendentColumns.get(i).getName());
    }
}

private static void createMandatoryColumns(Scanner sc, Board board, ColumnDAO columnDAO) {
        System.out.println("\n--- Configuração das Colunas Obrigatórias ---");

        try {
            System.out.println("Digite o nome do coluna de tarefas iniciais: ");
            String initialColumnName = sc.nextLine();
            Column initialColumn = new Column();
            initialColumn.setName(initialColumnName);
            initialColumn.setBoardId(board.getId());
            initialColumn.setColumnOrder(1);
            initialColumn.setType(ColumnType.INICIAL);
            columnDAO.create(initialColumn);

            System.out.println("Digite o nome para a coluna de tarefas concluídas: ");
            String finalColumnName = sc.nextLine();
            Column finalColumn = new Column();
            finalColumn.setName(finalColumnName);
            finalColumn.setBoardId(board.getId());
            finalColumn.setColumnOrder(2);
            finalColumn.setType(ColumnType.FINAL);
            columnDAO.create(finalColumn);

            System.out.println("Digite o nome para a coluna de tarefas canceladas: ");
            String canceledColumnName = sc.nextLine();
            Column canceledColumn = new Column();
            canceledColumn.setName(canceledColumnName);
            canceledColumn.setBoardId(board.getId());
            canceledColumn.setColumnOrder(3);
            canceledColumn.setType(ColumnType.CANCELAMENTO);
            columnDAO.create(canceledColumn);

            System.out.println("Colunas obrigatórias criadas com sucesso!");
        } catch (DBException e) {
            System.out.println("Ocorreu um erro ao criar as colunas obrigatórias: " + e.getMessage());
        }
}
private static void createNewCard(Scanner sc, int boardId, CardDAO cardDAO, List<Column> columns) {
        Column initialColumn = columns.stream()
                .filter(c -> c.getType() == ColumnType.INICIAL)
                .findFirst()
                .orElse(null);

        if (initialColumn == null) {
            System.out.println("ERRO: Não foi possível encontrar a coluna inicial desse board.");
            return;
        }

        System.out.println("\n--- Criar Novo Card ---");
        System.out.print("Digite o título do card: ");
        String title = sc.nextLine();
        System.out.print("Digite a descrição do card: ");
        String description = sc.nextLine();

        Card newCard = new Card();
        newCard.setTitle(title);
        newCard.setDescription(description);
        newCard.setColumnId(initialColumn.getId());

        cardDAO.create(newCard);
        System.out.println("Card '" + title + "'criado com sucesso!");
}
private static void moveCard(Scanner sc, CardDAO cardDAO, List<Column> columns) {
        System.out.println("\n--- Mover Card para próxima Coluna ---");
        System.out.print("Digite o ID do card que deseja mover: ");
        int cardId = sc.nextInt();
        sc.nextLine();

        Card card = cardDAO.findById(cardId);
        if (card == null) {
            System.out.println("ERRO: O card com ID " + cardId + "não foi encontrado.");
            return;
        }
        if (card.getIsBlocked()) {
            System.out.println("ERRO: O card está bloqueado e não pode ser movido.");
            return;
        }

        Column currentColumn = columns.stream()
                .filter(c -> c.getId() == card.getColumnId())
                .findFirst()
                .get();

        if (currentColumn.getType() == ColumnType.FINAL || currentColumn.getType() == ColumnType.CANCELAMENTO) {
            System.out.println("ERRO: O card já está em uma coluna final e não pode ser movido.");
            return;
        }

        int currentOrder = currentColumn.getColumnOrder();
        Column nextColumn = columns.stream()
                .filter(c -> c.getColumnOrder() > currentOrder && c.getType() != ColumnType.CANCELAMENTO)
                .findFirst()
                .orElse(null);

        if(nextColumn == null) {
            System.out.println("ERRO: Não há uma próxima coluna para mover o card.");
            return;
        }

        cardDAO.updateColumn(card.getId(), currentColumn.getId(), nextColumn.getId());
        System.out.println("Card ID " + cardId + " movido de '" + currentColumn.getName() + "' para '" + nextColumn.getName() + "'.");
}

private static void cancelCard(Scanner sc, CardDAO cardDAO, List<Column> columns) {
        System.out.println("\n--- Cancelar Card ---");
        System.out.print("Digite o ID do card que deseja cancelar: ");
        int cardId = sc.nextInt();
        sc.nextLine();

        Card card = cardDAO.findById(cardId);
        if (card == null) {
            System.out.println("ERRO: Card não encontrado.");
            return;
        }

        Column currentColumn = columns.stream()
                .filter(c -> c.getId() == card.getColumnId())
                .findFirst()
                .get();

        if (currentColumn.getType() == ColumnType.CANCELAMENTO) {
            System.out.println("Erro: Este card já está cancelado.");
            return;
        }

        Column cancelColumn = columns.stream()
                .filter(c -> c.getType() == ColumnType.CANCELAMENTO)
                .findFirst()
                .get();

        cardDAO.updateColumn(card.getId(), currentColumn.getId(), cancelColumn.getId());
        System.out.println("Card ID " + cardId + " movido para a coluna de cancelamento.");
}

private static void blockCard(Scanner sc, CardDAO cardDAO) {
        System.out.println("\n--- Bloquear Card ---");
        System.out.print("Digite o ID do card a ser bloqueado: ");
        int cardId = sc.nextInt();
        sc.nextLine();

        Card card = cardDAO.findById(cardId);
        if (card == null) {
            System.out.println("ERRO: O card já está bloqueado.");
            return;
        }

        System.out.print("Digite o motivo do bloqueio: ");
        String reason = sc.nextLine();

        cardDAO.updateBlockStatus(cardId, true, reason);
        System.out.println("Card ID " + cardId + " bloqueado com sucesso.");
}

private static void unblockCard(Scanner sc, CardDAO cardDAO) {
        System.out.println("\n--- Desbloquear Card ---");
        System.out.print("Digite o ID do card a ser desbloqueado: ");
        int cardId = sc.nextInt();
        sc.nextLine();

        Card card = cardDAO.findById(cardId);
        if (card == null) {
            System.out.println("ERRO: Card não encontrado.");
            return;
        }
        if (!card.getIsBlocked()) {
            System.out.println("ERRO: O card não está bloqueado.");
            return;
        }

        System.out.print("Digite o motivo do desbloqueio: ");
        String reason = sc.nextLine();

        cardDAO.updateBlockStatus(cardId, false, reason);
        System.out.println("Card ID " + cardId + " desbloqueado com sucesso.");
}
}