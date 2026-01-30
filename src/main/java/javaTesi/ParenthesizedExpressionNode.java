package javaTesi;

public class ParenthesizedExpressionNode extends ExpressionNode {
    private final ExpressionNode expression;

    public ParenthesizedExpressionNode(ExpressionNode expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        // Garantisce che le parentesi appaiano nel file
        return "(" + expression.toString() + ")";
    }

    @Override
    public ASTNode copy() {
        // Crea una copia del contenuto delle parentesi
        return new ParenthesizedExpressionNode((ExpressionNode) expression.copy());
    }
    
    public ExpressionNode getExpression() { return expression; }
}