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
import dto.Profesores;
import dto.Clases;
import dto.Cursos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class CursosJpaController implements Serializable {

    public CursosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cursos cursos) {
        if (cursos.getClasesCollection() == null) {
            cursos.setClasesCollection(new ArrayList<Clases>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Profesores profesorId = cursos.getProfesorId();
            if (profesorId != null) {
                profesorId = em.getReference(profesorId.getClass(), profesorId.getId());
                cursos.setProfesorId(profesorId);
            }
            Collection<Clases> attachedClasesCollection = new ArrayList<Clases>();
            for (Clases clasesCollectionClasesToAttach : cursos.getClasesCollection()) {
                clasesCollectionClasesToAttach = em.getReference(clasesCollectionClasesToAttach.getClass(), clasesCollectionClasesToAttach.getId());
                attachedClasesCollection.add(clasesCollectionClasesToAttach);
            }
            cursos.setClasesCollection(attachedClasesCollection);
            em.persist(cursos);
            if (profesorId != null) {
                profesorId.getCursosCollection().add(cursos);
                profesorId = em.merge(profesorId);
            }
            for (Clases clasesCollectionClases : cursos.getClasesCollection()) {
                Cursos oldCursoIdOfClasesCollectionClases = clasesCollectionClases.getCursoId();
                clasesCollectionClases.setCursoId(cursos);
                clasesCollectionClases = em.merge(clasesCollectionClases);
                if (oldCursoIdOfClasesCollectionClases != null) {
                    oldCursoIdOfClasesCollectionClases.getClasesCollection().remove(clasesCollectionClases);
                    oldCursoIdOfClasesCollectionClases = em.merge(oldCursoIdOfClasesCollectionClases);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cursos cursos) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cursos persistentCursos = em.find(Cursos.class, cursos.getId());
            Profesores profesorIdOld = persistentCursos.getProfesorId();
            Profesores profesorIdNew = cursos.getProfesorId();
            Collection<Clases> clasesCollectionOld = persistentCursos.getClasesCollection();
            Collection<Clases> clasesCollectionNew = cursos.getClasesCollection();
            List<String> illegalOrphanMessages = null;
            for (Clases clasesCollectionOldClases : clasesCollectionOld) {
                if (!clasesCollectionNew.contains(clasesCollectionOldClases)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Clases " + clasesCollectionOldClases + " since its cursoId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (profesorIdNew != null) {
                profesorIdNew = em.getReference(profesorIdNew.getClass(), profesorIdNew.getId());
                cursos.setProfesorId(profesorIdNew);
            }
            Collection<Clases> attachedClasesCollectionNew = new ArrayList<Clases>();
            for (Clases clasesCollectionNewClasesToAttach : clasesCollectionNew) {
                clasesCollectionNewClasesToAttach = em.getReference(clasesCollectionNewClasesToAttach.getClass(), clasesCollectionNewClasesToAttach.getId());
                attachedClasesCollectionNew.add(clasesCollectionNewClasesToAttach);
            }
            clasesCollectionNew = attachedClasesCollectionNew;
            cursos.setClasesCollection(clasesCollectionNew);
            cursos = em.merge(cursos);
            if (profesorIdOld != null && !profesorIdOld.equals(profesorIdNew)) {
                profesorIdOld.getCursosCollection().remove(cursos);
                profesorIdOld = em.merge(profesorIdOld);
            }
            if (profesorIdNew != null && !profesorIdNew.equals(profesorIdOld)) {
                profesorIdNew.getCursosCollection().add(cursos);
                profesorIdNew = em.merge(profesorIdNew);
            }
            for (Clases clasesCollectionNewClases : clasesCollectionNew) {
                if (!clasesCollectionOld.contains(clasesCollectionNewClases)) {
                    Cursos oldCursoIdOfClasesCollectionNewClases = clasesCollectionNewClases.getCursoId();
                    clasesCollectionNewClases.setCursoId(cursos);
                    clasesCollectionNewClases = em.merge(clasesCollectionNewClases);
                    if (oldCursoIdOfClasesCollectionNewClases != null && !oldCursoIdOfClasesCollectionNewClases.equals(cursos)) {
                        oldCursoIdOfClasesCollectionNewClases.getClasesCollection().remove(clasesCollectionNewClases);
                        oldCursoIdOfClasesCollectionNewClases = em.merge(oldCursoIdOfClasesCollectionNewClases);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = cursos.getId();
                if (findCursos(id) == null) {
                    throw new NonexistentEntityException("The cursos with id " + id + " no longer exists.");
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
            Cursos cursos;
            try {
                cursos = em.getReference(Cursos.class, id);
                cursos.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cursos with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Clases> clasesCollectionOrphanCheck = cursos.getClasesCollection();
            for (Clases clasesCollectionOrphanCheckClases : clasesCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Cursos (" + cursos + ") cannot be destroyed since the Clases " + clasesCollectionOrphanCheckClases + " in its clasesCollection field has a non-nullable cursoId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Profesores profesorId = cursos.getProfesorId();
            if (profesorId != null) {
                profesorId.getCursosCollection().remove(cursos);
                profesorId = em.merge(profesorId);
            }
            em.remove(cursos);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cursos> findCursosEntities() {
        return findCursosEntities(true, -1, -1);
    }

    public List<Cursos> findCursosEntities(int maxResults, int firstResult) {
        return findCursosEntities(false, maxResults, firstResult);
    }

    private List<Cursos> findCursosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cursos.class));
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

    public Cursos findCursos(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cursos.class, id);
        } finally {
            em.close();
        }
    }

    public int getCursosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cursos> rt = cq.from(Cursos.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
