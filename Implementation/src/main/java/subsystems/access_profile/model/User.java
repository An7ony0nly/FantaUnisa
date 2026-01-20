package subsystems.access_profile.model;

import java.io.Serializable;
import java.util.Date;
/*+*/
/**
 * Rappresenta l'utente registrato nel sistema.
 */
public class User{

    private String nome;
    private String cognome;
    private String email;
    private String username;
    private String password; // Hash della password
    private Role role;
    private boolean is_active;
    private String verificationToken;
    private String resetToken;
    private Date resetExpiry;

    public User() {}

    public User(String nome, String cognome, String email, String username, String password, Role role, boolean is_active, String verificationToken, Date resetExpiry, String resetToken) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.is_active = is_active;
        this.verificationToken = verificationToken;
        this.resetExpiry = resetExpiry;
        this.resetToken = resetToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public boolean is_Active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verification_token) {
        this.verificationToken = verification_token;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Date getResetExpiry() {
        return resetExpiry;
    }

    public void setResetExpiry(Date resetExpiry) {
        this.resetExpiry = resetExpiry;
    }

    @Override
    public String toString() {
        return "User [email=" + email + ", username=" + username + ", role=" + role + "]";
    }
}
