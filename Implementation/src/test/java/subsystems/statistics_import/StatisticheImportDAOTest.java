package subsystems.statistics_import;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import subsystems.calcolo_formazione.Statistiche;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticheImportDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    
    private StatisticheImportDAO statisticheImportDAO;

    @BeforeEach
    void setUp() throws SQLException {
        statisticheImportDAO = new StatisticheImportDAO();
        lenient().when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testDoSaveOrUpdate() throws SQLException {
        Statistiche stats = new Statistiche();
        stats.setIdCalciatore(1);
        stats.setGiornata(1);
        stats.setPartiteVoto(1);
        stats.setMediaVoto(6.0);
        stats.setFantaMedia(6.5);
        stats.setGolFatti(1);
        stats.setGolSubiti(0);
        stats.setRigoriParati(0);
        stats.setRigoriCalciati(0);
        stats.setRigoriSegnati(0);
        stats.setRigoriSbagliati(0);
        stats.setAssist(1);
        stats.setAmmonizioni(0);
        stats.setEspulsioni(0);
        stats.setAutogol(0);

        statisticheImportDAO.doSaveOrUpdate(connection, stats);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setDouble(4, 6.0);
        verify(preparedStatement).setDouble(5, 6.5);
        verify(preparedStatement).setInt(6, 1);
        verify(preparedStatement).setInt(7, 0);
        verify(preparedStatement).setInt(8, 0);
        verify(preparedStatement).setInt(9, 0);
        verify(preparedStatement).setInt(10, 0);
        verify(preparedStatement).setInt(11, 0);
        verify(preparedStatement).setInt(12, 1);
        verify(preparedStatement).setInt(13, 0);
        verify(preparedStatement).setInt(14, 0);
        verify(preparedStatement).setInt(15, 0);

        // Verify update parameters
        verify(preparedStatement).setInt(16, 1);
        verify(preparedStatement).setDouble(17, 6.0);
        verify(preparedStatement).setDouble(18, 6.5);
        verify(preparedStatement).setInt(19, 1);
        verify(preparedStatement).setInt(20, 0);
        verify(preparedStatement).setInt(21, 0);
        verify(preparedStatement).setInt(22, 0);
        verify(preparedStatement).setInt(23, 0);
        verify(preparedStatement).setInt(24, 0);
        verify(preparedStatement).setInt(25, 1);
        verify(preparedStatement).setInt(26, 0);
        verify(preparedStatement).setInt(27, 0);
        verify(preparedStatement).setInt(28, 0);

        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDoSaveOrUpdateNullStats() {
        assertThrows(IllegalArgumentException.class, () -> statisticheImportDAO.doSaveOrUpdate(connection, null));
    }

    @Test
    void testDoSaveOrUpdateSQLException() throws SQLException {
        Statistiche stats = new Statistiche();
        stats.setIdCalciatore(1);

        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> statisticheImportDAO.doSaveOrUpdate(connection, stats));
    }
}
