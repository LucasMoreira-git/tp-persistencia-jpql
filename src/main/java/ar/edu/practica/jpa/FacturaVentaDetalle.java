package ar.edu.practica.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facturas_venta_detalles")
public class FacturaVentaDetalle extends EntityId {

    @ManyToOne
    @JoinColumn(name = "factura_id", nullable = false)
    private FacturaVenta factura;

    @ManyToOne
    @JoinColumn(name = "lista_precio_articulo_id", nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;

    private String descripcion;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private double precioUnitario;

    @Column(nullable = false)
    private double importeSubtotal;

    private double porcentajeBonificacion;

    private double importeNeto;

    private double importeIva;

    public FacturaVentaDetalle(ListaPrecioArticulo listaPrecioArticulo, String descripcion, int cantidad,
                               double precioUnitario, double porcentajeBonificacion, double importeNeto,
                               double importeIva, double importeSubtotal) {
        this.listaPrecioArticulo = listaPrecioArticulo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeBonificacion = porcentajeBonificacion;
        this.importeNeto = importeNeto;
        this.importeIva = importeIva;
        this.importeSubtotal = importeSubtotal;
    }
}

