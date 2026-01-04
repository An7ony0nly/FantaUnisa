package subsystems.team_management.model;

import connection.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SquadDAO {

    /**
     * Recupera la rosa dell'utente completa di tutte le statistiche aggiornate.
     */
    public List<Player> doRetrieveByEmail(String userEmail) {
        List<Player> squad = new ArrayList<>();
        // Seleziona tutti i campi di Player facendo il JOIN con la tabella Squad
        String query = "SELECT p.* FROM player p " +
                "JOIN squad s ON p.id = s.player_id " +
                "WHERE s.user_email = ? " +
                "ORDER BY p.ruolo DESC, p.fantamedia DESC"; // Ordine: P-D-C-A e per forza

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userEmail);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Player p = new Player();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setSquadra(rs.getString("squadra_seriea"));
                    p.setRuolo(rs.getString("ruolo"));
                    p.setMediaVoto(rs.getFloat("media_voto"));
                    p.setFantamedia(rs.getFloat("fantamedia"));
                    p.setGolFatti(rs.getInt("gol_fatti"));
                    p.setGolSubiti(rs.getInt("gol_subiti"));
                    p.setAssist(rs.getInt("assist"));

                    squad.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore recupero rosa", e);
        }
        return squad;
    }

    /**
     * Aggiorna la rosa: Cancella tutto e riscrive i 25 giocatori scelti.
     * (Logica "Wipe and Rewrite" per evitare problemi di concorrenza sui singoli acquisti)
     */
    public void doUpdateSquad(String userEmail, List<Integer> playerIds) {
        String deleteQuery = "DELETE FROM squad WHERE user_email = ?";
        String insertQuery = "INSERT INTO squad (user_email, player_id) VALUES (?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // TRANSAZIONE ATOMICA

            // 1. Reset della rosa attuale
            try (PreparedStatement psDelete = con.prepareStatement(deleteQuery)) {
                psDelete.setString(1, userEmail);
                psDelete.executeUpdate();
            }

            // 2. Inserimento massivo della nuova rosa
            try (PreparedStatement psInsert = con.prepareStatement(insertQuery)) {
                for (Integer pId : playerIds) {
                    psInsert.setString(1, userEmail);
                    psInsert.setInt(2, pId);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }

            con.commit(); // Conferma modifiche

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Errore salvataggio rosa", e);
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Squad doRetrieveSquadObject(String email) {
        List<Player> players = doRetrieveByEmail(email);
        return new Squad(email, players);
    }
}