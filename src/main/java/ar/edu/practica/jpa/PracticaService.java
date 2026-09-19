package ar.edu.practica.jpa;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class PracticaService {
    public void ejecutarTrabajoPractico() {
        EntityManager entityManager = JpaUtil.entityManagerFactory().createEntityManager();
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();

            List<Categoria> categorias = crearCategorias();
            categorias.forEach(entityManager::persist);

            List<Producto> productos = crearProductos(categorias);
            productos.forEach(entityManager::persist);

            Usuario usuarioAdmin = new Usuario("Ana", "García", "ana@mail.com", "111111111", "secreto", Rol.ADMIN);
            Usuario usuario = new Usuario("Luis", "Pérez", "luis@mail.com", "222222222", "secreto", Rol.USUARIO);
            entityManager.persist(usuarioAdmin);
            entityManager.persist(usuario);

            crearPedidos(usuarioAdmin, usuario, productos).forEach(entityManager::persist);
            transaction.commit();

            actualizarProductos(entityManager, productos.get(0), productos.get(1));
            buscarUsuarios(entityManager, usuarioAdmin.getId(), "luis@mail.com");
            eliminarProducto(entityManager, productos.get(9));
        } finally {
            entityManager.close();
        }
    }

    private List<Categoria> crearCategorias() {
        return List.of(
                new Categoria("Bebidas", "Bebidas frías y calientes"),
                new Categoria("Almacén", "Productos de almacén"),
                new Categoria("Limpieza", "Artículos de limpieza")
        );
    }

    private List<Producto> crearProductos(List<Categoria> categorias) {
        return List.of(
                producto("Tatin", 500.0, categorias.get(0)),
                producto("Coca-Cola", 1200.0, categorias.get(0)),
                producto("Jugo de naranja", 900.0, categorias.get(0)),
                producto("Arroz", 1500.0, categorias.get(1)),
                producto("Fideos", 800.0, categorias.get(1)),
                producto("Yerba", 2500.0, categorias.get(1)),
                producto("Jabón", 700.0, categorias.get(2)),
                producto("Lavandina", 1100.0, categorias.get(2)),
                producto("Esponja", 400.0, categorias.get(2)),
                producto("Detergente", 1300.0, categorias.get(2))
        );
    }

    private Producto producto(String nombre, double precio, Categoria categoria) {
        return new Producto(nombre, precio, "Producto " + nombre, 20, nombre.toLowerCase() + ".jpg", true, categoria);
    }

    private List<Pedido> crearPedidos(Usuario admin, Usuario usuario, List<Producto> productos) {
        Pedido primero = pedido(admin, FormaPago.TARJETA);
        primero.addDetallePedido(2, productos.get(0));
        primero.addDetallePedido(1, productos.get(1));

        Pedido segundo = pedido(usuario, FormaPago.TRANSFERENCIA);
        segundo.addDetallePedido(3, productos.get(2));
        segundo.addDetallePedido(2, productos.get(3));

        Pedido tercero = pedido(usuario, FormaPago.EFECTIVO);
        tercero.addDetallePedido(1, productos.get(4));
        tercero.addDetallePedido(4, productos.get(5));
        return List.of(primero, segundo, tercero);
    }

    private Pedido pedido(Usuario usuario, FormaPago formaPago) {
        Pedido pedido = new Pedido(LocalDate.now(), Estado.PENDIENTE, formaPago);
        usuario.agregarPedido(pedido);
        return pedido;
    }

    private void actualizarProductos(EntityManager entityManager, Producto primero, Producto segundo) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        primero.setPrecio(550.0);
        segundo.setStock(35);
        entityManager.merge(primero);
        entityManager.merge(segundo);
        transaction.commit();
    }

    private void buscarUsuarios(EntityManager entityManager, Long id, String mail) {
        Usuario porId = entityManager.find(Usuario.class, id);
        Usuario porMail = entityManager.createQuery(
                        "select u from Usuario u where u.mail = :mail", Usuario.class)
                .setParameter("mail", mail)
                .getSingleResult();
        System.out.printf("Usuario por id: %s %s%n", porId.getNombre(), porId.getApellido());
        System.out.printf("Usuario por mail: %s %s%n", porMail.getNombre(), porMail.getApellido());
    }

    private void eliminarProducto(EntityManager entityManager, Producto producto) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Producto productoManaged = entityManager.find(Producto.class, producto.getId());
        productoManaged.setEliminado(true);
        entityManager.remove(productoManaged);
        transaction.commit();
    }
}