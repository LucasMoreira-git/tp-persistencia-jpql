package ar.edu.practica.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contactos")
public class Contacto extends EntityId {

    private String email;
    private String telefono;
    private String celular;
}

