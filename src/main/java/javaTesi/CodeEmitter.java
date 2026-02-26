package javaTesi;

public class CodeEmitter {

	public String emit(ASTNode node) {
	    if (node == null) {
	        System.out.println("FATAL DEBUG: Chiamata a emit(null)");
	        return "";
	    }
	    
	    if (node instanceof BlockNode block) {
            StringBuilder sb = new StringBuilder();
            for (ASTNode stmt : block.statements) {
                sb.append(emit(stmt)).append("\n");
            }
            return sb.toString();
        }

	    if (node instanceof BinaryOpNode binaryNode) {
	        String emittedRight = emit(binaryNode.right);

	        //System.out.println("DEBUG RIGHT EMITTED: " + emittedRight); 

	        return emit(binaryNode.left) + " " + binaryNode.operator + " " + emittedRight;
	    }
        if (node instanceof VariableNode variableNode) {
            // Emette il nome della variabile (es. B o C)
            return variableNode.name;
        }


        if (node instanceof AssignmentNode assignmentNode) {
            // Esempio: target := expression;
        	//System.out.println("DEBUG Emitter: Target=" + assignmentNode.target + ", Expression=" + assignmentNode.expression);
            return emit(assignmentNode.target) + " := " + emit(assignmentNode.expression) + ";";
        }
        
        if (node instanceof ParenthesizedExpressionNode) {
            return node.toString();
        }
        
        if (node instanceof IfStatementNode ifNode) {
            StringBuilder sb = new StringBuilder();
            sb.append("IF ").append(emit(ifNode.condition)).append(" THEN\n");
            
            for (ASTNode stmt : ifNode.thenStatements) {
                sb.append("    ").append(emit(stmt)).append("\n");
            }
            
            if (ifNode.elseStatements != null && !ifNode.elseStatements.isEmpty()) {
                sb.append("ELSE\n");
                for (ASTNode stmt : ifNode.elseStatements) {
                    sb.append("    ").append(emit(stmt)).append("\n");
                }
            }
            
            sb.append("END_IF;");
            return sb.toString();
        }
        
        if (node instanceof ForStatementNode forNode) {
            StringBuilder sb = new StringBuilder();
            sb.append("FOR ").append(emit(forNode.loopVariable))
              .append(" := ").append(emit(forNode.startValue))
              .append(" TO ").append(emit(forNode.endValue));
            
            if (forNode.stepValue != null) {
                sb.append(" BY ").append(emit(forNode.stepValue));
            }
            
            sb.append(" DO\n");
            
            for (ASTNode stmt : forNode.body) {
                sb.append("    ").append(emit(stmt)).append("\n");
            }
            
            sb.append("END_FOR;");
            return sb.toString();
        }
        
        // Nodi non gestiti
        return "/* ERR: Unsupported node type: " + node.getClass().getSimpleName() + " */";
    }
}