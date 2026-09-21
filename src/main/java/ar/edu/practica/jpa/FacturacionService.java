package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Date;
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

    // =========================================================================
    // 4. POBLADO DE DATOS DE PRUEBA (SEEDING IDEMPOTENTE)
    // =========================================================================

    /**
     * Puebla la base de datos con datos de prueba si se encuentra vacía,
     * demostrando el patrón de persistencia en cascada desde FacturaVenta.
     *
     * @return true si se insertaron datos nuevos; false si ya existían facturas.
     */
    public boolean poblarDatosPruebaSiEsNecesario() {
        Long cantidadFactura1001 = em.createQuery("SELECT COUNT(f) FROM FacturaVenta f WHERE f.numero = 1001L", Long.class)
                .getSingleResult();

        if (cantidadFactura1001 > 0) {
            return false;
        }

        em.getTransaction().begin();
        try {
            // 1. Maestros requeridos (o recuperar si ya existen)
            Usuario usuarioAdmin = em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = 'admin'", Usuario.class)
                    .getResultStream().findFirst().orElse(null);
            if (usuarioAdmin == null) {
                usuarioAdmin = new Usuario("admin", "123456", "Lucas", "Moreira");
                em.persist(usuarioAdmin);
            }

            CondicionIva condicionIva = em.createQuery("SELECT c FROM CondicionIva c WHERE c.codigoAfip = 1", CondicionIva.class)
                    .getResultStream().findFirst().orElse(null);
            if (condicionIva == null) {
                condicionIva = new CondicionIva(1, "IVA Responsable Inscripto");
                condicionIva.inicializarAuditoria(usuarioAdmin);
                em.persist(condicionIva);
            }

            TipoMoneda tipoMoneda = em.createQuery("SELECT t FROM TipoMoneda t WHERE t.codigoAfip = '019'", TipoMoneda.class)
                    .getResultStream().findFirst().orElse(null);
            if (tipoMoneda == null) {
                tipoMoneda = new TipoMoneda("019", "Pesos Argentinos", "$");
                tipoMoneda.inicializarAuditoria(usuarioAdmin);
                em.persist(tipoMoneda);
            }

            PuntoVenta puntoVenta = em.createQuery("SELECT p FROM PuntoVenta p WHERE p.numero = 1", PuntoVenta.class)
                    .getResultStream().findFirst().orElse(null);
            if (puntoVenta == null) {
                puntoVenta = new PuntoVenta(1, "Sucursal Central Mendoza", "Electrónica Web", "Av. San Martín 1234");
                puntoVenta.inicializarAuditoria(usuarioAdmin);
                em.persist(puntoVenta);
            }

            Cliente cliente = em.createQuery("SELECT c FROM Cliente c WHERE c.cuitCuil = '30-71234567-9'", Cliente.class)
                    .getResultStream().findFirst().orElse(null);
            if (cliente == null) {
                Contacto contacto = new Contacto("info@techcorp.com", "2614000000", "2615000000");
                Domicilio domicilio = new Domicilio("Av. España", 500);
                cliente = new Cliente("30-71234567-9", "Tech Corp S.A.", contacto, domicilio);
                cliente.inicializarAuditoria(usuarioAdmin);
                em.persist(cliente);
            }

            Rubro rubro = em.createQuery("SELECT r FROM Rubro r WHERE r.codigo = 101", Rubro.class)
                    .getResultStream().findFirst().orElse(null);
            if (rubro == null) {
                rubro = new Rubro("Computación e Informática", 101);
                rubro.inicializarAuditoria(usuarioAdmin);
                em.persist(rubro);
            }

            Marca marca = em.createQuery("SELECT m FROM Marca m WHERE m.codigo = 201", Marca.class)
                    .getResultStream().findFirst().orElse(null);
            if (marca == null) {
                marca = new Marca("Lenovo", 201);
                marca.inicializarAuditoria(usuarioAdmin);
                em.persist(marca);
            }

            Articulo articulo1 = em.createQuery("SELECT a FROM Articulo a WHERE a.codigo = 'LEN-T14'", Articulo.class)
                    .getResultStream().findFirst().orElse(null);
            if (articulo1 == null) {
                articulo1 = new Articulo("LEN-T14", "Notebook Lenovo ThinkPad T14 Gen 4", rubro, marca);
                articulo1.inicializarAuditoria(usuarioAdmin);
                em.persist(articulo1);
            }

            Articulo articulo2 = em.createQuery("SELECT a FROM Articulo a WHERE a.codigo = 'LEN-M70'", Articulo.class)
                    .getResultStream().findFirst().orElse(null);
            if (articulo2 == null) {
                articulo2 = new Articulo("LEN-M70", "Mouse Inalámbrico Lenovo M70", rubro, marca);
                articulo2.inicializarAuditoria(usuarioAdmin);
                em.persist(articulo2);
            }

            ListaPrecio listaPrecio = em.createQuery("SELECT l FROM ListaPrecio l WHERE l.codigo = 'LP-01'", ListaPrecio.class)
                    .getResultStream().findFirst().orElse(null);
            if (listaPrecio == null) {
                listaPrecio = new ListaPrecio("LP-01", "Lista Mayorista General");
                listaPrecio.inicializarAuditoria(usuarioAdmin);
                em.persist(listaPrecio);
            }

            ListaPrecio finalListaPrecio = listaPrecio;
            Articulo finalArticulo1 = articulo1;
            ListaPrecioArticulo lpa1 = em.createQuery("SELECT lpa FROM ListaPrecioArticulo lpa WHERE lpa.articulo.codigo = 'LEN-T14'", ListaPrecioArticulo.class)
                    .getResultStream().findFirst().orElse(null);
            if (lpa1 == null) {
                lpa1 = new ListaPrecioArticulo(finalListaPrecio, finalArticulo1, 1500000.0);
                lpa1.inicializarAuditoria(usuarioAdmin);
                em.persist(lpa1);
            }

            Articulo finalArticulo2 = articulo2;
            ListaPrecioArticulo lpa2 = em.createQuery("SELECT lpa FROM ListaPrecioArticulo lpa WHERE lpa.articulo.codigo = 'LEN-M70'", ListaPrecioArticulo.class)
                    .getResultStream().findFirst().orElse(null);
            if (lpa2 == null) {
                lpa2 = new ListaPrecioArticulo(finalListaPrecio, finalArticulo2, 35000.0);
                lpa2.inicializarAuditoria(usuarioAdmin);
                em.persist(lpa2);
            }

            // 2. Factura de Venta A - Nro 1001
            FacturaVenta factura1 = new FacturaVenta();
            factura1.setNumero(1001L);
            factura1.setFechaEmision(new Date());
            factura1.setCliente(cliente);
            factura1.setCondicionIva(condicionIva);
            factura1.setTipoMoneda(tipoMoneda);
            factura1.setPuntoVenta(puntoVenta);
            factura1.setCae("73259821456987");
            factura1.setCaeFechaVencimiento(new Date(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000));
            factura1.setResultadoAfip("A");
            factura1.setObservaciones("Factura Electrónica AFIP Tipo A generada.");
            factura1.setEstado("AUTORIZADA");
            factura1.setImporteCobrado(3070000.0);
            factura1.setImporteSaldo(0.0);
            factura1.setImporteTotal(3070000.0);
            factura1.inicializarAuditoria(usuarioAdmin);

            FacturaVentaDetalle d1 = new FacturaVentaDetalle(
                    lpa1, "Notebook Lenovo ThinkPad T14 Gen 4 - Core i7 16GB",
                    2, 1500000.0, 0.0, 3000000.0, 0.0, 3000000.0
            );
            FacturaVentaDetalle d2 = new FacturaVentaDetalle(
                    lpa2, "Mouse Inalámbrico Lenovo M70",
                    2, 35000.0, 0.0, 70000.0, 0.0, 70000.0
            );

            // Vinculación bidireccional
            factura1.addDetalle(d1);
            factura1.addDetalle(d2);

            // Persistencia en Cascada: ÚNICAMENTE la cabecera
            em.persist(factura1);

            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error durante el poblado de datos iniciales: " + e.getMessage(), e);
        }
    }
}
