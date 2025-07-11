package com.example.preely.util;

import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;

public class DataUtil {

    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static <T> T mapObj(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.newInstance();

            Field[] declaredSourceFields = source.getClass().getDeclaredFields();
            Field[] declaredSourceSuperFields = source.getClass().getSuperclass() != null
                    ?  targetClass.getSuperclass().getDeclaredFields()
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
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
