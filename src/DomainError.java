public class DomainError extends Exception {
    public String message;

    public DomainError(String message) {
        this.message = message;
    }
}
