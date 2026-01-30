/*
 * Descrizione: Grammatica ANTLR4 per il parsing del testo strutturato (ST) 
 * con supporto alla gerarchia delle precedenze e gestione dei blocchi di controllo.
 */

grammar IEC61131;

options {
    // Abilitazione del Visitor per l'esplorazione e la manipolazione dell'AST
    visitor = true; 
}


// REGOLE SINTATTICHE PRINCIPALI


// Punto di ingresso per una sequenza di istruzioni
statement_list: statement+;

// Definizione generica di un'istruzione (Assegnazione, Controllo o Chiamata)
statement: assignment_statement | if_statement | for_statement | function_call_statement; 

// Istruzione di assegnazione (Sintassi: Target := Expression;)
assignment_statement:
    variable_access COLON_EQUAL expression ';'; 

// Struttura condizionale IF-THEN-ELSE
if_statement:
    IF expression THEN statement_list
    (elsif_clause)*
    (else_clause)?
    END_IF ';';

elsif_clause: ELSIF expression THEN statement_list;
else_clause: ELSE statement_list;

// Stuttura ciclo FOR
for_statement:
    FOR variable_access COLON_EQUAL expression TO expression (BY expression)? DO
    statement_list
    END_FOR ';';


// GERARCHIA DELLE ESPRESSIONI (Precedenza degli Operatori)


// Punto di ingresso per la valutazione delle espressioni
expression: logical_expression;

// Livello 1: Operatori Logici (Minima precedenza)
logical_expression:
    comparison_expression ( (AND | OR) comparison_expression )* #logicalExpression
;

// Livello 2: Operatori di Confronto Relazionale
comparison_expression:
    additive_expression ( (GT | LT | EQ | LE | GE | NE) additive_expression )? #comparisonExpression
;

// Livello 3: Operatori Additivi (+, -)
additive_expression:
    multiplicative_expression ( (PLUS | MINUS) multiplicative_expression )* #additiveExpression
;

// Livello 4: Operatori Moltiplicativi (*, /)
multiplicative_expression:
    atomic_expression ( (STAR | DIV) atomic_expression )* #multiplicativeExpression
;

// Livello 5: Espressioni Atomiche (Massima precedenza)
atomic_expression:
    variable_access #variableExpression 
    | literal #literalExpression
    | '(' expression ')' #parenthesizedExpression
    ;


// DEFINIZIONE POU (Program Organization Units) E VARIABILI


variable_access: ID; 

function_call_statement: ID '(' (expression (',' expression)*)? ')' ';';

pou
  : function
  | function_block
  ;

function: 'FUNCTION' name=ID ':' type=type_rule var_blocks+=var_block*;

function_block:
  'FUNCTION_BLOCK' name=ID
  var_blocks+=var_block*
  (statement_list)? 
  'END_FUNCTION_BLOCK'
  ; 
  
// Gestione dei blocchi di dichiarazione variabili con scope specifico
var_block locals[boolean input, boolean output, boolean temp]
  : ('VAR'
     | { $input=true; } 'VAR_INPUT'
     | { $output=true; } 'VAR_OUTPUT'
     | { $input=$output=true; } 'VAR_INPUT_OUTPUT'
     | { $temp=true; } 'VAR_TEMP')
    ( variables+=variable_declaration* 'END_VAR');

type_rule:
  name=ID #simpleType
  | array=array_type #arrayType
  | pointer=pointer_type #pointerType
  ;

array_type
  : 'ARRAY' '[' ranges+=range (',' ranges+=range)* ']' 'OF' type=type_rule;

range
  : lbound=integer_literal '..' ubound=integer_literal;

pointer_type: 'POINTER' 'TO' type=type_rule;

variable_declaration:
  names+=ID (',' names+=ID)* ':' type=type_rule (':=' initializer=variable_initializer)? ';' ;

variable_initializer:
  literal;


// LETTERALI E TIPI DATO PRIMITIVI


literal:
  numeric_literal | string_literal | boolean_literal;

boolean_literal: 'TRUE' | 'FALSE';

numeric_literal
  : '-'? integer_literal
  | '-'? Floating_point_literal
  ;

integer_literal
 : Binary_literal
 | Octal_literal
 | Decimal_literal
 | Pure_decimal_digits
 | Hexadecimal_literal
 ;


// ANALISI LESSICALE (Tokens)


// Rappresentazioni numeriche specifiche dello standard IEC 61131-3
Binary_literal : '2#' Binary_digit Binary_literal_characters? ;
fragment Binary_digit : [01] ;
fragment Binary_literal_character : Binary_digit | '_'  ;
fragment Binary_literal_characters : Binary_literal_character+ ;

Octal_literal : '8#' Octal_digit Octal_literal_characters? ;
fragment Octal_digit : [0-7] ;
fragment Octal_literal_character : Octal_digit | '_'  ;
fragment Octal_literal_characters : Octal_literal_character+ ;

Decimal_literal		: [0-9] [0-9_]* ;
Pure_decimal_digits : [0-9]+ ;
fragment Decimal_digit : [0-9] ;
fragment Decimal_literal_character : Decimal_digit | '_'  ;
fragment Decimal_literal_characters : Decimal_literal_character+ ;

Hexadecimal_literal : '16#' Hexadecimal_digit Hexadecimal_literal_characters? ;
fragment Hexadecimal_digit : [0-9a-fA-F] ;
fragment Hexadecimal_literal_character : Hexadecimal_digit | '_'  ;
fragment Hexadecimal_literal_characters : Hexadecimal_literal_character+ ;

Floating_point_literal
 : Decimal_literal Decimal_fraction? Decimal_exponent?
 ;

fragment Decimal_fraction : '.' Decimal_literal ;
fragment Decimal_exponent : Floating_point_e Sign? Decimal_literal ;
fragment Floating_point_e : [eE] ;
fragment Floating_point_p : [pP] ;
fragment Sign : [+\-] ;

string_literal
  : Static_string_literal
  ;
Static_string_literal : '\'' Quoted_text? '\'' ;
fragment Quoted_text : Quoted_text_item+ ;
fragment Quoted_text_item
  : Escaped_character
  | ~["\n\r\\]
  ;
fragment
Escaped_character
  : '$' [$'LNPRT]
  | '$' Hexadecimal_digit Hexadecimal_digit
  ;

// Gestione spazi bianchi e commenti (vengono ignorati dal parser)
WS : [ \n\r\t]+ -> channel(HIDDEN) ;
Block_comment : '(*' (Block_comment|.)*? '*)' -> channel(HIDDEN) ; 

// Parole Chiave
END_FUNCTION_BLOCK: 'END_FUNCTION_BLOCK';
END_VAR: 'END_VAR'; 
END_IF: 'END_IF';
FUNCTION_BLOCK: 'FUNCTION_BLOCK'; 
VAR: 'VAR';
VAR_INPUT: 'VAR_INPUT';
VAR_OUTPUT: 'VAR_OUTPUT';
VAR_INPUT_OUTPUT: 'VAR_INPUT_OUTPUT';
VAR_TEMP: 'VAR_TEMP';
IF: 'IF';
THEN: 'THEN';
ELSE: 'ELSE';
ELSIF: 'ELSIF';
FOR: 'FOR';
TO: 'TO';
BY: 'BY';
DO: 'DO';
END_FOR: 'END_FOR';

// Operatori Aritmetici
PLUS:   '+';
MINUS:  '-';
STAR:   '*';
DIV:    '/';

// Operatori Logici e Relazionali
GT:     '>';  
LT:     '<';  
EQ:     '=';  
GE:     '>='; 
LE:     '<='; 
NE:     '<>'; 
AND:    'AND';
OR:     'OR';

// Operatore di Assegnazione
COLON_EQUAL: ':='; 

// Identificatori
ID: [A-Za-z][A-Za-z_0-9]*;