package ua.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;
import ua.nupp.kpp.server.model.SampleState;

import static org.junit.jupiter.api.Assertions.*;

class SampleLifecycleTest {

    @Test
    void forwardTransitionsAreAllowed() {
        assertTrue(SampleLifecycle.canTransition(SampleState.REGISTERED, SampleState.VALIDATED));
        assertTrue(SampleLifecycle.canTransition(SampleState.VALIDATED, SampleState.ANALYZED));
        assertTrue(SampleLifecycle.canTransition(SampleState.ANALYZED, SampleState.ARCHIVED));
    }

    @Test
    void skippingAStateIsForbidden() {
        assertFalse(SampleLifecycle.canTransition(SampleState.REGISTERED, SampleState.ANALYZED));
        assertFalse(SampleLifecycle.canTransition(SampleState.REGISTERED, SampleState.ARCHIVED));
        assertFalse(SampleLifecycle.canTransition(SampleState.VALIDATED, SampleState.ARCHIVED));
    }

    @Test
    void goingBackwardIsForbidden() {
        assertFalse(SampleLifecycle.canTransition(SampleState.ARCHIVED, SampleState.REGISTERED));
        assertFalse(SampleLifecycle.canTransition(SampleState.ANALYZED, SampleState.VALIDATED));
    }

    @Test
    void stayingInTheSameStateIsForbidden() {
        assertFalse(SampleLifecycle.canTransition(SampleState.VALIDATED, SampleState.VALIDATED));
    }

    @Test
    void archivedStateIsFinal() {
        assertTrue(SampleLifecycle.allowedFrom(SampleState.ARCHIVED).isEmpty());
    }

    @Test
    void requireTransitionThrowsForForbiddenMove() {
        assertThrows(ValidationException.class,
                () -> SampleLifecycle.requireTransition(SampleState.ARCHIVED, SampleState.VALIDATED));
    }
}
