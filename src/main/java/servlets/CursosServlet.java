package servlets;

import dao.CursosJpaController;
import dto.Cursos;
import dto.Profesores;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/cursos")
public class CursosServlet extends HttpServlet {

    private CursosJpaController controller;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU");
        controller = new CursosJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            List<Cursos> cursosList = controller.findCursosEntities();

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Cursos c : cursosList) {
                JsonObjectBuilder builder = Json.createObjectBuilder()
                        .add("id", c.getId())
                        .add("nombre", c.getNombre())
                        .add("anioAcademico", dateFormat.format(c.getAnioAcademico() != null ? c.getAnioAcademico() : new Date()));

                if (c.getProfesorId() != null) {
                    builder.add("profesorId", c.getProfesorId().getId());
                } else {
                    builder.add("profesorId", JsonValue.NULL);
                }

                arrayBuilder.add(builder);
            }

            JsonArray jsonArray = arrayBuilder.build();
            out.print(jsonArray.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject obj = reader.readObject();
            Cursos curso = new Cursos();
            curso.setNombre(obj.getString("nombre"));
            curso.setAnioAcademico(dateFormat.parse(obj.getString("anioAcademico")));

            if (!obj.isNull("profesorId")) {
                Profesores p = new Profesores();
                p.setId(obj.getInt("profesorId"));
                curso.setProfesorId(p);
            }

            controller.create(curso);

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject obj = reader.readObject();
            Cursos curso = controller.findCursos(obj.getInt("id"));
            if (curso == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            curso.setNombre(obj.getString("nombre"));
            curso.setAnioAcademico(dateFormat.parse(obj.getString("anioAcademico")));

            if (!obj.isNull("profesorId")) {
                Profesores p = new Profesores();
                p.setId(obj.getInt("profesorId"));
                curso.setProfesorId(p);
            }

            controller.edit(curso);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject obj = reader.readObject();
            int id = obj.getInt("id");
            controller.destroy(id);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }
}
