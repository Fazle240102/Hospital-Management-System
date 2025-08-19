package exception;

/**
 * Custom exception class for Hospital Management System.
 * Extends RuntimeException to represent unchecked application errors.
 */
public class HmsException extends RuntimeException {

    /** Creates exception with a message. */
    public HmsException(String message) {
        super(message);
    }

    /** Creates exception with a message and a cause. */
    public HmsException(String message, Throwable cause) {
        super(message, cause);
    }
}