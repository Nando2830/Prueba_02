package servlets;

import dao.InscripcionesJpaController;
import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Clases;
import dto.Estudiantes;
import dto.Inscripciones;

import java.io.BufferedReader;
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

@WebServlet(name = "InscripcionesServlet", urlPatterns = {"/inscripciones"})
public class InscripcionesServlet extends HttpServlet {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Prueba_02_war_1.0-SNAPSHOTPU");
    private static final InscripcionesJpaController DAO = new InscripcionesJpaController(emf);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            List<Inscripciones> lista = DAO.findInscripcionesEntities();

            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

            for (Inscripciones i : lista) {
                JsonObjectBuilder obj = Json.createObjectBuilder()
                        .add("id", i.getId())
                        .add("fechaInscripcion", i.getFechaInscripcion() != null ? sdf.format(i.getFechaInscripcion()) : "")
                        .add("estudianteId", i.getEstudianteId() != null ? i.getEstudianteId().getId() : 0)
                        .add("claseId", i.getClaseId() != null ? i.getClaseId().getId() : 0);
                arrBuilder.add(obj);
            }
            JsonArray arr = arrBuilder.build();
            out.print(arr.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Leer JSON
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader br = request.getReader();
             PrintWriter out = response.getWriter()) {
            JsonReader jsonReader = Json.createReader(br);
            JsonObject json = jsonReader.readObject();

            Inscripciones ins = new Inscripciones();

            if (json.containsKey("fechaInscripcion") && !json.isNull("fechaInscripcion")) {
                String fechaStr = json.getString("fechaInscripcion");
                Date fecha = sdf.parse(fechaStr);
                ins.setFechaInscripcion(fecha);
            }

            if (json.containsKey("estudianteId")) {
                Estudiantes e = new Estudiantes();
                e.setId(json.getInt("estudianteId"));
                ins.setEstudianteId(e);
            }

            if (json.containsKey("claseId")) {
                Clases c = new Clases();
                c.setId(json.getInt("claseId"));
                ins.setClaseId(c);
            }

            DAO.create(ins);

            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Creado con éxito")
                    .add("id", ins.getId())
                    .build();
            out.print(resp.toString());

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build();
                out.print(resp.toString());
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Leer JSON para editar
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader br = request.getReader();
             PrintWriter out = response.getWriter()) {

            JsonReader jsonReader = Json.createReader(br);
            JsonObject json = jsonReader.readObject();

            if (!json.containsKey("id")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", "Falta campo 'id'")
                        .build();
                out.print(resp.toString());
                return;
            }

            Integer id = json.getInt("id");
            Inscripciones ins = DAO.findInscripciones(id);
            if (ins == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", "Inscripción no encontrada")
                        .build();
                out.print(resp.toString());
                return;
            }

            if (json.containsKey("fechaInscripcion") && !json.isNull("fechaInscripcion")) {
                String fechaStr = json.getString("fechaInscripcion");
                Date fecha = sdf.parse(fechaStr);
                ins.setFechaInscripcion(fecha);
            }

            if (json.containsKey("estudianteId")) {
                Estudiantes e = new Estudiantes();
                e.setId(json.getInt("estudianteId"));
                ins.setEstudianteId(e);
            }

            if (json.containsKey("claseId")) {
                Clases c = new Clases();
                c.setId(json.getInt("claseId"));
                ins.setClaseId(c);
            }

            DAO.edit(ins);

            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Actualizado con éxito")
                    .build();
            out.print(resp.toString());

        } catch (IllegalOrphanException | NonexistentEntityException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build();
                out.print(resp.toString());
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build();
                out.print(resp.toString());
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Leer JSON para id a eliminar
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader br = request.getReader();
             PrintWriter out = response.getWriter()) {
            JsonReader jsonReader = Json.createReader(br);
            JsonObject json = jsonReader.readObject();

            if (!json.containsKey("id")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", "Falta campo 'id'")
                        .build();
                out.print(resp.toString());
                return;
            }

            Integer id = json.getInt("id");
            DAO.destroy(id);

            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Eliminado con éxito")
                    .build();
            out.print(resp.toString());

        } catch (IllegalOrphanException | NonexistentEntityException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build();
                out.print(resp.toString());
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("error", ex.getMessage())
                        .build();
                out.print(resp.toString());
            }
        }
    }
}
