package ua.nupp.kpp.server.service;

import java.math.BigDecimal;

/** Складові інтегрального показника проби: фактори F1–F3 та підсумкове значення I. */
public record IndexResult(int sampleId, double f1, double f2, double f3, BigDecimal integral) {
}
