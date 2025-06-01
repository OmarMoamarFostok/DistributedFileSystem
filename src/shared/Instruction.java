package shared;

import java.io.Serializable;

public abstract class Instruction implements Serializable {
    public String department;
    public String filename;
    public NodeInterface targetNode;
    public String token;
    long expirationTimestamp;
    public Instruction(String department, String filename, NodeInterface targetNode, String token) {
        this.department = department;
        this.filename = filename;
        this.targetNode = targetNode;
        this.token = token;
        this.expirationTimestamp = System.currentTimeMillis() + 5 * 60 * 1000;
    }
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTimestamp;
    }
}
