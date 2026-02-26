package javaTesi;

import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {
    public List<ASTNode> statements = new ArrayList<>();

    public void addStatement(ASTNode node) {
        this.statements.add(node);
    }

    @Override
    public ASTNode copy() {
        BlockNode copy = new BlockNode();
        copy.id = this.id; 
        for (ASTNode stmt : this.statements) {
            copy.addStatement(stmt.copy());
        }
        return copy;
    }
}