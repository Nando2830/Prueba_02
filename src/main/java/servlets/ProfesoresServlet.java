package servlets;

import dao.ProfesoresJpaController;
import dto.Profesores;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ProfesoresServlet", urlPatterns = {"/profesores"})
public class ProfesoresServlet extends HttpServlet {

    private ProfesoresJpaController controller = new ProfesoresJpaController(Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU"));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<Profesores> lista = controller.findProfesoresEntities();
        try (JsonWriter writer = Json.createWriter(response.getWriter())) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            for (Profesores p : lista) {
                jsonArray.add(Json.createObjectBuilder()
                        .add("id", p.getId())
                        .add("nombre", p.getNombre())
                        .add("apellido", p.getApellido())
                        .add("correo", p.getCorreo() == null ? "" : p.getCorreo())
                        .add("especialidad", p.getEspecialidad() == null ? "" : p.getEspecialidad())
                        .add("telefono", p.getTelefono() == null ? "" : p.getTelefono())
                );
            }
            writer.writeArray(jsonArray.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        Profesores p = new Profesores();
        p.setNombre(json.getString("nombre"));
        p.setApellido(json.getString("apellido"));
        p.setCorreo(json.getString("correo", ""));
        p.setEspecialidad(json.getString("especialidad", ""));
        p.setTelefono(json.getString("telefono", ""));

        controller.create(p);

        response.setContentType("application/json;charset=UTF-8");
        try (JsonWriter writer = Json.createWriter(response.getWriter())) {
            writer.writeObject(Json.createObjectBuilder()
                .add("message", "Profesor creado exitosamente.")
                .build());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        Profesores p = controller.findProfesores(json.getInt("id"));
        if (p != null) {
            p.setNombre(json.getString("nombre"));
            p.setApellido(json.getString("apellido"));
            p.setCorreo(json.getString("correo", ""));
            p.setEspecialidad(json.getString("especialidad", ""));
            p.setTelefono(json.getString("telefono", ""));

            try {
                controller.edit(p);
                response.setContentType("application/json;charset=UTF-8");
                try (JsonWriter writer = Json.createWriter(response.getWriter())) {
                    writer.writeObject(Json.createObjectBuilder()
                        .add("message", "Profesor actualizado correctamente.")
                        .build());
                }
            } catch (Exception ex) {
                response.sendError(500, "Error actualizando profesor: " + ex.getMessage());
            }
        } else {
            response.sendError(404, "Profesor no encontrado");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        try {
            controller.destroy(id);
            response.setContentType("application/json;charset=UTF-8");
            try (JsonWriter writer = Json.createWriter(response.getWriter())) {
                writer.writeObject(Json.createObjectBuilder()
                    .add("message", "Profesor eliminado correctamente.")
                    .build());
            }
        } catch (Exception ex) {
            response.sendError(500, "Error eliminando profesor: " + ex.getMessage());
        }
    }
}
