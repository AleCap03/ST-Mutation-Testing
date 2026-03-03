package javaTesi;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.vlsi.iec61131.parser.IEC61131Lexer;
import com.github.vlsi.iec61131.parser.IEC61131Parser;

public class StParserDemo {
	
	public static void main(String[] args) throws Exception {
        // INPUT e PARSING
        String inputFile = "src/test/resources/test.st";
        String stCode = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
        CharStream input = CharStreams.fromString(stCode);
        IEC61131Lexer lexer = new IEC61131Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IEC61131Parser parser = new IEC61131Parser(tokens);
        ParseTree tree = parser.statement_list();
        
        /*System.out.println("--- PARSE TREE (Gerarchia ANTLR4) ---");
        // Stampa il Parse Tree in formato testuale indentato
        System.out.println(tree.toStringTree(parser)); 
        System.out.println("\n");*/
        
        
        ASTBuilder builder = new ASTBuilder(tokens);
        ASTNode rootAST = builder.visit(tree);
        
        /*System.out.println("--- ABSTRACT SYNTAX TREE (AST Personalizzato) ---");
        printAST(rootAST, 0);
        System.out.println("\n");*/
        

        // IDENTIFICAZIONE: Trova tutti i punti dove possiamo applicare una mutazione
        List<BinaryOpNode> puntiDiMutazione = new ArrayList<>();
        raccogliPunti(rootAST, puntiDiMutazione);
        
        AstMutator mutator = new AstMutator();
        List<MutationPlan> listaPiani = new ArrayList<>();
        
        for (BinaryOpNode node : puntiDiMutazione) {
            String opMutato = mutator.mutaOperatore(node.operator);
            // Crea la lista di coppie (Punto, Mutazione) 
            listaPiani.add(new MutationPlan(node.id, node.operator, opMutato));
        }
        //System.out.println("Mutanti candidati identificati: " + listaPiani.size());
 
        // FILTRAZIONE: Selezione casuale
        double mutationRate = 1.0;
        Collections.shuffle(listaPiani); 
        int targetSize = (int) (listaPiani.size() * mutationRate);
        // Assicura di generare almeno un mutante se la lista non è vuota
        List<MutationPlan> pianiFiltrati = listaPiani.subList(0, Math.max(1, Math.min(listaPiani.size(), targetSize)));
        //System.out.println("Mutanti selezionati dopo filtraggio: " + pianiFiltrati.size());

        // GENERAZIONE
        CodeEmitter emitter = new CodeEmitter();
        Path outputDir = Paths.get("target/mutants");
        // PULIZIA CARTELLA: Cancella i file esistenti prima di generare i nuovi
        if (Files.exists(outputDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDir)) {
                for (Path entry : stream) {
                    Files.delete(entry);
                }
            }
        } else {
            Files.createDirectories(outputDir);
        }

        for (int i = 0; i < pianiFiltrati.size(); i++) {
            MutationPlan piano = pianiFiltrati.get(i);
            ASTNode clone = rootAST.copy(); // Copia per First-Order Mutant
            BinaryOpNode targetNelClone = (BinaryOpNode) trovaNodoPerId(clone, piano.nodeId);
            
            if (targetNelClone != null) {
                // Applica la mutazione
                targetNelClone.operator = piano.newOp;
                
                // Trasforma l'AST mutato in codice ST
                String codiceMutato = emitter.emit(clone);
                
                // Salva ogni mutante in un file separato
                String nomeFile = "mutant_" + (i + 1) + ".st";
                Files.write(outputDir.resolve(nomeFile), codiceMutato.getBytes(StandardCharsets.UTF_8));
                
                //System.out.println("[OK] Generato: " + nomeFile + " (" + piano.originalOp + " -> " + piano.newOp + ")");
            }
        }
        System.out.println("\n>>> OPERAZIONE COMPLETATA. File generati in: " + outputDir.toAbsolutePath());
    }

    
    private static void raccogliPunti(ASTNode nodo, List<BinaryOpNode> lista) {
        if (nodo == null) return;

        if (nodo instanceof BinaryOpNode) {
            lista.add((BinaryOpNode) nodo);
            raccogliPunti(((BinaryOpNode) nodo).left, lista);
            raccogliPunti(((BinaryOpNode) nodo).right, lista);
        } 
        else if (nodo instanceof AssignmentNode) {
            raccogliPunti(((AssignmentNode) nodo).expression, lista);
        }
        else if (nodo instanceof ParenthesizedExpressionNode) {
            raccogliPunti(((ParenthesizedExpressionNode) nodo).getExpression(), lista);
        }
       
        else if (nodo instanceof IfStatementNode ifNode) {
            // Cerca nella condizione
            raccogliPunti(ifNode.condition, lista);
           
            // Cerca nel corpo del THEN
            for (ASTNode stmt : ifNode.thenStatements) {
                raccogliPunti(stmt, lista);
            }
            
            // Cerca nel corpo dell'ELSE (se esiste)
            if (ifNode.elseStatements != null) {
                for (ASTNode stmt : ifNode.elseStatements) {
                    raccogliPunti(stmt, lista);
                }
            }
        }
        else if (nodo instanceof BlockNode block) {
            for (ASTNode stmt : block.statements) {
                raccogliPunti(stmt, lista);
            }
        }
        else if (nodo instanceof ForStatementNode forNode) {
            // Cerca punti di mutazione nei limiti del ciclo (se sono espressioni)
            raccogliPunti(forNode.startValue, lista);
            raccogliPunti(forNode.endValue, lista);
            if (forNode.stepValue != null) raccogliPunti(forNode.stepValue, lista);
            
            // Cerca punti di mutazione dentro il corpo del ciclo
            for (ASTNode stmt : forNode.body) {
                raccogliPunti(stmt, lista);
            }
        }
    }

    
    private static ASTNode trovaNodoPerId(ASTNode radice, int id) {
        if (radice == null) return null;
        if (radice.id == id) return radice;

        if (radice instanceof BinaryOpNode bin) {
            ASTNode t = trovaNodoPerId(bin.left, id);
            return (t != null) ? t : trovaNodoPerId(bin.right, id);
        } 
        else if (radice instanceof AssignmentNode asn) {
            return trovaNodoPerId(asn.expression, id);
        }
        else if (radice instanceof ParenthesizedExpressionNode paren) {
            return trovaNodoPerId(paren.getExpression(), id);
        }
      
        else if (radice instanceof IfStatementNode ifNode) {
            ASTNode t = trovaNodoPerId(ifNode.condition, id);
            if (t != null) return t;
            for (ASTNode s : ifNode.thenStatements) {
                t = trovaNodoPerId(s, id);
                if (t != null) return t;
            }
            if (ifNode.elseStatements != null) {
                for (ASTNode s : ifNode.elseStatements) {
                    t = trovaNodoPerId(s, id);
                    if (t != null) return t;
                }
            }
        }
        else if (radice instanceof BlockNode block) {
            for (ASTNode s : block.statements) {
                ASTNode t = trovaNodoPerId(s, id);
                if (t != null) return t;
            }
        }
        else if (radice instanceof ForStatementNode forNode) {
            ASTNode t = trovaNodoPerId(forNode.startValue, id);
            if (t != null) return t;
            t = trovaNodoPerId(forNode.endValue, id);
            if (t != null) return t;
            if (forNode.stepValue != null) {
                t = trovaNodoPerId(forNode.stepValue, id);
                if (t != null) return t;
            }
            for (ASTNode s : forNode.body) {
                t = trovaNodoPerId(s, id);
                if (t != null) return t;
            }
        }
        return null;
    }
    
    // Metodo per stampare AST (semplificato)
    /*private static void printAST(ASTNode node, int indent) {
        if (node == null) return;
        
        String indentation = "  ".repeat(indent);
        System.out.println(indentation + "|-- " + node.getClass().getSimpleName() + 
            (node instanceof BinaryOpNode ? " [Op: " + ((BinaryOpNode)node).operator + "]" : "") +
            (node instanceof AssignmentNode ? " [Var: " + ((AssignmentNode)node).target + "]" : ""));
            
        if (node instanceof BlockNode block) {
            for (ASTNode s : block.statements) printAST(s, indent + 1);
        } 
        else if (node instanceof AssignmentNode asn) {
            printAST(asn.expression, indent + 1);
        }
        else if (node instanceof BinaryOpNode bin) {
            printAST(bin.left, indent + 1);
            printAST(bin.right, indent + 1);
        }
        else if (node instanceof ParenthesizedExpressionNode paren) {
            printAST(paren.getExpression(), indent + 1);
        }
    }*/
}