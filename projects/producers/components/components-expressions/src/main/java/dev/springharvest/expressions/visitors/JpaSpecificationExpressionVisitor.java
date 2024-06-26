package dev.springharvest.expressions.visitors;

import dev.springharvest.expressions.ast.*;
import dev.springharvest.expressions.client.FieldValuePair;
import dev.springharvest.expressions.client.FieldValueTransformer;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * This class is responsible for traversing
 * the expression tree and generating a compound
 * JPA Specification from it with correct precedence
 * order.
 *
 * @author sjaiswal
 * @author NeroNemesis
 */

public class JpaSpecificationExpressionVisitor<T> implements ExpressionVisitor<Specification<T>>{

    private Map<String, String> fieldMap;
    private Deque<String> fieldStack;
    private FieldValueTransformer fieldValueTransformer;

    public JpaSpecificationExpressionVisitor(Map<String, String> fieldMap, FieldValueTransformer fieldValueTransformer) {
        this.fieldMap = fieldMap;
        this.fieldStack = new ArrayDeque<>();
        this.fieldValueTransformer = fieldValueTransformer;
    }

    /**
     * Returns the JPA Specification from
     * the expression tree.
     * @return
     * @param expression
     */
    @Override
    public Specification<T> expression(Expression expression) {
        Specification<T> specification = null;
        if (expression != null){
            specification = expression.accept(this, null);
        }
        return specification;
    }

    /*allows to perform distinct operations*/
    public Specification<T> distinct() {
        return (root, query, cb) -> {
            query.distinct(true);
            return null;
        };
    }

    /**
     * Handles the processing of compound
     * expression node.
     * @param compoundExpression
     *          Contains compound expression.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed node.
     */
    @Override
    public Specification<T> visitCompoundExpression(CompoundExpression compoundExpression, Specification<T> data) {
        Specification<T> result = null;
        switch (compoundExpression.getOperator()) {
            /* Logical operations.*/
            case AND:
                Specification<T> left = compoundExpression.getLeftOperand().accept(this, null);
                Specification<T> right = compoundExpression.getRightOperand().accept(this, null);
                result = Specification.where(left).and(right);

                break;

            case OR:
                left = compoundExpression.getLeftOperand().accept(this, null);
                right = compoundExpression.getRightOperand().accept(this, null);
                result = Specification.where(left).or(right);
                break;

            case DISTINCT:
                left = compoundExpression.getLeftOperand().accept(this, null);
                right = compoundExpression.getRightOperand().accept(this, null);
                result = Specification.where(distinct().and(Specification.where(left).and(right)));
                break;
        }
        return result;
    }

    /**
     * Handles the processing of binary
     * expression node.
     * @param binaryExpression
     *          Contains binary expression.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed node.
     */
    @Override
    public Specification<T> visitBinaryExpression(BinaryExpression binaryExpression, Specification<T> data) {

        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder
                    criteriaBuilder) {

                ExpressionValue<? extends Comparable> operandValue = (ExpressionValue<? extends Comparable>)binaryExpression.getRightOperand();
                Predicate predicate = null;
                String fieldName = mappedFieldName(binaryExpression.getLeftOperand().infix());
                operandValue = getTransformedValue(operandValue);
                Path path = root.get(fieldName);

                switch (binaryExpression.getOperator()) {
                    /* String operations.*/
                    case STARTS:
                        predicate = criteriaBuilder.like(path, operandValue.value() + "%");
                        break;

                    case STARTSIC:
                        predicate = criteriaBuilder.like(criteriaBuilder.lower(path), operandValue.value().toString().toLowerCase() + "%");
                        break;

                    case ENDS:
                        predicate = criteriaBuilder.like(path, "%" + operandValue.value());
                        break;

                    case ENDSIC:
                        predicate = criteriaBuilder.like(criteriaBuilder.lower(path), "%" + operandValue.value().toString().toLowerCase());
                        break;

                    case CONTAINS:
                        predicate = criteriaBuilder.like(path, "%" + operandValue.value() + "%");
                        break;

                    case CONTAINSIC:
                        predicate = criteriaBuilder.like(criteriaBuilder.lower(path), "%" + operandValue.value().toString().toLowerCase() + "%");
                        break;

                    case EQUALS:
                        predicate = criteriaBuilder.equal(path,  operandValue.value());
                        break;

                    case EQUALSIC:
                        predicate = criteriaBuilder.equal(criteriaBuilder.lower(path), operandValue.value().toString().toLowerCase());
                        break;

                    /* Numeric operations.*/
                    case LT:
                        predicate = criteriaBuilder.lessThan(path, operandValue.value());
                        break;

                    case LTE:
                        predicate = criteriaBuilder.lessThanOrEqualTo(path, operandValue.value());
                        break;

                    case EQ:
                        if (operandValue.value() == null) {
                            predicate = criteriaBuilder.isNull(path);
                        } else {
                            predicate = criteriaBuilder.equal(path, operandValue.value());
                        }
                        break;

                    case GT:
                        predicate = criteriaBuilder.greaterThan(path, operandValue.value());
                        break;

                    case GTE:
                        predicate = criteriaBuilder.greaterThanOrEqualTo(path, operandValue.value());
                        break;

                    case IN:
                        List<Comparable> expressionInValues = (List<Comparable>)operandValue.value();
                        predicate = criteriaBuilder.in(path).value(expressionInValues);
                        break;

                    case BETWEEN:
                        List<Comparable> expressionBetweenValues = (List<Comparable>)operandValue.value();
                        predicate = criteriaBuilder.between(path,expressionBetweenValues.get(0),expressionBetweenValues.get(1));
                        break;
                }
                return predicate;
            }
        };
    }

    /**
     * Handles the processing of unary
     * expression node.
     * @param unaryExpression
     *          Contains unary expression.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed node.
     */
    @Override
    public Specification<T> visitUnaryExpression(UnaryExpression unaryExpression, Specification<T> data) {
        Specification<T> left = unaryExpression.getLeftOperand().accept(this, null);
        return Specification.not(left);
    }

    /**
     * Handles the processing of expression
     * field node.
     * @param field
     *          Contains expression field.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed node.
     */
    @Override
    public Specification<T> visitExpressionField(ExpressionField field, Specification<T> data) {
        /* ExpressionField has been taken care in the Binary expression visitor. */
        return null;
    }

    /**
     * Handles the processing of expression
     * value node.
     * @param value
     *          Contains expression value.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed node.
     */
    @Override
    public Specification<T> visitExpressionValue(ExpressionValue<? extends Comparable> value, Specification<T> data) {
        /* ExpressionValue has been taken care in the Binary expression visitor. */
        return null;
    }

    private String mappedFieldName(String fieldName) {
        StringBuilder expressionBuilder = new StringBuilder();
        if (fieldMap != null && fieldMap.get(fieldName) != null) {
            expressionBuilder.append(fieldMap.get(fieldName));
        } else if (fieldValueTransformer != null && fieldValueTransformer.transformField(fieldName) != null) {
            expressionBuilder.append(fieldValueTransformer.transformField(fieldName));
            fieldStack.push(fieldName); //pushing the field for lookup while visiting value.
        } else {
            expressionBuilder.append(fieldName);
        }
        return expressionBuilder.toString();
    }

    private ExpressionValue getTransformedValue(ExpressionValue<? extends Comparable> value) {
        if (!fieldStack.isEmpty() && fieldValueTransformer != null) {
            String field  = fieldStack.pop(); // pop the field associated with this value.
            FieldValuePair fieldValuePair = fieldValueTransformer.transformValue(field,value.value());
            if (fieldValuePair != null && fieldValuePair.getValue() != null) {
                value = new ExpressionValue(fieldValuePair.getValue());
            }
        }
        return value;
    }
}
