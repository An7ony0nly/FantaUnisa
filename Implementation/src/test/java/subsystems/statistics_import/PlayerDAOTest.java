package subsystems.statistics_import;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    
    private PlayerDAO playerImportDAO;

    @BeforeEach
    void setUp() throws SQLException {
        playerImportDAO = new PlayerDAO();
        lenient().when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testDoSaveOrUpdate() throws SQLException {
        Player player = new Player();
        player.setId(1);
        player.setNome("Test Player");
        player.setSquadra("Test Team");
        player.setRuolo("A");
        player.setMediaVoto(6.5f);
        player.setFantamedia(7.0f);
        player.setGolFatti(1);
        player.setGolSubiti(0);
        player.setAssist(1);

        playerImportDAO.doSaveOrUpdate(connection, player);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "Test Player");
        verify(preparedStatement).setString(3, "Test Team");
        verify(preparedStatement).setString(4, "A");
        verify(preparedStatement).setFloat(5, 6.5f);
        verify(preparedStatement).setFloat(6, 7.0f);
        verify(preparedStatement).setInt(7, 1);
        verify(preparedStatement).setInt(8, 0);
        verify(preparedStatement).setInt(9, 1);
        
        // Verify update parameters
        verify(preparedStatement).setString(10, "Test Player");
        verify(preparedStatement).setString(11, "Test Team");
        verify(preparedStatement).setString(12, "A");
        verify(preparedStatement).setFloat(13, 6.5f);
        verify(preparedStatement).setFloat(14, 7.0f);
        verify(preparedStatement).setInt(15, 1);
        verify(preparedStatement).setInt(16, 0);
        verify(preparedStatement).setInt(17, 1);

        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDoSaveOrUpdateNullPlayer() {
        assertThrows(IllegalArgumentException.class, () -> playerImportDAO.doSaveOrUpdate(connection, null));
    }
    
    @Test
    void testDoSaveOrUpdateSQLException() throws SQLException {
        Player player = new Player();
        player.setId(1);
        
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        assertThrows(RuntimeException.class, () -> playerImportDAO.doSaveOrUpdate(connection, player));
    }
}
