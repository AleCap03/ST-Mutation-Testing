IEC 61131-3 MUTATION TESTING
Questo progetto implementa un framework per il Mutation Testing applicato al linguaggio Structured Text (ST) dello standard IEC 61131-3. Il tool analizza il codice sorgente, identifica i punti del codice candidati alla mutazione e genera mutanti di "primo ordine" per valutare l'efficacia delle suite di test industriali.

Architettura del Progetto
Il tool segue un workflow a tre fasi basato sulla manipolazione dell'Abstract Syntax Tree (AST):
- Analisi e Identificazione: Utilizzando ANTLR4, il codice ST viene convertito in un AST. Il sistema mappa tutti i possibili candidati alla mutazione (operatori aritmetici, logici e relazionali).
- Filtraggio Strategico: Per ottimizzare i tempi di esecuzione, il tool applica un Mutation Rate configurabile. Questo permette di selezionare casualmente solo una percentuale dei mutanti identificati, in linea con i risultati sperimentali riportati nella letteratura scientifica (Zhang et al.).
- Generazione dei Mutanti: Il sistema genera file .st fisici, ognuno contenente una singola mutazione, garantendo che ogni mutante sia isolato e pronto per la compilazione in ambiente PLC.

Operatori di Mutazione Supportati
Il sistema implementa le seguenti classi di mutazione (coerenti con gli studi di Offutt):
- AOR (Arithmetic Operator Replacement): Sostituzione di +, -, *, /.
- ROR (Relational Operator Replacement): Modifica di >, <, =, >=, <=, <>.
- LOR (Logical Operator Replacement): Scambio tra AND e OR.

Struttura delle Cartelle
src/main/antlr4: Contiene la grammatica IEC61131.g4.
src/main/java/javaTesi: Core del sistema (AST Building, Mutation Logic, Code Emission).
src/test/resources: Esempi di codice ST per il testing.
target/mutants: Directory di output dove vengono salvati i file mutati.

Come iniziare
Opzione 1: utilizzo tramite IDE (Eclipse)
- Importare il progetto come Maven Project in Eclipse.
- Assicurarsi di utilizzare Java 17.
- Eseguire la classe StParserDemo.java direttamente dall’IDE per generare i mutanti a partire dai file in src/test/resources.

Opzione 2: utilizzo da linea di comando
- Requisiti: Java 17 e Maven installati.
- Compilazione: Eseguire 'mvn clean compile' per generare le classi ANTLR.
- Esecuzione: Eseguire 'mvn exec:java', che esegue la classe StParserDemo.java e genera i mutanti a partire dai file in src/test/resources.