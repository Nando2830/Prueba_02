package servlets;

import dao.EstudiantesJpaController;
import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Estudiantes;

import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/estudiantes")
public class EstudiantesServlet extends HttpServlet {
    private final EstudiantesJpaController dao = new EstudiantesJpaController(Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU"));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        List<Estudiantes> estudiantes = dao.findEstudiantesEntities();

        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Estudiantes e : estudiantes) {
            jsonArray.add(Json.createObjectBuilder()
                .add("id", e.getId())
                .add("nombre", e.getNombre())
                .add("apellido", e.getApellido())
                .add("correo", e.getCorreo() != null ? e.getCorreo() : "")
                .add("fechaNacimiento", e.getFechaNacimiento() != null ? sdf.format(e.getFechaNacimiento()) : "")
                .add("genero", e.getGenero() != null ? e.getGenero() : "")
                .add("direccion", e.getDireccion() != null ? e.getDireccion() : "")
                .add("telefono", e.getTelefono() != null ? e.getTelefono() : "")
            );
        }

        JsonWriter writer = Json.createWriter(resp.getWriter());
        writer.writeArray(jsonArray.build());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject json = reader.readObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Estudiantes e = new Estudiantes();
        e.setNombre(json.getString("nombre", ""));
        e.setApellido(json.getString("apellido", ""));
        e.setCorreo(json.getString("correo", ""));
        e.setGenero(json.getString("genero", ""));
        e.setDireccion(json.getString("direccion", ""));
        e.setTelefono(json.getString("telefono", ""));
        try {
            String fecha = json.getString("fechaNacimiento", "");
            e.setFechaNacimiento(fecha.isEmpty() ? null : sdf.parse(fecha));
            dao.create(e);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception ex) {
            resp.sendError(500, "Error al crear: " + ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject json = reader.readObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Estudiantes e = dao.findEstudiantes(json.getInt("id"));
            if (e == null) {
                resp.sendError(404, "Estudiante no encontrado");
                return;
            }

            e.setNombre(json.getString("nombre", ""));
            e.setApellido(json.getString("apellido", ""));
            e.setCorreo(json.getString("correo", ""));
            e.setGenero(json.getString("genero", ""));
            e.setDireccion(json.getString("direccion", ""));
            e.setTelefono(json.getString("telefono", ""));
            String fecha = json.getString("fechaNacimiento", "");
            e.setFechaNacimiento(fecha.isEmpty() ? null : sdf.parse(fecha));

            dao.edit(e);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception ex) {
            resp.sendError(500, "Error al actualizar: " + ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject json = reader.readObject();
        int id = json.getInt("id");

        try {
            dao.destroy(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalOrphanException | NonexistentEntityException ex) {
            resp.sendError(500, "Error al eliminar: " + ex.getMessage());
        }
    }
}
