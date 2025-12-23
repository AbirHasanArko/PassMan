package com.passman.core.services;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service for QR code generation and password sharing
 */
public class QRCodeService {

    private final EncryptionServiceImpl encryptionService;
    private final Gson gson;

    public QRCodeService() {
        this.encryptionService = new EncryptionServiceImpl();
        this.gson = new Gson();
    }

    /**
     * Generate QR code for password sharing
     * @param password The password to share
     * @param expiryMinutes How long the QR code is valid (0 = no expiry)
     * @return PNG image bytes
     */
    public byte[] generatePasswordQRCode(String password, int expiryMinutes) throws Exception {
        // Create payload
        QRPayload payload = new QRPayload();
        payload.password = password;
        payload.timestamp = LocalDateTime.now().toString();

        if (expiryMinutes > 0) {
            payload.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes).toString();
        }

        // Convert to JSON
        String json = gson.toJson(payload);

        // Encode to Base64
        String data = Base64.getEncoder().encodeToString(json.getBytes());

        // Generate QR code
        return generateQRCode(data, 400, 400);
    }

    /**
     * Generate QR code image
     */
    private byte[] generateQRCode(String data, int width, int height) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Validate QR code hasn't expired
     */
    public boolean isQRCodeValid(QRPayload payload) {
        if (payload.expiresAt == null) {
            return true; // No expiry
        }

        LocalDateTime expiry = LocalDateTime.parse(payload.expiresAt);
        return LocalDateTime.now().isBefore(expiry);
    }

    // Data classes

    public static class QRPayload {
        public String password;
        public String timestamp;
        public String expiresAt; // Optional
    }
}