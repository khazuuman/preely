package com.example.preely.util;

import com.google.firebase.firestore.GeoPoint;

public class LocationHelper {
    // Default locations
    public static final GeoPoint HANOI_DEFAULT = new GeoPoint(21.0285, 105.8542);
    public static final GeoPoint HCMC_DEFAULT = new GeoPoint(10.7769, 106.7009);
    public static final GeoPoint DANANG_DEFAULT = new GeoPoint(16.0544, 108.2022);

    /**
     * Format location for display
     * @param location GeoPoint to format
     * @return Formatted string "Lat: X.XXX, Lng: Y.YYY"
     */
    public static String formatLocation(GeoPoint location) {
        if (location == null) return "Vị trí: Chưa chọn";
        return String.format("Vị trí: %.6f, %.6f", location.getLatitude(), location.getLongitude());
    }

    /**
     * Get distance between two points (simplified)
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in km (approximate)
     */
    public static double getDistance(GeoPoint loc1, GeoPoint loc2) {
        if (loc1 == null || loc2 == null) return 0;

        double lat1Rad = Math.toRadians(loc1.getLatitude());
        double lat2Rad = Math.toRadians(loc2.getLatitude());
        double deltaLat = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double deltaLng = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng/2) * Math.sin(deltaLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return 6371 * c; // Earth radius in km
    }
}
