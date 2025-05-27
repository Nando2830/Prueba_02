package servlets;

import dao.CalificacionesJpaController;
import dto.Calificaciones;
import dto.Inscripciones;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/calificaciones")
public class CalificacionesServlet extends HttpServlet {

    private final CalificacionesJpaController controller = new CalificacionesJpaController(Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU"));
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Calificaciones c : controller.findCalificacionesEntities()) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", c.getId())
                    .add("nota", c.getNota().toString())
                    .add("observaciones", c.getObservaciones() == null ? "" : c.getObservaciones())
                    .add("fechaRegistro", sdf.format(c.getFechaRegistro()))
                    .add("inscripcionId", c.getInscripcionId().getId()));
        }
        Json.createWriter(response.getWriter()).writeArray(arrayBuilder.build());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = Json.createReader(request.getInputStream()).readObject();
        Calificaciones c = new Calificaciones();
        c.setNota(new BigDecimal(json.getString("nota")));
        c.setObservaciones(json.getString("observaciones", ""));
        try {
            c.setFechaRegistro(sdf.parse(json.getString("fechaRegistro")));
        } catch (Exception e) {
            response.sendError(400, "Fecha inválida");
            return;
        }
        Inscripciones inscripcion = new Inscripciones();
        inscripcion.setId(json.getInt("inscripcionId"));
        c.setInscripcionId(inscripcion);
        controller.create(c);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject json = Json.createReader(request.getInputStream()).readObject();
        int id = json.getInt("id");
        Calificaciones c = controller.findCalificaciones(id);
        if (c == null) {
            response.sendError(404, "Registro no encontrado");
            return;
        }
        c.setNota(new BigDecimal(json.getString("nota")));
        c.setObservaciones(json.getString("observaciones", ""));
        try {
            c.setFechaRegistro(sdf.parse(json.getString("fechaRegistro")));
        } catch (Exception e) {
            response.sendError(400, "Fecha inválida");
            return;
        }
        Inscripciones inscripcion = new Inscripciones();
        inscripcion.setId(json.getInt("inscripcionId"));
        c.setInscripcionId(inscripcion);
        try {
            controller.edit(c);
        } catch (Exception e) {
            response.sendError(500, "Error al actualizar");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        try {
            controller.destroy(id);
        } catch (Exception e) {
            response.sendError(404, "No se pudo eliminar");
        }
    }
}
