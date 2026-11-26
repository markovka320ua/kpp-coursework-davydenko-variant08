package ua.nupp.kpp.server.web;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/** GET /api/ping — перевірка, що сервер запущено. */
public class PingServlet extends ApiServlet {
    @Override
    protected Object get(HttpServletRequest req) {
        return Map.of("status", "ok", "time", LocalDateTime.now().toString());
    }

    @Override
    protected Object post(HttpServletRequest req) {
        throw unknownAction(req);
    }
}
