package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormationDAO {

    public int doSave(Formation formation) {
        String queryHeader = "INSERT INTO formation (user_email, giornata, modulo) VALUES (?, ?, ?)";
        String queryDetail = "INSERT INTO formation_player (formation_id, player_id, tipo, posizione) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement psHeader = con.prepareStatement(queryHeader, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDetail = con.prepareStatement(queryDetail)) {

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


                int countTitolari = 1;
                int countPanchina = 1;

                for (Map.Entry<Integer, String> entry : formation.getPlayersMap().entrySet()) {
                    int playerId = entry.getKey();
                    String schieramento = entry.getValue(); // "T" o "P"

                    psDetail.setInt(1, formation.getId());
                    psDetail.setInt(2, playerId);

                    if ("T".equals(schieramento)) {
                        psDetail.setString(3, "TITOLARE"); // Mappiamo T -> TITOLARE
                        psDetail.setInt(4, countTitolari++); // Assegna 1, poi incrementa a 2, ecc.
                    } else {
                        psDetail.setString(3, "PANCHINA"); // Mappiamo P -> PANCHINA
                        psDetail.setInt(4, countPanchina++); // Assegna 1, poi 2...
                    }

                    psDetail.addBatch();
                }

                psDetail.executeBatch(); // Esegue tutti gli inserimenti

                con.commit();
                return formation.getId();

            } catch (SQLException e) {
                con.rollback(); // Annulla in caso di errore
                throw e;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("Hai già schierato una formazione per questa giornata!");
            }
            throw new RuntimeException("Errore salvataggio formazione", e);
        }
    }

    public Map<String, List<Player>> doRetrieveDetailById(int formationId) {
        Map<String, List<Player>> result = new HashMap<>();
        result.put("TITOLARI", new ArrayList<>());
        result.put("PANCHINA", new ArrayList<>());

        String sql = "SELECT p.*, fp.tipo " +
                "FROM formation_player fp " +
                "JOIN player p ON fp.player_id = p.id " +
                "WHERE fp.formation_id = ? " +
                "ORDER BY fp.tipo DESC, fp.posizione ASC";
        // fp.tipo DESC mette 'TITOLARE' prima di 'PANCHINA' (alfabeticamente T > P)

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, formationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Player p = new Player();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setRuolo(rs.getString("ruolo"));
                    p.setSquadra(rs.getString("squadra_seriea"));

                    String tipo = rs.getString("tipo"); // "TITOLARE" o "PANCHINA"

                    if (result.containsKey(tipo)) {
                        result.get(tipo).add(p);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}