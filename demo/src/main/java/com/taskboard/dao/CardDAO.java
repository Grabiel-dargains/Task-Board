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
        PreparedStatement stCard = null;
        PreparedStatement stHistory = null;
        String sqlCard = "INSERT INTO cards (title, description, column_id) VALUES (?, ?, ?)";
        String sqlHistory = "INSERT INTO card_movement_history (card_id, column_id) VALUES (?, ?)";
        try {
            conn.setAutoCommit(false);

            stCard = conn.prepareStatement(sqlCard, Statement.RETURN_GENERATED_KEYS);
            stCard.setString(1, card.getTitle());
            stCard.setString(2, card.getDescription());
            stCard.setInt(3, card.getColumnId());
            stCard.executeUpdate();

            ResultSet rs = stCard.getGeneratedKeys();
            if(rs.next()){
                card.setID(rs.getInt(1));}
            else {
                throw new SQLException("Falha ao obter ID do card criado.");
            }
            DB.closeResultSet(rs);

            stHistory = conn.prepareStatement(sqlHistory);
            stHistory.setInt(1, card.getId());
            stHistory.setInt(2, card.getColumnId());
            stHistory.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                throw new DBException("Erro ao tentar reverter a criação de card.");
            }
            throw new DBException("Erro ao criar card: " + e.getMessage());
        } finally {
            DB.closeStatement(stCard);
            DB.closeStatement(stHistory);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

            stUpdateHistory = conn.prepareStatement(updateHistorySql);
            stUpdateHistory.setInt(1, cardId);
            stUpdateHistory.setInt(2, oldColumnId);
            stUpdateHistory.executeUpdate();

            stUpdateCard = conn.prepareStatement(updateCardSql);
            stUpdateCard.setInt(1, newColumnId);
            stUpdateCard.setInt(2, cardId);
            stUpdateCard.executeUpdate();

            stInsertHistory = conn.prepareStatement(insertHistorySql);
            stInsertHistory.setInt(1, cardId);
            stInsertHistory.setInt(2, newColumnId);
            stInsertHistory.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            try {
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
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateBlockStatus(int cardId, boolean isBlocked, String reason) {
        String updateCardSql = "UPDATE cards SET is_blocked = ? WHERE id = ?";
        
        PreparedStatement stUpdateCard = null;
        PreparedStatement stHistory = null;

        try {
            conn.setAutoCommit(false);

            stUpdateCard = conn.prepareStatement(updateCardSql);
            stUpdateCard.setBoolean(1, isBlocked);
            stUpdateCard.setInt(2, cardId);
            stUpdateCard.executeUpdate();

            if (isBlocked) {
                String insertBlockSql = "INSERT INTO card_block_history (card_id, block_reason) VALUES (?, ?)";
                stHistory = conn.prepareStatement(insertBlockSql);
                stHistory.setInt(1, cardId);
                stHistory.setString(2, reason);
            } else {
                String updateBlockSql = "UPDATE card_block_history SET unblock_time = CURRENT_TIMESTAMP, unblock_reason = ? WHERE card_id = ? AND unblock_time IS NULL";
                stHistory = conn.prepareStatement(updateBlockSql);
                stHistory.setString(1, reason);
                stHistory.setInt(2, cardId);
            }
            stHistory.executeUpdate();
            conn.commit();
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                throw new DBException("Erro ao atualizar status de bloqueio, transação revertida. " + e.getMessage());
            } catch (SQLException e1) {
                throw new DBException("Erro crítico ao tentar reverter a transação. " + e1.getMessage());
            }
        } finally {
            DB.closeStatement(stUpdateCard);
            DB.closeStatement(stHistory);
             try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public List<Card> findByColumnId(int columnId){
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Card> list = new ArrayList<>();
        String sql = "SELECT * FROM cards WHERE column_id = ? ORDER BY creation_date DESC";

        try {
            st = conn.prepareStatement(sql);
            st.setInt(1, columnId);
            rs = st.executeQuery();

            while (rs.next()) {
                Card card = new Card();
                card.setID(rs.getInt("id"));
                card.setTitle(rs.getString("title"));
                card.setDescription(rs.getString("description"));
                card.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                card.setBlock(rs.getBoolean("is_blocked"));
                card.setColumnId(rs.getInt("column_id"));
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
    public Card findById(int cardId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM cards WHERE id = ?";
        try {
            st = conn.prepareStatement(sql);
            st.setInt(1, cardId);
            rs = st.executeQuery();
            if (rs.next()){
                Card card = new Card();
                card.setID(rs.getInt("id"));
                card.setTitle(rs.getString("title"));
                card.setDescription(rs.getString("description"));
                card.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                card.setBlock(rs.getBoolean("is_blocked"));
                card.setColumnId(rs.getInt("column_id"));
                return card;
            }
            return null;
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }
}
