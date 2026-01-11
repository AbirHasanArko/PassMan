package com.passman.android.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating QR codes for credential sharing
 */
public class QRCodeGenerator {

    private static final int DEFAULT_QR_SIZE = 512;

    /**
     * Generate a QR code bitmap from credential data
     * 
     * @param title The credential title
     * @param username The username
     * @param email The email
     * @param password The decrypted password
     * @param url The website URL
     * @return A Bitmap containing the QR code
     */
    public static Bitmap generateCredentialQR(String title, String username, 
                                               String email, String password, String url) {
        try {
            String jsonData = createCredentialJson(title, username, email, password, url);
            return generateQRCode(jsonData, DEFAULT_QR_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate a QR code bitmap from credential data with custom size
     */
    public static Bitmap generateCredentialQR(String title, String username, 
                                               String email, String password, 
                                               String url, int size) {
        try {
            String jsonData = createCredentialJson(title, username, email, password, url);
            return generateQRCode(jsonData, size);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a JSON string from credential data
     */
    private static String createCredentialJson(String title, String username, 
                                                String email, String password, String url) {
        try {
            JSONObject json = new JSONObject();
            json.put("app", "PassMan");
            json.put("version", "1.0");
            json.put("type", "credential");
            
            JSONObject data = new JSONObject();
            data.put("title", title != null ? title : "");
            data.put("username", username != null ? username : "");
            data.put("email", email != null ? email : "");
            data.put("password", password != null ? password : "");
            data.put("url", url != null ? url : "");
            
            json.put("data", data);
            
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Generate a QR code bitmap from any string content
     */
    public static Bitmap generateQRCode(String content, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    /**
     * Generate a QR code with custom colors
     */
    public static Bitmap generateQRCode(String content, int size, 
                                         int foregroundColor, int backgroundColor) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? foregroundColor : backgroundColor;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }
}
