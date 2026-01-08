package subsystems.statistics_import;

import subsystems.calcolo_formazione.Statistiche;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatisticheImportDAO {

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
}
