package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturaVentaPersistenceTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = Persistence.createEntityManagerFactory("FacturacionPU");
    }

    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void testPersistenciaEnCascadaFacturaYDetalles() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Usuario admin = new Usuario("admin", "1234", "Admin", "General");
        em.persist(admin);

        CondicionIva condicionIva = new CondicionIva(1, "Responsable Inscripto");
        condicionIva.inicializarAuditoria(admin);
        em.persist(condicionIva);

        TipoMoneda moneda = new TipoMoneda("019", "Pesos Argentinos", "$");
        moneda.inicializarAuditoria(admin);
        em.persist(moneda);

        PuntoVenta puntoVenta = new PuntoVenta(1, "Sucursal 1", "Factura Electronica", "San Martin 100");
        puntoVenta.inicializarAuditoria(admin);
        em.persist(puntoVenta);

        Contacto contacto = new Contacto("cliente@empresa.com", "2610001", "2610002");
        Domicilio domicilio = new Domicilio("Mitre", 123);
        Cliente cliente = new Cliente("30-12345678-9", "Empresa Test SA", contacto, domicilio);
        cliente.inicializarAuditoria(admin);
        em.persist(cliente);

        Rubro rubro = new Rubro("Hardware", 10);
        rubro.inicializarAuditoria(admin);
        em.persist(rubro);

        Marca marca = new Marca("HP", 20);
        marca.inicializarAuditoria(admin);
        em.persist(marca);

        Articulo articulo = new Articulo("HP-15", "Notebook HP 15", rubro, marca);
        articulo.inicializarAuditoria(admin);
        em.persist(articulo);

        ListaPrecio listaPrecio = new ListaPrecio("LP-01", "Lista Oficial");
        listaPrecio.inicializarAuditoria(admin);
        em.persist(listaPrecio);

        ListaPrecioArticulo lpa = new ListaPrecioArticulo(listaPrecio, articulo, 800000.0);
        lpa.inicializarAuditoria(admin);
        em.persist(lpa);

        // Crear FacturaVenta
        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(5001L);
        factura.setFechaEmision(new Date());
        factura.setCliente(cliente);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(moneda);
        factura.setPuntoVenta(puntoVenta);
        factura.setCae("71234567890123");
        factura.setCaeFechaVencimiento(new Date());
        factura.setResultadoAfip("A");
        factura.setEstado("AUTORIZADA");
        factura.setImporteTotal(1600000.0);
        factura.inicializarAuditoria(admin);

        // Crear dos detalles y asociarlos bidireccionalmente
        FacturaVentaDetalle d1 = new FacturaVentaDetalle(lpa, "Detalle 1", 1, 800000.0, 0, 800000.0, 0, 800000.0);
        FacturaVentaDetalle d2 = new FacturaVentaDetalle(lpa, "Detalle 2", 1, 800000.0, 0, 800000.0, 0, 800000.0);
        factura.addDetalle(d1);
        factura.addDetalle(d2);

        // Persistir ÚNICAMENTE la cabecera
        em.persist(factura);
        em.getTransaction().commit();

        assertNotNull(factura.getId(), "La factura debe tener ID generado.");
        assertEquals(2, factura.getDetalles().size());
        assertNotNull(d1.getId(), "El detalle 1 debe tener ID generado por cascada.");
        assertNotNull(d2.getId(), "El detalle 2 debe tener ID generado por cascada.");

        // Limpiar contexto de persistencia y consultar desde la base de datos
        em.clear();

        FacturaVenta facturaRecuperada = em.find(FacturaVenta.class, factura.getId());
        assertNotNull(facturaRecuperada);
        assertEquals(5001L, facturaRecuperada.getNumero());
        assertEquals("71234567890123", facturaRecuperada.getCae());
        assertEquals(2, facturaRecuperada.getDetalles().size());
        assertEquals(1600000.0, facturaRecuperada.getImporteTotal());

        Long totalDetalles = em.createQuery("SELECT count(d) FROM FacturaVentaDetalle d WHERE d.factura.id = :facturaId", Long.class)
                .setParameter("facturaId", factura.getId())
                .getSingleResult();
        assertEquals(2L, totalDetalles);

        em.close();
    }
}

