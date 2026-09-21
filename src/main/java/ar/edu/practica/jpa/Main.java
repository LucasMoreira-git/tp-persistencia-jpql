package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
            boolean poblado = service.poblarDatosPruebaSiEsNecesario();
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
}