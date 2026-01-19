package utils;

import subsystems.statistics_viewer.model.Statistiche;
import subsystems.team_management.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    // classe interna per contenere i dati di una riga
    public static class ImportData {
        public Player player;
        public Statistiche statistiche;

        public ImportData(Player player, Statistiche statistiche) {
            this.player = player;
            this.statistiche = statistiche;
        }
    }

    public List<ImportData> parse(InputStream is, int giornata) throws IOException {
        List<ImportData> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;

                // prima riga: intestazione
                if (lineNum == 1) {
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                // formato fisso: 17 colonne
                String[] parts = line.split(";", -1);
                if (parts.length < 17) {
                    throw new IllegalArgumentException("CSV non valido");
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String ruolo = parts[1].trim();
                    String nome = parts[2].trim();
                    String squadra = parts[3].trim();

                    Player p = new Player();
                    p.setId(id);
                    p.setNome(nome);
                    p.setRuolo(ruolo);
                    p.setSquadra(squadra);


                    p.setMediaVoto((float) parseDouble(parts[5]));
                    p.setFantamedia((float) parseDouble(parts[6]));
                    p.setGolFatti(Integer.parseInt(parts[7].trim()));
                    p.setGolSubiti(Integer.parseInt(parts[8].trim()));
                    p.setAssist(Integer.parseInt(parts[13].trim()));

                    Statistiche s = new Statistiche();
                    s.setIdCalciatore(id);
                    s.setGiornata(giornata);

                    s.setPartiteVoto(Integer.parseInt(parts[4].trim()));
                    s.setMediaVoto(parseDouble(parts[5]));
                    s.setFantaMedia(parseDouble(parts[6]));
                    s.setGolFatti(Integer.parseInt(parts[7].trim()));
                    s.setGolSubiti(Integer.parseInt(parts[8].trim()));
                    s.setRigoriParati(Integer.parseInt(parts[9].trim()));
                    s.setRigoriCalciati(Integer.parseInt(parts[10].trim()));
                    s.setRigoriSegnati(Integer.parseInt(parts[11].trim()));
                    s.setRigoriSbagliati(Integer.parseInt(parts[12].trim()));
                    s.setAssist(Integer.parseInt(parts[13].trim()));
                    s.setAmmonizioni(Integer.parseInt(parts[14].trim()));
                    s.setEspulsioni(Integer.parseInt(parts[15].trim()));
                    s.setAutogol(Integer.parseInt(parts[16].trim()));

                    list.add(new ImportData(p, s));
                } catch (Exception e) {
                    // errore generico: fermo import
                    throw new IllegalArgumentException("CSV non valido");
                }
            }
        }

        return list;
    }

    private double parseDouble(String val) {
        return Double.parseDouble(val.trim().replace(",", "."));
    }
}
