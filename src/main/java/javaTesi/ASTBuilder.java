package javaTesi;

import com.github.vlsi.iec61131.parser.IEC61131BaseVisitor;
import com.github.vlsi.iec61131.parser.IEC61131Parser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

public class ASTBuilder extends IEC61131BaseVisitor<ASTNode> {
    
    private final TokenStream tokens;

    public ASTBuilder(TokenStream tokens) {
        this.tokens = tokens;
    }
    
    // OR, AND
    @Override
    public ASTNode visitLogicalExpression(IEC61131Parser.LogicalExpressionContext ctx) {
        // Se ci sono 3 o più figli (es: A AND B), è un BinaryOpNode
        if (ctx.getChildCount() >= 3) {
            return buildBinaryOpNode(ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
        }
        // Altrimenti scende verso i confronti
        return visit(ctx.getChild(0));
    }

    @Override
    public ASTNode visitExpression(IEC61131Parser.ExpressionContext ctx) {
        return visit(ctx.getChild(0)); 
    }
    
    // CONFRONTI: >, <, =, >=, <=, <>
    @Override
    public ASTNode visitComparisonExpression(IEC61131Parser.ComparisonExpressionContext ctx) {
        // Se ci sono 3 figli (es: A > B), è un BinaryOpNode
        if (ctx.getChildCount() >= 3) {
            return buildBinaryOpNode(ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
        }
        // Se c'è solo 1 figlio, scendiamo nella gerarchia verso le somme (AdditiveExpression)
        if (ctx.getChildCount() > 0) {
            return visit(ctx.getChild(0));
        }
        
        return defaultResult();
    }
    
    // SOMME E SOTTRAZIONI: +, -
    @Override
    public ASTNode visitAdditiveExpression(IEC61131Parser.AdditiveExpressionContext ctx) {
        // Se ctx ha 3 o più figli, è una vera operazione
        if (ctx.getChildCount() >= 3) {
            return buildBinaryOpNode(ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
        }

        if (ctx.getChildCount() > 0) {
            return visit(ctx.getChild(0));
        }
        return defaultResult();
    }
    
    // MOLTIPLICAZIONI E DIVISIONI: *, /
    @Override
    public ASTNode visitMultiplicativeExpression(IEC61131Parser.MultiplicativeExpressionContext ctx) {
        if (ctx.getChildCount() >= 3) {
            return buildBinaryOpNode(ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
        }
        
        if (ctx.getChildCount() > 0) {
            return visit(ctx.getChild(0));
        }
        return defaultResult();
    }
    
    
    private ASTNode buildBinaryOpNode(ParseTree leftTree, ParseTree opTree, ParseTree rightTree) {
        
        // LEFT OPERAND
        ExpressionNode left = (ExpressionNode) visit(leftTree);
        if (left == null) {
            left = new VariableNode(leftTree.getText());
        }

        // OPERATOR
        if (opTree == null || !(opTree instanceof TerminalNode)) {
             throw new IllegalStateException("ASTBuilder: Operatore binario non trovato al posto previsto.");
        }
        String operator = opTree.getText();
        
        // RIGHT OPERAND
        ExpressionNode right = (ExpressionNode) visit(rightTree);
        if (right == null) {
             right = new VariableNode(rightTree.getText());
        }
        return new BinaryOpNode(left, operator, right);
    }
    
    // Gestisce variabili semplici (es. B, C)
    @Override
    public ASTNode visitVariableExpression(IEC61131Parser.VariableExpressionContext ctx) {
        ParseTree child = ctx.getChild(0);
        
        // Se il child è presente, lo visitiamo. Il metodo visitVariable_access si occuperà di creare il VariableNode.
        if (child != null) {
            return visit(child); 
        }
        
        return null;
    }
    
    @Override
    public ASTNode visitIf_statement(IEC61131Parser.If_statementContext ctx) {
        // Estrae la condizione
        ExpressionNode condition = (ExpressionNode) visit(ctx.expression());

        // Estrae le istruzioni del blocco THEN
        List<ASTNode> thenStatements = new ArrayList<>();
        
        if (ctx.statement_list() != null) {
            for (IEC61131Parser.StatementContext stmtCtx : ctx.statement_list().statement()) {
                thenStatements.add(visit(stmtCtx));
            }
        }

        // Estrae le istruzioni del blocco ELSE (se presente)
        List<ASTNode> elseStatements = new ArrayList<>();
        if (ctx.else_clause() != null && ctx.else_clause().statement_list() != null) {
            for (IEC61131Parser.StatementContext stmtCtx : ctx.else_clause().statement_list().statement()) {
                elseStatements.add(visit(stmtCtx));
            }
        }
        return new IfStatementNode(condition, thenStatements, elseStatements);
    }
    
    @Override
    public ASTNode visitFor_statement(IEC61131Parser.For_statementContext ctx) {
        // Estrae la variabile di controllo (es. "i")
        VariableNode loopVar = new VariableNode(ctx.variable_access().getText());

        // Valore iniziale
        ExpressionNode start = (ExpressionNode) visit(ctx.expression(0));

        // Valore finale
        ExpressionNode end = (ExpressionNode) visit(ctx.expression(1));

        // Incremento
        ExpressionNode step = null;
      
        if (ctx.expression().size() > 2) {
            step = (ExpressionNode) visit(ctx.expression(2));
        }

        // Corpo del ciclo
        List<ASTNode> body = new ArrayList<>();
        if (ctx.statement_list() != null) {
            for (IEC61131Parser.StatementContext stmtCtx : ctx.statement_list().statement()) {
                body.add(visit(stmtCtx));
            }
        }
        return new ForStatementNode(loopVar, start, end, step, body);
    }

    // Gestisce espressioni tra parentesi
    @Override
    public ASTNode visitParenthesizedExpression(IEC61131Parser.ParenthesizedExpressionContext ctx) {
        // Invece di restituire solo l'interno, lo avvolge nel nuovo nodo
        ExpressionNode inner = (ExpressionNode) visit(ctx.getChild(1));
        return new ParenthesizedExpressionNode(inner);
    }
    
    @Override
    public ASTNode visitVariable_access(IEC61131Parser.Variable_accessContext ctx) {
        // La variabile è l'ID
        if (ctx.ID() != null) {
            return new VariableNode(ctx.ID().getText()); 
        }
        if (ctx.getChildCount() > 0) {
            return visit(ctx.getChild(0));
        }
        return null;
    }
 
    // ES: A := B + C; divide in target ed espressione
    @Override
    public ASTNode visitAssignment_statement(IEC61131Parser.Assignment_statementContext ctx) {
        ParseTree targetTree = ctx.getChild(0); 
        ExpressionNode target = (ExpressionNode) visit(targetTree); 

        ExpressionNode expression = (ExpressionNode) visit(ctx.getChild(2)); 

        if (target == null) {
            throw new IllegalStateException("ASTBuilder: Impossibile mappare il target di assegnazione.");
        }

        return new AssignmentNode(target, expression);
    }

    @Override
    public ASTNode visitStatement(IEC61131Parser.StatementContext ctx) {
        return visit(ctx.getChild(0)); 
    }

    @Override
    public ASTNode visitLiteralExpression(IEC61131Parser.LiteralExpressionContext ctx) {
        return new VariableNode(ctx.getText()); // Tratta i numeri come nodi foglia semplici
    }
    
    @Override
    protected ASTNode defaultResult() {
        return null;
    }  
}