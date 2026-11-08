package com.migestor.finanzas.seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/**
 * Cifra contrasenas con PBKDF2-HMAC-SHA256 (incluido en Java, sin librerias externas).
 * Formato guardado: pbkdf2$iteraciones$salBase64$hashBase64
 */
@Component
public class HashService {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65536;
    private static final int LONGITUD_BITS = 256;
    private final SecureRandom random = new SecureRandom();

    public String cifrar(String password) {
        byte[] sal = new byte[16];
        random.nextBytes(sal);
        byte[] hash = derivar(password, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String password, String guardado) {
        if (password == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        int iteraciones = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = derivar(password, sal, iteraciones);
        // Comparacion en tiempo constante para evitar ataques de temporizacion
        return MessageDigest.isEqual(esperado, calculado);
    }

    private byte[] derivar(String password, byte[] sal, int iteraciones) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), sal, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible cifrar la contrasena", e);
        }
    }
}
