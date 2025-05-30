package shared;

import java.io.Serializable;

public class User implements Serializable {
    public String username;
    public String password;
    public String department;
    public User(String username, String password, String department)
    {
        this.username = username;
        this.password = password;
        this. department = department;
    }

    // Constructor to reconstruct User object from bytes
    public User(byte[] bytes) {
        String str = new String(bytes);
        // Remove trailing newline character if present
        if (str.endsWith("\n") || str.endsWith("\r\n")) {
            str = str.trim(); // This will remove all whitespace, including newlines
        }
        var list = str.split(",");
        if (list.length == 3) {
            this.username = list[0];
            this.password = list[1];
            this.department = list[2];
        } else {
            // Handle malformed input, e.g., throw an IllegalArgumentException
            System.err.println("Malformed user byte data: " + str);
            throw new IllegalArgumentException("Invalid user data format.");
        }
    }

    // Method to convert User object to byte array for storage
    public byte[] bytes() {
        // Add a newline character at the end to ensure each user is on a new line in the file
        String str = username + "," + password + "," + department + "\n";
        return str.getBytes();
    }

    @Override
    public String toString() {
        return "User [username: " + username + ", department: " + department + "]";
    }
}
