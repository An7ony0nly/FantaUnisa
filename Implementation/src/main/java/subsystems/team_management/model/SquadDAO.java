package subsystems.team_management.model;

import connection.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/*+*/
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
     */
    public void doUpdateSquad(String userEmail, List<Integer> playerIds) {
        String deleteQuery = "DELETE FROM squad WHERE user_email = ?";
        String insertQuery = "INSERT INTO squad (user_email, player_id) VALUES (?, ?)";

        // 1. TRY ESTERNO: Gestisce il ciclo di vita della connessione
        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false); // INIZIO TRANSAZIONE

            // 2. TRY INTERNO (Logico): Serve a catturare errori per fare il ROLLBACK
            try {

                // A. Reset della rosa (Delete)
                try (PreparedStatement psDelete = con.prepareStatement(deleteQuery)) {
                    psDelete.setString(1, userEmail);
                    psDelete.executeUpdate();
                }

                // B. Inserimento nuova rosa (Insert Batch)
                // psInsert si chiude da solo alla fine di questo piccolo blocco
                try (PreparedStatement psInsert = con.prepareStatement(insertQuery)) {
                    for (Integer pId : playerIds) {
                        psInsert.setString(1, userEmail);
                        psInsert.setInt(2, pId);
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio rosa", e);
        }
    }

    public Squad doRetrieveSquadObject(String email) {
        List<Player> players = doRetrieveByEmail(email);
        return new Squad(email, players);
    }
}