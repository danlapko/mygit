package exceptions;

public class MyGitException extends Exception {
    public MyGitException() {
        super();
    }

    public MyGitException(String message) {
        super(message);
    }

    public MyGitException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyGitException(Throwable cause) {
        super(cause);
    }
}
