/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Clases;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Cursos;
import dto.Materias;
import dto.Inscripciones;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class ClasesJpaController implements Serializable {

    public ClasesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Clases clases) {
        if (clases.getInscripcionesCollection() == null) {
            clases.setInscripcionesCollection(new ArrayList<Inscripciones>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cursos cursoId = clases.getCursoId();
            if (cursoId != null) {
                cursoId = em.getReference(cursoId.getClass(), cursoId.getId());
                clases.setCursoId(cursoId);
            }
            Materias materiaId = clases.getMateriaId();
            if (materiaId != null) {
                materiaId = em.getReference(materiaId.getClass(), materiaId.getId());
                clases.setMateriaId(materiaId);
            }
            Collection<Inscripciones> attachedInscripcionesCollection = new ArrayList<Inscripciones>();
            for (Inscripciones inscripcionesCollectionInscripcionesToAttach : clases.getInscripcionesCollection()) {
                inscripcionesCollectionInscripcionesToAttach = em.getReference(inscripcionesCollectionInscripcionesToAttach.getClass(), inscripcionesCollectionInscripcionesToAttach.getId());
                attachedInscripcionesCollection.add(inscripcionesCollectionInscripcionesToAttach);
            }
            clases.setInscripcionesCollection(attachedInscripcionesCollection);
            em.persist(clases);
            if (cursoId != null) {
                cursoId.getClasesCollection().add(clases);
                cursoId = em.merge(cursoId);
            }
            if (materiaId != null) {
                materiaId.getClasesCollection().add(clases);
                materiaId = em.merge(materiaId);
            }
            for (Inscripciones inscripcionesCollectionInscripciones : clases.getInscripcionesCollection()) {
                Clases oldClaseIdOfInscripcionesCollectionInscripciones = inscripcionesCollectionInscripciones.getClaseId();
                inscripcionesCollectionInscripciones.setClaseId(clases);
                inscripcionesCollectionInscripciones = em.merge(inscripcionesCollectionInscripciones);
                if (oldClaseIdOfInscripcionesCollectionInscripciones != null) {
                    oldClaseIdOfInscripcionesCollectionInscripciones.getInscripcionesCollection().remove(inscripcionesCollectionInscripciones);
                    oldClaseIdOfInscripcionesCollectionInscripciones = em.merge(oldClaseIdOfInscripcionesCollectionInscripciones);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Clases clases) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Clases persistentClases = em.find(Clases.class, clases.getId());
            Cursos cursoIdOld = persistentClases.getCursoId();
            Cursos cursoIdNew = clases.getCursoId();
            Materias materiaIdOld = persistentClases.getMateriaId();
            Materias materiaIdNew = clases.getMateriaId();
            Collection<Inscripciones> inscripcionesCollectionOld = persistentClases.getInscripcionesCollection();
            Collection<Inscripciones> inscripcionesCollectionNew = clases.getInscripcionesCollection();
            List<String> illegalOrphanMessages = null;
            for (Inscripciones inscripcionesCollectionOldInscripciones : inscripcionesCollectionOld) {
                if (!inscripcionesCollectionNew.contains(inscripcionesCollectionOldInscripciones)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Inscripciones " + inscripcionesCollectionOldInscripciones + " since its claseId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (cursoIdNew != null) {
                cursoIdNew = em.getReference(cursoIdNew.getClass(), cursoIdNew.getId());
                clases.setCursoId(cursoIdNew);
            }
            if (materiaIdNew != null) {
                materiaIdNew = em.getReference(materiaIdNew.getClass(), materiaIdNew.getId());
                clases.setMateriaId(materiaIdNew);
            }
            Collection<Inscripciones> attachedInscripcionesCollectionNew = new ArrayList<Inscripciones>();
            for (Inscripciones inscripcionesCollectionNewInscripcionesToAttach : inscripcionesCollectionNew) {
                inscripcionesCollectionNewInscripcionesToAttach = em.getReference(inscripcionesCollectionNewInscripcionesToAttach.getClass(), inscripcionesCollectionNewInscripcionesToAttach.getId());
                attachedInscripcionesCollectionNew.add(inscripcionesCollectionNewInscripcionesToAttach);
            }
            inscripcionesCollectionNew = attachedInscripcionesCollectionNew;
            clases.setInscripcionesCollection(inscripcionesCollectionNew);
            clases = em.merge(clases);
            if (cursoIdOld != null && !cursoIdOld.equals(cursoIdNew)) {
                cursoIdOld.getClasesCollection().remove(clases);
                cursoIdOld = em.merge(cursoIdOld);
            }
            if (cursoIdNew != null && !cursoIdNew.equals(cursoIdOld)) {
                cursoIdNew.getClasesCollection().add(clases);
                cursoIdNew = em.merge(cursoIdNew);
            }
            if (materiaIdOld != null && !materiaIdOld.equals(materiaIdNew)) {
                materiaIdOld.getClasesCollection().remove(clases);
                materiaIdOld = em.merge(materiaIdOld);
            }
            if (materiaIdNew != null && !materiaIdNew.equals(materiaIdOld)) {
                materiaIdNew.getClasesCollection().add(clases);
                materiaIdNew = em.merge(materiaIdNew);
            }
            for (Inscripciones inscripcionesCollectionNewInscripciones : inscripcionesCollectionNew) {
                if (!inscripcionesCollectionOld.contains(inscripcionesCollectionNewInscripciones)) {
                    Clases oldClaseIdOfInscripcionesCollectionNewInscripciones = inscripcionesCollectionNewInscripciones.getClaseId();
                    inscripcionesCollectionNewInscripciones.setClaseId(clases);
                    inscripcionesCollectionNewInscripciones = em.merge(inscripcionesCollectionNewInscripciones);
                    if (oldClaseIdOfInscripcionesCollectionNewInscripciones != null && !oldClaseIdOfInscripcionesCollectionNewInscripciones.equals(clases)) {
                        oldClaseIdOfInscripcionesCollectionNewInscripciones.getInscripcionesCollection().remove(inscripcionesCollectionNewInscripciones);
                        oldClaseIdOfInscripcionesCollectionNewInscripciones = em.merge(oldClaseIdOfInscripcionesCollectionNewInscripciones);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = clases.getId();
                if (findClases(id) == null) {
                    throw new NonexistentEntityException("The clases with id " + id + " no longer exists.");
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
            Clases clases;
            try {
                clases = em.getReference(Clases.class, id);
                clases.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The clases with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Inscripciones> inscripcionesCollectionOrphanCheck = clases.getInscripcionesCollection();
            for (Inscripciones inscripcionesCollectionOrphanCheckInscripciones : inscripcionesCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Clases (" + clases + ") cannot be destroyed since the Inscripciones " + inscripcionesCollectionOrphanCheckInscripciones + " in its inscripcionesCollection field has a non-nullable claseId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Cursos cursoId = clases.getCursoId();
            if (cursoId != null) {
                cursoId.getClasesCollection().remove(clases);
                cursoId = em.merge(cursoId);
            }
            Materias materiaId = clases.getMateriaId();
            if (materiaId != null) {
                materiaId.getClasesCollection().remove(clases);
                materiaId = em.merge(materiaId);
            }
            em.remove(clases);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Clases> findClasesEntities() {
        return findClasesEntities(true, -1, -1);
    }

    public List<Clases> findClasesEntities(int maxResults, int firstResult) {
        return findClasesEntities(false, maxResults, firstResult);
    }

    private List<Clases> findClasesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Clases.class));
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

    public Clases findClases(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Clases.class, id);
        } finally {
            em.close();
        }
    }

    public int getClasesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Clases> rt = cq.from(Clases.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
