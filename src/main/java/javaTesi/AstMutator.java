package javaTesi;

public class AstMutator {

    protected String mutaOperatore(String op) {
        return switch (op) {
            case "+" -> "-";
            case "-" -> "+";
            case "*" -> "/";
            case "/" -> "*";
            
            case ">" -> ">="; 
            case "<" -> "<=";
            case ">=" -> "<";
            case "<=" -> ">";
            case "=" -> "<>"; 
            case "<>" -> "=";
            
            case "AND" -> "OR";
            case "OR" -> "AND";
            
            default -> op;
        };
    }

    // Metodo ricorsivo che visita i nodi
    public ASTNode visit(ASTNode node) {
        if (node == null) {
            return null;
        }

        // 1 Caso: Istruzione di Assegnazione 
        if (node instanceof AssignmentNode assignmentNode) {
            // Visita ricorsivamente l'espressione, ma non il target 
            assignmentNode.expression = (ExpressionNode) visit(assignmentNode.expression);
            return assignmentNode;
        }

        // 2 Caso: Espressione Binaria (Mutazione) 
        if (node instanceof BinaryOpNode binaryNode) {
            // Esegue la mutazione sul nodo corrente
            binaryNode.operator = mutaOperatore(binaryNode.operator);
            
            // Visita ricorsivamente gli operandi (per espressioni annidate)
            binaryNode.left = (ExpressionNode) visit(binaryNode.left);
            binaryNode.right = (ExpressionNode) visit(binaryNode.right);
            
            return binaryNode;
        }
        
        // 3 Caso: Variabile (Foglia)
        if (node instanceof VariableNode) {
            // Non fa nulla, è una foglia
            return node;
        }
        
        // 4 Caso: Parentesi
        if (node instanceof ParenthesizedExpressionNode parenNode) {
            // Entriamo nelle parentesi e visitiamo quello che c'è dentro
            ExpressionNode innerMutated = (ExpressionNode) visit(parenNode.getExpression());
            return new ParenthesizedExpressionNode(innerMutated);
        }
        
        // 5 Caso: Blocco IF
        if (node instanceof IfStatementNode ifNode) {
            // Muta la condizione
            ifNode.condition = (ExpressionNode) visit(ifNode.condition);

            // Muta le istruzioni nel THEN
            for (int i = 0; i < ifNode.thenStatements.size(); i++) {
                ifNode.thenStatements.set(i, visit(ifNode.thenStatements.get(i)));
            }

            // Muta le istruzioni nell'ELSE (se esiste)
            if (ifNode.elseStatements != null) {
                for (int i = 0; i < ifNode.elseStatements.size(); i++) {
                    ifNode.elseStatements.set(i, visit(ifNode.elseStatements.get(i)));
                }
            }
            return ifNode;
        }
        
        // 6 Caso: Ciclo FOR
        if (node instanceof ForStatementNode forNode) {
            // Muta i valori del ciclo (inizio, fine, passo) se sono espressioni
            forNode.startValue = (ExpressionNode) visit(forNode.startValue);
            forNode.endValue = (ExpressionNode) visit(forNode.endValue);
            if (forNode.stepValue != null) {
                forNode.stepValue = (ExpressionNode) visit(forNode.stepValue);
            }

            // Muta tutte le istruzioni dentro il corpo del ciclo
            for (int i = 0; i < forNode.body.size(); i++) {
                forNode.body.set(i, visit(forNode.body.get(i)));
            }
            return forNode;
        }

        return node;
    }
    
}