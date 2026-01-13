package subsystems.calcolo_formazione;

import connection.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StatisticheDAO {


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

