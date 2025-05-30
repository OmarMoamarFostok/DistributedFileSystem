package shared;

public class FileMetaData {
    public String name;
    public Department department;
    public long lastModified;

    public FileMetaData(String name, Department department, long lastModified) {
        this.name = name;
        this.department = department;
        this.lastModified = lastModified;
    }
}
