package servlets;

import dao.UsuariosJpaController;
import dto.Usuarios;
import dto.Roles;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/usuarios")
public class UsuariosServlet extends HttpServlet {
    private final UsuariosJpaController dao = new UsuariosJpaController(Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU"));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        List<Usuarios> usuarios = dao.findUsuariosEntities();
        for (Usuarios u : usuarios) {
            JsonObject json = Json.createObjectBuilder()
                .add("id", u.getId())
                .add("username", u.getUsername())
                .add("password", u.getPassword())
                .add("correo", u.getCorreo() == null ? "" : u.getCorreo())
                .add("rolId", u.getRolId() != null ? u.getRolId().getId() : 0)
                .build();
            jsonArray.add(json);
        }
        Json.createWriter(response.getWriter()).writeArray(jsonArray.build());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject body = Json.createReader(request.getInputStream()).readObject();
        Usuarios u = new Usuarios();
        u.setUsername(body.getString("username", ""));
        u.setPassword(body.getString("password", ""));
        u.setCorreo(body.getString("correo", ""));
        int rolId = body.getInt("rolId", 0);
        if (rolId > 0) {
            u.setRolId(new Roles(rolId));
        }
        dao.create(u);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject body = Json.createReader(request.getInputStream()).readObject();
        try {
            Usuarios u = dao.findUsuarios(body.getInt("id"));
            if (u != null) {
                u.setUsername(body.getString("username", ""));
                u.setPassword(body.getString("password", ""));
                u.setCorreo(body.getString("correo", ""));
                int rolId = body.getInt("rolId", 0);
                if (rolId > 0) {
                    u.setRolId(new Roles(rolId));
                } else {
                    u.setRolId(null);
                }
                dao.edit(u);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception ex) {
            Logger.getLogger(UsuariosServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject body = Json.createReader(request.getInputStream()).readObject();
        int id = body.getInt("id", 0);
        try {
            dao.destroy(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
