package com.example.preely.util;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DbUtil {

    public static <T> T mappingFieldFromDoc(DocumentSnapshot doc, Class<T> cl, boolean hasSuperClass) {
        try {
            T instance = cl.getDeclaredConstructor().newInstance();
            List<Field> fields = new ArrayList<>();
            if (hasSuperClass && cl.getSuperclass() != null) {
                fields.addAll(Arrays.asList(cl.getSuperclass().getDeclaredFields()));
            }
            fields.addAll(Arrays.asList(cl.getDeclaredFields()));

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = null;
                if (field.getType() == Timestamp.class) {
                    value = doc.getTimestamp(fieldName);
                } else if (field.getType() == GeoPoint.class) {
                    value = doc.getGeoPoint(fieldName);
                } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                    value = doc.getBoolean(fieldName);
                } else if (field.getType() == BigDecimal.class) {
                    Double d = doc.getDouble(fieldName);
                    value = d != null ? new BigDecimal(d) : null;
                } else if (field.getType() == BigInteger.class) {
                    Long l = doc.getLong(fieldName);
                    value = l != null ? new BigInteger(l.toString()) : null;
                } else if (Number.class.isAssignableFrom(field.getType())) {
                    value = doc.getDouble(fieldName);
                } else {
                    value = doc.get(fieldName);
                }

                if (value != null) {
                    field.set(instance, value);
                }
            }
            if (hasSuperClass && cl.getSuperclass() != null) {
                Field idField = cl.getSuperclass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(instance, doc.getId());
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface OnInsertCallback {
        void onSuccess(DocumentReference documentReference);
        void onFailure(Exception e);
    }

    public interface OnInsertManyCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnDeleteCallBack {
        void onSuccess();
        void onFailure(Exception e);
    }
}
