package pt.ua.MovingTree.exceptions;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
