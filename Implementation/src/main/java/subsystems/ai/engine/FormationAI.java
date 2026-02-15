package subsystems.ai.engine;

import subsystems.ai.model.PlayerStats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormationAI {

    private static final int[][] MODULI = {
            {3, 4, 3}, {3, 5, 2}, {4, 3, 3}, {4, 4, 2}, {4, 5, 1}, {5, 3, 2}, {5, 4, 1}
    };

    private Map<String, String> opponentMap = new HashMap<>();

    // Mappa per salvare l'Indice di Difficoltà (0-100) di ogni squadra
    private Map<String, Double> teamDifficultyMap = new HashMap<>();

    // --- 1. RECUPERA TUTTI I GIOCATORI ---
    public Map<Integer, PlayerStats> getAllPlayerStats(File csvDir) {
        Map<Integer, PlayerStats> map = new HashMap<>();
        if (csvDir == null || !csvDir.exists()) return map;

        File[] files = csvDir.listFiles((dir, name) -> name.startsWith("Statistiche") && name.endsWith(".csv"));
        if (files == null || files.length == 0) return map;

        Arrays.sort(files, (f1, f2) -> Integer.compare(extractGiornata(f1.getName()), extractGiornata(f2.getName())));
        File lastFile = files[files.length - 1];

        // Carica calendario per le avversarie
        File calendarFile = new File(csvDir, "Calendario.csv");
        if(calendarFile.exists()) {
            int day = findUpcomingGiornata(calendarFile);
            loadCalendarForDay(calendarFile, day);
        }

        List<PlayerStats> list = loadStatsFromCsv(lastFile.getAbsolutePath());
        for (PlayerStats p : list) {
            String opp = opponentMap.getOrDefault(p.getSquadra(), "--");
            p.setProssimaAvversaria(opp);
            map.put(p.getId(), p);
        }
        return map;
    }

    // --- 2. METODO PRINCIPALE GENERAZIONE---
    public Map<String, List<PlayerStats>> generateFormationWithMatchup(File csvDir, File calendarFile, List<Integer> userSquadIds, String forcedModule) {
        int targetDay = findUpcomingGiornata(calendarFile);

        File[] files = csvDir.listFiles((dir, name) -> name.startsWith("Statistiche") && name.endsWith(".csv"));
        if (files == null || files.length == 0) return new HashMap<>();

        Arrays.sort(files, (f1, f2) -> Integer.compare(extractGiornata(f1.getName()), extractGiornata(f2.getName())));

        List<PlayerStats> currentStats = loadStatsFromCsv(files[files.length - 1].getAbsolutePath());

        // CALCOLA LA FORZA DI TUTTE LE SQUADRE
        calculateAllTeamDifficulties(currentStats);

        loadCalendarForDay(calendarFile, targetDay);

        // Storico per trend
        Map<Integer, List<Double>> historyFm = new HashMap<>();
        for (File f : files) {
            List<PlayerStats> stats = loadStatsFromCsv(f.getAbsolutePath());
            for (PlayerStats p : stats) historyFm.computeIfAbsent(p.getId(), k -> new ArrayList<>()).add(p.getFantaMedia());
        }

        List<PlayerStats> mySquadStats = currentStats.stream()
                .filter(p -> userSquadIds.contains(p.getId()))
                .collect(Collectors.toList());

        // --- CUORE DEL CALCOLO PUNTEGGIO ---
        for (PlayerStats p : mySquadStats) {

            // 1. Punteggio Base:
            double basePerformance = (p.getFantaMedia() * 0.6) + (p.getMediaVoto() * 0.4);

            // 2. Bonus Assist:
            double assistFactor = 0;
            if (p.getPartiteVoto() > 0) {
                double avgAssist = (double) p.getAssist() / p.getPartiteVoto();
                assistFactor = avgAssist * 2.0;
            }

            double scoreWithStats = basePerformance + assistFactor;

            // 3. Trend Storico
            double trendScore = applyTrend(scoreWithStats, historyFm.get(p.getId()), p.getFantaMedia());

            // 4. Setta Avversaria
            String oppTeamName = opponentMap.getOrDefault(p.getSquadra(), "Riposo");
            p.setProssimaAvversaria(oppTeamName);

            // 5. FATTORE AVVERSARIO
            double finalScore = applyOpponentDifficultyFactor(p, trendScore, oppTeamName);

            p.setAiScore(finalScore);
        }

        List<PlayerStats> por = getByRole(mySquadStats, "P");
        List<PlayerStats> dif = getByRole(mySquadStats, "D");
        List<PlayerStats> cen = getByRole(mySquadStats, "C");
        List<PlayerStats> att = getByRole(mySquadStats, "A");

        sortPlayers(por); sortPlayers(dif); sortPlayers(cen); sortPlayers(att);

        if (forcedModule != null && !forcedModule.isEmpty()) {
            return generateForSpecificModule(por, dif, cen, att, forcedModule);
        } else {
            return selectBestModule(por, dif, cen, att);
        }
    }

    // --- CALCOLO DIFFICOLTÀ SQUADRE (Identico alla Servlet Statistiche) ---
    private void calculateAllTeamDifficulties(List<PlayerStats> allPlayers) {
        teamDifficultyMap.clear();
        Map<String, List<PlayerStats>> byTeam = allPlayers.stream().collect(Collectors.groupingBy(PlayerStats::getSquadra));

        for (Map.Entry<String, List<PlayerStats>> entry : byTeam.entrySet()) {
            List<PlayerStats> roster = entry.getValue();
            // Media top 14
            double avgFm = roster.stream().mapToDouble(PlayerStats::getFantaMedia)
                    .sorted().skip(Math.max(0, roster.size() - 14)).average().orElse(0.0);

            double gf = roster.stream().mapToInt(PlayerStats::getGoalFatti).sum();
            double gs = roster.stream().mapToInt(PlayerStats::getGolSubiti).sum();

            // Indice 0-100
            double index = (avgFm * 10) + (gf * 0.2) - (gs * 0.5);
            teamDifficultyMap.put(entry.getKey(), index);
        }
    }


    private double applyOpponentDifficultyFactor(PlayerStats p, double currentScore, String opponentName) {
        if (opponentName.equals("Riposo") || opponentName.equals("--")) return currentScore * 0.5; // Malus se riposa

        // Recupera la forza dell'avversario
        double oppStrength = teamDifficultyMap.getOrDefault(opponentName, 50.0);


        double difficultyImpact = 0.0;

        // Logica Differenziata per Ruolo
        if (p.getRuolo().equalsIgnoreCase("P") || p.getRuolo().equalsIgnoreCase("D")) {

            difficultyImpact = (50.0 - oppStrength) / 100.0;
        } else {

            difficultyImpact = (50.0 - oppStrength) / 100.0;
        }


        double weight = 1.5;
        double multiplier = 1.0 + (difficultyImpact * weight);

        // Evitiamo moltiplicatori negativi
        if (multiplier < 0.1) multiplier = 0.1;

        return currentScore * multiplier;
    }

    private double applyTrend(double score, List<Double> history, double currentFm) {
        if (history == null || history.isEmpty()) return score;
        double pastAvg = 0; int count = 0;
        for (int i = 0; i < history.size() - 1; i++) { pastAvg += history.get(i); count++; }
        if (count > 0) pastAvg /= count; else pastAvg = currentFm;

        double diff = currentFm - pastAvg;
        // Trend pesa poco (0.1), giusto per favorire chi è in forma
        return score * (1.0 + (diff * 0.1));
    }


    public int findUpcomingGiornata(File calendarFile) {
        if (calendarFile == null || !calendarFile.exists()) return 1;
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TreeMap<LocalDate, Integer> schedule = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(calendarFile))) {
            String line; br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    try {
                        String dataStr = parts[0].trim();
                        int g = Integer.parseInt(parts[2].trim());
                        LocalDate matchDate = LocalDate.parse(dataStr, formatter);
                        schedule.putIfAbsent(matchDate, g);
                    } catch(Exception e) { }
                }
            }
        } catch (Exception e) { }
        for (Map.Entry<LocalDate, Integer> entry : schedule.entrySet()) {
            if (entry.getKey().isEqual(today) || entry.getKey().isAfter(today)) return entry.getValue();
        }
        return 38;
    }

    private void loadCalendarForDay(File calendarFile, int day) {
        opponentMap.clear();
        if (calendarFile == null || !calendarFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(calendarFile))) {
            String line; br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    try {
                        int g = Integer.parseInt(parts[2].trim());
                        if (g == day) {
                            String casa = parts[3].trim(); String ospite = parts[4].trim();
                            opponentMap.put(casa, ospite); opponentMap.put(ospite, casa);
                        }
                    } catch(NumberFormatException e) {}
                }
            }
        } catch (Exception e) { }
    }

    private Map<String, List<PlayerStats>> generateForSpecificModule(List<PlayerStats> por, List<PlayerStats> dif, List<PlayerStats> cen, List<PlayerStats> att, String moduleStr) {
        Map<String, List<PlayerStats>> f = new HashMap<>();
        try {
            String[] parts = moduleStr.split("-");
            int nDif = Integer.parseInt(parts[0]);
            int nCen = Integer.parseInt(parts[1]);
            int nAtt = Integer.parseInt(parts[2]);
            f.put("P", por.stream().limit(1).collect(Collectors.toList()));
            f.put("D", dif.stream().limit(nDif).collect(Collectors.toList()));
            f.put("C", cen.stream().limit(nCen).collect(Collectors.toList()));
            f.put("A", att.stream().limit(nAtt).collect(Collectors.toList()));
        } catch (Exception e) { return selectBestModule(por, dif, cen, att); }
        return f;
    }

    private Map<String, List<PlayerStats>> selectBestModule(List<PlayerStats> por, List<PlayerStats> dif, List<PlayerStats> cen, List<PlayerStats> att) {
        int[] bestModule = null; double bestTotalScore = -Double.MAX_VALUE;
        for (int[] modulo : MODULI) {
            int nDif = modulo[0], nCen = modulo[1], nAtt = modulo[2];
            if (dif.size() >= nDif && cen.size() >= nCen && att.size() >= nAtt && !por.isEmpty()) {
                double currentScore = por.get(0).getAiScore() + sumScore(dif, nDif) + sumScore(cen, nCen) + sumScore(att, nAtt);
                if (currentScore > bestTotalScore) { bestTotalScore = currentScore; bestModule = modulo; }
            }
        }
        Map<String, List<PlayerStats>> f = new HashMap<>();
        if (bestModule != null) {
            f.put("P", por.subList(0, 1)); f.put("D", dif.subList(0, bestModule[0])); f.put("C", cen.subList(0, bestModule[1])); f.put("A", att.subList(0, bestModule[2]));
        }
        return f;
    }

    private double sumScore(List<PlayerStats> players, int count) { return players.stream().limit(count).mapToDouble(PlayerStats::getAiScore).sum(); }
    private void sortPlayers(List<PlayerStats> players) { players.sort((p1, p2) -> Double.compare(p2.getAiScore(), p1.getAiScore())); }
    private List<PlayerStats> getByRole(List<PlayerStats> list, String role) { return list.stream().filter(p -> p.getRuolo().equalsIgnoreCase(role)).collect(Collectors.toList()); }
    private int extractGiornata(String filename) { Pattern p = Pattern.compile("\\d+"); Matcher m = p.matcher(filename); if (m.find()) return Integer.parseInt(m.group()); return 0; }

    public List<PlayerStats> loadStatsFromCsv(String path) {
        List<PlayerStats> stats = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.mark(4096);
            String firstLine = br.readLine();
            if (firstLine != null && (firstLine.toLowerCase().contains("ruolo") || firstLine.toLowerCase().contains("nome"))) { } else { br.reset(); }
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.contains(";") ? line.split(";") : line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (values.length > 8) {
                    try {
                        int id = parseIntSafe(values[0]);
                        String ruolo = values[1].replace("\"", "").trim();
                        String nome = values[2].replace("\"", "").trim();
                        String squadra = values[3].replace("\"", "").trim();
                        int pv = parseIntSafe(values[4]);
                        double mv = parseDoubleSafe(values[5]);
                        double fm = parseDoubleSafe(values[6]);
                        int gf = parseIntSafe(values[7]);
                        int gs = parseIntSafe(values[8]);
                        int ass = 0;
                        if(values.length > 13) ass = parseIntSafe(values[13]);
                        stats.add(new PlayerStats(id, ruolo, nome, squadra, pv, mv, fm, gf, gs, ass));
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return stats;
    }
    private double parseDoubleSafe(String val) { if (val == null) return 0.0; val = val.replace("\"", "").trim().replace(",", "."); if (val.isEmpty() || val.equals("-")) return 0.0; try { return Double.parseDouble(val); } catch (Exception e) { return 0.0; } }
    private int parseIntSafe(String val) { if (val == null) return 0; val = val.replace("\"", "").trim(); if (val.isEmpty() || val.equals("-")) return 0; try { return Integer.parseInt(val); } catch (Exception e) { return 0; } }
}