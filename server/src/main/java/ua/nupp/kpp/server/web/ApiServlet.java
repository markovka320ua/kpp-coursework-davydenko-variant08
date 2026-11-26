package ua.nupp.kpp.server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.nupp.kpp.server.dao.DaoException;
import ua.nupp.kpp.server.service.ValidationException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Базовий клас HTTP/JSON-сервлетів. Читає параметри й тіло запиту, передає виконання
 * сервісу, а результат або помилку повертає у форматі JSON:
 * успіх — HTTP 200 і об'єкт/масив; помилка — HTTP 400/500 і {"error": "..."}.
 * SQL і предметна логіка в сервлетах не розміщуються.
 */
public abstract class ApiServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ApiServlet.class.getName());

    protected static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @FunctionalInterface
    private interface Action {
        Object run() throws Exception;
    }

    protected abstract Object get(HttpServletRequest req) throws Exception;

    protected abstract Object post(HttpServletRequest req) throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> get(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> post(req));
    }

    private void respond(HttpServletRequest req, HttpServletResponse resp, Action action) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        int status = HttpServletResponse.SC_OK;
        Object body;
        try {
            body = action.run();
        } catch (ValidationException e) {
            LOG.log(Level.WARNING, "Відхилено запит {0} {1}: {2}",
                    new Object[]{req.getMethod(), req.getRequestURI(), e.getMessage()});
            status = HttpServletResponse.SC_BAD_REQUEST;
            body = Map.of("error", e.getMessage());
        } catch (JsonProcessingException e) {
            LOG.log(Level.WARNING, "Некоректний JSON у запиті {0}", req.getRequestURI());
            status = HttpServletResponse.SC_BAD_REQUEST;
            body = Map.of("error", "Некоректний JSON у тілі запиту");
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Некоректний параметр запиту: {0}", e.getMessage());
            status = HttpServletResponse.SC_BAD_REQUEST;
            body = Map.of("error", "Некоректне значення параметра: " + e.getMessage());
        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Помилка роботи з базою даних", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            body = Map.of("error", "Помилка роботи з базою даних");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Внутрішня помилка сервера", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            body = Map.of("error", "Внутрішня помилка сервера");
        }
        resp.setStatus(status);
        MAPPER.writeValue(resp.getOutputStream(), body);
    }

    protected static int intParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            throw new ValidationException("Не вказано параметр «" + name + "»");
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Параметр «" + name + "» має бути цілим числом");
        }
    }

    protected static Integer optionalIntParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isBlank()) ? null : intParam(req, name);
    }

    protected static String action(HttpServletRequest req) {
        String a = req.getParameter("action");
        if (a == null || a.isBlank()) {
            throw new ValidationException("Не вказано параметр «action»");
        }
        return a.trim();
    }

    protected static ValidationException unknownAction(HttpServletRequest req) {
        return new ValidationException("Невідома дія: «" + req.getParameter("action") + "»");
    }

    protected static <T> T body(HttpServletRequest req, Class<T> type) throws IOException {
        return MAPPER.readValue(req.getInputStream(), type);
    }

    protected static Map<String, Object> deleted(int id) {
        return Map.of("deleted", id);
    }
}
