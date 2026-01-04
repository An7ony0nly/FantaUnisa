package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    /**
     * Salva o Aggiorna il giocatore con TUTTE le statistiche stagionali.
     */
    public void doSaveOrUpdate(Player p) {
        String query = "INSERT INTO player (id, nome, squadra_seriea, ruolo, media_voto, fantamedia, gol_fatti, gol_subiti, assist) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "squadra_seriea = ?, nome = ?, ruolo = ?, " +
                "media_voto = ?, fantamedia = ?, gol_fatti = ?, gol_subiti = ?, assist = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            // INSERT
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getSquadra());
            ps.setString(4, p.getRuolo());
            ps.setFloat(5, p.getMediaVoto());
            ps.setFloat(6, p.getFantamedia());
            ps.setInt(7, p.getGolFatti());
            ps.setInt(8, p.getGolSubiti());
            ps.setInt(9, p.getAssist());

            // UPDATE
            ps.setString(10, p.getSquadra());
            ps.setString(11, p.getNome());
            ps.setString(12, p.getRuolo());
            ps.setFloat(13, p.getMediaVoto());
            ps.setFloat(14, p.getFantamedia());
            ps.setInt(15, p.getGolFatti());
            ps.setInt(16, p.getGolSubiti());
            ps.setInt(17, p.getAssist());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore upsert player", e);
        }
    }

    /**
     * Recupera giocatori filtrati per Ruolo e/o Squadra.
     * Se i parametri sono null o "Tutti", ignora il filtro.
     */
    public List<Player> doRetrieveByFilter(String ruolo, String squadra) {
        List<Player> players = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM player WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (ruolo != null && !ruolo.isEmpty() && !ruolo.equalsIgnoreCase("Tutti")) {
            sql.append(" AND ruolo = ?");
            params.add(ruolo);
        }

        if (squadra != null && !squadra.isEmpty() && !squadra.equalsIgnoreCase("Tutti")) {
            sql.append(" AND squadra_seriea = ?");
            params.add(squadra);
        }

        sql.append(" ORDER BY fantamedia DESC, nome ASC"); // Default order: i più forti prima

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Player p = mapRowToPlayer(rs);
                    players.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public Player doRetrieveById(int id) {
        String query = "SELECT * FROM player WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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

                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Giocatore non trovato
    }

    // Metodo di utilità per mappare il ResultSet
    private Player mapRowToPlayer(ResultSet rs) throws SQLException {
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
        return p;
    }
}