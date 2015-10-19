package PolyInfonuagique.RMIFileManager.shared;

/**
 * Custom exception
 * Handle logical issue
 * @author David Livet, Arnaud Baillym
 */
public class ManageException extends Exception {
    public ManageException(String message) {
        super(message);
    }

    public ManageException(String message, Throwable cause) {
        super(message, cause);
    }
}
