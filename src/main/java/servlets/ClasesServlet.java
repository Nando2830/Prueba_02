package servlets;

import dao.ClasesJpaController;
import dto.Clases;
import dto.Cursos;
import dto.Materias;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ClasesServlet", urlPatterns = {"/clases"})
public class ClasesServlet extends HttpServlet {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU");
    ClasesJpaController dao = new ClasesJpaController(emf);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            for (Clases c : dao.findClasesEntities()) {
                jsonArray.add(Json.createObjectBuilder()
                        .add("id", c.getId())
                        .add("horario", c.getHorario())
                        .add("aula", c.getAula())
                        .add("cursoId", c.getCursoId().getId())
                        .add("materiaId", c.getMateriaId().getId())
                );
            }
            out.print(Json.createObjectBuilder().add("data", jsonArray).build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        JsonObject json = Json.createReader(request.getInputStream()).readObject();

        Clases c = new Clases();
        c.setHorario(json.getString("horario"));
        c.setAula(json.getString("aula"));
        c.setCursoId(new Cursos(json.getInt("cursoId")));
        c.setMateriaId(new Materias(json.getInt("materiaId")));
        dao.create(c);

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(Json.createObjectBuilder().add("message", "Clase creada").build());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        JsonObject json = Json.createReader(request.getInputStream()).readObject();

        Clases c = dao.findClases(json.getInt("id"));
        if (c != null) {
            c.setHorario(json.getString("horario"));
            c.setAula(json.getString("aula"));
            c.setCursoId(new Cursos(json.getInt("cursoId")));
            c.setMateriaId(new Materias(json.getInt("materiaId")));
            try {
                dao.edit(c);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print(Json.createObjectBuilder().add("message", "Clase actualizada").build());
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        JsonObject json = Json.createReader(request.getInputStream()).readObject();

        try {
            dao.destroy(json.getInt("id"));
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(Json.createObjectBuilder().add("message", "Clase eliminada").build());
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
