package dev.springharvest.expressions.helpers;

import jakarta.persistence.AttributeOverride;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SharedHelper {

    /**
     * Checks if the specified field in the given class is a complex field.
     * <p>
     * A complex field is defined as a field that is not primitive, not a String,
     * not overridden, and not one of the following types: UUID, Date, LocalDate,
     * LocalDateTime, OffsetDateTime.
     *
     * @param clazz The class containing the field.
     * @param fieldName The name of the field to check.
     * @return true if the field is complex, false otherwise.
     * @throws NoSuchFieldException If the field is not found in the class.
     */
    public static boolean isComplexField(Class<?> clazz, Class<?> keyClass, String fieldName) throws NoSuchFieldException {
        try {
            Class<?> parentParamClass = null;
            Class<?> fieldType = getActualFieldType(clazz, fieldName);
            if (fieldType == Serializable.class) {
                parentParamClass = getRealTypeParameter(clazz, keyClass);
                fieldType = parentParamClass;
            }

            return !fieldType.isPrimitive()
                    && !fieldType.equals(String.class)
                    && !isOverriddenField(clazz, fieldName)
                    && !fieldType.equals(UUID.class)
                    && !fieldType.equals(Date.class)
                    && !fieldType.equals(LocalDate.class)
                    && !fieldType.equals(LocalDateTime.class)
                    && !fieldType.equals(OffsetDateTime.class);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(fieldName);
        }
    }

    /**
     * Checks if the specified field in the given class is overridden.
     * <p>
     * This method traverses the class hierarchy to check for the presence of the @AttributeOverride annotation
     * on the specified field. If the annotation is found, it indicates that the field is overridden.
     *
     * @param clazz The class containing the field.
     * @param fieldName The name of the field to check.
     * @return true if the field is overridden, false otherwise.
     */
    private static boolean isOverriddenField(Class<?> clazz, String fieldName) {
        // Check for @AttributeOverride
        while (clazz != null) {
            for (AttributeOverride override : clazz.getDeclaredAnnotationsByType(AttributeOverride.class)) {
                if (override.name().equals(fieldName)) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * Retrieves the type of the specified field in the given class.
     * <p>
     * This method attempts to get the actual field type of the specified field in the provided class.
     * If the field is not found, it throws a RuntimeException.
     *
     * @param clazz The class containing the field.
     * @param fieldName The name of the field whose type is to be retrieved.
     * @return The type of the specified field.
     * @throws RuntimeException If the field is not found in the class.
     */
    public static Class<?> getFieldType(Class<?> clazz, String fieldName) {
        try {
            return getActualFieldType(clazz, fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the actual type of the specified field in the given class.
     * <p>
     * This method traverses the class hierarchy to find the field with the specified name
     * and returns its type. If the field is not found, it throws a NoSuchFieldException.
     *
     * @param clazz The class containing the field.
     * @param fieldName The name of the field whose type is to be retrieved.
     * @return The type of the specified field.
     * @throws NoSuchFieldException If the field is not found in the class.
     */
    private static Class<?> getActualFieldType(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        // Traverse the class hierarchy to find the field
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field.getType();
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Retrieves the actual field name of the specified field in the given class.
     * <p>
     * This method traverses the class hierarchy to find the field with the specified name
     * and checks for the presence of the @AttributeOverride annotation. If the annotation
     * is found, it returns the overridden column name. If the field is not found, it returns
     * the original field name.
     *
     * @param clazz The class containing the field.
     * @param fieldName The name of the field whose actual name is to be retrieved.
     * @return The actual field name, or the original field name if no override is found.
     */
    public static String getActualFieldName(Class<?> clazz, String fieldName) {
        // Check for @AttributeOverride
        while (clazz != null) {
            for (AttributeOverride override : clazz.getDeclaredAnnotationsByType(AttributeOverride.class)) {
                if (override.name().equals(fieldName)) {
                    return override.column().name();
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fieldName;
    }

    /**
     * Converts the provided value to a `Date` if it is an instance of `LocalDate`, `LocalDateTime`, or `OffsetDateTime`.
     * <p>
     * This method checks the type of the provided value and converts it to a `Date` object if it is a date-related type.
     * If the value is `null` or not a date-related type, it returns the original value.
     *
     * @param value The value to be converted.
     * @return The converted `Date` object if the value is a date-related type, otherwise the original value.
     */
    public static Comparable convertIfDate(Comparable value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            value = Date.from(localDate.atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        } else if (value instanceof LocalDateTime localDateTime) {
            value = Date
                    .from(localDateTime.atZone(ZoneId.systemDefault())
                            .toInstant());
        } else if (value instanceof OffsetDateTime offsetDateTime) {
            value = Date
                    .from(offsetDateTime.toInstant());
        }
        return value;
    }

    public static Class<?> getRealTypeParameter(Class<?> clazz, Class<?> keyClass) {
        while (clazz != null) {
            // Check if the superclass is parameterized
            Type genericSuperclass = clazz.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                // Assume the first type argument is the one we are resolving
                if (typeArguments.length > 0) {
                    return resolveTypeArgument(typeArguments[0], keyClass);
                }
            }

            // Traverse the implemented interfaces
            for (Type iface : clazz.getGenericInterfaces()) {
                if (iface instanceof ParameterizedType parameterizedInterface) {
                    Type[] typeArguments = parameterizedInterface.getActualTypeArguments();

                    if (typeArguments.length > 0) {
                        return resolveTypeArgument(typeArguments[0], keyClass);
                    }
                }
            }

            // Move up the hierarchy
            clazz = clazz.getSuperclass();
        }

        throw new IllegalArgumentException("Could not resolve the type parameter.");
    }

    private static Class<?> resolveTypeArgument(Type typeArgument, Class<?> keyClass) {
        if (typeArgument instanceof Class<?>) {
            return (Class<?>) typeArgument;
        } else if (typeArgument instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) typeArgument).getRawType();
        } else {
            return keyClass;
        }
    }

    /**
     * Checks if the provided key represents a logical operator.
     *
     * @param key The key to be checked.
     * @return true if the key represents a logical operator, false otherwise.
     */
    public static boolean isLogicalOperator(String key) {
        try {
            Operator operator = Operator.getOperator(key);
            return Objects.equals(operator.getType(), "Logical");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Determines the root operator for combining predicates based on the provided filter map.
     *
     * @param filterMap A map containing filter criteria.
     * @return The root operator as a string (e.g., "and", "or"). Defaults to "and" if no logical operator is found.
     */
    public static String determineRootOperator(Map<String, Object> filterMap) {
        for (String key : filterMap.keySet()) {
            if (isLogicalOperator(key)) {
                return key.toLowerCase();
            }
        }
        return "and";
    }

//    /**
//     * Finds the primary key field of the specified entity class.
//     *
//     * @param entityClass The class of the entity for which the primary key field is to be found.
//     * @return An Optional containing the name of the primary key field, or an empty Optional if no primary key field is found.
//     */
//    public static Optional<String> findPrimaryKeyField(Class<?> entityClass) {
//        // Inspect superclass for @Id annotated field
//        Class<?> currentClass = entityClass;
//        while (currentClass != null) {
//            for (Field field : currentClass.getDeclaredFields()) {
//                if (field.isAnnotationPresent(Id.class)) {
//                    // Check for AttributeOverride in subclass
//                    AttributeOverride[] overrides = entityClass.getAnnotationsByType(AttributeOverride.class);
//                    for (AttributeOverride override : overrides) {
//                        if (override.name().equals(field.getName())) {
//                            return Optional.of(override.column().name());
//                        }
//                    }
//                    return Optional.of(field.getName());
//                }
//            }
//            currentClass = currentClass.getSuperclass();
//        }
//        return Optional.empty();
//    }
}
