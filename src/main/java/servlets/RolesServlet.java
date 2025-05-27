package servlets;

import dao.RolesJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Roles;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/roles")
public class RolesServlet extends HttpServlet {
    private RolesJpaController dao;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU");
        dao = new RolesJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        List<Roles> roles = dao.findRolesEntities();

        for (Roles r : roles) {
            jsonArray.add(Json.createObjectBuilder()
                .add("id", r.getId())
                .add("nombre", r.getNombre()));
        }

        try (JsonWriter writer = Json.createWriter(response.getWriter())) {
            writer.writeArray(jsonArray.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        JsonReader jsonReader = Json.createReader(new StringReader(request.getReader().lines().reduce("", String::concat)));
        JsonObject jsonObject = jsonReader.readObject();

        Roles rol = new Roles();
        rol.setNombre(jsonObject.getString("nombre"));

        dao.create(rol);

        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        JsonReader jsonReader = Json.createReader(new StringReader(request.getReader().lines().reduce("", String::concat)));
        JsonObject jsonObject = jsonReader.readObject();

        int id = jsonObject.getInt("id");
        Roles rol = dao.findRoles(id);
        if (rol != null) {
            rol.setNombre(jsonObject.getString("nombre"));
            try {
                dao.edit(rol);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al actualizar");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Rol no encontrado");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        try {
            dao.destroy(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NonexistentEntityException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Rol no encontrado");
        }
    }
}
