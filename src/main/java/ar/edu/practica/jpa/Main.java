package ar.edu.practica.jpa;

public class Main {
    public static void main(String[] args) {
        try {
            new PracticaService().ejecutarTrabajoPractico();
            System.out.println("Trabajo práctico JPA ejecutado correctamente.");
        } finally {
            JpaUtil.cerrar();
        }
    }
}