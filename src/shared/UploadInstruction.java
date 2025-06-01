package shared;

import java.io.Serializable;
import java.util.UUID;

public class UploadInstruction extends Instruction {
public UploadInstruction(String department, String filename, NodeInterface targetNode, String token)  {
    super(department,filename,targetNode,token);
}

}
