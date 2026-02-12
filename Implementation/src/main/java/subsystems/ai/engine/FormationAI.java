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
    private Map<String, int[]> teamStrength = new HashMap<>();

    // --- NUOVO METODO: Restituisce la mappa completa delle statistiche per la JSP ---
    public Map<Integer, PlayerStats> getAllPlayerStats(File csvDir) {
        Map<Integer, PlayerStats> map = new HashMap<>();
        if (csvDir == null || !csvDir.exists()) return map;

        File[] files = csvDir.listFiles((dir, name) -> name.startsWith("Statistiche") && name.endsWith(".csv"));
        if (files == null || files.length == 0) return map;

        // Prendi l'ultimo file disponibile (il più recente)
        Arrays.sort(files, (f1, f2) -> Integer.compare(extractGiornata(f1.getName()), extractGiornata(f2.getName())));
        File lastFile = files[files.length - 1];

        List<PlayerStats> list = loadStatsFromCsv(lastFile.getAbsolutePath());
        for (PlayerStats p : list) {
            map.put(p.getId(), p);
        }
        return map;
    }

    // --- 1. TROVA GIORNATA ---
    public int findUpcomingGiornata(File calendarFile) {
        if (calendarFile == null || !calendarFile.exists()) return 1;

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TreeMap<LocalDate, Integer> schedule = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(calendarFile))) {
            String line;
            br.readLine();
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
        } catch (Exception e) { e.printStackTrace(); }

        for (Map.Entry<LocalDate, Integer> entry : schedule.entrySet()) {
            if (entry.getKey().isEqual(today) || entry.getKey().isAfter(today)) {
                return entry.getValue();
            }
        }
        return 38;
    }

    // --- 2. GENERA FORMAZIONE ---
    public Map<String, List<PlayerStats>> generateFormationWithMatchup(File csvDir, File calendarFile, List<Integer> userSquadIds) {
        int targetDay = findUpcomingGiornata(calendarFile);

        File[] files = csvDir.listFiles((dir, name) -> name.startsWith("Statistiche") && name.endsWith(".csv"));
        if (files == null || files.length == 0) return new HashMap<>();

        Arrays.sort(files, (f1, f2) -> Integer.compare(extractGiornata(f1.getName()), extractGiornata(f2.getName())));

        List<PlayerStats> currentStats = loadStatsFromCsv(files[files.length - 1].getAbsolutePath());
        calculateTeamStrengths(files);
        loadCalendarForDay(calendarFile, targetDay);

        Map<Integer, List<Double>> historyFm = new HashMap<>();
        for (File f : files) {
            List<PlayerStats> stats = loadStatsFromCsv(f.getAbsolutePath());
            for (PlayerStats p : stats) historyFm.computeIfAbsent(p.getId(), k -> new ArrayList<>()).add(p.getFantaMedia());
        }

        List<PlayerStats> mySquadStats = currentStats.stream()
                .filter(p -> userSquadIds.contains(p.getId()))
                .collect(Collectors.toList());

        for (PlayerStats p : mySquadStats) {
            double baseScore = calculateTrendScore(p, historyFm.get(p.getId()));
            double finalScore = applyMatchupFactor(p, baseScore);
            p.setAiScore(finalScore);
        }

        List<PlayerStats> por = getByRole(mySquadStats, "P");
        List<PlayerStats> dif = getByRole(mySquadStats, "D");
        List<PlayerStats> cen = getByRole(mySquadStats, "C");
        List<PlayerStats> att = getByRole(mySquadStats, "A");

        sortPlayers(por); sortPlayers(dif); sortPlayers(cen); sortPlayers(att);

        Map<String, List<PlayerStats>> result = selectBestModule(por, dif, cen, att);

        if(!result.isEmpty()) {
            result.put("NEXT_DAY_INFO", new ArrayList<>());
            result.get("NEXT_DAY_INFO").add(new PlayerStats(targetDay, "", "", "", 0, 0, 0, 0, 0));
        }
        return result;
    }

    private void loadCalendarForDay(File calendarFile, int day) {
        opponentMap.clear();
        if (calendarFile == null || !calendarFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(calendarFile))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    try {
                        int g = Integer.parseInt(parts[2].trim());
                        if (g == day) {
                            String casa = parts[3].trim();
                            String ospite = parts[4].trim();
                            opponentMap.put(casa, ospite);
                            opponentMap.put(ospite, casa);
                        }
                    } catch(NumberFormatException e) {}
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- HELPERS ---
    private void calculateTeamStrengths(File[] files) {
        if (files.length > 0) {
            List<PlayerStats> lastStats = loadStatsFromCsv(files[files.length-1].getAbsolutePath());
            teamStrength.clear();
            for (PlayerStats p : lastStats) {
                teamStrength.putIfAbsent(p.getSquadra(), new int[]{0, 0});
                teamStrength.get(p.getSquadra())[0] += p.getGoalFatti();
            }
            for (String team : teamStrength.keySet()) {
                int gf = teamStrength.get(team)[0];
                teamStrength.get(team)[1] = 50 - (gf / 2);
            }
        }
    }

    private double calculateTrendScore(PlayerStats p, List<Double> history) {
        if (history == null || history.isEmpty()) return p.getAiScore();
        double currentFm = p.getFantaMedia();
        double pastAvg = 0;
        int count = 0;
        for (int i = 0; i < history.size() - 1; i++) {
            pastAvg += history.get(i);
            count++;
        }
        if (count > 0) pastAvg /= count; else pastAvg = currentFm;
        double diff = currentFm - pastAvg;
        double trendFactor = 1.0 + (diff * 0.2);
        return p.getAiScore() * trendFactor;
    }

    private double applyMatchupFactor(PlayerStats p, double score) {
        String myTeam = p.getSquadra();
        String opponent = null;
        for(String k : opponentMap.keySet()) if(k.equalsIgnoreCase(myTeam)) opponent = opponentMap.get(k);
        if (opponent == null) return score;

        int[] oppStats = new int[]{20, 20}; // default
        for(String k : teamStrength.keySet()) if(k.equalsIgnoreCase(opponent)) oppStats = teamStrength.get(k);

        int oppGoalsScored = oppStats[0];
        int oppGoalsConceded = oppStats[1];
        double modifier = 1.0;

        if (p.getRuolo().equalsIgnoreCase("P") || p.getRuolo().equalsIgnoreCase("D")) {
            if (oppGoalsScored > 35) modifier -= 0.15;
            else if (oppGoalsScored > 25) modifier -= 0.05;
            else if (oppGoalsScored < 15) modifier += 0.10;
        } else if (p.getRuolo().equalsIgnoreCase("A")) {
            if (oppGoalsConceded > 35) modifier += 0.15;
            else if (oppGoalsConceded > 25) modifier += 0.05;
            else if (oppGoalsConceded < 15) modifier -= 0.10;
        }
        if (p.getRuolo().equalsIgnoreCase("C")) {
            if (oppGoalsConceded > 30) modifier += 0.05;
            if (oppGoalsScored > 30) modifier -= 0.05;
        }
        return score * modifier;
    }

    private Map<String, List<PlayerStats>> selectBestModule(List<PlayerStats> por, List<PlayerStats> dif, List<PlayerStats> cen, List<PlayerStats> att) {
        int[] bestModule = null; double bestTotalScore = -Double.MAX_VALUE;
        for (int[] modulo : MODULI) {
            int nDif = modulo[0]; int nCen = modulo[1]; int nAtt = modulo[2];
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
            String line; br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length > 7) {
                    try {
                        int id = Integer.parseInt(values[0]); String ruolo = values[1]; String nome = values[2]; String squadra = values[3];
                        int pv = Integer.parseInt(values[4]); double mv = parseDouble(values[5]); double fm = parseDouble(values[6]);
                        int gf = Integer.parseInt(values[7]); int ass = 0;
                        if(values.length > 13) ass = Integer.parseInt(values[13]);
                        stats.add(new PlayerStats(id, ruolo, nome, squadra, pv, mv, fm, gf, ass));
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return stats;
    }
    private double parseDouble(String val) { if (val == null || val.isEmpty()) return 0.0; return Double.parseDouble(val.replace(",", ".")); }
}