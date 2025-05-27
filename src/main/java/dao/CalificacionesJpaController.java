/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Calificaciones;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Inscripciones;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class CalificacionesJpaController implements Serializable {

    public CalificacionesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Calificaciones calificaciones) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Inscripciones inscripcionId = calificaciones.getInscripcionId();
            if (inscripcionId != null) {
                inscripcionId = em.getReference(inscripcionId.getClass(), inscripcionId.getId());
                calificaciones.setInscripcionId(inscripcionId);
            }
            em.persist(calificaciones);
            if (inscripcionId != null) {
                inscripcionId.getCalificacionesCollection().add(calificaciones);
                inscripcionId = em.merge(inscripcionId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Calificaciones calificaciones) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Calificaciones persistentCalificaciones = em.find(Calificaciones.class, calificaciones.getId());
            Inscripciones inscripcionIdOld = persistentCalificaciones.getInscripcionId();
            Inscripciones inscripcionIdNew = calificaciones.getInscripcionId();
            if (inscripcionIdNew != null) {
                inscripcionIdNew = em.getReference(inscripcionIdNew.getClass(), inscripcionIdNew.getId());
                calificaciones.setInscripcionId(inscripcionIdNew);
            }
            calificaciones = em.merge(calificaciones);
            if (inscripcionIdOld != null && !inscripcionIdOld.equals(inscripcionIdNew)) {
                inscripcionIdOld.getCalificacionesCollection().remove(calificaciones);
                inscripcionIdOld = em.merge(inscripcionIdOld);
            }
            if (inscripcionIdNew != null && !inscripcionIdNew.equals(inscripcionIdOld)) {
                inscripcionIdNew.getCalificacionesCollection().add(calificaciones);
                inscripcionIdNew = em.merge(inscripcionIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = calificaciones.getId();
                if (findCalificaciones(id) == null) {
                    throw new NonexistentEntityException("The calificaciones with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Calificaciones calificaciones;
            try {
                calificaciones = em.getReference(Calificaciones.class, id);
                calificaciones.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The calificaciones with id " + id + " no longer exists.", enfe);
            }
            Inscripciones inscripcionId = calificaciones.getInscripcionId();
            if (inscripcionId != null) {
                inscripcionId.getCalificacionesCollection().remove(calificaciones);
                inscripcionId = em.merge(inscripcionId);
            }
            em.remove(calificaciones);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Calificaciones> findCalificacionesEntities() {
        return findCalificacionesEntities(true, -1, -1);
    }

    public List<Calificaciones> findCalificacionesEntities(int maxResults, int firstResult) {
        return findCalificacionesEntities(false, maxResults, firstResult);
    }

    private List<Calificaciones> findCalificacionesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Calificaciones.class));
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

    public Calificaciones findCalificaciones(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Calificaciones.class, id);
        } finally {
            em.close();
        }
    }

    public int getCalificacionesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Calificaciones> rt = cq.from(Calificaciones.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
