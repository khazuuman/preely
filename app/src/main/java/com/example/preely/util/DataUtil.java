package com.example.preely.util;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DataUtil {

    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static <T> T mapObj(Object source, Class<T> targetClass) throws IllegalAccessException, InstantiationException {
        T target = targetClass.newInstance();
        try {
            Field[] declaredSourceFields = source.getClass().getDeclaredFields();
            Field[] declaredSourceSuperFields = source.getClass().getSuperclass() != null
                    ? source.getClass().getSuperclass().getDeclaredFields()
                    : new Field[0];

            Field[] sourceFields = new Field[declaredSourceFields.length + declaredSourceSuperFields.length];
            System.arraycopy(declaredSourceFields, 0, sourceFields, 0, declaredSourceFields.length);
            System.arraycopy(declaredSourceSuperFields, 0, sourceFields, declaredSourceFields.length, declaredSourceSuperFields.length);

            Field[] declaredTargetFields = targetClass.getDeclaredFields();
            Field[] declaredTargetSuperFields = targetClass.getSuperclass() != null
                    ? targetClass.getSuperclass().getDeclaredFields()
                    : new Field[0];

            Field[] targetFields = new Field[declaredTargetFields.length + declaredTargetSuperFields.length];
            System.arraycopy(declaredTargetFields, 0, targetFields, 0, declaredTargetFields.length);
            System.arraycopy(declaredTargetSuperFields, 0, targetFields, declaredTargetFields.length, declaredTargetSuperFields.length);

            for (Field sourceField : sourceFields) {
                sourceField.setAccessible(true);
                String name = sourceField.getName();

                for (Field targetField : targetFields) {
                    targetField.setAccessible(true);
                    if (targetField.getName().equals(name) && targetField.getType().equals(sourceField.getType())) {
                        targetField.set(target, sourceField.get(source));
                        break;
                    }
                }
            }
            return target;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidPassword(String input) {
        return input.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$");
    }

    public static Gson buildGsonAccountSession() {
        return new GsonBuilder()
                // GeoPoint
                .registerTypeAdapter(GeoPoint.class, (JsonSerializer<GeoPoint>) (src, typeOfSrc, context) -> {
                    if (src == null) return null;
                    JsonObject obj = new JsonObject();
                    obj.addProperty("lat", src.getLatitude());
                    obj.addProperty("lng", src.getLongitude());
                    return obj;
                })
                .registerTypeAdapter(GeoPoint.class, (JsonDeserializer<GeoPoint>) (json, typeOfT, context) -> {
                    if (json == null || !json.isJsonObject()) return null;
                    JsonObject obj = json.getAsJsonObject();
                    double lat = obj.get("lat").getAsDouble();
                    double lng = obj.get("lng").getAsDouble();
                    return new GeoPoint(lat, lng);
                })

                // DocumentReference
                .registerTypeAdapter(DocumentReference.class, (JsonSerializer<DocumentReference>) (src, typeOfSrc, context) -> {
                    if (src == null) return null;
                    return new JsonPrimitive(src.getId());
                })
                .registerTypeAdapter(DocumentReference.class, (JsonDeserializer<DocumentReference>) (json, typeOfT, context) -> {
                    if (json == null || !json.isJsonPrimitive()) return null;
                    String id = json.getAsString();
                    return FirebaseFirestore.getInstance()
                            .collection("user")
                            .document(id);
                })
                .create();
    }

    @SuppressLint("SimpleDateFormat")
    public static int isFutureDate(String dateInput) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date inputDate = dateFormat.parse(dateInput);
            Date currentDate = new Date();

            assert inputDate != null;

            Calendar calInput = Calendar.getInstance();
            calInput.setTime(inputDate);
            calInput.set(Calendar.HOUR_OF_DAY, 0);
            calInput.set(Calendar.MINUTE, 0);
            calInput.set(Calendar.SECOND, 0);
            calInput.set(Calendar.MILLISECOND, 0);

            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(currentDate);
            calCurrent.set(Calendar.HOUR_OF_DAY, 0);
            calCurrent.set(Calendar.MINUTE, 0);
            calCurrent.set(Calendar.SECOND, 0);
            calCurrent.set(Calendar.MILLISECOND, 0);

            if (calInput.after(calCurrent)) {
                return 1;  // dateInput > currentDate
            } else if (calInput.equals(calCurrent)) {
                return 2;  // dateInput == currentDate
            } else {
                return 0;  // dateInput < currentDate
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }



    @SuppressLint("SimpleDateFormat")
    public static boolean isAtLeast3HoursFromNow(String timeInput) {
        try {
           SimpleDateFormat format = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
            Date bookingDate = format.parse(timeInput);

            long currentTimeMillis = System.currentTimeMillis();
            assert bookingDate != null;
            long bookingTimeMillis = bookingDate.getTime();
            Log.i("TIME FORMAT", String.valueOf(bookingTimeMillis));
            long threeHoursInMillis = 3 * 60 * 60 * 1000;

            Log.i("TIME MINUS", String.valueOf(bookingTimeMillis - currentTimeMillis));
            return bookingTimeMillis - currentTimeMillis >= threeHoursInMillis;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
