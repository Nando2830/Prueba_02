/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Estudiantes;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
public class EstudiantesJpaController implements Serializable {

    public EstudiantesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Estudiantes estudiantes) {
        if (estudiantes.getInscripcionesCollection() == null) {
            estudiantes.setInscripcionesCollection(new ArrayList<Inscripciones>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Inscripciones> attachedInscripcionesCollection = new ArrayList<Inscripciones>();
            for (Inscripciones inscripcionesCollectionInscripcionesToAttach : estudiantes.getInscripcionesCollection()) {
                inscripcionesCollectionInscripcionesToAttach = em.getReference(inscripcionesCollectionInscripcionesToAttach.getClass(), inscripcionesCollectionInscripcionesToAttach.getId());
                attachedInscripcionesCollection.add(inscripcionesCollectionInscripcionesToAttach);
            }
            estudiantes.setInscripcionesCollection(attachedInscripcionesCollection);
            em.persist(estudiantes);
            for (Inscripciones inscripcionesCollectionInscripciones : estudiantes.getInscripcionesCollection()) {
                Estudiantes oldEstudianteIdOfInscripcionesCollectionInscripciones = inscripcionesCollectionInscripciones.getEstudianteId();
                inscripcionesCollectionInscripciones.setEstudianteId(estudiantes);
                inscripcionesCollectionInscripciones = em.merge(inscripcionesCollectionInscripciones);
                if (oldEstudianteIdOfInscripcionesCollectionInscripciones != null) {
                    oldEstudianteIdOfInscripcionesCollectionInscripciones.getInscripcionesCollection().remove(inscripcionesCollectionInscripciones);
                    oldEstudianteIdOfInscripcionesCollectionInscripciones = em.merge(oldEstudianteIdOfInscripcionesCollectionInscripciones);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Estudiantes estudiantes) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Estudiantes persistentEstudiantes = em.find(Estudiantes.class, estudiantes.getId());
            Collection<Inscripciones> inscripcionesCollectionOld = persistentEstudiantes.getInscripcionesCollection();
            Collection<Inscripciones> inscripcionesCollectionNew = estudiantes.getInscripcionesCollection();
            List<String> illegalOrphanMessages = null;
            for (Inscripciones inscripcionesCollectionOldInscripciones : inscripcionesCollectionOld) {
                if (!inscripcionesCollectionNew.contains(inscripcionesCollectionOldInscripciones)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Inscripciones " + inscripcionesCollectionOldInscripciones + " since its estudianteId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Inscripciones> attachedInscripcionesCollectionNew = new ArrayList<Inscripciones>();
            for (Inscripciones inscripcionesCollectionNewInscripcionesToAttach : inscripcionesCollectionNew) {
                inscripcionesCollectionNewInscripcionesToAttach = em.getReference(inscripcionesCollectionNewInscripcionesToAttach.getClass(), inscripcionesCollectionNewInscripcionesToAttach.getId());
                attachedInscripcionesCollectionNew.add(inscripcionesCollectionNewInscripcionesToAttach);
            }
            inscripcionesCollectionNew = attachedInscripcionesCollectionNew;
            estudiantes.setInscripcionesCollection(inscripcionesCollectionNew);
            estudiantes = em.merge(estudiantes);
            for (Inscripciones inscripcionesCollectionNewInscripciones : inscripcionesCollectionNew) {
                if (!inscripcionesCollectionOld.contains(inscripcionesCollectionNewInscripciones)) {
                    Estudiantes oldEstudianteIdOfInscripcionesCollectionNewInscripciones = inscripcionesCollectionNewInscripciones.getEstudianteId();
                    inscripcionesCollectionNewInscripciones.setEstudianteId(estudiantes);
                    inscripcionesCollectionNewInscripciones = em.merge(inscripcionesCollectionNewInscripciones);
                    if (oldEstudianteIdOfInscripcionesCollectionNewInscripciones != null && !oldEstudianteIdOfInscripcionesCollectionNewInscripciones.equals(estudiantes)) {
                        oldEstudianteIdOfInscripcionesCollectionNewInscripciones.getInscripcionesCollection().remove(inscripcionesCollectionNewInscripciones);
                        oldEstudianteIdOfInscripcionesCollectionNewInscripciones = em.merge(oldEstudianteIdOfInscripcionesCollectionNewInscripciones);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = estudiantes.getId();
                if (findEstudiantes(id) == null) {
                    throw new NonexistentEntityException("The estudiantes with id " + id + " no longer exists.");
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
            Estudiantes estudiantes;
            try {
                estudiantes = em.getReference(Estudiantes.class, id);
                estudiantes.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The estudiantes with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Inscripciones> inscripcionesCollectionOrphanCheck = estudiantes.getInscripcionesCollection();
            for (Inscripciones inscripcionesCollectionOrphanCheckInscripciones : inscripcionesCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Estudiantes (" + estudiantes + ") cannot be destroyed since the Inscripciones " + inscripcionesCollectionOrphanCheckInscripciones + " in its inscripcionesCollection field has a non-nullable estudianteId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(estudiantes);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Estudiantes> findEstudiantesEntities() {
        return findEstudiantesEntities(true, -1, -1);
    }

    public List<Estudiantes> findEstudiantesEntities(int maxResults, int firstResult) {
        return findEstudiantesEntities(false, maxResults, firstResult);
    }

    private List<Estudiantes> findEstudiantesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Estudiantes.class));
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

    public Estudiantes findEstudiantes(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Estudiantes.class, id);
        } finally {
            em.close();
        }
    }

    public int getEstudiantesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Estudiantes> rt = cq.from(Estudiantes.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
