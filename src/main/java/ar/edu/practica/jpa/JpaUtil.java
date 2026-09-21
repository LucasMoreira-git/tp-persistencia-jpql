package ar.edu.practica.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            Persistence.createEntityManagerFactory("FacturacionPU");

    private JpaUtil() {
    }

    public static EntityManagerFactory entityManagerFactory() {
        return ENTITY_MANAGER_FACTORY;
    }

    public static void cerrar() {
        if (ENTITY_MANAGER_FACTORY != null && ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }
}