package dev.springharvest.expressions.ast;

import dev.springharvest.expressions.visitors.ExpressionVisitor;
/**
 * Base interface for an expression node
 * in the expression tree.
 *
 */

public interface Expression {
    /**
     * Returns the infix string representation of the
     * filter expression ast.
     * @return
     */
    public String infix();

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
    public<T> T accept(ExpressionVisitor visitor, T data);
}
