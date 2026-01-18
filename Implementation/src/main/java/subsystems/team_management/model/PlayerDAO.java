package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    public void doSaveOrUpdate(Connection con, Player player) throws SQLException {
        if (player == null) {
            throw new IllegalArgumentException("player is null");
        }

        String sql = "INSERT INTO player (id, nome, squadra_seriea, ruolo, media_voto, fantamedia, gol_fatti, gol_subiti, assist) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "nome = ?, squadra_seriea = ?, ruolo = ?, media_voto = ?, fantamedia = ?, gol_fatti = ?, gol_subiti = ?, assist = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, player.getId());
            ps.setString(2, player.getNome());
            ps.setString(3, player.getSquadra());
            ps.setString(4, player.getRuolo());
            ps.setFloat(5, player.getMediaVoto());
            ps.setFloat(6, player.getFantamedia());
            ps.setInt(7, player.getGolFatti());
            ps.setInt(8, player.getGolSubiti());
            ps.setInt(9, player.getAssist());

            ps.setString(10, player.getNome());
            ps.setString(11, player.getSquadra());
            ps.setString(12, player.getRuolo());
            ps.setFloat(13, player.getMediaVoto());
            ps.setFloat(14, player.getFantamedia());
            ps.setInt(15, player.getGolFatti());
            ps.setInt(16, player.getGolSubiti());
            ps.setInt(17, player.getAssist());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore upsert player", e);
        }
    }

    public List<Player> doRetrieveAll() {
        List<Player> players = new ArrayList<>();
        String query = "SELECT * FROM player ORDER BY nome ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                players.add(mapRowToPlayer(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore recupero lista completa giocatori", e);
        }
        return players;
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