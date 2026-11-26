package ua.nupp.kpp.server.web;

import ua.nupp.kpp.server.model.Sample;
import ua.nupp.kpp.server.model.SampleState;
import ua.nupp.kpp.server.service.SampleService;
import ua.nupp.kpp.server.service.ValidationException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * /api/samples
 * GET  [?id=N]                       — список проб або одна проба
 * GET  ?action=index&id=N            — розклад інтегрального показника (F1, F2, F3, I)
 * POST ?action=create|update (JSON)  — додати / змінити
 * POST ?action=delete&id=N           — видалити
 * POST ?action=state&id=N&target=S   — перевести пробу в стан S (життєвий цикл)
 */
public class SampleServlet extends ApiServlet {
    private final SampleService service = new SampleService();

    @Override
    protected Object get(HttpServletRequest req) {
        Integer id = optionalIntParam(req, "id");
        if ("index".equals(req.getParameter("action"))) {
            if (id == null) {
                throw new ValidationException("Не вказано параметр «id»");
            }
            return service.breakdown(id);
        }
        return id == null ? service.list() : service.get(id);
    }

    @Override
    protected Object post(HttpServletRequest req) throws IOException {
        switch (action(req)) {
            case "create":
                return service.create(body(req, Sample.class));
            case "update":
                return service.update(body(req, Sample.class));
            case "delete": {
                int id = intParam(req, "id");
                service.delete(id);
                return deleted(id);
            }
            case "state": {
                int id = intParam(req, "id");
                String target = req.getParameter("target");
                if (target == null || target.isBlank()) {
                    throw new ValidationException("Не вказано параметр «target»");
                }
                return service.changeState(id, SampleState.valueOf(target.trim().toUpperCase()));
            }
            default:
                throw unknownAction(req);
        }
    }
}
