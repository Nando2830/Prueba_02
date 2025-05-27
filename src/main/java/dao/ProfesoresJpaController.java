/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Cursos;
import dto.Profesores;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class ProfesoresJpaController implements Serializable {

    public ProfesoresJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Profesores profesores) {
        if (profesores.getCursosCollection() == null) {
            profesores.setCursosCollection(new ArrayList<Cursos>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Cursos> attachedCursosCollection = new ArrayList<Cursos>();
            for (Cursos cursosCollectionCursosToAttach : profesores.getCursosCollection()) {
                cursosCollectionCursosToAttach = em.getReference(cursosCollectionCursosToAttach.getClass(), cursosCollectionCursosToAttach.getId());
                attachedCursosCollection.add(cursosCollectionCursosToAttach);
            }
            profesores.setCursosCollection(attachedCursosCollection);
            em.persist(profesores);
            for (Cursos cursosCollectionCursos : profesores.getCursosCollection()) {
                Profesores oldProfesorIdOfCursosCollectionCursos = cursosCollectionCursos.getProfesorId();
                cursosCollectionCursos.setProfesorId(profesores);
                cursosCollectionCursos = em.merge(cursosCollectionCursos);
                if (oldProfesorIdOfCursosCollectionCursos != null) {
                    oldProfesorIdOfCursosCollectionCursos.getCursosCollection().remove(cursosCollectionCursos);
                    oldProfesorIdOfCursosCollectionCursos = em.merge(oldProfesorIdOfCursosCollectionCursos);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Profesores profesores) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Profesores persistentProfesores = em.find(Profesores.class, profesores.getId());
            Collection<Cursos> cursosCollectionOld = persistentProfesores.getCursosCollection();
            Collection<Cursos> cursosCollectionNew = profesores.getCursosCollection();
            Collection<Cursos> attachedCursosCollectionNew = new ArrayList<Cursos>();
            for (Cursos cursosCollectionNewCursosToAttach : cursosCollectionNew) {
                cursosCollectionNewCursosToAttach = em.getReference(cursosCollectionNewCursosToAttach.getClass(), cursosCollectionNewCursosToAttach.getId());
                attachedCursosCollectionNew.add(cursosCollectionNewCursosToAttach);
            }
            cursosCollectionNew = attachedCursosCollectionNew;
            profesores.setCursosCollection(cursosCollectionNew);
            profesores = em.merge(profesores);
            for (Cursos cursosCollectionOldCursos : cursosCollectionOld) {
                if (!cursosCollectionNew.contains(cursosCollectionOldCursos)) {
                    cursosCollectionOldCursos.setProfesorId(null);
                    cursosCollectionOldCursos = em.merge(cursosCollectionOldCursos);
                }
            }
            for (Cursos cursosCollectionNewCursos : cursosCollectionNew) {
                if (!cursosCollectionOld.contains(cursosCollectionNewCursos)) {
                    Profesores oldProfesorIdOfCursosCollectionNewCursos = cursosCollectionNewCursos.getProfesorId();
                    cursosCollectionNewCursos.setProfesorId(profesores);
                    cursosCollectionNewCursos = em.merge(cursosCollectionNewCursos);
                    if (oldProfesorIdOfCursosCollectionNewCursos != null && !oldProfesorIdOfCursosCollectionNewCursos.equals(profesores)) {
                        oldProfesorIdOfCursosCollectionNewCursos.getCursosCollection().remove(cursosCollectionNewCursos);
                        oldProfesorIdOfCursosCollectionNewCursos = em.merge(oldProfesorIdOfCursosCollectionNewCursos);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = profesores.getId();
                if (findProfesores(id) == null) {
                    throw new NonexistentEntityException("The profesores with id " + id + " no longer exists.");
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
            Profesores profesores;
            try {
                profesores = em.getReference(Profesores.class, id);
                profesores.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The profesores with id " + id + " no longer exists.", enfe);
            }
            Collection<Cursos> cursosCollection = profesores.getCursosCollection();
            for (Cursos cursosCollectionCursos : cursosCollection) {
                cursosCollectionCursos.setProfesorId(null);
                cursosCollectionCursos = em.merge(cursosCollectionCursos);
            }
            em.remove(profesores);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Profesores> findProfesoresEntities() {
        return findProfesoresEntities(true, -1, -1);
    }

    public List<Profesores> findProfesoresEntities(int maxResults, int firstResult) {
        return findProfesoresEntities(false, maxResults, firstResult);
    }

    private List<Profesores> findProfesoresEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Profesores.class));
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

    public Profesores findProfesores(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Profesores.class, id);
        } finally {
            em.close();
        }
    }

    public int getProfesoresCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Profesores> rt = cq.from(Profesores.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
