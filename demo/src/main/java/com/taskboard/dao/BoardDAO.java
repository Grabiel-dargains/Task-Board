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
import com.taskboard.model.Board;

public class BoardDAO {

    private Connection conn;

    public BoardDAO(Connection conn) {
        this.conn = conn;
    }

    public void create(Board board) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                "INSERT INTO boards (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS);
            st.setString(1, board.getName());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    board.setId(rs.getInt(1));
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

    public List<Board> findAll() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM boards ORDER BY name");
            rs = st.executeQuery();
            List<Board> list = new ArrayList<>();
            while (rs.next()) {
                Board board = new Board();
                board.setId(rs.getInt("id"));
                board.setName(rs.getString("name"));
                list.add(board);
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
            st = conn.prepareStatement("DELETE FROM boards WHERE id = ?");
            st.setInt(1, id);
            int rows = st.executeUpdate();
            if (rows == 0) {
                System.out.println("ID do board não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }
}