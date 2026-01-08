package subsystems.calcolo_formazione;

import java.io.Serializable;

public class AlgoritmoConfig implements Serializable {
    private double pesoMvPortiere = 0.60;
    private double pesoGsPortiere = 0.30;
    private double pesoCostanzaPortiere = 0.10;

    private double pesoFmGiocatore = 0.70;
    private double pesoCostanzaGiocatore = 0.10;
    private double pesoGol = 0.15;
    private double pesoAssist = 0.05;

    public static AlgoritmoConfig defaultConfig() {
        return new AlgoritmoConfig();
    }

    public double getPesoMvPortiere() {
        return pesoMvPortiere;
    }

    public void setPesoMvPortiere(double pesoMvPortiere) {
        this.pesoMvPortiere = pesoMvPortiere;
    }

    public double getPesoGsPortiere() {
        return pesoGsPortiere;
    }

    public void setPesoGsPortiere(double pesoGsPortiere) {
        this.pesoGsPortiere = pesoGsPortiere;
    }

    public double getPesoCostanzaPortiere() {
        return pesoCostanzaPortiere;
    }

    public void setPesoCostanzaPortiere(double pesoCostanzaPortiere) {
        this.pesoCostanzaPortiere = pesoCostanzaPortiere;
    }

    public double getPesoFmGiocatore() {
        return pesoFmGiocatore;
    }

    public void setPesoFmGiocatore(double pesoFmGiocatore) {
        this.pesoFmGiocatore = pesoFmGiocatore;
    }

    public double getPesoCostanzaGiocatore() {
        return pesoCostanzaGiocatore;
    }

    public void setPesoCostanzaGiocatore(double pesoCostanzaGiocatore) {
        this.pesoCostanzaGiocatore = pesoCostanzaGiocatore;
    }

    public double getPesoGol() {
        return pesoGol;
    }

    public void setPesoGol(double pesoGol) {
        this.pesoGol = pesoGol;
    }

    public double getPesoAssist() {
        return pesoAssist;
    }

    public void setPesoAssist(double pesoAssist) {
        this.pesoAssist = pesoAssist;
    }
}
