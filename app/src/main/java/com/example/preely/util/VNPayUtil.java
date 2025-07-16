package com.example.preely.util;

import java.net.URLEncoder;
import java.util.*;

public class VNPayUtil {
    
    public static String createPaymentUrl(Map<String, String> params, String hashSecret, String payUrl) throws Exception {
        // Sắp xếp params theo thứ tự alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        // Tạo query string đã encode
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append("&");
            }
            String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
            String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
            queryBuilder.append(encodedKey).append("=").append(encodedValue);
        }
        String query = queryBuilder.toString();
        
        System.out.println("Query string for hash: " + query);
        
        // Tạo hash từ query string đã encode
        String secureHash = hmacSHA512(query, hashSecret);
        System.out.println("Generated hash: " + secureHash);
        
        // Thêm hash vào params
        sortedParams.put("vnp_SecureHash", secureHash);
        
        // Tạo URL cuối cùng
        StringBuilder urlBuilder = new StringBuilder(payUrl + "?");
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '?') {
                urlBuilder.append("&");
            }
            String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
            String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
            urlBuilder.append(encodedKey).append("=").append(encodedValue);
        }
        
        return urlBuilder.toString();
    }

    public static String hmacSHA512(String data, String key) throws Exception {
        javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] bytes = hmac512.doFinal(data.getBytes("UTF-8"));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }
    
    // Method cũ để tương thích ngược
    public static String hashAllFields(Map<String, String> fields, String hashSecret) throws Exception {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = fields.get(fieldName);
            // Skip vnp_SecureHash field as it's not part of the hash calculation
            if ((value != null) && (value.length() > 0) && !fieldName.equals("vnp_SecureHash")) {
                sb.append(fieldName);
                sb.append('=');
                sb.append(value);
                sb.append('&');
            }
        }
        // Remove last '&'
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String data = sb.toString();
        System.out.println("Data to hash: " + data);
        return hmacSHA512(data, hashSecret);
    }
} 