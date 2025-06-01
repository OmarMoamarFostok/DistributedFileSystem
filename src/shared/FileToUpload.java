package shared;

import java.io.Serializable;

public class FileToUpload implements Serializable {
    public String department;
    public String filename;
    public FileToUpload(String department, String filename) {
        this.department = department;
        this.filename = filename;
    }
}
