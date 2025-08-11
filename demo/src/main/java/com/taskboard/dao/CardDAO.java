package com.taskboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.taskboard.db.DB;
import com.taskboard.db.DBException;
import com.taskboard.model.Card;

public class CardDAO {

    private Connection conn;

    public CardDAO(Connection conn) {
        this.conn = conn;
    }

    public void create(Card card) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                "INSERT INTO cards (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS);
            st.setString(1, card.getTitle());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    card.setID(rs.getInt(1));
                }
                DB.closeResultSet(rs);
            } else {
                throw new DBException("Erro inesperado! Nenhuma linha afetada.");
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    public List<Card> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM cards ORDER BY name");
            rs = st.executeQuery();
            List<Card> list = new ArrayList<>();
            while (rs.next()) {
                Card card = new Card();
                card.setID(rs.getInt("id"));
                card.setTitle(rs.getString("name"));
                list.add(card);
            }
            return list;
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    public void deleteById(Integer id) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("DELETE FROM cards WHERE id = ?");
            st.setInt(1, id);
            int rows = st.executeUpdate();
            if (rows == 0) {
                System.out.println("ID do card não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    public void updateColumn(int cardId, int oldColumnId, int newColumnId) {
        
        String updateCardSql = "UPDATE cards SET column_id = ? WHERE id = ?";
        
        String updateHistorySql = "UPDATE card_movement_history SET exit_time = CURRENT_TIMESTAMP WHERE card_id = ? AND column_id = ? AND exit_time IS NULL";
        
        String insertHistorySql = "INSERT INTO card_movement_history (card_id, column_id) VALUES (?, ?)";

        PreparedStatement stUpdateCard = null;
        PreparedStatement stUpdateHistory = null;
        PreparedStatement stInsertHistory = null;

        try {
            conn.setAutoCommit(false);

            // 1. Atualiza o histórico de movimento, registrando a data/hora de SAÍDA
            stUpdateHistory = conn.prepareStatement(updateHistorySql);
            stUpdateHistory.setInt(1, cardId);
            stUpdateHistory.setInt(2, oldColumnId);
            stUpdateHistory.executeUpdate();

            // 2. Atualiza a tabela 'cards' para refletir a nova coluna
            stUpdateCard = conn.prepareStatement(updateCardSql);
            stUpdateCard.setInt(1, newColumnId);
            stUpdateCard.setInt(2, cardId);
            stUpdateCard.executeUpdate();
            
            // 3. Insere um novo registro no histórico para a ENTRADA na nova coluna
            stInsertHistory = conn.prepareStatement(insertHistorySql);
            stInsertHistory.setInt(1, cardId);
            stInsertHistory.setInt(2, newColumnId);
            stInsertHistory.executeUpdate();

            // Se tudo deu certo, confirma a transação
            conn.commit();

        } catch (SQLException e) {
            try {
                // Se algo deu errado, desfaz TODAS as operações
                conn.rollback();
                throw new DBException("Erro ao mover card, transação revertida. " + e.getMessage());
            } catch (SQLException e1) {
                throw new DBException("Erro crítico ao tentar reverter a transação. " + e1.getMessage());
            }
        } finally {
            DB.closeStatement(stUpdateCard);
            DB.closeStatement(stUpdateHistory);
            DB.closeStatement(stInsertHistory);
            try {
                // Sempre retorna ao modo de auto-commit
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- MÉTODO 2: updateBlockStatus ---
    /**
     * Bloqueia ou desbloqueia um card e registra o motivo no histórico.
     * Esta operação é transacional.
     *
     * @param cardId O ID do card.
     * @param isBlocked true para bloquear, false para desbloquear.
     * @param reason O motivo para o bloqueio ou desbloqueio.
     */
    public void updateBlockStatus(int cardId, boolean isBlocked, String reason) {
        String updateCardSql = "UPDATE cards SET is_blocked = ? WHERE id = ?";
        
        PreparedStatement stUpdateCard = null;
        PreparedStatement stHistory = null;

        try {
            // Inicia a transação
            conn.setAutoCommit(false);

            // 1. Atualiza o status de bloqueio na tabela principal de cards
            stUpdateCard = conn.prepareStatement(updateCardSql);
            stUpdateCard.setBoolean(1, isBlocked);
            stUpdateCard.setInt(2, cardId);
            stUpdateCard.executeUpdate();

            // 2. Registra a ação no histórico de bloqueios
            if (isBlocked) {
                // Se está BLOQUEANDO, insere um novo registro de bloqueio
                String insertBlockSql = "INSERT INTO card_block_history (card_id, block_reason) VALUES (?, ?)";
                stHistory = conn.prepareStatement(insertBlockSql);
                stHistory.setInt(1, cardId);
                stHistory.setString(2, reason);
            } else {
                // Se está DESBLOQUEANDO, atualiza o registro de bloqueio aberto
                // A condição 'unblock_time IS NULL' garante que estamos fechando o bloqueio correto
                String updateBlockSql = "UPDATE card_block_history SET unblock_time = CURRENT_TIMESTAMP, unblock_reason = ? WHERE card_id = ? AND unblock_time IS NULL";
                stHistory = conn.prepareStatement(updateBlockSql);
                stHistory.setString(1, reason);
                stHistory.setInt(2, cardId);
            }
            stHistory.executeUpdate();

            // Se tudo deu certo, confirma a transação
            conn.commit();
            
        } catch (SQLException e) {
            try {
                // Se algo deu errado, desfaz TODAS as operações
                conn.rollback();
                throw new DBException("Erro ao atualizar status de bloqueio, transação revertida. " + e.getMessage());
            } catch (SQLException e1) {
                throw new DBException("Erro crítico ao tentar reverter a transação. " + e1.getMessage());
            }
        } finally {
            DB.closeStatement(stUpdateCard);
            DB.closeStatement(stHistory);
             try {
                // Sempre retorna ao modo de auto-commit
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
