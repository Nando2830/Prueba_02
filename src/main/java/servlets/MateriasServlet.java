package servlets;

import dao.MateriasJpaController;
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

@WebServlet(name = "MateriasServlet", urlPatterns = {"/materias"})
public class MateriasServlet extends HttpServlet {

    private MateriasJpaController dao;

    @Override
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU");
        dao = new MateriasJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");

        List<Materias> materias = dao.findMateriasEntities();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Materias m : materias) {
            jsonArrayBuilder.add(Json.createObjectBuilder()
                .add("id", m.getId())
                .add("nombre", m.getNombre())
                .add("descripcion", m.getDescripcion() != null ? m.getDescripcion() : ""));
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(Json.createObjectBuilder()
                .add("materias", jsonArrayBuilder)
                .build()
                .toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonObject json = Json.createReader(request.getInputStream()).readObject();
        String nombre = json.getString("nombre", "");
        String descripcion = json.getString("descripcion", "");

        Materias nueva = new Materias();
        nueva.setNombre(nombre);
        nueva.setDescripcion(descripcion);

        dao.create(nueva);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonObject json = Json.createReader(request.getInputStream()).readObject();

        int id = json.getInt("id");
        Materias m = dao.findMaterias(id);
        if (m == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        m.setNombre(json.getString("nombre", ""));
        m.setDescripcion(json.getString("descripcion", ""));

        try {
            dao.edit(m);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        try {
            dao.destroy(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
