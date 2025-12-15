package com.passman.core.crypto;

import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecureRandomProvider
 */
public class SecureRandomProviderTest {

    @Test
    public void testGetInstance() {
        SecureRandom random1 = SecureRandomProvider.getInstance();
        SecureRandom random2 = SecureRandomProvider.getInstance();

        assertNotNull(random1, "SecureRandom instance should not be null");
        assertNotNull(random2, "SecureRandom instance should not be null");

        // Should return the same instance (singleton)
        assertSame(random1, random2, "Should return same singleton instance");

        System.out.println("✅ SecureRandomProvider singleton works correctly!");
    }

    @Test
    public void testRandomGeneration() {
        SecureRandom random = SecureRandomProvider. getInstance();

        byte[] bytes1 = new byte[16];
        byte[] bytes2 = new byte[16];

        random.nextBytes(bytes1);
        random.nextBytes(bytes2);

        // Two random byte arrays should be different
        assertFalse(java.util.Arrays.equals(bytes1, bytes2),
                "Random byte arrays should be different");

        System.out.println("✅ Random generation works correctly!");
    }

    @Test
    public void testRandomBytesNotEmpty() {
        SecureRandom random = SecureRandomProvider.getInstance();

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        // Check that at least some bytes are non-zero
        boolean hasNonZero = false;
        for (byte b : bytes) {
            if (b != 0) {
                hasNonZero = true;
                break;
            }
        }

        assertTrue(hasNonZero, "Random bytes should contain non-zero values");

        System.out.println("✅ Random bytes are properly generated!");
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        final SecureRandom[] results = new SecureRandom[THREAD_COUNT];

        // Create multiple threads that get the instance
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = SecureRandomProvider.getInstance();
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread. start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // All threads should get the same instance
        SecureRandom first = results[0];
        for (int i = 1; i < THREAD_COUNT; i++) {
            assertSame(first, results[i], "All threads should get same singleton instance");
        }

        System.out.println("✅ Thread safety verified!");
    }
}