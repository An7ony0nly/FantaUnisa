package subsystems.statistics_import.model;

import connection.DBConnection;
import subsystems.statistics_viewer.model.Statistiche;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StatisticheDAO {

    public void doSaveOrUpdate(Connection con, Statistiche stats) throws SQLException {
        if (stats == null) {
            throw new IllegalArgumentException("stats is null");
        }

        String sql = "INSERT INTO statistic (player_id, giornata, partite_voto, media_voto, fanta_media, " +
                "gol_fatti, gol_subiti, rigori_parati, rigori_calciati, rigori_segnati, rigori_sbagliati, " +
                "assist, ammonizioni, espulsioni, autogol) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "partite_voto = ?, media_voto = ?, fanta_media = ?, gol_fatti = ?, gol_subiti = ?, " +
                "rigori_parati = ?, rigori_calciati = ?, rigori_segnati = ?, rigori_sbagliati = ?, " +
                "assist = ?, ammonizioni = ?, espulsioni = ?, autogol = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stats.getIdCalciatore());
            ps.setInt(2, stats.getGiornata());
            ps.setInt(3, stats.getPartiteVoto());
            ps.setDouble(4, stats.getMediaVoto());
            ps.setDouble(5, stats.getFantaMedia());
            ps.setInt(6, stats.getGolFatti());
            ps.setInt(7, stats.getGolSubiti());
            ps.setInt(8, stats.getRigoriParati());
            ps.setInt(9, stats.getRigoriCalciati());
            ps.setInt(10, stats.getRigoriSegnati());
            ps.setInt(11, stats.getRigoriSbagliati());
            ps.setInt(12, stats.getAssist());
            ps.setInt(13, stats.getAmmonizioni());
            ps.setInt(14, stats.getEspulsioni());
            ps.setInt(15, stats.getAutogol());

            ps.setInt(16, stats.getPartiteVoto());
            ps.setDouble(17, stats.getMediaVoto());
            ps.setDouble(18, stats.getFantaMedia());
            ps.setInt(19, stats.getGolFatti());
            ps.setInt(20, stats.getGolSubiti());
            ps.setInt(21, stats.getRigoriParati());
            ps.setInt(22, stats.getRigoriCalciati());
            ps.setInt(23, stats.getRigoriSegnati());
            ps.setInt(24, stats.getRigoriSbagliati());
            ps.setInt(25, stats.getAssist());
            ps.setInt(26, stats.getAmmonizioni());
            ps.setInt(27, stats.getEspulsioni());
            ps.setInt(28, stats.getAutogol());

            ps.executeUpdate();
        }
    }

    public int getLastGiornataCalcolata() {
        String sql = "SELECT MAX(giornata) FROM statistic";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int max = rs.getInt(1);
                return max;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

public List<Statistiche> findByPlayerAndRange(int playerId, Integer fromGiornata, Integer toGiornata) {
        List<Statistiche> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM statistic WHERE player_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(playerId);

        if (fromGiornata != null) {
            sql.append(" AND giornata >= ?");
            params.add(fromGiornata);
        }
        if (toGiornata != null) {
            sql.append(" AND giornata <= ?");
            params.add(toGiornata);
        }

        sql.append(" ORDER BY giornata DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle statistiche per playerId=" + playerId, e);
        }

        return result;
    }

   
    public Statistiche findByPlayerAndGiornata(int playerId, int giornata) {
        String sql = "SELECT * FROM statistic WHERE player_id = ? AND giornata = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setInt(2, giornata);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero statistiche per playerId=" + playerId +
                    ", giornata=" + giornata, e);
        }
        return null;
    }

    private Statistiche mapRow(ResultSet rs) throws SQLException {
        Statistiche s = new Statistiche();
        s.setIdCalciatore(rs.getInt("player_id"));
        s.setGiornata(rs.getInt("giornata"));
        s.setPartiteVoto(rs.getInt("partite_voto"));
        s.setMediaVoto(rs.getDouble("media_voto"));
        s.setFantaMedia(rs.getDouble("fanta_media"));
        s.setGolFatti(rs.getInt("gol_fatti"));
        s.setGolSubiti(rs.getInt("gol_subiti"));
        s.setRigoriParati(rs.getInt("rigori_parati"));
        s.setRigoriCalciati(rs.getInt("rigori_calciati"));
        s.setRigoriSegnati(rs.getInt("rigori_segnati"));
        s.setRigoriSbagliati(rs.getInt("rigori_sbagliati"));
        s.setAssist(rs.getInt("assist"));
        s.setAmmonizioni(rs.getInt("ammonizioni"));
        s.setEspulsioni(rs.getInt("espulsioni"));
        s.setAutogol(rs.getInt("autogol"));
        return s;
    }
}

