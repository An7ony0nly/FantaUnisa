package utils;

import org.junit.jupiter.api.Test;
import subsystems.statistics_viewer.model.Statistiche;
import subsystems.team_management.model.Player;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @Test
    void testParseValidCsv() throws IOException {
        String csvContent = """
                Id;R;Nome;Squadra;Pv;Mv;Fm;Gf;Gs;Rp;Rc;R+;R-;Ass;Amm;Esp;Au
                101;P;Szczesny;Juventus;1;6,5;6,5;0;0;0;0;0;0;0;0;0;0
                102;D;Bremer;Juventus;1;6,0;6,0;0;0;0;0;0;0;0;1;0;0""";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        CsvParser parser = new CsvParser();
        List<CsvParser.ImportData> result = parser.parse(inputStream, 1);

        assertEquals(2, result.size());

        // Check first player
        Player p1 = result.getFirst().player;
        Statistiche s1 = result.getFirst().statistiche;
        assertEquals(101, p1.getId());
        assertEquals("Szczesny", p1.getNome());
        assertEquals("Juventus", p1.getSquadra());
        assertEquals("P", p1.getRuolo());
        assertEquals(6.5, p1.getMediaVoto(), 0.01);
        assertEquals(6.5, p1.getFantamedia(), 0.01);
        assertEquals(0, p1.getGolFatti());
        assertEquals(0, p1.getGolSubiti());
        assertEquals(0, p1.getAssist());

        assertEquals(101, s1.getIdCalciatore());
        assertEquals(1, s1.getGiornata());
        assertEquals(1, s1.getPartiteVoto());
        assertEquals(6.5, s1.getMediaVoto(), 0.01);
        assertEquals(6.5, s1.getFantaMedia(), 0.01);
        assertEquals(0, s1.getGolFatti());
        assertEquals(0, s1.getGolSubiti());
        assertEquals(0, s1.getRigoriParati());
        assertEquals(0, s1.getRigoriCalciati());
        assertEquals(0, s1.getRigoriSegnati());
        assertEquals(0, s1.getRigoriSbagliati());
        assertEquals(0, s1.getAssist());
        assertEquals(0, s1.getAmmonizioni());
        assertEquals(0, s1.getEspulsioni());
        assertEquals(0, s1.getAutogol());

        // Check second player
        Player p2 = result.get(1).player;
        Statistiche s2 = result.get(1).statistiche;
        assertEquals(102, p2.getId());
        assertEquals("Bremer", p2.getNome());
        assertEquals("Juventus", p2.getSquadra());
        assertEquals("D", p2.getRuolo());
        assertEquals(6.0, p2.getMediaVoto(), 0.01);
        assertEquals(6.0, p2.getFantamedia(), 0.01);
        assertEquals(0, p2.getGolFatti());
        assertEquals(0, p2.getGolSubiti());
        assertEquals(0, p2.getAssist());

        assertEquals(102, s2.getIdCalciatore());
        assertEquals(1, s2.getGiornata());
        assertEquals(1, s2.getPartiteVoto());
        assertEquals(6.0, s2.getMediaVoto(), 0.01);
        assertEquals(6.0, s2.getFantaMedia(), 0.01);
        assertEquals(0, s2.getGolFatti());
        assertEquals(0, s2.getGolSubiti());
        assertEquals(0, s2.getRigoriParati());
        assertEquals(0, s2.getRigoriCalciati());
        assertEquals(0, s2.getRigoriSegnati());
        assertEquals(0, s2.getRigoriSbagliati());
        assertEquals(0, s2.getAssist());
        assertEquals(1, s2.getAmmonizioni());
        assertEquals(0, s2.getEspulsioni());
        assertEquals(0, s2.getAutogol());
    }

    @Test
    void testParseEmptyCsv() throws IOException {
        String csvContent = "";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        CsvParser parser = new CsvParser();
        List<CsvParser.ImportData> result = parser.parse(inputStream, 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseInvalidFormat() {
        String csvContent = """
                Id;R;Nome;Squadra
                101;P;Szczesny;Juventus"""; // Not enough columns

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        CsvParser parser = new CsvParser();
        
        assertThrows(IllegalArgumentException.class, () -> parser.parse(inputStream, 1));
    }

    @Test
    void testParseInvalidNumber() {
        String csvContent = """
                Id;R;Nome;Squadra;Pv;Mv;Fm;Gf;Gs;Rp;Rc;R+;R-;Ass;Amm;Esp;Au
                abc;P;Szczesny;Juventus;1;6,5;6,5;0;0;0;0;0;0;0;0;0;0"""; // Invalid ID

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        CsvParser parser = new CsvParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(inputStream, 1));
    }
}
