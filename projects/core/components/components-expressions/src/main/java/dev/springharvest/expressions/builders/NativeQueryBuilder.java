//package dev.springharvest.expressions.builders;
//
//import dev.springharvest.expressions.helpers.Operator;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.persistence.Query;
//import jakarta.persistence.Table;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import static dev.springharvest.expressions.helpers.SharedHelper.*;
//
//@Component
//public class NativeQueryBuilder implements IQueryBuilder {
//
//    @Autowired
//    private EntityManagerFactory entityManagerFactory;
//    private static final Map<String, String> tableNames = new HashMap<>();
//    private final StringBuilder sqlQuery;
//    private final StringBuilder whereClause;
//    private final Map<String, Object> parameters;
//    private final StringBuilder joinClause;
//    private final StringBuilder groupByClause;
//    private final StringBuilder orderByClause;
//
//    private int limit = -1;
//    private int offset = -1;
//
//    public NativeQueryBuilder() {
//        this.sqlQuery = new StringBuilder();
//        this.whereClause = new StringBuilder();
//        this.joinClause = new StringBuilder();
//        this.groupByClause = new StringBuilder();
//        this.orderByClause = new StringBuilder();
//        this.parameters = new java.util.HashMap<>();
//    }
//
//    // SELECT clause, including aggregate functions
//    public String createNativeQuerySelectClause(Class<?> rootClass, List<String> fields) {
//        StringBuilder selectClause = new StringBuilder("SELECT ");
//        if (fields.isEmpty()) {
//            selectClause.append("* ");
//        } else {
//            for (String field : fields) {
//                String [] pathParts = field.split("\\.");
//                Class<?> partClass = null;
//                for (int i = 1; i < pathParts.length; i++) {
//                    if (i == 1)
//                        partClass = getFieldType(rootClass, pathParts[i]);
//                    partClass = getFieldType(partClass, pathParts[i]);
//                    String tableName = getTableNameIfComplexFieldIsTable(rootClass, pathParts[i]);
//                    if (tableName != null) {
//                        registerTable(tableName);
//                    }
//
//                    if (i == pathParts.length - 1) {
//                        selectClause.append(tableNames.get(tableName)).append(".").append(pathParts[i]).append(", ");
//                    }
//                }
//            }
//        }
//
//        return selectClause.toString();
//    }
//
//    public String createNativeQueryFromClause(String rootTableName) {
//        return "FROM " + rootTableName + " " + tableNames.get(rootTableName) + " ";
//    }
//
//    // Joins
//    public NativeQueryBuilder innerJoin(String tableName, String onCondition) {
//        joinClause.append("INNER JOIN ").append(tableName).append(" ON ").append(onCondition).append(" ");
//        return this;
//    }
//
//    public NativeQueryBuilder leftJoin(String tableName, String onCondition) {
//        joinClause.append("LEFT JOIN ").append(tableName).append(" ON ").append(onCondition).append(" ");
//        return this;
//    }
//
//    public NativeQueryBuilder rightJoin(String tableName, String onCondition) {
//        joinClause.append("RIGHT JOIN ").append(tableName).append(" ON ").append(onCondition).append(" ");
//        return this;
//    }
//
//    public NativeQueryBuilder fullOuterJoin(String tableName, String onCondition) {
//        joinClause.append("FULL OUTER JOIN ").append(tableName).append(" ON ").append(onCondition).append(" ");
//        return this;
//    }
//
//    // WHERE clause
//    private static String createNativeQueryWhereClause(Map<String, Object> filterMap, Class<?> rootClass, Class<?> keyClass, String parentPath, String rootOperator) {
//        StringBuilder whereClause = new StringBuilder();
//        List<String> conditions = new ArrayList<>();
//
//        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
//            String key = entry.getKey();
//            Object value = entry.getValue();
//
//            if (isLogicalOperator(key)) {
//                // Logical operator (e.g., AND, OR)
//                String subCondition = createLogicalOperatorWhereClause(key, (List<Map<String, Object>>) value, rootClass, keyClass, parentPath);
//                conditions.add(subCondition);
//            } else {
//                try {
//                    if (isComplexField(rootClass, keyClass, key)) {
//                        // Nested field
//                        String newPath = parentPath.isEmpty() ? key : parentPath + "." + key;
//                        String tableName = getTableNameIfComplexFieldIsTable(rootClass, key);
//                        if (tableName != null) {
//                            registerTable(tableName);
//                        }
//                        Class<?> nestedClass = getFieldType(rootClass, key);
//                        String subOperator = determineRootOperator((Map<String, Object>) value);
//                        String subCondition = createNativeQueryWhereClause((Map<String, Object>) value, nestedClass, keyClass, newPath, subOperator);
//                        conditions.add(subCondition);
//                    } else {
//                        // Simple field
//                        String condition = createSimpleFieldCondition(key, value, parentPath, rootClass);
//                        conditions.add(condition);
//                    }
//                } catch (NoSuchFieldException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        if (!conditions.isEmpty()) {
//            whereClause.append("(").append(String.join(" " + rootOperator + " ", conditions)).append(")");
//        }
//
//        return whereClause.toString();
//    }
//
//    private static String createLogicalOperatorWhereClause(String operator, List<Map<String, Object>> value, Class<?> rootClass, Class<?> keyClass, String parentPath) {
//        List<String> subConditions = new ArrayList<>();
//
//        for (Map<String, Object> subFilter : value) {
//            String subOperator = determineRootOperator(subFilter);
//            String subCondition = createNativeQueryWhereClause(subFilter, rootClass, keyClass, parentPath, subOperator);
//            subConditions.add(subCondition);
//        }
//
//        return "(" + String.join(" " + operator + " ", subConditions) + ")";
//    }
//
//    private static String createSimpleFieldCondition(String key, Object value, String parentPath, Class<?> rootClass) {
//        // Trim the parentPath to remove unnecessary leading parts
//        String dbPath = extractDbPath(rootClass, parentPath, key);
//
//        // Generate the condition with a parameterized placeholder
//        String placeholder = dbPath.replace(".", "_"); // Example: pet.name -> pet_name
//        return createPredicate(dbPath, value, getFieldType(rootClass, key));
//    }
//
//    /**
//     * Creates an SQL fragment based on the provided value and field type.
//     *
//     * @param column The column name in the database.
//     * @param value The value to be matched against the column.
//     * @param fieldType The type of the field.
//     * @return An SQL fragment representing the filter criteria for the column.
//     */
//    private static String createPredicate(String column, Object value, Class<?> fieldType) {
//        if (value instanceof Map) {
//            Map<String, Object> valueMap = (Map<String, Object>) value;
//            StringBuilder sqlCondition = new StringBuilder();
//
//            /* String operations */
//            if (valueMap.containsKey(Operator.STARTS.getName())) {
//                sqlCondition.append(column).append(" LIKE '").append(valueMap.get(Operator.STARTS.getName())).append("%'");
//            } else if (valueMap.containsKey(Operator.STARTSIC.getName())) {
//                sqlCondition.append("LOWER(").append(column).append(") LIKE '").append(((String) valueMap.get(Operator.STARTSIC.getName())).toLowerCase()).append("%'");
//            } else if (valueMap.containsKey(Operator.ENDS.getName())) {
//                sqlCondition.append(column).append(" LIKE '%").append(valueMap.get(Operator.ENDS.getName())).append("'");
//            } else if (valueMap.containsKey(Operator.ENDSIC.getName())) {
//                sqlCondition.append("LOWER(").append(column).append(") LIKE '%").append(((String) valueMap.get(Operator.ENDSIC.getName())).toLowerCase()).append("'");
//            } else if (valueMap.containsKey(Operator.CONTAINS.getName())) {
//                sqlCondition.append(column).append(" LIKE '%").append(valueMap.get(Operator.CONTAINS.getName())).append("%'");
//            } else if (valueMap.containsKey(Operator.CONTAINSIC.getName())) {
//                sqlCondition.append("LOWER(").append(column).append(") LIKE '%").append(((String) valueMap.get(Operator.CONTAINSIC.getName())).toLowerCase()).append("%'");
//            } else if (valueMap.containsKey(Operator.EQUALS.getName())) {
//                sqlCondition.append(column).append(" = '").append(valueMap.get(Operator.EQUALS.getName())).append("'");
//            } else if (valueMap.containsKey(Operator.EQUALSIC.getName())) {
//                sqlCondition.append("LOWER(").append(column).append(") = '").append(((String) valueMap.get(Operator.EQUALSIC.getName())).toLowerCase()).append("'");
//            }
//            /* Numeric operations */
//            else if (valueMap.containsKey(Operator.LT.getName())) {
//                sqlCondition.append(column).append(" < ").append(valueMap.get(Operator.LT.getName()));
//            } else if (valueMap.containsKey(Operator.LTE.getName())) {
//                sqlCondition.append(column).append(" <= ").append(valueMap.get(Operator.LTE.getName()));
//            } else if (valueMap.containsKey(Operator.EQ.getName())) {
//                Object eqValue = valueMap.get(Operator.EQ.getName());
//                if (eqValue == null) {
//                    sqlCondition.append(column).append(" IS NULL");
//                } else {
//                    sqlCondition.append(column).append(" = ").append(formatValue(eqValue, fieldType));
//                }
//            } else if (valueMap.containsKey(Operator.GT.getName())) {
//                sqlCondition.append(column).append(" > ").append(valueMap.get(Operator.GT.getName()));
//            } else if (valueMap.containsKey(Operator.GTE.getName())) {
//                sqlCondition.append(column).append(" >= ").append(valueMap.get(Operator.GTE.getName()));
//            } else if (valueMap.containsKey(Operator.IN.getName())) {
//                List<?> inValues = (List<?>) valueMap.get(Operator.IN.getName());
//                sqlCondition.append(column).append(" IN (").append(formatInClause(inValues, fieldType)).append(")");
//            } else if (valueMap.containsKey(Operator.BETWEEN.getName())) {
//                List<?> betweenValues = (List<?>) valueMap.get(Operator.BETWEEN.getName());
//                sqlCondition.append(column).append(" BETWEEN ")
//                        .append(formatValue(betweenValues.get(0), fieldType)).append(" AND ")
//                        .append(formatValue(betweenValues.get(1), fieldType));
//            } else {
//                sqlCondition.append(column).append(" = ").append(formatValue(value, fieldType));
//            }
//
//            return sqlCondition.toString();
//        }
//        return column + " = " + formatValue(value, fieldType);
//    }
//
//
//    private static String extractDbPath(Class<?> rootClass,String parentPath, String key) {
//        if (parentPath == null || parentPath.isEmpty()) {
//            return key; // No parent path, return the key directly
//        }
//
//        // Split the path and get the last table in the hierarchy
//        String[] pathParts = parentPath.split("\\.");
//        String lastTable = pathParts[pathParts.length - 1];
//
//        // Combine the last table with the key to form the database path
//        for (int i = pathParts.length - 1; i >= 0; i--) {
//            Class<?> type = getFieldType(rootClass, pathParts[i]);
//            String tableName = getTableName(type);
//            if (tableName != null) {
//                lastTable = tableName;
//                break;
//            }
//        }
//        if (!tableNames.containsKey(lastTable))
//            registerTable(lastTable);
//        return tableNames.get(lastTable) + "." + key;
//    }
//
//    private static String formatValue(Object value, Class<?> fieldType) {
//        if (value == null) {
//            return "NULL";
//        }
//        if (fieldType == String.class) {
//            return "'" + value.toString().replace("'", "''") + "'";
//        }
//        if (fieldType == java.util.Date.class || fieldType == java.time.LocalDate.class || fieldType == java.time.LocalDateTime.class) {
//            return "'" + value.toString() + "'"; // Adjust date formatting as needed
//        }
//        return value.toString(); // For numbers or other types
//    }
//
//    private static String formatInClause(List<?> values, Class<?> fieldType) {
//        List<String> formattedValues = new ArrayList<>();
//        for (Object value : values) {
//            formattedValues.add(formatValue(value, fieldType));
//        }
//        return String.join(", ", formattedValues);
//    }
//
//    public static String getTableNameAndThrow(Class<?> rootClass) {
//        String tableName = getTableName(rootClass);
//        if (tableName != null) {
//            return tableName;
//        }
//        throw new IllegalArgumentException("No @Table annotation found on class " + rootClass.getName());
//    }
//
//    public static String getTableName(Class<?> rootClass) {
//        // Check if the @Table annotation is present
//        if (rootClass.isAnnotationPresent(Table.class)) {
//            // Get the @Table annotation
//            Table tableAnnotation = rootClass.getAnnotation(Table.class);
//            // Return the name attribute
//            return tableAnnotation.name();
//        }
//        return null;
//    }
//
//    public static void registerTable(String tableName) {
//        String simplifiedTableName = tableName.charAt(0) + "";
//        simplifiedTableName = simplifiedTableName.toLowerCase();
//        for (int i = 0 ; i < 1000; i++)
//        {
//            if (!tableNames.containsKey(simplifiedTableName + "_" + i)) {
//                tableNames.put(tableName, simplifiedTableName);
//                break;
//            }
//        }
//    }
//
//    public static String getTableNameIfComplexFieldIsTable(Class<?> rootClass, String field) {
//        Class<?> fieldType = getFieldType(rootClass, field);
//        return getTableName(fieldType);
//    }
//
//    public NativeQueryBuilder orWhere(String condition) {
//        if (whereClause.length() == 0) {
//            whereClause.append("WHERE ").append(condition).append(" ");
//        } else {
//            whereClause.append("OR ").append(condition).append(" ");
//        }
//        return this;
//    }
//
//    // GROUP BY clause
//    public NativeQueryBuilder groupBy(String... columns) {
//        if (groupByClause.length() == 0) {
//            groupByClause.append("GROUP BY ").append(String.join(", ", columns)).append(" ");
//        }
//        return this;
//    }
//
//    // ORDER BY clause
//    public NativeQueryBuilder orderBy(String column, boolean ascending) {
//        if (orderByClause.length() == 0) {
//            orderByClause.append("ORDER BY ").append(column).append(ascending ? " ASC " : " DESC ");
//        } else {
//            orderByClause.append(", ").append(column).append(ascending ? " ASC " : " DESC ");
//        }
//        return this;
//    }
//
//    // Pagination
//    public NativeQueryBuilder limit(int limit) {
//        this.limit = limit;
//        return this;
//    }
//
//    public NativeQueryBuilder offset(int offset) {
//        this.offset = offset;
//        return this;
//    }
//
//    // Set parameters for the query
//    public NativeQueryBuilder setParameter(String name, Object value) {
//        parameters.put(name, value);
//        return this;
//    }
//
//    // Build the query
//    public <T, K> Query build(Class<T> rootClass, Class<K> keyClass, List<String> fields, Map<String, Object> filterMap) {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            try {
//                fields = cleanFields(rootClass, keyClass, fields);
//            } catch (NoSuchFieldException e) {
//                throw new RuntimeException("Field not found.\n For each field make sure the field's name exists in the class corresponding to your schema definition and follows this format 'Schema type name' + '.' + 'fieldName.\n e.g: 'Book.title', 'Book.author.pet.name'.");
//            }
//            String rootOperator = determineRootOperator(filterMap);
//            String rootTableName = getTableNameAndThrow(rootClass);
//            registerTable(rootTableName);
//            StringBuilder fullQuery = new StringBuilder(sqlQuery);
//            if (joinClause.length() > 0) {
//                fullQuery.append(joinClause);
//            }
//            if (!filterMap.isEmpty()) {
//                createNativeQueryWhereClause(filterMap, rootClass, keyClass, rootTableName, rootOperator);
//            }
//            if (groupByClause.length() > 0) {
//                fullQuery.append(groupByClause);
//            }
//            if (orderByClause.length() > 0) {
//                fullQuery.append(orderByClause);
//            }
//            if (limit > -1) {
//                fullQuery.append("LIMIT ").append(limit).append(" ");
//            }
//            if (offset > -1) {
//                fullQuery.append("OFFSET ").append(offset).append(" ");
//            }
//
//            Query query = entityManager.createNativeQuery(fullQuery.toString());
//            parameters.forEach(query::setParameter);
//            return query;
//        } finally {
//            if (entityManager != null && entityManager.isOpen()) {
//                entityManager.close();
//            }
//        }
//    }
//
//    // Execute query and return results
//    public List<Object> getResultList() {
//        return build().getResultList();
//    }
//
//    public Object getSingleResult() {
//        return build().getSingleResult();
//    }
//
//    @Override
//    public List<String> cleanFields(Class<?> rootClass, Class<?> keyClass, List<String> fields) throws NoSuchFieldException {
//        // Create a copy of the fields list to avoid concurrent modification issues
//        if (fields == null)
//            return new ArrayList<>();
//
//        List<String> cleanedFields = new ArrayList<>(fields);
//        String rootTableName = getTableNameAndThrow(rootClass);
//        for (int i = 0; i < cleanedFields.size(); i++) {
//            String[] temp = cleanedFields.get(i).split("\\.");
//            temp[0] = rootTableName;  // Replace the root name with the actual table name
//            Class<?> currentClass = rootClass;  // Reset rootClass for each field entry
//
//            // Start from the second element (index 1)
//            for (int j = 1; j < temp.length; j++) {
//                String fieldName = temp[j];
//
//                if (isComplexField(currentClass, keyClass, fieldName)) {
//                    // Mark as complex if we encounter a complex field
//                    // If we are at the last element and it's complex, remove the entry
//                    if (j + 1 == temp.length) {
//                        cleanedFields.remove(i);
//                        i--;  // Adjust the index after removal
//                        break;
//                    } else {
//                        // Update the current class to the complex field's type for further checks
//                        currentClass = getFieldType(currentClass, fieldName);
//                    }
//                }
//            }
//        }
//
//        return cleanedFields;
//    }
//}