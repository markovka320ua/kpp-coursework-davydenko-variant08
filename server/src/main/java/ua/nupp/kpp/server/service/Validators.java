package ua.nupp.kpp.server.service;

import java.math.BigDecimal;

/** Допоміжні перевірки вхідних даних; кидають ValidationException з поясненням українською. */
public final class Validators {
    private Validators() {
    }

    /** Обов'язковий текст: не null, не порожній, не довший за max. Повертає обрізаний (trim) текст. */
    public static String requireText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Поле «" + field + "» є обов'язковим");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new ValidationException("Поле «" + field + "» не може бути довшим за " + max + " символів");
        }
        return trimmed;
    }

    /** Необов'язковий текст: порожнє значення перетворюється на null. */
    public static String optionalText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, field, max);
    }

    public static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new ValidationException("Поле «" + field + "» є обов'язковим");
        }
        return value;
    }

    public static int requireRange(int value, String field, int min, int max) {
        if (value < min || value > max) {
            throw new ValidationException("Поле «" + field + "» має бути в межах від " + min + " до " + max);
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String field) {
        requireNotNull(value, field);
        if (value.signum() < 0) {
            throw new ValidationException("Поле «" + field + "» не може бути від'ємним");
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value, String field) {
        requireNotNull(value, field);
        if (value.signum() <= 0) {
            throw new ValidationException("Поле «" + field + "» має бути більшим за нуль");
        }
        return value;
    }
}
