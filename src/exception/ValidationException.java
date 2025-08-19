package exception;

/**
 * Exception for validation-related errors in the system.
 * Extends HmsException to represent invalid input or data.
 */
public class ValidationException extends HmsException {

    /** Creates a ValidationException with a message. */
    public ValidationException(String message) {
        super(message);
    }
}