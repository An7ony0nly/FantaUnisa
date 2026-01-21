package subsystems.access_profile.model;

import connection.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Descrizione: Gestisce la persistenza dei dati anagrafici e delle credenziali utente.
 */
public class UserDAO {

    /**
     * Salva un nuovo utente nel database.
     */
    public synchronized void doSave(User user) throws SQLException {
        String query = "INSERT INTO user (nome, cognome, email, username, password, ruolo, is_active, verification_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPassword());
            // Salvataggio Ruolo (gestione null safety)
            ps.setString(6, user.getRole() != null ? user.getRole().name() : Role.FANTALLENATORE.name());
            // IMPORTANTE: Usa il valore dell'oggetto, non 'false' fisso, per permettere i test
            ps.setBoolean(7, user.is_Active());
            ps.setString(8, user.getVerificationToken());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Rilancia l'eccezione per gestirla nella Servlet/Test
        }
    }

    /**
     * Attiva l'utente tramite token.
     */
    public boolean doActivate(String token) {
        String query = "UPDATE user SET is_active = TRUE, verification_token = NULL WHERE verification_token = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, token);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera un utente tramite email.
     */
    public User doRetrieveByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs); // Usa l'helper per non dimenticare campi
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recupera utente per Login (Email + Password).
     */
    public User doRetrieveByEmailAndPassword(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Imposta il token per il reset password.
     */
    public boolean setResetToken(String email, String token) {
        String query = "UPDATE user SET reset_token = ?, reset_expiry = DATE_ADD(NOW(), INTERVAL 45 MINUTE) WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, token);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trova utente tramite token di reset valido.
     */
    public User findByResetToken(String token) {
        String query = "SELECT * FROM user WHERE reset_token = ? AND reset_expiry > NOW()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aggiorna la password (usato sia per reset che per cambio password).
     */
    public void updatePassword(String email, String newPassword) {
        // Pulisce anche i token di reset per sicurezza
        String query = "UPDATE user SET password = ?, reset_token = NULL, reset_expiry = NULL WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore cambio password", e);
        }
    }

    /**
     * Aggiorna informazioni profilo.
     */
    public void doUpdateInfo(User user) {
        String query = "UPDATE user SET nome = ?, cognome = ? WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());
            ps.setString(3, user.getEmail());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento info profilo", e);
        }
    }

    /**
     * Elimina un utente.
     */
    public void doDelete(String email) {
        String query = "DELETE FROM user WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore eliminazione utente", e);
        }
    }

    /**
     * Helper per mappare ResultSet in Oggetto User.
     * Centralizza la logica per evitare dimenticanze.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setNome(rs.getString("nome"));
        user.setCognome(rs.getString("cognome"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));

        // Mappatura booleano (cruciale per i test)
        // Cerca prima "is_active", se fallisce prova "is_Active" per compatibilità
        try {
            user.setIs_active(rs.getBoolean("is_active"));
        } catch (SQLException e) {
            try {
                user.setIs_active(rs.getBoolean("is_Active"));
            } catch (SQLException ex) {
                user.setIs_active(false); // Default
            }
        }

        // Mappatura Ruolo
        try {
            String roleStr = rs.getString("ruolo");
            if (roleStr != null) {
                user.setRole(Role.valueOf(roleStr));
            } else {
                user.setRole(Role.FANTALLENATORE);
            }
        } catch (IllegalArgumentException e) {
            user.setRole(Role.FANTALLENATORE);
        }

        // Token vari
        try { user.setVerificationToken(rs.getString("verification_token")); } catch (SQLException e) {}
        try { user.setResetToken(rs.getString("reset_token")); } catch (SQLException e) {}

        return user;
    }
}