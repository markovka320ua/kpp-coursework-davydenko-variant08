package ua.nupp.kpp.server.service;

/** Порушення правил предметної області або недопустимі вхідні дані. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
