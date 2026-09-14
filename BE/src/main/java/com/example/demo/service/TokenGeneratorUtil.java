package com.example.demo.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

/**
 * Genera codici casuali per i vari tipi di token dell'applicazione.
 */
public final class TokenGeneratorUtil {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenGeneratorUtil() {
    }

    public static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Genera un codice numerico casuale con il numero di cifre indicato (es. 6 -> 000000..999999).
     */
    public static BigDecimal randomNumericCode(int digits) {
        int bound = (int) Math.pow(10, digits);
        int value = RANDOM.nextInt(bound);
        return BigDecimal.valueOf(value);
    }

    public static String formatNumericCode(BigDecimal code, int digits) {
        return String.format("%0" + digits + "d", code.intValue());
    }
}
