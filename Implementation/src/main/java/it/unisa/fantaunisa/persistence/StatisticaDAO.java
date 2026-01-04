package it.unisa.fantaunisa.persistence;

import connection.DBConnection;
import it.unisa.fantaunisa.model.Statistiche;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticaDAO {

    //salva usando una connessione esterna (per transazioni)
    public void doSave(Connection con, Statistiche stat) throws SQLException {
        if (exists(con, stat.getIdCalciatore(), stat.getGiornata())) {
            doUpdate(con, stat);
        } else {
            doInsert(con, stat);
        }
    }


    public void doSave(Statistiche stat) {
        try (Connection con = DBConnection.getConnection()) {
            doSave(con, stat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean exists(Connection con, int idCalciatore, int giornata) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Statistica WHERE id_calciatore = ? AND giornata = ?");
        ps.setInt(1, idCalciatore);
        ps.setInt(2, giornata);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private void doInsert(Connection con, Statistiche stat) throws SQLException {
        String sql = "INSERT INTO Statistica (id_calciatore, giornata, partite_voto, media_voto, fanta_media, " +
                     "gol_fatti, gol_subiti, rigori_parati, rigori_calciati, rigori_segnati, rigori_sbagliati, " +
                     "assist, ammonizioni, espulsioni, autogol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stat.getIdCalciatore());
            ps.setInt(2, stat.getGiornata());
            ps.setInt(3, stat.getPartiteVoto());
            ps.setDouble(4, stat.getMediaVoto());
            ps.setDouble(5, stat.getFantaMedia());
            ps.setInt(6, stat.getGolFatti());
            ps.setInt(7, stat.getGolSubiti());
            ps.setInt(8, stat.getRigoriParati());
            ps.setInt(9, stat.getRigoriCalciati());
            ps.setInt(10, stat.getRigoriSegnati());
            ps.setInt(11, stat.getRigoriSbagliati());
            ps.setInt(12, stat.getAssist());
            ps.setInt(13, stat.getAmmonizioni());
            ps.setInt(14, stat.getEspulsioni());
            ps.setInt(15, stat.getAutogol());
            
            ps.executeUpdate();
        }
    }

    private void doUpdate(Connection con, Statistiche stat) throws SQLException {
        String sql = "UPDATE Statistica SET partite_voto=?, media_voto=?, fanta_media=?, gol_fatti=?, gol_subiti=?, " +
                     "rigori_parati=?, rigori_calciati=?, rigori_segnati=?, rigori_sbagliati=?, assist=?, " +
                     "ammonizioni=?, espulsioni=?, autogol=? WHERE id_calciatore=? AND giornata=?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stat.getPartiteVoto());
            ps.setDouble(2, stat.getMediaVoto());
            ps.setDouble(3, stat.getFantaMedia());
            ps.setInt(4, stat.getGolFatti());
            ps.setInt(5, stat.getGolSubiti());
            ps.setInt(6, stat.getRigoriParati());
            ps.setInt(7, stat.getRigoriCalciati());
            ps.setInt(8, stat.getRigoriSegnati());
            ps.setInt(9, stat.getRigoriSbagliati());
            ps.setInt(10, stat.getAssist());
            ps.setInt(11, stat.getAmmonizioni());
            ps.setInt(12, stat.getEspulsioni());
            ps.setInt(13, stat.getAutogol());
            ps.setInt(14, stat.getIdCalciatore());
            ps.setInt(15, stat.getGiornata());
            
            ps.executeUpdate();
        }
    }

    //recupera le statistiche più recenti per un giocatore
    public Statistiche doRetrieveLastByCalciatore(int idCalciatore) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Statistica WHERE id_calciatore = ? ORDER BY giornata DESC LIMIT 1";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCalciatore);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToStatistica(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //recupera il numero dell'ultima giornata presente nel db
    public int getLastGiornata() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT MAX(giornata) FROM Statistica");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Statistiche mapRowToStatistica(ResultSet rs) throws SQLException {
        Statistiche s = new Statistiche();
        s.setId(rs.getInt("id"));
        s.setIdCalciatore(rs.getInt("id_calciatore"));
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
