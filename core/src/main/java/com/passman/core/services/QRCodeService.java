package com.passman.core.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.function.BiConsumer;

/**
 * Service for QR code generation, scanning, and credential sharing
 * Supports both simple password sharing and full credential sharing (compatible with Android app)
 */
public class QRCodeService {

    private static final int DEFAULT_QR_SIZE = 400;

    public QRCodeService() {
        // No initialization needed for simple QR generation
    }

    /**
     * Generate QR code for password sharing
     * @param password The password to share
     * @param expiryMinutes How long the QR code is valid (0 = no expiry) - kept for API compatibility
     * @return PNG image bytes
     */
    public byte[] generatePasswordQRCode(String password, int expiryMinutes) throws Exception {
        // Generate QR code with the password directly
        // This allows any standard QR scanner to read the password immediately
        return generateQRCode(password, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }

    /**
     * Generate QR code for full credential sharing (compatible with Android app)
     * @param title The credential title
     * @param username The username
     * @param email The email
     * @param password The decrypted password
     * @param url The website URL
     * @return PNG image bytes
     */
    public byte[] generateCredentialQRCode(String title, String username,
                                           String email, String password, String url) throws Exception {
        String jsonData = createCredentialJson(title, username, email, password, url);
        return generateQRCode(jsonData, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }

    /**
     * Create a JSON string from credential data (compatible with Android app format)
     */
    private String createCredentialJson(String title, String username,
                                        String email, String password, String url) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"app\":\"PassMan\",");
        json.append("\"version\":\"1.0\",");
        json.append("\"type\":\"credential\",");
        json.append("\"data\":{");
        json.append("\"title\":\"").append(escapeJson(title != null ? title : "")).append("\",");
        json.append("\"username\":\"").append(escapeJson(username != null ? username : "")).append("\",");
        json.append("\"email\":\"").append(escapeJson(email != null ? email : "")).append("\",");
        json.append("\"password\":\"").append(escapeJson(password != null ? password : "")).append("\",");
        json.append("\"url\":\"").append(escapeJson(url != null ? url : "")).append("\"");
        json.append("}}");
        return json.toString();
    }

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Parse credential data from a scanned QR code
     * @param qrContent The raw content from QR code
     * @return CredentialData object or null if invalid format
     */
    public CredentialData parseCredentialQR(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }

        try {
            // Check if it's a PassMan QR code
            if (!qrContent.contains("\"app\":\"PassMan\"")) {
                return null;
            }

            // Parse JSON manually (to avoid dependency)
            CredentialData data = new CredentialData();
            data.title = extractJsonValue(qrContent, "title");
            data.username = extractJsonValue(qrContent, "username");
            data.email = extractJsonValue(qrContent, "email");
            data.password = extractJsonValue(qrContent, "password");
            data.url = extractJsonValue(qrContent, "url");

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract a value from JSON string
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return "";

        start += searchKey.length();
        int end = start;
        boolean escaped = false;

        while (end < json.length()) {
            char c = json.charAt(end);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            end++;
        }

        String value = json.substring(start, end);
        // Unescape
        return value.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    /**
     * Scan QR code from an image file
     * @param imageFile The image file containing QR code
     * @return The decoded content or null if no QR code found
     */
    public String scanQRCodeFromFile(File imageFile) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);
        return scanQRCodeFromImage(image);
    }

    /**
     * Scan QR code from image bytes
     * @param imageBytes The image bytes (PNG, JPG, etc.)
     * @return The decoded content or null if no QR code found
     */
    public String scanQRCodeFromBytes(byte[] imageBytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        return scanQRCodeFromImage(image);
    }

    /**
     * Scan QR code from BufferedImage
     * @param image The BufferedImage containing QR code
     * @return The decoded content or null if no QR code found
     */
    public String scanQRCodeFromImage(BufferedImage image) throws Exception {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
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
     * Generate QR code and write pixel data directly using a callback.
     * This method avoids AWT/Swing dependencies and works reliably in packaged applications.
     * 
     * @param data The data to encode in QR code
     * @param width The width of the QR code
     * @param height The height of the QR code
     * @param pixelWriter A callback that receives (x, y) coordinates and writes the pixel.
     *                    The callback receives coordinates where black pixels should be set.
     * @return Array of [width, height] for the generated QR code
     */
    public int[] generateQRCodeDirect(String data, int width, int height, BiConsumer<Integer, Integer> pixelWriter) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        int matrixWidth = bitMatrix.getWidth();
        int matrixHeight = bitMatrix.getHeight();

        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (bitMatrix.get(x, y)) {
                    pixelWriter.accept(x, y);
                }
            }
        }

        return new int[]{matrixWidth, matrixHeight};
    }

    /**
     * Generate credential QR code using direct pixel writing (no AWT dependency).
     * This is the preferred method for JavaFX applications.
     * 
     * @param title The credential title
     * @param username The username
     * @param email The email
     * @param password The decrypted password
     * @param url The website URL
     * @param pixelWriter A callback that receives (x, y) coordinates for black pixels
     * @return Array of [width, height] for the generated QR code
     */
    public int[] generateCredentialQRCodeDirect(String title, String username,
                                                String email, String password, String url,
                                                BiConsumer<Integer, Integer> pixelWriter) throws Exception {
        String jsonData = createCredentialJson(title, username, email, password, url);
        return generateQRCodeDirect(jsonData, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE, pixelWriter);
    }

    /**
     * Data class for parsed credential from QR code
     */
    public static class CredentialData {
        public String title = "";
        public String username = "";
        public String email = "";
        public String password = "";
        public String url = "";

        @Override
        public String toString() {
            return "CredentialData{" +
                    "title='" + title + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}