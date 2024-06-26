package dev.springharvest.expressions.ast;

import dev.springharvest.expressions.visitors.ExpressionVisitor;

public class ExpressionField implements Expression {
    private String fieldName;

    public ExpressionField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Returns the field name.
     * @return
     */
    @Override
    public String infix() {
        return fieldName;
    }

    /**
     * This method accepts a expression visitor and calls
     * the visit method on the visitor passing itself.
     * @param visitor
     *        ExpressionVisitor for traversing the expression tree.
     * @param data
     *        Buffer to hold the result of process node.
     * @param <T>
     * @return
     *        Returns the processed data.
     */
    @Override
    public <T> T accept(ExpressionVisitor visitor, T data) {
        return (T)visitor.visitExpressionField(this, data);
    }
}
