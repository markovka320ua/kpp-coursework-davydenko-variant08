package ua.nupp.kpp.server.web;

import ua.nupp.kpp.server.model.ObservationPoint;
import ua.nupp.kpp.server.service.ObservationPointService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * /api/points
 * GET  [?id=N]                      — список пунктів або один пункт
 * POST ?action=create (тіло JSON)   — додати
 * POST ?action=update (тіло JSON)   — змінити
 * POST ?action=delete&id=N          — видалити
 */
public class PointServlet extends ApiServlet {
    private final ObservationPointService service = new ObservationPointService();

    @Override
    protected Object get(HttpServletRequest req) {
        Integer id = optionalIntParam(req, "id");
        return id == null ? service.list() : service.get(id);
    }

    @Override
    protected Object post(HttpServletRequest req) throws IOException {
        switch (action(req)) {
            case "create":
                return service.create(body(req, ObservationPoint.class));
            case "update":
                return service.update(body(req, ObservationPoint.class));
            case "delete": {
                int id = intParam(req, "id");
                service.delete(id);
                return deleted(id);
            }
            default:
                throw unknownAction(req);
        }
    }
}
