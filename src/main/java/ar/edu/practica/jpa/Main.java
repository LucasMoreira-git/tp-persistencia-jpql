package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("    SISTEMA DE FACTURACIÓN COMERCIAL INTEGRADO CON AFIP - JPA & JPQL");
        System.out.println("================================================================================");

        // 1. Inicialización de EntityManagerFactory y EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            FacturacionService service = new FacturacionService(em);

            // 2. Poblado de datos de prueba demostrando persistencia en cascada
            boolean poblado = poblarDatosPruebaSiEsNecesario(em);
            if (poblado) {
                System.out.println("[INFO] Base de datos inicializada y datos de prueba persistidos en cascada (CascadeType.ALL).");
            } else {
                System.out.println("[INFO] La base de datos ya contenía registros previos.");
            }

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("1. BÚSQUEDAS POR ATRIBUTOS ÚNICOS / FILTROS (JPQL)");
            System.out.println("--------------------------------------------------------------------------------");

            // A. Buscar Usuario por nombre de usuario
            String usernameBuscado = "admin";
            Optional<Usuario> usuarioOpt = service.buscarUsuarioPorNombreUsuario(usernameBuscado);
            usuarioOpt.ifPresentOrElse(
                    u -> System.out.printf(" -> [USUARIO ENCONTRADO] ID: %d | Username: %s | Nombre: %s %s%n",
                            u.getId(), u.getUsuario(), u.getNombre(), u.getApellido()),
                    () -> System.out.printf(" -> [USUARIO] No se encontró usuario '%s'%n", usernameBuscado)
            );

            // B. Buscar Cliente por CUIT/CUIL
            String cuitBuscado = "30-71234567-9";
            Optional<Cliente> clienteOpt = service.buscarClientePorCuit(cuitBuscado);
            clienteOpt.ifPresentOrElse(
                    c -> System.out.printf(" -> [CLIENTE ENCONTRADO] ID: %d | CUIT: %s | Denominación: %s | Contacto: %s | Domicilio: %s %d%n",
                            c.getId(), c.getCuitCuil(), c.getDenominacion(),
                            c.getContacto().getEmail(), c.getDomicilio().getNombreCalle(), c.getDomicilio().getNumeroCalle()),
                    () -> System.out.printf(" -> [CLIENTE] No se encontró cliente con CUIT '%s'%n", cuitBuscado)
            );

            // C. Buscar Artículo por código único
            String codigoArticulo = "LEN-T14";
            Optional<Articulo> articuloOpt = service.buscarArticuloPorCodigo(codigoArticulo);
            articuloOpt.ifPresentOrElse(
                    a -> System.out.printf(" -> [ARTÍCULO ENCONTRADO] ID: %d | Código: %s | Denominación: %s | Marca: %s | Rubro: %s%n",
                            a.getId(), a.getCodigo(), a.getDenominacion(),
                            a.getMarca() != null ? a.getMarca().getDenominacion() : "N/A",
                            a.getRubro() != null ? a.getRubro().getDenominacion() : "N/A"),
                    () -> System.out.printf(" -> [ARTÍCULO] No se encontró artículo con código '%s'%n", codigoArticulo)
            );

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("2. CONSULTAS CON JOINS Y NAVEGACIÓN ORIENTADA A OBJETOS (JPQL)");
            System.out.println("--------------------------------------------------------------------------------");

            // A. Obtener facturas emitidas a un cliente específico
            List<FacturaVenta> facturasCliente = service.buscarFacturasPorClienteCuit(cuitBuscado);
            System.out.printf(" -> Facturas emitidas al cliente CUIT '%s' (%d encontradas):%n", cuitBuscado, facturasCliente.size());
            for (FacturaVenta f : facturasCliente) {
                System.out.printf("    * Factura Nro: %d | Fecha: %s | Estado: %s | CAE: %s | Total: $%.2f%n",
                        f.getNumero(), f.getFechaEmision(), f.getEstado(), f.getCae(), f.getImporteTotal());
            }

            // B. Consultar ítems de detalle navegando desde la relación 'd.factura'
            Long numeroFactura = 1001L;
            List<FacturaVentaDetalle> detalles = service.buscarDetallesPorNumeroFactura(numeroFactura);
            System.out.printf("%n -> Ítems de Detalle asociados a la Factura Nro %d (%d renglones):%n", numeroFactura, detalles.size());
            for (FacturaVentaDetalle d : detalles) {
                System.out.printf("    * Detalle ID: %d | Cantidad: %d | Descripción: '%s' | Precio Unit: $%.2f | Subtotal: $%.2f%n",
                        d.getId(), d.getCantidad(), d.getDescripcion(), d.getPrecioUnitario(), d.getImporteSubtotal());
            }

            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("3. CONSULTAS DE AGREGACIÓN Y MÉTRICAS COMERCIALES (JPQL)");
            System.out.println("--------------------------------------------------------------------------------");

            // A. Importe total facturado general (SUM)
            Double importeTotal = service.calcularImporteTotalFacturadoGeneral();
            System.out.printf(" -> Total Facturado General (SUM): $%.2f%n", importeTotal);

            // B. Total de facturas por Punto de Venta (COUNT)
            int puntoVentaNro = 1;
            Long cantidadFacturasPV = service.contarFacturasPorPuntoVenta(puntoVentaNro);
            System.out.printf(" -> Cantidad de Facturas emitidas en Punto de Venta %d (COUNT): %d comprobante(s)%n",
                    puntoVentaNro, cantidadFacturasPV);

            System.out.println("================================================================================");
            System.out.println("    EJECUCIÓN DEL SERVICIO JPQL COMPLETADA CON ÉXITO");
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("Error durante la ejecución del sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 4. Cierre adecuado de recursos
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }

    /**
     * Puebla la base de datos con datos de prueba si se encuentra vacía,
     * demostrando el patrón de persistencia en cascada desde FacturaVenta.
     *
     * @return true si se insertaron datos nuevos; false si ya existían facturas.
     */
    public static boolean poblarDatosPruebaSiEsNecesario(EntityManager em) {
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