package ua.nupp.kpp.server.web;

import ua.nupp.kpp.server.model.Inspection;
import ua.nupp.kpp.server.service.InspectionService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * /api/inspections
 * GET  [?id=N | ?sampleId=N]         — усі перевірки, одна, або перевірки проби
 * POST ?action=create|update (JSON)  — додати / змінити
 * POST ?action=delete&id=N           — видалити
 */
public class InspectionServlet extends ApiServlet {
    private final InspectionService service = new InspectionService();

    @Override
    protected Object get(HttpServletRequest req) {
        Integer id = optionalIntParam(req, "id");
        if (id != null) {
            return service.get(id);
        }
        Integer sampleId = optionalIntParam(req, "sampleId");
        return sampleId == null ? service.list() : service.listBySample(sampleId);
    }

    @Override
    protected Object post(HttpServletRequest req) throws IOException {
        switch (action(req)) {
            case "create":
                return service.create(body(req, Inspection.class));
            case "update":
                return service.update(body(req, Inspection.class));
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
