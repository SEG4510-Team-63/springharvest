package dev.springharvest.expressions.ast;

/**
 * Enum of operators for supporting relational
 * and logical expressions.
 *
 * @author sjaiswal
 * @author NeroNemesis
 */
public enum Operator {

    /* Logical Operators */
    AND("and", "Logical",  Kind.COMPOUND),
    OR ("or", "Logical",  Kind.COMPOUND),
    NOT ("not", "Logical", Kind.UNARY),
    DISTINCT("distinct", "Logical", Kind.COMPOUND),

    /* String Operators */
    EQUALS("equals", "String", Kind.BINARY),
    //JPA orperator
    EQUALSIC("equalsic", "String", Kind.BINARY),
    CONTAINS("contains", "String",  Kind.BINARY),
    //JPA orperator
    CONTAINSIC("containsic", "String", Kind.BINARY),
    STARTS("starts", "String",  Kind.BINARY),
    //JPA orperator
    STARTSIC("startsic", "String",  Kind.BINARY),
    ENDS("ends", "String",  Kind.BINARY),
    //JPA orperator
    ENDSIC("endsic", "String",  Kind.BINARY),

    /* Numeric Operators */
    EQ("eq", "Numeric", Kind.BINARY),
    GT("gt", "Numeric",  Kind.BINARY),
    GTE("gte", "Numeric",  Kind.BINARY),
    LT("lt", "Numeric",  Kind.BINARY),
    LTE("lte", "Numeric",  Kind.BINARY),

    /* Range Operators */
    IN("in", "String|Numeric",  Kind.BINARY),
    BETWEEN("between","DateTime|Numeric", Kind.BINARY);

    /* Others */

    /**
     * Enum of operator kind.
     */
    enum Kind {
        COMPOUND,
        BINARY,
        UNARY;
    }

    private String name;
    private String type;
    private Kind kind;

    Operator(String name, String type, Kind kind) {
        this.type = type;
        this.name = name;
        this.kind = kind;
    }

    /**
     * Returns the Operator enum based
     * on operator name.
     * @param name
     * @return
     */
    public static Operator getOperator(String name) {
        for (Operator operator : Operator.values()) {
            if (operator.getName().equals(name)) {
                return operator;
            }
        }
        throw new IllegalArgumentException(String.valueOf(name));
    }

    /**
     * Returns Operator kind based
     * on operator name.
     * @param name
     * @return
     */
    public static String getOperatorKind(String name) {
        return getOperator(name).getKind().name();
    }

    /**
     * Returns operator name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns operator kind.
     * @return
     */
    public Kind getKind() {
        return kind;
    }
}
