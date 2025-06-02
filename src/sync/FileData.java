package sync;

import java.io.Serializable;

public class FileData implements Serializable {
    public String fullPath;
    public int version;
    public byte[] content;
    public FileData(String fullPath,int version, byte[] content) {
        this.fullPath = fullPath;
        this.version = version;
        this.content = content;
    }
}
