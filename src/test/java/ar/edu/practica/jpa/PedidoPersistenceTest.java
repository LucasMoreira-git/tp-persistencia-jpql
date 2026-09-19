package ar.edu.practica.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoPersistenceTest {
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void abrirUnidadDePersistencia() {
        entityManagerFactory = Persistence.createEntityManagerFactory("miUnidad");
    }

    @AfterAll
    static void cerrarUnidadDePersistencia() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Test
    void persisteProductoYCalculaTotalDelPedido() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Categoria categoria = new Categoria("Bebidas", "Bebidas frías");
        Producto producto = new Producto("Agua", 500.0, "Agua mineral", 10, "agua.jpg", true, categoria);
        Pedido pedido = new Pedido();
        Usuario usuario = new Usuario("Ana", "García", "ana@mail.com", "111111111", "secreto", Rol.ADMIN);
        usuario.agregarPedido(pedido);
        pedido.addDetallePedido(2, producto);

        entityManager.getTransaction().begin();
        entityManager.persist(categoria);
        entityManager.persist(producto);
        entityManager.persist(usuario);
        entityManager.getTransaction().commit();

        assertEquals(1000.0, pedido.getTotal());
        assertEquals(1L, entityManager.createQuery("select count(p) from Producto p", Long.class)
                .getSingleResult());

        entityManager.close();
    }
}