package javaTesi;

import java.util.List;
import java.util.stream.Collectors;

public class IfStatementNode extends ASTNode {
    public ExpressionNode condition;
    public List<ASTNode> thenStatements;
    public List<ASTNode> elseStatements;

    public IfStatementNode(ExpressionNode condition, List<ASTNode> thenStatements, List<ASTNode> elseStatements) {
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseStatements = elseStatements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IF ").append(condition.toString()).append(" THEN\n");
        
        for (ASTNode stmt : thenStatements) {
            sb.append("    ").append(stmt.toString()).append("\n");
        }
        
        if (elseStatements != null && !elseStatements.isEmpty()) {
            sb.append("ELSE\n");
            for (ASTNode stmt : elseStatements) {
                sb.append("    ").append(stmt.toString()).append("\n");
            }
        }
        
        sb.append("END_IF;");
        return sb.toString();
    }

    @Override
    public ASTNode copy() {
        // Copia della condizione
        ExpressionNode conditionCopy = (ExpressionNode) this.condition.copy();
        
        // Copia della lista THEN
        List<ASTNode> thenCopy = this.thenStatements.stream()
                                    .map(ASTNode::copy)
                                    .collect(Collectors.toList());
        
        // Copia della lista ELSE (se esiste)
        List<ASTNode> elseCopy = null;
        if (this.elseStatements != null) {
            elseCopy = this.elseStatements.stream()
                                    .map(ASTNode::copy)
                                    .collect(Collectors.toList());
        }
        
        return new IfStatementNode(conditionCopy, thenCopy, elseCopy);
    }
}