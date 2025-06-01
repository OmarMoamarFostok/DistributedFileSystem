package shared;

public class DownloadInstruction extends Instruction{
    public DownloadInstruction(String department,String filename,NodeInterface targetNode,String token) {
        super(department,filename,targetNode,token);
    }
}
