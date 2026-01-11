package com.passman.android.security;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import com.passman.android.PassManApp;
import com.passman.android.security.SessionManager;
import com.passman.core.crypto.AESCipher;
import com.passman.core.model.EncryptedFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * Manager for encrypting and decrypting files
 */
public class FileEncryptionManager {

    private final Application application;
    private final AESCipher cipher;

    public FileEncryptionManager(Application application) {
        this.application = application;
        this.cipher = new AESCipher();
    }

    /**
     * Encrypt a file from URI and store encrypted data
     */
    public EncryptedFile encryptFile(Uri fileUri, long vaultId) throws Exception {
        ContentResolver contentResolver = application.getContentResolver();
        
        // Get original file info
        String originalFileName = getFileName(fileUri);
        InputStream inputStream = contentResolver.openInputStream(fileUri);
        
        if (inputStream == null) {
            throw new Exception("Cannot open file: " + fileUri);
        }

        // Get master key from session
        SessionManager sessionManager = ((PassManApp) application).getSessionManager();
        SecretKey masterKey = sessionManager.getMasterKey();
        if (masterKey == null) {
            throw new Exception("Master key not available");
        }

        // Read file content
        byte[] fileContent = inputStream.readAllBytes();
        inputStream.close();

        // Encrypt file content (IV prepended to ciphertext)
        byte[] encryptedData = cipher.encryptBytes(fileContent, masterKey);

        // Calculate checksum
        String checksum = calculateChecksum(fileContent);

        // Persist encrypted data to app-private storage: files/vaults/<vaultId>/<random>.enc
        File vaultDir = new File(application.getFilesDir(), "vaults/" + vaultId);
        if (!vaultDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            vaultDir.mkdirs();
        }

        String encryptedName = UUID.randomUUID() + ".enc";
        File outFile = new File(vaultDir, encryptedName);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(encryptedData);
            fos.flush();
        }

        // Write metadata sidecar JSON for quick listing
        File metaFile = new File(vaultDir, encryptedName + ".json");
        try (FileOutputStream mfos = new FileOutputStream(metaFile)) {
            String mime = contentResolver.getType(fileUri);
            String json = "{"+
                "\"vaultId\":" + vaultId + ","+
                "\"originalFileName\":\"" + originalFileName.replace("\"","\\\"") + "\","+
                "\"mimeType\":\"" + (mime == null ? "" : mime.replace("\"","\\\"")) + "\","+
                "\"originalSize\":" + fileContent.length + ","+
                "\"encryptedSize\":" + outFile.length() + ","+
                "\"checksum\":\"" + checksum + "\""+
                "}";
            mfos.write(json.getBytes());
            mfos.flush();
        }

        // Create EncryptedFile record
        EncryptedFile encryptedFile = new EncryptedFile();
        encryptedFile.setVaultId(vaultId);
        encryptedFile.setOriginalFileName(originalFileName);
        encryptedFile.setEncryptedFileName(encryptedName);
        encryptedFile.setOriginalSize(fileContent.length);
        encryptedFile.setEncryptedSize(outFile.length());
        encryptedFile.setMimeType(contentResolver.getType(fileUri));
        encryptedFile.setChecksum(checksum);

        return encryptedFile;
    }

    /**
     * Decrypt file and return decrypted content
     */
    public byte[] decryptFile(EncryptedFile encryptedFile) throws Exception {
        // Get master key from session
        SessionManager sessionManager = ((PassManApp) application).getSessionManager();
        SecretKey masterKey = sessionManager.getMasterKey();
        if (masterKey == null) {
            throw new Exception("Master key not available");
        }

        // Load encrypted data from storage
        File inFile = new File(new File(application.getFilesDir(),
                "vaults/" + encryptedFile.getVaultId()),
                encryptedFile.getEncryptedFileName());

        if (!inFile.exists()) {
            throw new Exception("Encrypted file data not found");
        }

        byte[] encryptedData;
        try (FileInputStream fis = new FileInputStream(inFile)) {
            encryptedData = fis.readAllBytes();
        }

        // Decrypt
        return cipher.decryptBytes(encryptedData, masterKey);
    }

    /**
     * Get file name from URI
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = application.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Calculate SHA-256 checksum of file content
     */
    private String calculateChecksum(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Encrypt a file and save to specified directory, returning the path
     */
    public String encryptFileToPath(File sourceFile, File targetDir) throws Exception {
        // Get master key from session
        SessionManager sessionManager = ((PassManApp) application).getSessionManager();
        SecretKey masterKey = sessionManager.getMasterKey();
        if (masterKey == null) {
            throw new Exception("Master key not available");
        }

        // Read file content
        byte[] fileContent;
        try (FileInputStream fis = new FileInputStream(sourceFile)) {
            fileContent = fis.readAllBytes();
        }

        // Encrypt file content
        byte[] encryptedData = cipher.encryptBytes(fileContent, masterKey);

        // Create target directory if needed
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Generate encrypted file name
        String encryptedName = UUID.randomUUID() + ".enc";
        File outFile = new File(targetDir, encryptedName);
        
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(encryptedData);
            fos.flush();
        }

        return outFile.getAbsolutePath();
    }

    /**
     * Decrypt a file from path and return content
     */
    public byte[] decryptFileFromPath(String filePath) throws Exception {
        // Get master key from session
        SessionManager sessionManager = ((PassManApp) application).getSessionManager();
        SecretKey masterKey = sessionManager.getMasterKey();
        if (masterKey == null) {
            throw new Exception("Master key not available");
        }

        File inFile = new File(filePath);
        if (!inFile.exists()) {
            throw new Exception("Encrypted file not found");
        }

        byte[] encryptedData;
        try (FileInputStream fis = new FileInputStream(inFile)) {
            encryptedData = fis.readAllBytes();
        }

        return cipher.decryptBytes(encryptedData, masterKey);
    }
}
