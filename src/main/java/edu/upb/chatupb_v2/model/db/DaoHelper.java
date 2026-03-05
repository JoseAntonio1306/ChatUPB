package edu.upb.chatupb_v2.model.db;

import edu.upb.chatupb_v2.model.Model;

import java.net.ConnectException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper genérico para DAOs.
 *
 * Mantiene el estilo original del proyecto, pero:
 * - elimina dependencia de SLF4J
 * - usa try-with-resources (cierres seguros)
 */
public class DaoHelper<T> {

    public interface QueryParameters {
        void setParameters(PreparedStatement pst) throws SQLException;
    }

    public interface ResultReader<T> {
        T getResult(ResultSet result) throws SQLException;
    }

    public interface ResultProcedureReader<T> {
        T getResult(CallableStatement callableStatement) throws SQLException;
    }

    public List<T> executeQuery(String query, ResultReader<T> reader) throws ConnectException, SQLException {
        return executeQuery(query, null, reader);
    }

    public List<T> executeQuery(String query, QueryParameters params, ResultReader<T> reader)
            throws ConnectException, SQLException {

        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {

            if (params != null) {
                params.setParameters(st);
            }

            boolean status = st.execute();
            if (!status) {
                return new ArrayList<>();
            }

            List<T> results = new ArrayList<>();
            try (ResultSet result = st.getResultSet()) {
                while (result.next()) {
                    T value = reader.getResult(result);
                    if (value != null) {
                        results.add(value);
                    }
                }
            }
            return results;

        } catch (SQLException e) {
            System.err.println("[DB] SQL error en query: " + query + " => " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[DB] Error inesperado en query: " + query + " => " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public void insert(String query, QueryParameters params, Model model) throws Exception {
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                params.setParameters(st);
            }

            if (st.executeUpdate() > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        model.setId(rs.getLong(1));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB] SQL error en INSERT: " + query + " => " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[DB] Error inesperado en INSERT: " + query + " => " + e.getMessage());
            throw new Exception(e);
        }
    }

    public void update(String query, QueryParameters params) throws ConnectException, SQLException {
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                params.setParameters(st);
            }

            st.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DB] SQL error en UPDATE: " + query + " => " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[DB] Error inesperado en UPDATE: " + query + " => " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public int executeQueryCount(String query, QueryParameters params) throws ConnectException, SQLException {
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {

            if (params != null) {
                params.setParameters(st);
            }

            boolean status = st.execute();
            if (!status) {
                return -1;
            }

            try (ResultSet result = st.getResultSet()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }

            return -1;

        } catch (SQLException e) {
            System.err.println("[DB] SQL error en COUNT: " + query + " => " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[DB] Error inesperado en COUNT: " + query + " => " + e.getMessage());
            throw new SQLException(e);
        }
    }

    public T executeProcedureStore(String query, QueryParameters params, ResultProcedureReader<T> reader) throws Exception {
        try (Connection conn = ConnectionDB.getInstance().getConection();
             CallableStatement st = conn.prepareCall(query)) {

            if (params != null) {
                params.setParameters(st);
            }

            if (st.execute()) {
                return reader.getResult(st);
            }
            return null;

        } catch (SQLException e) {
            System.err.println("[DB] SQL error en PROCEDURE: " + query + " => " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[DB] Error inesperado en PROCEDURE: " + query + " => " + e.getMessage());
            throw new Exception(e);
        }
    }
}
