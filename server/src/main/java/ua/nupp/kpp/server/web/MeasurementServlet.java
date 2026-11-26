package ua.nupp.kpp.server.web;

import ua.nupp.kpp.server.model.Measurement;
import ua.nupp.kpp.server.service.MeasurementService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * /api/measurements
 * GET  [?id=N | ?sampleId=N]         — усі вимірювання, одне, або вимірювання проби
 * POST ?action=create|update (JSON)  — додати / змінити
 * POST ?action=delete&id=N           — видалити
 */
public class MeasurementServlet extends ApiServlet {
    private final MeasurementService service = new MeasurementService();

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
                return service.create(body(req, Measurement.class));
            case "update":
                return service.update(body(req, Measurement.class));
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
