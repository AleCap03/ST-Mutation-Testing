package javaTesi;

public class BinaryOpNode extends ExpressionNode {
    public ExpressionNode left;
    public String operator; // Questo campo sarà modificato dal Mutator
    public ExpressionNode right;

    public BinaryOpNode(ExpressionNode left, String operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    @Override
    public String toString() {
        return left.toString() + " " + operator + " " + right.toString();
    }
    
    @Override
    public ASTNode copy() {
        // Crea l'oggetto clone
        BinaryOpNode clone = new BinaryOpNode(
            (ExpressionNode) this.left.copy(), 
            this.operator, 
            (ExpressionNode) this.right.copy()
        );
        // Copia l'ID originale nel clone
        clone.id = this.id; 
        // Restituisce il clone
        return clone;
    }
}