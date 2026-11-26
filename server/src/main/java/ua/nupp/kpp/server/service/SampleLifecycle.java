package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.model.SampleState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Чотиристанний життєвий цикл проби:
 * REGISTERED → VALIDATED → ANALYZED → ARCHIVED.
 * Дозволені лише переходи вперед на один крок; повернення назад і пропуск стану заборонені.
 */
public final class SampleLifecycle {
    private static final Map<SampleState, Set<SampleState>> ALLOWED = new EnumMap<>(SampleState.class);

    static {
        ALLOWED.put(SampleState.REGISTERED, EnumSet.of(SampleState.VALIDATED));
        ALLOWED.put(SampleState.VALIDATED, EnumSet.of(SampleState.ANALYZED));
        ALLOWED.put(SampleState.ANALYZED, EnumSet.of(SampleState.ARCHIVED));
        ALLOWED.put(SampleState.ARCHIVED, EnumSet.noneOf(SampleState.class));
    }

    private SampleLifecycle() {
    }

    public static boolean canTransition(SampleState from, SampleState to) {
        return from != null && to != null && ALLOWED.get(from).contains(to);
    }

    public static Set<SampleState> allowedFrom(SampleState from) {
        return EnumSet.copyOf(ALLOWED.get(from));
    }

    public static void requireTransition(SampleState from, SampleState to) {
        if (!canTransition(from, to)) {
            throw new ValidationException("Перехід «" + (from == null ? "—" : from.getLabel())
                    + "» → «" + (to == null ? "—" : to.getLabel()) + "» не дозволений");
        }
    }
}
