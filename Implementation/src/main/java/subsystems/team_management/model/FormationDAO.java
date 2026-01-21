package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.Map;

public class FormationDAO {

    public int doSave(Formation formation) {
        String queryHeader = "INSERT INTO formation (user_email, giornata, modulo) VALUES (?, ?, ?)";
        // COLONNE DB: posizione (varchar 1) per il Ruolo, tipo (varchar 10) per lo Status
        String queryDetail = "INSERT INTO formation_player (formation_id, player_id, posizione, tipo) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // 1. SALVATAGGIO HEADER
            try (PreparedStatement psHeader = con.prepareStatement(queryHeader, Statement.RETURN_GENERATED_KEYS)) {
                psHeader.setString(1, formation.getUserEmail());
                psHeader.setInt(2, formation.getGiornata());
                psHeader.setString(3, formation.getModulo());
                psHeader.executeUpdate();

                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (rs.next()) formation.setId(rs.getInt(1));
                    else throw new SQLException("Errore ID Formazione");
                }
            }

            // 2. SALVATAGGIO DETTAGLI
            try (PreparedStatement psDetail = con.prepareStatement(queryDetail)) {

                // Iteriamo sulla vecchia Map<Integer, String>
                for (Map.Entry<Integer, String> entry : formation.getPlayersMap().entrySet()) {

                    int playerId = entry.getKey();
                    String packedValue = entry.getValue(); // Qui dentro c'è "C:titolare"

                    // SPACCHETTIAMO LA STRINGA
                    String[] info = packedValue.split(":");
                    String ruoloDb = info[0];   // "C"
                    String statusDb = info[1];  // "titolare"

                    psDetail.setInt(1, formation.getId());
                    psDetail.setInt(2, playerId);

                    // Colonna 'posizione' del DB -> Mettiamo il Ruolo (A, C, D, P)
                    psDetail.setString(3, ruoloDb);

                    // Colonna 'tipo' del DB -> Mettiamo lo Status (titolare/panchina)
                    psDetail.setString(4, statusDb);

                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            con.commit();
            return formation.getId();

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL", e);
        }
    }

    public Formation doRetrieveById(int formationId) {
        Formation formation = null;

        // Query 1: Prende i dati della formazione
        String headerSql = "SELECT * FROM formation WHERE id = ?";

        // Query 2: Prende i giocatori CON I NOMI (Join con Player)
        String playersSql = "SELECT fp.player_id, fp.posizione, p.nome, p.ruolo " +
                "FROM formation_player fp " +
                "JOIN player p ON fp.player_id = p.id " +
                "WHERE fp.formation_id = ? AND fp.tipo = 'titolare'";

        try (Connection con = DBConnection.getConnection()) {

            // Esegui Query 1 (Header)
            try (PreparedStatement ps = con.prepareStatement(headerSql)) {
                ps.setInt(1, formationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        formation = new Formation();
                        formation.setId(formationId);
                        formation.setModulo(rs.getString("modulo"));
                        // ... altri set ...
                    }
                }
            }

            if (formation != null) {
                // Esegui Query 2 (Giocatori)
                try (PreparedStatement ps = con.prepareStatement(playersSql)) {
                    ps.setInt(1, formationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Player p = new Player();
                            p.setId(rs.getInt("player_id"));
                            p.setNome(rs.getString("nome"));
                            // IMPORTANTE: Impostiamo il ruolo in base alla POSIZIONE in campo (P, D, C, A)
                            p.setRuolo(rs.getString("posizione"));

                            // Aggiungiamo alla lista che la JSP leggerà
                            formation.addFullPlayer(p);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formation;
    }
}