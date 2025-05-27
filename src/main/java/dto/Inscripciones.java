/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author FERNANDO
 */
@Entity
@Table(name = "inscripciones")
@NamedQueries({
    @NamedQuery(name = "Inscripciones.findAll", query = "SELECT i FROM Inscripciones i"),
    @NamedQuery(name = "Inscripciones.findById", query = "SELECT i FROM Inscripciones i WHERE i.id = :id"),
    @NamedQuery(name = "Inscripciones.findByFechaInscripcion", query = "SELECT i FROM Inscripciones i WHERE i.fechaInscripcion = :fechaInscripcion")})
public class Inscripciones implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "fecha_inscripcion")
    @Temporal(TemporalType.DATE)
    private Date fechaInscripcion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "inscripcionId")
    private Collection<Asistencia> asistenciaCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "inscripcionId")
    private Collection<Calificaciones> calificacionesCollection;
    @JoinColumn(name = "estudiante_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Estudiantes estudianteId;
    @JoinColumn(name = "clase_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Clases claseId;

    public Inscripciones() {
    }

    public Inscripciones(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(Date fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public Collection<Asistencia> getAsistenciaCollection() {
        return asistenciaCollection;
    }

    public void setAsistenciaCollection(Collection<Asistencia> asistenciaCollection) {
        this.asistenciaCollection = asistenciaCollection;
    }

    public Collection<Calificaciones> getCalificacionesCollection() {
        return calificacionesCollection;
    }

    public void setCalificacionesCollection(Collection<Calificaciones> calificacionesCollection) {
        this.calificacionesCollection = calificacionesCollection;
    }

    public Estudiantes getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Estudiantes estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Clases getClaseId() {
        return claseId;
    }

    public void setClaseId(Clases claseId) {
        this.claseId = claseId;
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
        if (!(object instanceof Inscripciones)) {
            return false;
        }
        Inscripciones other = (Inscripciones) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Inscripciones[ id=" + id + " ]";
    }
    
}
