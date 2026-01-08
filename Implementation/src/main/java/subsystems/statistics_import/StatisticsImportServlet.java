package subsystems.statistics_import;

import connection.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/import-statistiche")
@MultipartConfig
public class StatisticsImportServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StatisticsImportServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String giornataStr = request.getParameter("giornata");
        Part filePart = request.getPart("file");

        if (giornataStr == null || filePart == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");
            return;
        }

        int giornata;
        try {
            giornata = Integer.parseInt(giornataStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Giornata non valida");
            return;
        }

        Connection con = null;
        try (InputStream fileContent = filePart.getInputStream()) {
            //parsing
            CsvParser parser = new CsvParser();
            List<CsvParser.ImportData> dati = parser.parse(fileContent, giornata);

            //salvataggio transazionale
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            PlayerImportDAO playerDAO = new PlayerImportDAO();
            StatisticheImportDAO statisticheDAO = new StatisticheImportDAO();

            for (CsvParser.ImportData item : dati) {
                playerDAO.doSaveOrUpdate(con, item.player);
                statisticheDAO.doSaveOrUpdate(con, item.statistiche);
            }

            con.commit();
            response.getWriter().write("Importazione completata con successo per la giornata " + giornata);

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Errore durante il rollback", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Errore durante l'importazione", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'importazione: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Errore durante la chiusura della connessione", e);
                }
            }
        }
    }
}
