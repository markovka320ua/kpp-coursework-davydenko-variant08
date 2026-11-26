package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.dao.ObservationPointDao;
import ua.nupp.kpp.server.dao.SampleDao;
import ua.nupp.kpp.server.model.ObservationPoint;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Предметна логіка пунктів спостереження. */
public class ObservationPointService {
    private static final Logger LOG = Logger.getLogger(ObservationPointService.class.getName());

    private final ObservationPointDao pointDao = new ObservationPointDao();
    private final SampleDao sampleDao = new SampleDao();

    public List<ObservationPoint> list() {
        return pointDao.findAll();
    }

    public ObservationPoint get(int id) {
        return pointDao.findById(id)
                .orElseThrow(() -> new ValidationException("Пункт спостереження id=" + id + " не знайдено"));
    }

    public ObservationPoint create(ObservationPoint input) {
        ObservationPoint p = validate(input, 0);
        int id = pointDao.insert(p);
        LOG.info("Додано пункт спостереження id=" + id + ", код " + p.getCode());
        return get(id);
    }

    public ObservationPoint update(ObservationPoint input) {
        get(input.getId());
        ObservationPoint p = validate(input, input.getId());
        pointDao.update(p);
        LOG.info("Змінено пункт спостереження id=" + p.getId());
        return get(p.getId());
    }

    public void delete(int id) {
        get(id);
        if (sampleDao.countByPoint(id) > 0) {
            LOG.log(Level.WARNING, "Відмова у видаленні пункту id={0}: є пов'язані проби", id);
            throw new ValidationException("Не можна видалити пункт, для якого існують проби");
        }
        pointDao.delete(id);
        LOG.info("Видалено пункт спостереження id=" + id);
    }

    private ObservationPoint validate(ObservationPoint in, int selfId) {
        ObservationPoint p = new ObservationPoint();
        p.setId(in.getId());
        p.setCode(Validators.requireText(in.getCode(), "код", 20));
        p.setName(Validators.requireText(in.getName(), "назва", 120));
        p.setWaterBody(Validators.requireText(in.getWaterBody(), "водойма", 120));
        p.setWaterUse(Validators.requireNotNull(in.getWaterUse(), "тип водокористування"));
        p.setSensitivity(Validators.requireRange(in.getSensitivity(), "чутливість", 0, 100));
        if (pointDao.existsByCode(p.getCode(), selfId)) {
            throw new ValidationException("Пункт із кодом «" + p.getCode() + "» уже існує");
        }
        return p;
    }
}
