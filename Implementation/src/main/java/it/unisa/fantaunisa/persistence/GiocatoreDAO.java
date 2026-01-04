package it.unisa.fantaunisa.persistence;

import connection.DBConnection;
import it.unisa.fantaunisa.model.Giocatore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiocatoreDAO {

    //salva un giocatore usando una connessione esistente (per transazioni)
    public void doSave(Connection con, Giocatore giocatore) throws SQLException {
        String sql = "INSERT INTO Calciatore (id, nome, squadra, ruolo, quotazione) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nome=?, squadra=?, ruolo=?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, giocatore.getId());
            ps.setString(2, giocatore.getNome());
            ps.setString(3, giocatore.getSquadraSerieA());
            ps.setString(4, giocatore.getRuolo());
            ps.setInt(5, 1); //quotazione default
            
            //valori per update
            ps.setString(6, giocatore.getNome());
            ps.setString(7, giocatore.getSquadraSerieA());
            ps.setString(8, giocatore.getRuolo());
            
            ps.executeUpdate();
        }
    }

    //metodo legacy per compatibilità (apre una nuova connessione)
    public void doSave(Giocatore giocatore) {
        try (Connection con = DBConnection.getConnection()) {
            doSave(con, giocatore);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //recupera tutti i giocatori (listone)
    public List<Giocatore> doRetrieveAll() {
        List<Giocatore> giocatori = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Calciatore");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Giocatore g = new Giocatore();
                g.setId(rs.getInt("id"));
                g.setNome(rs.getString("nome"));
                g.setSquadraSerieA(rs.getString("squadra"));
                g.setRuolo(rs.getString("ruolo"));
                giocatori.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return giocatori;
    }

    //recupera un giocatore per id
    public Giocatore doRetrieveById(int id) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Calciatore WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Giocatore g = new Giocatore();
                g.setId(rs.getInt("id"));
                g.setNome(rs.getString("nome"));
                g.setSquadraSerieA(rs.getString("squadra"));
                g.setRuolo(rs.getString("ruolo"));
                return g;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
