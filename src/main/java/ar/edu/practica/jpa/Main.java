package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Date;

public class Main {

    public static void main(String[] args) {
        // 1. Crear el EntityManagerFactory con el nombre "FacturacionPU" y obtener el EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("FacturacionPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 2. Iniciar una transacción explícita
            em.getTransaction().begin();

            // 3. Instanciar los objetos requeridos preexistentes (Usuario de auditoría, PuntoVenta, CondicionIva, TipoMoneda, Cliente, Articulo, ListaPrecioArticulo, etc.)
            Usuario usuarioAdmin = new Usuario("admin", "123456", "Lucas", "Moreira");
            em.persist(usuarioAdmin);

            CondicionIva condicionIva = new CondicionIva(1, "IVA Responsable Inscripto");
            condicionIva.inicializarAuditoria(usuarioAdmin);
            em.persist(condicionIva);

            TipoMoneda tipoMoneda = new TipoMoneda("019", "Pesos Argentinos", "$");
            tipoMoneda.inicializarAuditoria(usuarioAdmin);
            em.persist(tipoMoneda);

            PuntoVenta puntoVenta = new PuntoVenta(1, "Sucursal Central Mendoza", "Electrónica Web", "Av. San Martín 1234");
            puntoVenta.inicializarAuditoria(usuarioAdmin);
            em.persist(puntoVenta);

            Contacto contacto = new Contacto("info@techcorp.com", "2614000000", "2615000000");
            Domicilio domicilio = new Domicilio("Av. España", 500);
            Cliente cliente = new Cliente("30-71234567-9", "Tech Corp S.A.", contacto, domicilio);
            cliente.inicializarAuditoria(usuarioAdmin);
            em.persist(cliente);

            Rubro rubro = new Rubro("Computación e Informática", 101);
            rubro.inicializarAuditoria(usuarioAdmin);
            em.persist(rubro);

            Marca marca = new Marca("Lenovo", 201);
            marca.inicializarAuditoria(usuarioAdmin);
            em.persist(marca);

            Articulo articulo = new Articulo("LEN-T14", "Notebook Lenovo ThinkPad T14 Gen 4", rubro, marca);
            articulo.inicializarAuditoria(usuarioAdmin);
            em.persist(articulo);

            ListaPrecio listaPrecio = new ListaPrecio("LP-01", "Lista Mayorista General");
            listaPrecio.inicializarAuditoria(usuarioAdmin);
            em.persist(listaPrecio);

            ListaPrecioArticulo listaPrecioArticulo = new ListaPrecioArticulo(listaPrecio, articulo, 1500000.0);
            listaPrecioArticulo.inicializarAuditoria(usuarioAdmin);
            em.persist(listaPrecioArticulo);

            // 4. Crear una cabecera de FacturaVenta y asignarle uno o más objetos FacturaVentaDetalle, asegurando la vinculación bidireccional adecuada
            FacturaVenta facturaVenta = new FacturaVenta();
            facturaVenta.setNumero(1001L);
            facturaVenta.setFechaEmision(new Date());
            facturaVenta.setCliente(cliente);
            facturaVenta.setCondicionIva(condicionIva);
            facturaVenta.setTipoMoneda(tipoMoneda);
            facturaVenta.setPuntoVenta(puntoVenta);
            facturaVenta.setCae("73259821456987");
            facturaVenta.setCaeFechaVencimiento(new Date(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000));
            facturaVenta.setResultadoAfip("A");
            facturaVenta.setMotivoRechazo(null);
            facturaVenta.setObservaciones("Factura Electrónica AFIP generada y autorizada correctamente.");
            facturaVenta.setFechaAnulacion(null);
            facturaVenta.setEstado("AUTORIZADA");
            facturaVenta.setImporteCobrado(3000000.0);
            facturaVenta.setImporteSaldo(0.0);
            facturaVenta.setImporteTotal(3000000.0);
            facturaVenta.inicializarAuditoria(usuarioAdmin);

            FacturaVentaDetalle detalle1 = new FacturaVentaDetalle(
                    listaPrecioArticulo,
                    "Notebook Lenovo ThinkPad T14 Gen 4 - Core i7 16GB",
                    2,
                    1500000.0,
                    0.0,
                    3000000.0,
                    0.0,
                    3000000.0
            );
            // Vinculación bidireccional adecuada mediante helper method
            facturaVenta.addDetalle(detalle1);

            // 5. REQUISITO CLAVE: Persistir ÚNICAMENTE la cabecera ejecutando un solo llamado a em.persist(facturaVenta)
            em.persist(facturaVenta);

            // 6. Verificar que, gracias a CascadeType.ALL / PERSIST, se inserten correctamente tanto la factura cabecera como todos sus detalles
            System.out.println("=================================================================");
            System.out.println("VERIFICACIÓN DE PERSISTENCIA EN CASCADA (CascadeType.ALL)");
            System.out.println("=================================================================");
            System.out.println("Factura Cabecera persistida - ID: " + facturaVenta.getId() + " | Número: " + facturaVenta.getNumero() + " | CAE: " + facturaVenta.getCae());
            System.out.println("Cantidad de detalles en memoria/persistencia: " + facturaVenta.getDetalles().size());
            for (FacturaVentaDetalle d : facturaVenta.getDetalles()) {
                System.out.println(" -> Detalle insertado por Cascada: ID=" + d.getId()
                        + ", Descripción='" + d.getDescripcion() + "'"
                        + ", Cantidad=" + d.getCantidad()
                        + ", Subtotal=$" + d.getImporteSubtotal()
                        + ", Factura vinculada ID=" + (d.getFactura() != null ? d.getFactura().getId() : "null"));
            }
            System.out.println("=================================================================");

            // 7. Confirmar la transacción
            em.getTransaction().commit();
            System.out.println("Transacción confirmada (commit) exitosamente.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar los recursos
            if (em.isOpen()) {
                em.close();
            }
            if (emf.isOpen()) {
                emf.close();
            }
        }
    }
}