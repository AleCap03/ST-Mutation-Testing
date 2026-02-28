package javaTesi;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.antlr.v4.runtime.*;
import com.github.vlsi.iec61131.parser.IEC61131Lexer;
import com.github.vlsi.iec61131.parser.IEC61131Parser;

public class MutationLogicTest {

    private AstMutator mutator;
    private CodeEmitter emitter;

    @BeforeEach
    void setUp() {
        mutator = new AstMutator();
        emitter = new CodeEmitter();
    }

    
    // Metodo helper per trasformare una stringa di codice ST in un AST
    private ASTNode parseSnippet(String code) {
        CharStream input = CharStreams.fromString(code);
        IEC61131Lexer lexer = new IEC61131Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IEC61131Parser parser = new IEC61131Parser(tokens);
        
        ASTBuilder builder = new ASTBuilder(tokens);
        return builder.visit(parser.statement());
    }

    // Verifica che il parser e il builder creino correttamente un'assegnazione
    @Test
    void testAssignmentParsing() {
        ASTNode node = parseSnippet("Pressione := 100;");
        assertNotNull(node, "L'AST non dovrebbe essere null");
        assertTrue(node instanceof AssignmentNode, "Dovrebbe essere un'istanza di AssignmentNode");
        
        AssignmentNode asn = (AssignmentNode) node;
        assertEquals("Pressione", emitter.emit(asn.target));
    }
    
    // Verifica la mutazione aritmetica (+ -> -)
    @Test
    void testArithmeticMutation() {
        BinaryOpNode node = new BinaryOpNode(new VariableNode("X"), "+", new VariableNode("1"));
        
        mutator.visit(node);
        
        assertEquals("-", node.operator, "L'operatore '+' dovrebbe essere mutato in '-'");
    }

    // Verifica la logica interna del mutatore su un operatore di confronto (> -> >=)
    @Test
    void testComparisonMutation() {
        BinaryOpNode node = new BinaryOpNode(new VariableNode("A"), ">", new VariableNode("10"));
        
        mutator.visit(node);
        
        assertEquals(">=", node.operator, "L'operatore '>' dovrebbe essere mutato in '>='");
    }

    // Verifica la mutazione logica AND -> OR
    @Test
    void testBooleanMutation() {
        BinaryOpNode node = new BinaryOpNode(new VariableNode("A"), "AND", new VariableNode("B"));
        
        mutator.visit(node);
        
        assertEquals("OR", node.operator, "AND dovrebbe mutare in OR");
    }

    // Verifica l'intero processo sulla struttura condizionale: legge un IF completo, lo muta 
    // e lo trasforma di nuovo in testo
    @Test
    void testFullCycleIfStatement() {
        String code = "IF A > 10 THEN B := 1; END_IF;";
        ASTNode root = parseSnippet(code);
        
        mutator.visit(root);
        
        String result = emitter.emit(root);
        
        String cleanResult = result.replace(" ", "").toUpperCase();
        
        assertTrue(cleanResult.contains(">="), "Il codice mutato dovrebbe contenere '>='");
        assertTrue(cleanResult.contains("B:=1"), "Il corpo dell'IF deve essere presente");
    }
    
    // Verifica che l'estensione della grammatica per il ciclo FOR funziona correttamente
    @Test
    void testForLoopParsing() {
        String code = "FOR i := 1 TO 10 DO Massa := Massa + 1; END_FOR;";
        ASTNode root = parseSnippet(code);
        
        
        assertTrue(root instanceof ForStatementNode, "Dovrebbe essere un ForStatementNode");
        ForStatementNode forNode = (ForStatementNode) root;
        assertEquals("i", forNode.loopVariable.name);
        assertEquals(1, forNode.body.size(), "Il ciclo dovrebbe contenere un'istruzione");
    }
    
    // Verifica una mutazione ricorsiva complessa: carica un FOR che contiene un IF che contiene un >. 
    // Verifica che il sistema scavi in profondità e muti il > in >=, mantenendo la struttura corretta.
    @Test
    void testFullCycleForMutation() {
        String code = "FOR i := 1 TO 10 DO IF Massa > 100 THEN Massa := 0; END_IF; END_FOR;";
        ASTNode root = parseSnippet(code);
        
        mutator.visit(root);
        
        String result = emitter.emit(root);
        
        // DEBUG
        System.out.println("DEBUG FOR Mutato: " + result);
        
        String cleanResult = result.replace(" ", "");
        assertTrue(cleanResult.contains(">="), "La mutazione dentro il corpo del FOR deve essere applicata");
        assertTrue(cleanResult.contains("FOR"), "La struttura del FOR deve essere mantenuta");
    }
}