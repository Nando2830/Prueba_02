/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Estudiantes;
import dto.Clases;
import dto.Asistencia;
import java.util.ArrayList;
import java.util.Collection;
import dto.Calificaciones;
import dto.Inscripciones;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class InscripcionesJpaController implements Serializable {

    public InscripcionesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Inscripciones inscripciones) {
        if (inscripciones.getAsistenciaCollection() == null) {
            inscripciones.setAsistenciaCollection(new ArrayList<Asistencia>());
        }
        if (inscripciones.getCalificacionesCollection() == null) {
            inscripciones.setCalificacionesCollection(new ArrayList<Calificaciones>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Estudiantes estudianteId = inscripciones.getEstudianteId();
            if (estudianteId != null) {
                estudianteId = em.getReference(estudianteId.getClass(), estudianteId.getId());
                inscripciones.setEstudianteId(estudianteId);
            }
            Clases claseId = inscripciones.getClaseId();
            if (claseId != null) {
                claseId = em.getReference(claseId.getClass(), claseId.getId());
                inscripciones.setClaseId(claseId);
            }
            Collection<Asistencia> attachedAsistenciaCollection = new ArrayList<Asistencia>();
            for (Asistencia asistenciaCollectionAsistenciaToAttach : inscripciones.getAsistenciaCollection()) {
                asistenciaCollectionAsistenciaToAttach = em.getReference(asistenciaCollectionAsistenciaToAttach.getClass(), asistenciaCollectionAsistenciaToAttach.getId());
                attachedAsistenciaCollection.add(asistenciaCollectionAsistenciaToAttach);
            }
            inscripciones.setAsistenciaCollection(attachedAsistenciaCollection);
            Collection<Calificaciones> attachedCalificacionesCollection = new ArrayList<Calificaciones>();
            for (Calificaciones calificacionesCollectionCalificacionesToAttach : inscripciones.getCalificacionesCollection()) {
                calificacionesCollectionCalificacionesToAttach = em.getReference(calificacionesCollectionCalificacionesToAttach.getClass(), calificacionesCollectionCalificacionesToAttach.getId());
                attachedCalificacionesCollection.add(calificacionesCollectionCalificacionesToAttach);
            }
            inscripciones.setCalificacionesCollection(attachedCalificacionesCollection);
            em.persist(inscripciones);
            if (estudianteId != null) {
                estudianteId.getInscripcionesCollection().add(inscripciones);
                estudianteId = em.merge(estudianteId);
            }
            if (claseId != null) {
                claseId.getInscripcionesCollection().add(inscripciones);
                claseId = em.merge(claseId);
            }
            for (Asistencia asistenciaCollectionAsistencia : inscripciones.getAsistenciaCollection()) {
                Inscripciones oldInscripcionIdOfAsistenciaCollectionAsistencia = asistenciaCollectionAsistencia.getInscripcionId();
                asistenciaCollectionAsistencia.setInscripcionId(inscripciones);
                asistenciaCollectionAsistencia = em.merge(asistenciaCollectionAsistencia);
                if (oldInscripcionIdOfAsistenciaCollectionAsistencia != null) {
                    oldInscripcionIdOfAsistenciaCollectionAsistencia.getAsistenciaCollection().remove(asistenciaCollectionAsistencia);
                    oldInscripcionIdOfAsistenciaCollectionAsistencia = em.merge(oldInscripcionIdOfAsistenciaCollectionAsistencia);
                }
            }
            for (Calificaciones calificacionesCollectionCalificaciones : inscripciones.getCalificacionesCollection()) {
                Inscripciones oldInscripcionIdOfCalificacionesCollectionCalificaciones = calificacionesCollectionCalificaciones.getInscripcionId();
                calificacionesCollectionCalificaciones.setInscripcionId(inscripciones);
                calificacionesCollectionCalificaciones = em.merge(calificacionesCollectionCalificaciones);
                if (oldInscripcionIdOfCalificacionesCollectionCalificaciones != null) {
                    oldInscripcionIdOfCalificacionesCollectionCalificaciones.getCalificacionesCollection().remove(calificacionesCollectionCalificaciones);
                    oldInscripcionIdOfCalificacionesCollectionCalificaciones = em.merge(oldInscripcionIdOfCalificacionesCollectionCalificaciones);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Inscripciones inscripciones) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Inscripciones persistentInscripciones = em.find(Inscripciones.class, inscripciones.getId());
            Estudiantes estudianteIdOld = persistentInscripciones.getEstudianteId();
            Estudiantes estudianteIdNew = inscripciones.getEstudianteId();
            Clases claseIdOld = persistentInscripciones.getClaseId();
            Clases claseIdNew = inscripciones.getClaseId();
            Collection<Asistencia> asistenciaCollectionOld = persistentInscripciones.getAsistenciaCollection();
            Collection<Asistencia> asistenciaCollectionNew = inscripciones.getAsistenciaCollection();
            Collection<Calificaciones> calificacionesCollectionOld = persistentInscripciones.getCalificacionesCollection();
            Collection<Calificaciones> calificacionesCollectionNew = inscripciones.getCalificacionesCollection();
            List<String> illegalOrphanMessages = null;
            for (Asistencia asistenciaCollectionOldAsistencia : asistenciaCollectionOld) {
                if (!asistenciaCollectionNew.contains(asistenciaCollectionOldAsistencia)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Asistencia " + asistenciaCollectionOldAsistencia + " since its inscripcionId field is not nullable.");
                }
            }
            for (Calificaciones calificacionesCollectionOldCalificaciones : calificacionesCollectionOld) {
                if (!calificacionesCollectionNew.contains(calificacionesCollectionOldCalificaciones)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Calificaciones " + calificacionesCollectionOldCalificaciones + " since its inscripcionId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (estudianteIdNew != null) {
                estudianteIdNew = em.getReference(estudianteIdNew.getClass(), estudianteIdNew.getId());
                inscripciones.setEstudianteId(estudianteIdNew);
            }
            if (claseIdNew != null) {
                claseIdNew = em.getReference(claseIdNew.getClass(), claseIdNew.getId());
                inscripciones.setClaseId(claseIdNew);
            }
            Collection<Asistencia> attachedAsistenciaCollectionNew = new ArrayList<Asistencia>();
            for (Asistencia asistenciaCollectionNewAsistenciaToAttach : asistenciaCollectionNew) {
                asistenciaCollectionNewAsistenciaToAttach = em.getReference(asistenciaCollectionNewAsistenciaToAttach.getClass(), asistenciaCollectionNewAsistenciaToAttach.getId());
                attachedAsistenciaCollectionNew.add(asistenciaCollectionNewAsistenciaToAttach);
            }
            asistenciaCollectionNew = attachedAsistenciaCollectionNew;
            inscripciones.setAsistenciaCollection(asistenciaCollectionNew);
            Collection<Calificaciones> attachedCalificacionesCollectionNew = new ArrayList<Calificaciones>();
            for (Calificaciones calificacionesCollectionNewCalificacionesToAttach : calificacionesCollectionNew) {
                calificacionesCollectionNewCalificacionesToAttach = em.getReference(calificacionesCollectionNewCalificacionesToAttach.getClass(), calificacionesCollectionNewCalificacionesToAttach.getId());
                attachedCalificacionesCollectionNew.add(calificacionesCollectionNewCalificacionesToAttach);
            }
            calificacionesCollectionNew = attachedCalificacionesCollectionNew;
            inscripciones.setCalificacionesCollection(calificacionesCollectionNew);
            inscripciones = em.merge(inscripciones);
            if (estudianteIdOld != null && !estudianteIdOld.equals(estudianteIdNew)) {
                estudianteIdOld.getInscripcionesCollection().remove(inscripciones);
                estudianteIdOld = em.merge(estudianteIdOld);
            }
            if (estudianteIdNew != null && !estudianteIdNew.equals(estudianteIdOld)) {
                estudianteIdNew.getInscripcionesCollection().add(inscripciones);
                estudianteIdNew = em.merge(estudianteIdNew);
            }
            if (claseIdOld != null && !claseIdOld.equals(claseIdNew)) {
                claseIdOld.getInscripcionesCollection().remove(inscripciones);
                claseIdOld = em.merge(claseIdOld);
            }
            if (claseIdNew != null && !claseIdNew.equals(claseIdOld)) {
                claseIdNew.getInscripcionesCollection().add(inscripciones);
                claseIdNew = em.merge(claseIdNew);
            }
            for (Asistencia asistenciaCollectionNewAsistencia : asistenciaCollectionNew) {
                if (!asistenciaCollectionOld.contains(asistenciaCollectionNewAsistencia)) {
                    Inscripciones oldInscripcionIdOfAsistenciaCollectionNewAsistencia = asistenciaCollectionNewAsistencia.getInscripcionId();
                    asistenciaCollectionNewAsistencia.setInscripcionId(inscripciones);
                    asistenciaCollectionNewAsistencia = em.merge(asistenciaCollectionNewAsistencia);
                    if (oldInscripcionIdOfAsistenciaCollectionNewAsistencia != null && !oldInscripcionIdOfAsistenciaCollectionNewAsistencia.equals(inscripciones)) {
                        oldInscripcionIdOfAsistenciaCollectionNewAsistencia.getAsistenciaCollection().remove(asistenciaCollectionNewAsistencia);
                        oldInscripcionIdOfAsistenciaCollectionNewAsistencia = em.merge(oldInscripcionIdOfAsistenciaCollectionNewAsistencia);
                    }
                }
            }
            for (Calificaciones calificacionesCollectionNewCalificaciones : calificacionesCollectionNew) {
                if (!calificacionesCollectionOld.contains(calificacionesCollectionNewCalificaciones)) {
                    Inscripciones oldInscripcionIdOfCalificacionesCollectionNewCalificaciones = calificacionesCollectionNewCalificaciones.getInscripcionId();
                    calificacionesCollectionNewCalificaciones.setInscripcionId(inscripciones);
                    calificacionesCollectionNewCalificaciones = em.merge(calificacionesCollectionNewCalificaciones);
                    if (oldInscripcionIdOfCalificacionesCollectionNewCalificaciones != null && !oldInscripcionIdOfCalificacionesCollectionNewCalificaciones.equals(inscripciones)) {
                        oldInscripcionIdOfCalificacionesCollectionNewCalificaciones.getCalificacionesCollection().remove(calificacionesCollectionNewCalificaciones);
                        oldInscripcionIdOfCalificacionesCollectionNewCalificaciones = em.merge(oldInscripcionIdOfCalificacionesCollectionNewCalificaciones);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = inscripciones.getId();
                if (findInscripciones(id) == null) {
                    throw new NonexistentEntityException("The inscripciones with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Inscripciones inscripciones;
            try {
                inscripciones = em.getReference(Inscripciones.class, id);
                inscripciones.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The inscripciones with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Asistencia> asistenciaCollectionOrphanCheck = inscripciones.getAsistenciaCollection();
            for (Asistencia asistenciaCollectionOrphanCheckAsistencia : asistenciaCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Inscripciones (" + inscripciones + ") cannot be destroyed since the Asistencia " + asistenciaCollectionOrphanCheckAsistencia + " in its asistenciaCollection field has a non-nullable inscripcionId field.");
            }
            Collection<Calificaciones> calificacionesCollectionOrphanCheck = inscripciones.getCalificacionesCollection();
            for (Calificaciones calificacionesCollectionOrphanCheckCalificaciones : calificacionesCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Inscripciones (" + inscripciones + ") cannot be destroyed since the Calificaciones " + calificacionesCollectionOrphanCheckCalificaciones + " in its calificacionesCollection field has a non-nullable inscripcionId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Estudiantes estudianteId = inscripciones.getEstudianteId();
            if (estudianteId != null) {
                estudianteId.getInscripcionesCollection().remove(inscripciones);
                estudianteId = em.merge(estudianteId);
            }
            Clases claseId = inscripciones.getClaseId();
            if (claseId != null) {
                claseId.getInscripcionesCollection().remove(inscripciones);
                claseId = em.merge(claseId);
            }
            em.remove(inscripciones);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Inscripciones> findInscripcionesEntities() {
        return findInscripcionesEntities(true, -1, -1);
    }

    public List<Inscripciones> findInscripcionesEntities(int maxResults, int firstResult) {
        return findInscripcionesEntities(false, maxResults, firstResult);
    }

    private List<Inscripciones> findInscripcionesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Inscripciones.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Inscripciones findInscripciones(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Inscripciones.class, id);
        } finally {
            em.close();
        }
    }

    public int getInscripcionesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Inscripciones> rt = cq.from(Inscripciones.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
