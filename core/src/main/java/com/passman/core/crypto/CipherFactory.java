package com.passman.core.crypto;

import javax.crypto.SecretKey;

/**
 * Factory for creating cipher instances
 */
public class CipherFactory {

    public static AESCipher createAESCipher() {
        return new AESCipher();
    }

    public static PBKDF2KeyDerivation createKeyDerivation() {
        return new PBKDF2KeyDerivation();
    }

    /**
     * Creates a complete encryption context
     */
    public static EncryptionContext createEncryptionContext(char[] masterPassword) throws Exception {
        PBKDF2KeyDerivation keyDerivation = createKeyDerivation();
        byte[] salt = keyDerivation. generateSalt();
        SecretKey key = keyDerivation.deriveKey(masterPassword, salt);

        return new EncryptionContext(key, salt, createAESCipher());
    }

    public static class EncryptionContext {
        private final SecretKey key;
        private final byte[] salt;
        private final AESCipher cipher;

        public EncryptionContext(SecretKey key, byte[] salt, AESCipher cipher) {
            this.key = key;
            this.salt = salt;
            this.cipher = cipher;
        }

        public SecretKey getKey() { return key; }
        public byte[] getSalt() { return salt; }
        public AESCipher getCipher() { return cipher; }
    }
}