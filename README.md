# Trabajo Practico JPA

Implementacion del modelo del UML con Jakarta Persistence, Hibernate ORM y H2. Se utilizo Copilot para la resolución del Trabajo Practico, se hizo especialmente al querer entender como se utiliza para aprender a utlizar el gestor de independencia y automatizador de construccion **Maven**

## Maven o gradle

Se utiliza **Maven** porque el proyecto parte de un modelo Java pequeño y necesita una declaracion reproducible de dependencias, compilaciony ejecucion de una clase `main`. Gradle tambien seria valido, pero Maven resulta mas directo para este TP y coincide con la estructura estandar `src/main` y `src/test`.

## Estructura

```text
src/
  main/
    java/ar/edu/practica/jpa/
      Base.java              Entidad base con id, eliminado y createdAt
      Usuario.java           Usuario y sus pedidos
      Pedido.java            Pedido, detalles y calculo de total
      DetallePedido.java     Item del pedido
      Producto.java          Producto y categoria
      Categoria.java         Categoria y productos
      Calculable.java        Contrato del UML
      Estado.java            Estados del pedido
      FormaPago.java         Formas de pago
      Rol.java               Roles de usuario
      JpaUtil.java           EntityManagerFactory compartido
      PracticaService.java   Caso de uso completo del enunciado
      Main.java              Punto de entrada ejecutable
    resources/META-INF/
      persistence.xml        Unidad miUnidad y conexion H2
  test/java/
    ar/edu/practica/jpa/
      PedidoPersistenceTest.java
```

## Como ejecutar

```bash
mvn clean test
mvn exec:java
```

La base se guarda en `data/jpa_db` y se crea o actualiza con `hibernate.hbm2ddl.auto=update`, tal como pide el enunciado.

## Buenas practicas aplicadas

- Entidades separadas por responsabilidad y una `@MappedSuperclass` para datos comunes.
- Relaciones bidireccionales actualizadas mediante metodos de dominio (`agregarPedido`, `addDetallePedido`).
- Enums persistidos como texto para que los datos sean legibles.
- Transacciones explicitas y cierre de `EntityManager`/`EntityManagerFactory`.
- Constructor sin argumentos para JPA y Lombok solo para reducir boilerplate.
- Prueba de integracion que verifica persistencia real contra H2 y calculo del total.
- Compilacion fijada a Java 17 para que el proyecto sea portable aunque el equipo use un JDK mas nuevo.

## Patron "Singleton"
La clase **JpaUtil.java** implementa un patron de software denomianado "Singleton" con clases de utilidad final, es el encargado de centralizar la creacion de EntityManagerFactory mediante este patron. Dado que la fabrica es un recurso pesado, se garantiza la existencia de una **UNICA INSTANCIA** en toda la aplicacion, optimizando memoria y recursos

## Capa de lógica de negocio y Casos de uso
Encapsula la lógica del negocio y las operaciones CRUD requeridas por el enunciado. Desacopla la ejecución principal (`Main`) de la persistencia directa.

Se implementaron JpaUtil y PracticaService para prevenir problemas comunes de infraestructura: evitar la saturación de memoria mediante una única fábrica de conexiones, garantizar la limpieza de recursos con bloques finally, y mantener el código ordenado bajo el principio de separación de responsabilidades.