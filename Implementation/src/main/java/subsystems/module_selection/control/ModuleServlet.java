package subsystems.module_selection.control;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.module_selection.model.Module;

import java.io.IOException;
import java.util.List;


@WebServlet("/modules")
public class ModuleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Module> modules = Module.getValidModules();

        response.setContentType("application/json;charset=UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            json.append("{")
                    .append("\"id\":\"").append(m.getId()).append("\",")
                    .append("\"difensori\":").append(m.getDifensori()).append(",")
                    .append("\"centrocampisti\":").append(m.getCentrocampisti()).append(",")
                    .append("\"attaccanti\":").append(m.getAttaccanti())
                    .append("}");
            if (i < modules.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        response.getWriter().write(json.toString());
    }
}
