package javaTesi;

import java.util.List;

public class ForStatementNode extends ASTNode {
    public VariableNode loopVariable;
    public ExpressionNode startValue;
    public ExpressionNode endValue;
    public ExpressionNode stepValue;
    public List<ASTNode> body;

    public ForStatementNode(VariableNode loopVariable, ExpressionNode startValue, ExpressionNode endValue, ExpressionNode stepValue, List<ASTNode> body) {
        this.loopVariable = loopVariable;
        this.startValue = startValue;
        this.endValue = endValue;
        this.stepValue = stepValue;
        this.body = body;
    }

    @Override
    public ASTNode copy() {
        return new ForStatementNode(
            (VariableNode) loopVariable.copy(),
            (ExpressionNode) startValue.copy(),
            (ExpressionNode) endValue.copy(),
            (stepValue != null) ? (ExpressionNode) stepValue.copy() : null,
            body.stream().map(ASTNode::copy).toList()
        );
    }
}