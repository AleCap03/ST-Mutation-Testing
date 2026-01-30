package javaTesi;

public class VariableNode extends ExpressionNode {
    public String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public ASTNode copy() {
        VariableNode clone = new VariableNode(this.name);
        clone.id = this.id;
        return clone;
    }
}