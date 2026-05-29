package com.rentalapp.common;

import java.security.SecureRandom;
import java.util.UUID;

public final class IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {}

    public static String newId() {
        return generateUuidV7().toString();
    }

    private static UUID generateUuidV7() {
        long timestampMs = System.currentTimeMillis() & 0xFFFFFFFFFFFFL;
        int randA = RANDOM.nextInt() & 0xFFF;
        long randB = RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL;

        long msb = (timestampMs << 16) | (0x7L << 12) | randA;
        long lsb = (0x2L << 62) | randB;

        return new UUID(msb, lsb);
    }
}
