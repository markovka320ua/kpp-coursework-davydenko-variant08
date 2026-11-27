package ua.nupp.kpp.client.api;

/** Помилка звернення до сервера: відповідь із помилкою або недоступність сервера. */
public class ApiException extends RuntimeException {
    private final boolean connectionProblem;

    public ApiException(String message, boolean connectionProblem) {
        super(message);
        this.connectionProblem = connectionProblem;
    }

    /** true, якщо сервер недоступний (а не відхилив запит через дані). */
    public boolean isConnectionProblem() {
        return connectionProblem;
    }
}
