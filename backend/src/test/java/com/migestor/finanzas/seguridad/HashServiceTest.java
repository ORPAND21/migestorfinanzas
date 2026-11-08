package com.migestor.finanzas.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del cifrado de contraseñas. */
class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    @DisplayName("La contraseña cifrada no contiene el texto original y se puede verificar")
    void cifrarYVerificar() {
        String hash = hashService.cifrar("Clave123*");
        assertFalse(hash.contains("Clave123*"));
        assertTrue(hashService.verificar("Clave123*", hash));
        assertFalse(hashService.verificar("Otra123*", hash));
    }

    @Test
    @DisplayName("Dos cifrados de la misma contraseña son distintos por la sal aleatoria")
    void salAleatoria() {
        assertNotEquals(hashService.cifrar("Clave123*"), hashService.cifrar("Clave123*"));
    }
}
