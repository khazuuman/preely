package com.example.preely.util;

import android.util.Log;

import com.example.preely.model.entities.BaseEntity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    /**
     * Safe mapping từ DocumentSnapshot sang Entity với custom handling cho DocumentReference List
     */
    public static <T extends BaseEntity> T safeMapToEntity(DocumentSnapshot document, Class<T> entityClass) {
        try {
            // Tạo instance mới
            T entity = entityClass.newInstance();

            // Set ID từ document
            entity.setId(document.getId());

            // Map các fields
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // Skip ID vì đã set ở trên
                if ("id".equals(fieldName)) {
                    continue;
                }

                Object value = document.get(fieldName);
                if (value != null) {
                    setFieldValue(entity, field, value);
                }
            }

            // Handle superclass fields (BaseEntity)
            Field[] superFields = entityClass.getSuperclass().getDeclaredFields();
            for (Field field : superFields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (!"id".equals(fieldName)) { // ID đã set
                    Object value = document.get(fieldName);
                    if (value != null) {
                        setFieldValue(entity, field, value);
                    }
                }
            }

            return entity;

        } catch (Exception e) {
            Log.e("DbUtil", "Error mapping document to entity: " + e.getMessage());
            // Fallback to automatic deserialization for simple cases
            try {
                return document.toObject(entityClass);
            } catch (Exception ex) {
                Log.e("DbUtil", "Fallback deserialization also failed: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * Set giá trị field với custom handling cho các kiểu dữ liệu đặc biệt
     */
    private static void setFieldValue(Object entity, Field field, Object value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        try {
            // Handle List<DocumentReference> - Convert to List<String> (document IDs)
            if (List.class.isAssignableFrom(fieldType)) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericType;
                    Type[] typeArgs = paramType.getActualTypeArguments();

                    if (typeArgs.length > 0 && typeArgs[0] == DocumentReference.class) {
                        // Convert List<DocumentReference> to List<DocumentReference>
                        List<?> sourceList = (List<?>) value;
                        List<DocumentReference> docRefList = new ArrayList<>();

                        for (Object item : sourceList) {
                            if (item instanceof DocumentReference) {
                                docRefList.add((DocumentReference) item);
                            }
                        }
                        field.set(entity, docRefList);
                        return;
                    }
                }
                // For other List types, set directly
                field.set(entity, value);
                return;
            }

            // Handle DocumentReference
            if (fieldType == DocumentReference.class && value instanceof DocumentReference) {
                field.set(entity, value);
                return;
            }

            // Handle GeoPoint
            if (fieldType == GeoPoint.class && value instanceof GeoPoint) {
                field.set(entity, value);
                return;
            }

            // Handle Timestamp
            if (fieldType == Timestamp.class && value instanceof Timestamp) {
                field.set(entity, value);
                return;
            }

            // Handle boolean
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                if (value instanceof Boolean) {
                    field.set(entity, value);
                } else {
                    field.set(entity, Boolean.parseBoolean(value.toString()));
                }
                return;
            }

            // Handle primitive và wrapper types
            if (fieldType == String.class) {
                field.set(entity, value.toString());
            } else if (fieldType == int.class || fieldType == Integer.class) {
                if (value instanceof Number) {
                    field.set(entity, ((Number) value).intValue());
                } else {
                    field.set(entity, Integer.parseInt(value.toString()));
                }
            } else if (fieldType == float.class || fieldType == Float.class) {
                if (value instanceof Number) {
                    field.set(entity, ((Number) value).floatValue());
                } else {
                    field.set(entity, Float.parseFloat(value.toString()));
                }
            } else if (fieldType == double.class || fieldType == Double.class) {
                if (value instanceof Number) {
                    field.set(entity, ((Number) value).doubleValue());
                } else {
                    field.set(entity, Double.parseDouble(value.toString()));
                }
            } else {
                // Default case
                field.set(entity, value);
            }

        } catch (Exception e) {
            Log.w("DbUtil", "Could not set field " + field.getName() + ": " + e.getMessage());
            // Skip field if cannot set
        }
    }

    /**
     * Legacy method - mapping với fallback
     */
    public static void mappingFieldFromDoc(DocumentSnapshot document, Object entity) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = document.get(fieldName);

                if (value != null) {
                    setFieldValue(entity, field, value);
                }
            }
        } catch (Exception e) {
            Log.e("DbUtil", "Error in legacy mapping: " + e.getMessage());
        }
    }

}
