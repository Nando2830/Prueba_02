/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author FERNANDO
 */
@Entity
@Table(name = "clases")
@NamedQueries({
    @NamedQuery(name = "Clases.findAll", query = "SELECT c FROM Clases c"),
    @NamedQuery(name = "Clases.findById", query = "SELECT c FROM Clases c WHERE c.id = :id"),
    @NamedQuery(name = "Clases.findByHorario", query = "SELECT c FROM Clases c WHERE c.horario = :horario"),
    @NamedQuery(name = "Clases.findByAula", query = "SELECT c FROM Clases c WHERE c.aula = :aula")})
public class Clases implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 100)
    @Column(name = "horario")
    private String horario;
    @Size(max = 50)
    @Column(name = "aula")
    private String aula;
    @JoinColumn(name = "curso_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Cursos cursoId;
    @JoinColumn(name = "materia_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Materias materiaId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "claseId")
    private Collection<Inscripciones> inscripcionesCollection;

    public Clases() {
    }

    public Clases(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public Cursos getCursoId() {
        return cursoId;
    }

    public void setCursoId(Cursos cursoId) {
        this.cursoId = cursoId;
    }

    public Materias getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Materias materiaId) {
        this.materiaId = materiaId;
    }

    public Collection<Inscripciones> getInscripcionesCollection() {
        return inscripcionesCollection;
    }

    public void setInscripcionesCollection(Collection<Inscripciones> inscripcionesCollection) {
        this.inscripcionesCollection = inscripcionesCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Clases)) {
            return false;
        }
        Clases other = (Clases) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Clases[ id=" + id + " ]";
    }
    
}
