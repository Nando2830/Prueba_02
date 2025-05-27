package servlets;

import dao.AsistenciaJpaController;
import dto.Asistencia;
import dto.Inscripciones;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/asistencia")
public class AsistenciaServlet extends HttpServlet {

    private AsistenciaJpaController controller;

    @Override
    public void init() throws ServletException {
        controller = new AsistenciaJpaController(Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU")); // Ajusta el nombre
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Asistencia a : controller.findAsistenciaEntities()) {
            arrayBuilder.add(Json.createObjectBuilder()
                .add("id", a.getId())
                .add("fecha", a.getFecha() != null ? new SimpleDateFormat("yyyy-MM-dd").format(a.getFecha()) : "")
                .add("presente", a.getPresente() != null ? a.getPresente() : false)
                .add("inscripcionId", a.getInscripcionId() != null ? a.getInscripcionId().getId() : 0)
            );
        }

        JsonArray jsonArray = arrayBuilder.build();
        resp.getWriter().write(jsonArray.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json = Json.createReader(req.getInputStream()).readObject();

        try {
            Asistencia asistencia = new Asistencia();
            asistencia.setFecha(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("fecha")));
            asistencia.setPresente(json.getBoolean("presente", false));
            asistencia.setInscripcionId(new Inscripciones(json.getInt("inscripcionId")));

            controller.create(asistencia);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json = Json.createReader(req.getInputStream()).readObject();

        try {
            Asistencia asistencia = controller.findAsistencia(json.getInt("id"));
            if (asistencia != null) {
                asistencia.setFecha(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("fecha")));
                asistencia.setPresente(json.getBoolean("presente", false));
                asistencia.setInscripcionId(new Inscripciones(json.getInt("inscripcionId")));

                controller.edit(asistencia);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json = Json.createReader(req.getInputStream()).readObject();

        try {
            controller.destroy(json.getInt("id"));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace();
        }
    }
}
