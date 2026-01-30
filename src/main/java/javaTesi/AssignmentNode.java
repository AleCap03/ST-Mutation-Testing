package javaTesi;

public class AssignmentNode extends StatementNode {
    public ExpressionNode target;
    public ExpressionNode expression;

    public AssignmentNode(ExpressionNode target, ExpressionNode expression) {
        this.target = target;
        this.expression = expression;
    }
    
    @Override
    public String toString() {
        return "Assign(" + target.toString() + " := " + expression.toString() + ")";
    }
    
    @Override
    public ASTNode copy() {
        AssignmentNode clone = new AssignmentNode(
            (ExpressionNode) this.target.copy(), 
            (ExpressionNode) this.expression.copy()
        );
        clone.id = this.id;
        return clone;
    }
}