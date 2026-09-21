package ar.edu.practica.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facturas_venta")
public class FacturaVenta extends AuditoriaApp {

    private Long numero;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date fechaEmision;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "condicion_iva_id", nullable = false)
    private CondicionIva condicionIva;

    @ManyToOne
    @JoinColumn(name = "tipo_moneda_id", nullable = false)
    private TipoMoneda tipoMoneda;

    @ManyToOne
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    private double importeCobrado;

    private double importeSaldo;

    @Column(nullable = false)
    private double importeTotal;

    private String cae;

    private String resultadoAfip;

    private String motivoRechazo;

    private String observaciones;

    @Temporal(TemporalType.DATE)
    private Date caeFechaVencimiento;

    @Temporal(TemporalType.DATE)
    private Date fechaAnulacion;

    @Column(nullable = false)
    private String estado;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();

    public void addDetalle(FacturaVentaDetalle detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalles.add(detalle);
        detalle.setFactura(this);
    }

    public void removeDetalle(FacturaVentaDetalle detalle) {
        if (detalles != null) {
            detalles.remove(detalle);
            detalle.setFactura(null);
        }
    }
}

