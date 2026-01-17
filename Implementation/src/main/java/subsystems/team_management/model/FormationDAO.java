package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.Map;

public class FormationDAO {

    public int doSave(Formation formation) {
        String queryHeader = "INSERT INTO formation (user_email, giornata, modulo) VALUES (?, ?, ?)";
        String queryDetail = "INSERT INTO formation_player (formation_id, player_id, ruolo_schierato) VALUES (?, ?, ?)";

        // 1. TRY ESTERNO: Gestisce SOLO la Connessione
        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false); // Inizio Transazione

            // 2. TRY INTERNO: Gestisce Statement e ResultSet
            try (PreparedStatement psHeader = con.prepareStatement(queryHeader, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDetail = con.prepareStatement(queryDetail)) {

                // --- HEADER ---
                psHeader.setString(1, formation.getUserEmail());
                psHeader.setInt(2, formation.getGiornata());
                psHeader.setString(3, formation.getModulo());
                psHeader.executeUpdate();

                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (rs.next()) {
                        formation.setId(rs.getInt(1));
                    } else {
                        throw new SQLException("Fallimento creazione ID formazione.");
                    }
                }

                for (Map.Entry<Integer, String> entry : formation.getPlayersMap().entrySet()) {
                    psDetail.setInt(1, formation.getId());
                    psDetail.setInt(2, entry.getKey()); // Player ID
                    psDetail.setString(3, entry.getValue()); // Ruolo
                    psDetail.addBatch();
                }
                psDetail.executeBatch();

                con.commit();
                return formation.getId();

            } catch (SQLException e) {
                con.rollback();
                throw e; // Rilanciamo l'errore per gestirlo fuori o loggarlo
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio formazione", e);
        }
    }
}