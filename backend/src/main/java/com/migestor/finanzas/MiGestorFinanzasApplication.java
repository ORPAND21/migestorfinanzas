package com.migestor.finanzas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de MiGestorFinanzas.
 * Expone una API REST en http://localhost:8080/api consumida por el frontend React.
 */
@SpringBootApplication
public class MiGestorFinanzasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiGestorFinanzasApplication.class, args);
    }
}
