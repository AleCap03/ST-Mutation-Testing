package javaTesi;

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
        ParseTree tree = parser.statement();
        ASTBuilder builder = new ASTBuilder(tokens);
        ASTNode rootAST = builder.visit(tree);

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
        double mutationRate = 0.5;
        Collections.shuffle(listaPiani); 
        int targetSize = (int) (listaPiani.size() * mutationRate);
        // Assicura di generare almeno un mutante se la lista non è vuota
        List<MutationPlan> pianiFiltrati = listaPiani.subList(0, Math.max(1, Math.min(listaPiani.size(), targetSize)));
        //System.out.println("Mutanti selezionati dopo filtraggio: " + pianiFiltrati.size());

        // GENERAZIONE
        CodeEmitter emitter = new CodeEmitter();
        Path outputDir = Paths.get("target/mutants");
        if (!Files.exists(outputDir)) Files.createDirectories(outputDir);

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
        return null;
    }
}