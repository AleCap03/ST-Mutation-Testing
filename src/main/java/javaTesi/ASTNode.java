package javaTesi;

public abstract class ASTNode {
	// Ogni volta che viene creato un nodo in tutto il programma, riceve un ID incrementale
    private static int counter = 0;
    public int id = counter++; 

    public abstract ASTNode copy();
    
    public int getId() { return id; }
}