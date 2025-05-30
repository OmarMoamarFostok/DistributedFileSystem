package Storage;

import shared.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class UserStorageService{

    private static final Path path = Paths.get("users.bin"); // Separate file for users

    private UserStorageService() {
        // Ensure the file exists
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (Exception e) {
                System.err.println("Error creating users.bin file: " + e.getMessage());
            }
        }
    }

    static private final UserStorageService instance = new UserStorageService();

    public static UserStorageService getInstance() {
        return instance;
    }

    // Stores a new user. Appends if the user doesn't exist, otherwise does nothing for simplicity.
    public synchronized void store(User user) throws Exception {
        // Before storing, check if the username already exists to prevent duplicates
        List<User> existingUsers = getAll();
        boolean userExists = existingUsers.stream()
                .anyMatch(u -> u.username.equals(user.username));

        if (!userExists) {
            Files.write(path, user.bytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            throw new Exception("Username already exists.");
        }
    }

    public List<User> getAll() {
        try {
            var lines = Files.readAllLines(path);
            List<User> users = new ArrayList<>();
            for(var line : lines){
                if (!line.trim().isEmpty()) { // Avoid processing empty lines
                    try {
                        users.add(new User(line.getBytes()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping malformed user data: " + line);
                    }
                }
            }
            return users;
        } catch (Exception e) {
            System.err.println("Error reading users from file: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}