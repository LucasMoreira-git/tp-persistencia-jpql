package ar.edu.practica.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AuditoriaApp extends EntityId {

    @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaAlta;

    @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaBaja;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    protected Date fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "usuario_carga_id", nullable = false)
    protected Usuario usuarioCarga;

    @ManyToOne
    @JoinColumn(name = "usuario_baja_id", nullable = true)
    protected Usuario usuarioBaja;

    @ManyToOne
    @JoinColumn(name = "usuario_modificacion_id", nullable = false)
    protected Usuario usuarioModificacion;

    @PrePersist
    protected void onPrePersist() {
        Date now = new Date();
        if (fechaAlta == null) {
            fechaAlta = now;
        }
        if (fechaModificacion == null) {
            fechaModificacion = now;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        fechaModificacion = new Date();
    }

    public void inicializarAuditoria(Usuario usuario) {
        Date now = new Date();
        this.fechaAlta = now;
        this.fechaModificacion = now;
        this.usuarioCarga = usuario;
        this.usuarioModificacion = usuario;
    }
}

