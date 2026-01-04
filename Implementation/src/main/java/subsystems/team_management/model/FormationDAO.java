package subsystems.team_management.model;

import connection.DBConnection;

import java.sql.*;
import java.util.Map;

public class FormationDAO {

    public int doSave(Formation formation) {
        Connection con = null;
        PreparedStatement psHeader = null;
        PreparedStatement psDetail = null;
        ResultSet rs = null;

        String queryHeader = "INSERT INTO formation (user_email, giornata, modulo) VALUES (?, ?, ?)";
        // Assumiamo che la tabella di dettaglio si chiami formation_player
        String queryDetail = "INSERT INTO formation_player (formation_id, player_id, ruolo_schierato) VALUES (?, ?, ?)";

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // INIZIO TRANSAZIONE

            psHeader = con.prepareStatement(queryHeader, Statement.RETURN_GENERATED_KEYS);
            psHeader.setString(1, formation.getUserEmail());
            psHeader.setInt(2, formation.getGiornata());
            psHeader.setString(3, formation.getModulo());
            psHeader.executeUpdate();

            rs = psHeader.getGeneratedKeys();
            int formationId = -1;
            if (rs.next()) {
                formationId = rs.getInt(1);
            } else {
                throw new SQLException("Fallimento creazione ID formazione.");
            }
            formation.setId(formationId);

            psDetail = con.prepareStatement(queryDetail);

            for (Map.Entry<Integer, String> entry : formation.getPlayersMap().entrySet()) {
                psDetail.setInt(1, formationId);
                psDetail.setInt(2, entry.getKey()); // Player ID
                psDetail.setString(3, entry.getValue()); // "T" o "P"
                psDetail.addBatch();
            }
            psDetail.executeBatch();

            con.commit(); // CONFERMA TRANSAZIONE
            return formationId;

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback(); // ROLLBACK IN CASO DI ERRORE
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Errore salvataggio formazione", e);
        } finally {
            try {
                if (con != null) { con.setAutoCommit(true); con.close(); }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
