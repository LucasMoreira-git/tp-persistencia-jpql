package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

/**
 * Capa de servicio orientada a encapsular la lógica de acceso a datos
 * y la ejecución de consultas JPQL (Java Persistence Query Language).
 */
public class FacturacionService {

    private final EntityManager em;

    public FacturacionService(EntityManager em) {
        this.em = em;
    }

    // =========================================================================
    // 1. BÚSQUEDA POR ATRIBUTOS ÚNICOS / FILTROS
    // =========================================================================

    /**
     * Busca un Usuario por su nombre de usuario (username).
     */
    public Optional<Usuario> buscarUsuarioPorNombreUsuario(String usuario) {
        try {
            Usuario u = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.usuario = :usuario", Usuario.class)
                    .setParameter("usuario", usuario)
                    .getSingleResult();
            return Optional.of(u);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca un Cliente por su número de CUIT/CUIL.
     */
    public Optional<Cliente> buscarClientePorCuit(String cuitCuil) {
        try {
            Cliente c = em.createQuery(
                            "SELECT c FROM Cliente c WHERE c.cuitCuil = :cuitCuil", Cliente.class)
                    .setParameter("cuitCuil", cuitCuil)
                    .getSingleResult();
            return Optional.of(c);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca un Artículo por su código único.
     */
    public Optional<Articulo> buscarArticuloPorCodigo(String codigo) {
        try {
            Articulo a = em.createQuery(
                            "SELECT a FROM Articulo a WHERE a.codigo = :codigo", Articulo.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
            return Optional.of(a);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // =========================================================================
    // 2. CONSULTAS CON JOINS Y RELACIONES ORIENTADAS A OBJETOS
    // =========================================================================

    /**
     * Obtiene todas las facturas de venta emitidas a un cliente específico filtrando por CUIT.
     * Realiza un JOIN orientado a objetos entre FacturaVenta y Cliente.
     */
    public List<FacturaVenta> buscarFacturasPorClienteCuit(String cuitCuil) {
        return em.createQuery(
                        "SELECT f FROM FacturaVenta f JOIN f.cliente c WHERE c.cuitCuil = :cuitCuil ORDER BY f.fechaEmision DESC",
                        FacturaVenta.class)
                .setParameter("cuitCuil", cuitCuil)
                .getResultList();
    }

    /**
     * Consulta los ítems de detalle (FacturaVentaDetalle) asociados a un número
     * de factura particular, navegando a través de la relación de dominio 'd.factura'.
     */
    public List<FacturaVentaDetalle> buscarDetallesPorNumeroFactura(Long numeroFactura) {
        return em.createQuery(
                        "SELECT d FROM FacturaVentaDetalle d WHERE d.factura.numero = :numeroFactura",
                        FacturaVentaDetalle.class)
                .setParameter("numeroFactura", numeroFactura)
                .getResultList();
    }

    // =========================================================================
    // 3. CONSULTAS DE AGREGACIÓN Y MÉTRICAS COMERCIALES
    // =========================================================================

    /**
     * Calcula el importe total facturado general utilizando SUM(f.importeTotal).
     * Devuelve 0.0 si no existen facturas emitidas.
     */
    public Double calcularImporteTotalFacturadoGeneral() {
        Double total = em.createQuery(
                        "SELECT COALESCE(SUM(f.importeTotal), 0.0) FROM FacturaVenta f",
                        Double.class)
                .getSingleResult();
        return total != null ? total : 0.0;
    }

    /**
     * Cuenta la cantidad total de facturas emitidas por un Punto de Venta específico
     * navegando a la relación 'f.puntoVenta'.
     */
    public Long contarFacturasPorPuntoVenta(int numeroPuntoVenta) {
        return em.createQuery(
                        "SELECT COUNT(f) FROM FacturaVenta f WHERE f.puntoVenta.numero = :numeroPuntoVenta",
                        Long.class)
                .setParameter("numeroPuntoVenta", numeroPuntoVenta)
                .getSingleResult();
    }

}
