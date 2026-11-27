package ua.nupp.kpp.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.nupp.kpp.client.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * HTTP-клієнт для звернення JavaFX до сервера. Виконує запити, передає параметри,
 * отримує JSON і перетворює його на клієнтські моделі за допомогою ObjectMapper.
 * Інтерфейс не працює з SQL чи MySQL — лише з цим класом.
 */
public class ApiClient {
    public static final String DEFAULT_BASE_URL = "http://localhost:8080/water-quality-server/api";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final String baseUrl;

    public ApiClient() {
        this(System.getProperty("api.url", DEFAULT_BASE_URL));
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ---------- пункти спостереження ----------
    public List<ObservationPoint> points() {
        return read(send("GET", "/points", "", null), new TypeReference<List<ObservationPoint>>() { });
    }

    public ObservationPoint createPoint(ObservationPoint p) {
        return read(send("POST", "/points", "action=create", p), ObservationPoint.class);
    }

    public ObservationPoint updatePoint(ObservationPoint p) {
        return read(send("POST", "/points", "action=update", p), ObservationPoint.class);
    }

    public void deletePoint(int id) {
        send("POST", "/points", "action=delete&id=" + id, null);
    }

    // ---------- проби ----------
    public List<Sample> samples() {
        return read(send("GET", "/samples", "", null), new TypeReference<List<Sample>>() { });
    }

    public Sample createSample(Sample s) {
        return read(send("POST", "/samples", "action=create", s), Sample.class);
    }

    public Sample updateSample(Sample s) {
        return read(send("POST", "/samples", "action=update", s), Sample.class);
    }

    public void deleteSample(int id) {
        send("POST", "/samples", "action=delete&id=" + id, null);
    }

    /** Просить сервер перевести пробу в стан target; допустимість переходу перевіряє сервер. */
    public Sample changeState(int id, SampleState target) {
        return read(send("POST", "/samples", "action=state&id=" + id + "&target=" + target.name(), null), Sample.class);
    }

    public IndexResult index(int sampleId) {
        return read(send("GET", "/samples", "action=index&id=" + sampleId, null), IndexResult.class);
    }

    // ---------- вимірювання ----------
    public List<Measurement> measurements(Integer sampleId) {
        String query = sampleId == null ? "" : "sampleId=" + sampleId;
        return read(send("GET", "/measurements", query, null), new TypeReference<List<Measurement>>() { });
    }

    public Measurement createMeasurement(Measurement m) {
        return read(send("POST", "/measurements", "action=create", m), Measurement.class);
    }

    public Measurement updateMeasurement(Measurement m) {
        return read(send("POST", "/measurements", "action=update", m), Measurement.class);
    }

    public void deleteMeasurement(int id) {
        send("POST", "/measurements", "action=delete&id=" + id, null);
    }

    // ---------- перевірки ----------
    public List<Inspection> inspections() {
        return read(send("GET", "/inspections", "", null), new TypeReference<List<Inspection>>() { });
    }

    public Inspection createInspection(Inspection i) {
        return read(send("POST", "/inspections", "action=create", i), Inspection.class);
    }

    public Inspection updateInspection(Inspection i) {
        return read(send("POST", "/inspections", "action=update", i), Inspection.class);
    }

    public void deleteInspection(int id) {
        send("POST", "/inspections", "action=delete&id=" + id, null);
    }

    // ---------- низький рівень ----------
    private String send(String method, String path, String query, Object body) {
        try {
            String uri = baseUrl + path + (query.isEmpty() ? "" : "?" + query);
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json");
            if ("GET".equals(method)) {
                builder.GET();
            } else {
                String json = body == null ? "" : mapper.writeValueAsString(body);
                builder.header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            }
            HttpResponse<String> resp = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 400) {
                throw new ApiException(extractError(resp.body(), resp.statusCode()), false);
            }
            return resp.body();
        } catch (ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит перервано", false);
        } catch (IOException e) {
            throw new ApiException("Сервер недоступний: " + e.getMessage(), true);
        }
    }

    private String extractError(String body, int status) {
        try {
            String msg = mapper.readTree(body).path("error").asText("");
            if (!msg.isEmpty()) {
                return msg;
            }
        } catch (IOException ignored) {
            // тіло не є JSON — повертаємо загальне повідомлення нижче
        }
        return "Помилка сервера (HTTP " + status + ")";
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new ApiException("Некоректна відповідь сервера: " + e.getMessage(), false);
        }
    }

    private <T> T read(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new ApiException("Некоректна відповідь сервера: " + e.getMessage(), false);
        }
    }
}
