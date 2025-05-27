package servlet;

import com.sun.net.httpserver.Headers;
import dao.UsuariosJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Roles;
import dto.Usuarios;

import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "UsuariosServlet", urlPatterns = {"/usuarios"})
public class UsuariosServlet extends HttpServlet {

    private UsuariosJpaController usuariosJpa;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("TuUnidadPersistencia");
        usuariosJpa = new UsuariosJpaController(emf);
    }

    // For UTF-8 encoding
    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    // Convierte una entidad Usuarios a JsonObject
    private JsonObject usuarioToJson(Usuarios u) {
        JsonObjectBuilder job = Json.createObjectBuilder()
            .add("id", u.getId())
            .add("username", u.getUsername())
            .add("password", u.getPassword() == null ? "" : u.getPassword())
            .add("correo", u.getCorreo() == null ? "" : u.getCorreo());
        if (u.getRolId() != null) {
            job.add("rolId", u.getRolId().getId());
        } else {
            job.addNull("rolId");
        }
        return job.build();
    }

    // Parsea JsonObject recibido en un objeto Usuarios (sin ID para crear, con ID para editar)
    private Usuarios jsonToUsuario(JsonObject json) {
        Usuarios u = new Usuarios();
        if (json.containsKey("id") && !json.isNull("id")) {
            u.setId(json.getInt("id"));
        }
        u.setUsername(json.getString("username", ""));
        u.setPassword(json.getString("password", ""));
        u.setCorreo(json.getString("correo", null));

        if (json.containsKey("rolId") && !json.isNull("rolId")) {
            Roles rol = new Roles();
            rol.setId(json.getInt("rolId"));
            u.setRolId(rol);
        } else {
            u.setRolId(null);
        }
        return u;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        try (PrintWriter out = resp.getWriter()) {
            List<Usuarios> list = usuariosJpa.findUsuariosEntities();
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (Usuarios u : list) {
                jab.add(usuarioToJson(u));
            }
            JsonArray arr = jab.build();
            out.print(arr.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                JsonObject error = Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build();
                out.print(error.toString());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        try (InputStream is = req.getInputStream();
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             PrintWriter out = resp.getWriter()) {
            
            JsonObject json = reader.readObject();
            Usuarios nuevo = jsonToUsuario(json);
            usuariosJpa.create(nuevo);
            // Responder con el objeto creado y su id asignado
            JsonObject jsonRes = usuarioToJson(nuevo);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(jsonRes.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                JsonObject error = Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build();
                out.print(error.toString());
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        try (InputStream is = req.getInputStream();
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             PrintWriter out = resp.getWriter()) {
            
            JsonObject json = reader.readObject();
            Usuarios editado = jsonToUsuario(json);
            if (editado.getId() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(Json.createObjectBuilder()
                        .add("error", "El campo 'id' es obligatorio para actualizar")
                        .build().toString());
                return;
            }
            usuariosJpa.edit(editado);
            out.print(usuarioToJson(editado).toString());
        } catch (NonexistentEntityException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = resp.getWriter()) {
                out.print(Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build().toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.print(Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build().toString());
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        String idParam = req.getParameter("id");
        try (PrintWriter out = resp.getWriter()) {
            if (idParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(Json.createObjectBuilder()
                        .add("error", "Parámetro 'id' es obligatorio para eliminar")
                        .build().toString());
                return;
            }
            Integer id = Integer.parseInt(idParam);
            usuariosJpa.destroy(id);
            out.print(Json.createObjectBuilder()
                    .add("mensaje", "Usuario eliminado correctamente")
                    .build().toString());
        } catch (NonexistentEntityException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = resp.getWriter()) {
                out.print(Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build().toString());
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.print(Json.createObjectBuilder()
                        .add("error", "El parámetro 'id' debe ser un número")
                        .build().toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print(Json.createObjectBuilder()
                        .add("error", e.getMessage())
                        .build().toString());
            }
        }
    }
}
