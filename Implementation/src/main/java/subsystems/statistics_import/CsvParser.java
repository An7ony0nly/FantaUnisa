package subsystems.statistics_import;

import subsystems.calcolo_formazione.Statistiche;
import subsystems.team_management.model.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    //classe interna per contenere i dati di una riga
    public static class ImportData {
        public Player player;
        public Statistiche statistiche;

        public ImportData(Player player, Statistiche statistiche) {
            this.player = player;
            this.statistiche = statistiche;
        }
    }

    public List<ImportData> parse(InputStream is, int giornata) throws IOException, IllegalArgumentException {
        List<ImportData> list = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            
            while ((line = br.readLine()) != null) {
                lineNum++;
                //salta l'intestazione se presente
                if (lineNum == 1 && line.toLowerCase().startsWith("id")) {
                    continue;
                }
                
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 17) {
                    throw new IllegalArgumentException("Formato errato alla riga " + lineNum + ": colonne insufficienti");
                }

                try {
                    //parsing dati giocatore
                    int id = Integer.parseInt(parts[0]);
                    String ruolo = parts[1];
                    String nome = parts[2];
                    String squadra = parts[3];

                    Player p = new Player();
                    p.setId(id);
                    p.setNome(nome);
                    p.setRuolo(ruolo);
                    p.setSquadra(squadra);
                    p.setMediaVoto((float) parseDouble(parts[5]));
                    p.setFantamedia((float) parseDouble(parts[6]));
                    p.setGolFatti(Integer.parseInt(parts[7]));
                    p.setGolSubiti(Integer.parseInt(parts[8]));
                    p.setAssist(Integer.parseInt(parts[13]));

                    //ricavo statistiche
                    Statistiche s = new Statistiche();
                    s.setIdCalciatore(id);
                    s.setGiornata(giornata);

                    s.setPartiteVoto(Integer.parseInt(parts[4]));
                    s.setMediaVoto(parseDouble(parts[5]));
                    s.setFantaMedia(parseDouble(parts[6]));
                    s.setGolFatti(Integer.parseInt(parts[7]));
                    s.setGolSubiti(Integer.parseInt(parts[8]));
                    s.setRigoriParati(Integer.parseInt(parts[9]));
                    s.setRigoriCalciati(Integer.parseInt(parts[10]));
                    s.setRigoriSegnati(Integer.parseInt(parts[11]));
                    s.setRigoriSbagliati(Integer.parseInt(parts[12]));
                    s.setAssist(Integer.parseInt(parts[13]));
                    s.setAmmonizioni(Integer.parseInt(parts[14]));
                    s.setEspulsioni(Integer.parseInt(parts[15]));
                    s.setAutogol(Integer.parseInt(parts[16]));

                    list.add(new ImportData(p, s));

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Errore numerico alla riga " + lineNum + ": " + e.getMessage());
                }
            }
        }
        return list;
    }

    private double parseDouble(String val) {
        if (val == null || val.isEmpty()) return 0.0;
        return Double.parseDouble(val.replace(",", "."));
    }
}
