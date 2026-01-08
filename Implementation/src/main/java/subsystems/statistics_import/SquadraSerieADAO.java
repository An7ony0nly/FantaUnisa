package subsystems.statistics_import;

import connection.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SquadraSerieADAO {
    private static final Logger LOGGER = Logger.getLogger(SquadraSerieADAO.class.getName());

    // Salva una squadra di Serie A se non esiste (INSERT IGNORE)
    public void doSave(Connection con, String nomeSquadra) throws SQLException {
        String sql = "INSERT IGNORE INTO SquadraSerieA (nome) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nomeSquadra);
            ps.executeUpdate();
        }
    }

    public void doSave(String nomeSquadra) {
        try (Connection con = DBConnection.getConnection()) {
            doSave(con, nomeSquadra);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio della squadra: " + nomeSquadra, e);
        }
    }
}
