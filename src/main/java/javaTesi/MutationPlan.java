package javaTesi;

// Memorizza COSA mutare e COME mutarlo prima di creare effettivamente il file.
public class MutationPlan {
    public int nodeId;         
    public String originalOp;  
    public String newOp;      

    public MutationPlan(int nodeId, String originalOp, String newOp) {
        this.nodeId = nodeId;
        this.originalOp = originalOp;
        this.newOp = newOp;
    }
}