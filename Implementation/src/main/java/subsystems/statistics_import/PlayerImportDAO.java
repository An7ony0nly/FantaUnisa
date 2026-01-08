package subsystems.statistics_import;

import subsystems.team_management.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerImportDAO {

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
        }
    }
}
