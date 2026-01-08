package subsystems.calcolo_formazione;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import subsystems.team_management.model.Player;

public class CalcolaFormazione {

    private static final List<String> MODULI_AMMESSI = Arrays.asList(
            "3-4-3", "3-5-2", "4-3-3", "4-4-2", "4-5-1", "5-3-2", "5-4-1"
    );

    public List<Player> calcolaFormazione(List<PlayerWithStats> rosa, String modulo, int giornataCorrente, AlgoritmoConfig config) {
        if (rosa == null) {
            throw new IllegalArgumentException("Rosa nulla");
        }

        if (!MODULI_AMMESSI.contains(modulo)) {
            throw new IllegalArgumentException("Modulo non valido: " + modulo);
        }

        AlgoritmoConfig cfg = (config != null) ? config : AlgoritmoConfig.defaultConfig();

        List<PlayerWithStats> portieri = new ArrayList<>();
        List<PlayerWithStats> difensori = new ArrayList<>();
        List<PlayerWithStats> centrocampisti = new ArrayList<>();
        List<PlayerWithStats> attaccanti = new ArrayList<>();

        for (PlayerWithStats pws : rosa) {
            if (pws == null || pws.getPlayer() == null) {
                continue;
            }
            String ruolo = pws.getRuolo();
            if ("P".equalsIgnoreCase(ruolo)) {
                portieri.add(pws);
            } else if ("D".equalsIgnoreCase(ruolo)) {
                difensori.add(pws);
            } else if ("C".equalsIgnoreCase(ruolo)) {
                centrocampisti.add(pws);
            } else if ("A".equalsIgnoreCase(ruolo)) {
                attaccanti.add(pws);
            }
        }

        ordinaLista(portieri, giornataCorrente, cfg);
        ordinaLista(difensori, giornataCorrente, cfg);
        ordinaLista(centrocampisti, giornataCorrente, cfg);
        ordinaLista(attaccanti, giornataCorrente, cfg);

        String[] partiModulo = modulo.split("-");
        int numDifensori = Integer.parseInt(partiModulo[0]);
        int numCentrocampisti = Integer.parseInt(partiModulo[1]);
        int numAttaccanti = Integer.parseInt(partiModulo[2]);

        List<Player> formazioneFinale = new ArrayList<>();

        if (!portieri.isEmpty()) {
            formazioneFinale.add(portieri.get(0).getPlayer());
        }

        for (int i = 0; i < numDifensori && i < difensori.size(); i++) {
            formazioneFinale.add(difensori.get(i).getPlayer());
        }

        for (int i = 0; i < numCentrocampisti && i < centrocampisti.size(); i++) {
            formazioneFinale.add(centrocampisti.get(i).getPlayer());
        }

        for (int i = 0; i < numAttaccanti && i < attaccanti.size(); i++) {
            formazioneFinale.add(attaccanti.get(i).getPlayer());
        }

        for (int i = 1; i < portieri.size(); i++) {
            formazioneFinale.add(portieri.get(i).getPlayer());
        }

        for (int i = numDifensori; i < difensori.size(); i++) {
            formazioneFinale.add(difensori.get(i).getPlayer());
        }

        for (int i = numCentrocampisti; i < centrocampisti.size(); i++) {
            formazioneFinale.add(centrocampisti.get(i).getPlayer());
        }

        for (int i = numAttaccanti; i < attaccanti.size(); i++) {
            formazioneFinale.add(attaccanti.get(i).getPlayer());
        }

        return formazioneFinale;
    }

    private void ordinaLista(List<PlayerWithStats> lista, int giornata, AlgoritmoConfig config) {
        lista.sort((p1, p2) -> {
            double s1 = calcolaPunteggio(p1, giornata, config);
            double s2 = calcolaPunteggio(p2, giornata, config);
            return Double.compare(s2, s1);
        });
    }

    private double calcolaPunteggio(PlayerWithStats item, int giornataCorrente, AlgoritmoConfig config) {
        if (item == null || item.getPlayer() == null) {
            return 0.0;
        }

        Player player = item.getPlayer();
        Statistiche stats = item.getStatistiche();

        double mediaVoto = (stats != null) ? stats.getMediaVoto() : player.getMediaVoto();
        double fantaMedia = (stats != null) ? stats.getFantaMedia() : player.getFantamedia();
        int golFatti = (stats != null) ? stats.getGolFatti() : player.getGolFatti();
        int golSubiti = (stats != null) ? stats.getGolSubiti() : player.getGolSubiti();
        int assist = (stats != null) ? stats.getAssist() : player.getAssist();

        int giornataSafe = Math.max(giornataCorrente, 1);
        int partiteVoto = (stats != null && stats.getPartiteVoto() > 0) ? stats.getPartiteVoto() : giornataSafe;
        double costanza = ((double) partiteVoto / giornataSafe) * 100.0;

        if ("P".equalsIgnoreCase(player.getRuolo())) {
            double mvNormalizzata = (mediaVoto / 8.0) * 100.0;
            double malusGol = ((double) golSubiti / Math.max(partiteVoto, 1)) * 40.0;
            double gsScore = 100.0 - malusGol;

            return (mvNormalizzata * config.getPesoMvPortiere())
                    + (gsScore * config.getPesoGsPortiere())
                    + (costanza * config.getPesoCostanzaPortiere());
        }

        double fmNormalizzata = (fantaMedia / 11.0) * 100.0;
        double fattoreGol = ((double) golFatti / Math.max(partiteVoto, 1)) * 100.0;
        double fattoreAssist = ((double) assist / Math.max(partiteVoto, 1)) * 100.0;

        return (fmNormalizzata * config.getPesoFmGiocatore())
                + (costanza * config.getPesoCostanzaGiocatore())
                + (fattoreGol * config.getPesoGol())
                + (fattoreAssist * config.getPesoAssist());
    }
}
