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
import com.taskboard.model.Column;
import com.taskboard.model.ColumnType;


public class ColumnDAO {

    private Connection conn;

    public ColumnDAO(Connection conn) {
        this.conn = conn;
    }

    public void create(Column column) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                "INSERT INTO columns (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS);
            st.setString(1, column.getName());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    column.setID(rs.getInt(1));
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

    public List<Column> findByBoardId(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM columns WHERE id = ?");
            rs = st.executeQuery();
            List<Column> list = new ArrayList<>();
            while (rs.next()) {
                Column column = new Column();
                column.setID(rs.getInt("id"));
                column.setName(rs.getString("name"));
                list.add(column);
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
            st = conn.prepareStatement("DELETE FROM columns WHERE id = ?");
            st.setInt(1, id);
            int rows = st.executeUpdate();
            if (rows == 0) {
                System.out.println("ID da coluna não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    public void updateOrder(List<Column> columns){
        PreparedStatement st = null;
        String sql = "UPDATE columns SET column_order = ? WHERE id = ?";
        try {
             conn.setAutoCommit(false);

            st = conn.prepareStatement(sql);

        // A ordem começa em 2, pois a coluna 1 é sempre a INICIAL
            int order = 2; 

            for (Column col : columns) {
                st.setInt(1, order);
                st.setInt(2, col.getId());
                st.addBatch(); // Adiciona a instrução SQL ao lote
                order++;
            }
            st.executeBatch(); //executa todas as instruções do lote.
            conn.commit();  //confirma transação, torna permanente as alterações.
        } catch (SQLException e) {
            try {
                conn.rollback();
                throw new DBException("Erro ao reordenar colunas. Rollback executado. " + e.getMessage());
            } catch (SQLException e1) {
            throw new DBException("Erro de execução no rollback" + e1.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e){
                e.printStackTrace();
            }
            DB.closeStatement(st);
        }
    }

    public List<Column> findPendentByBoardId(int boardId) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<Column> list = new ArrayList<>();
    String sql = "SELECT * FROM columns WHERE board_id = ? AND type = 'PENDENTE' ORDER BY column_order";

    try {
        st = conn.prepareStatement(sql);
        st.setInt(1, boardId);
        rs = st.executeQuery();

        while (rs.next()) {
            Column col = new Column();
            col.setID(rs.getInt("id"));
            col.setName(rs.getString("name"));
            col.setBoardId(rs.getInt("board_id"));
            col.setColumnOrder(rs.getInt("column_order"));
            col.setType(ColumnType.valueOf(rs.getString("type")));
            list.add(col);
        }
        return list;
    } catch (SQLException e) {
        throw new DBException(e.getMessage());
    } finally {
        DB.closeStatement(st);
        DB.closeResultSet(rs);
    }
}
}
