package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.dao.InspectionDao;
import ua.nupp.kpp.server.dao.SampleDao;
import ua.nupp.kpp.server.model.Inspection;
import ua.nupp.kpp.server.model.Sample;
import ua.nupp.kpp.server.model.SampleState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка перевірок проб. Для архівованої проби нові перевірки не додаються. */
public class InspectionService {
    private static final Logger LOG = Logger.getLogger(InspectionService.class.getName());

    private final InspectionDao inspectionDao = new InspectionDao();
    private final SampleDao sampleDao = new SampleDao();

    public List<Inspection> list() {
        return inspectionDao.findAll();
    }

    public List<Inspection> listBySample(int sampleId) {
        return inspectionDao.findBySample(sampleId);
    }

    public Inspection get(int id) {
        return inspectionDao.findById(id)
                .orElseThrow(() -> new ValidationException("Перевірку id=" + id + " не знайдено"));
    }

    public Inspection create(Inspection input) {
        Inspection i = validate(input);
        Sample s = sampleDao.findById(i.getSampleId())
                .orElseThrow(() -> new ValidationException("Проба id=" + i.getSampleId() + " не існує"));
        if (s.getState() == SampleState.ARCHIVED) {
            throw new ValidationException("Для архівованої проби не можна додавати перевірки");
        }
        int id = inspectionDao.insert(i);
        LOG.info("Додано перевірку id=" + id + " проби " + i.getSampleId() + ": " + i.getResult());
        return get(id);
    }

    public Inspection update(Inspection input) {
        Inspection existing = get(input.getId());
        Inspection i = validate(input);
        i.setId(existing.getId());
        i.setSampleId(existing.getSampleId());
        inspectionDao.update(i);
        LOG.info("Змінено перевірку id=" + i.getId());
        return get(i.getId());
    }

    public void delete(int id) {
        get(id);
        inspectionDao.delete(id);
        LOG.info("Видалено перевірку id=" + id);
    }

    private Inspection validate(Inspection in) {
        Inspection i = new Inspection();
        i.setId(in.getId());
        i.setSampleId(in.getSampleId());
        i.setInspector(Validators.requireText(in.getInspector(), "перевіряльник", 100));
        i.setCheckedAt(Validators.requireNotNull(in.getCheckedAt(), "дата перевірки"));
        if (i.getCheckedAt().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Дата перевірки не може бути в майбутньому");
        }
        i.setResult(Validators.requireNotNull(in.getResult(), "результат"));
        i.setNote(Validators.optionalText(in.getNote(), "примітка", 255));
        return i;
    }
}
