package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.dao.MeasurementDao;
import ua.nupp.kpp.server.dao.SampleDao;
import ua.nupp.kpp.server.model.Measurement;
import ua.nupp.kpp.server.model.Sample;
import ua.nupp.kpp.server.model.SampleState;

import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка вимірювань. Змінювати вимірювання можна лише в пробі зі станом REGISTERED. */
public class MeasurementService {
    private static final Logger LOG = Logger.getLogger(MeasurementService.class.getName());

    private final MeasurementDao measurementDao = new MeasurementDao();
    private final SampleDao sampleDao = new SampleDao();

    public List<Measurement> listBySample(int sampleId) {
        return measurementDao.findBySample(sampleId);
    }

    public Measurement get(int id) {
        return measurementDao.findById(id)
                .orElseThrow(() -> new ValidationException("Вимірювання id=" + id + " не знайдено"));
    }

    public Measurement create(Measurement input) {
        Measurement m = validate(input);
        requireEditable(m.getSampleId());
        int id = measurementDao.insert(m);
        LOG.info("Додано вимірювання id=" + id + " до проби " + m.getSampleId());
        return get(id);
    }

    public Measurement update(Measurement input) {
        Measurement existing = get(input.getId());
        Measurement m = validate(input);
        m.setId(existing.getId());
        m.setSampleId(existing.getSampleId()); // вимірювання не можна «перенести» в іншу пробу
        requireEditable(existing.getSampleId());
        measurementDao.update(m);
        LOG.info("Змінено вимірювання id=" + m.getId());
        return get(m.getId());
    }

    public void delete(int id) {
        Measurement existing = get(id);
        requireEditable(existing.getSampleId());
        measurementDao.delete(id);
        LOG.info("Видалено вимірювання id=" + id);
    }

    private Measurement validate(Measurement in) {
        Measurement m = new Measurement();
        m.setId(in.getId());
        m.setSampleId(in.getSampleId());
        m.setParameter(Validators.requireText(in.getParameter(), "параметр", 60));
        m.setValue(Validators.requireNonNegative(in.getValue(), "значення"));
        m.setUnit(Validators.requireText(in.getUnit(), "одиниця виміру", 20));
        m.setNormLimit(Validators.requirePositive(in.getNormLimit(), "норматив"));
        return m;
    }

    private void requireEditable(int sampleId) {
        Sample s = sampleDao.findById(sampleId)
                .orElseThrow(() -> new ValidationException("Проба id=" + sampleId + " не існує"));
        if (s.getState() != SampleState.REGISTERED) {
            throw new ValidationException("Вимірювання можна змінювати лише в пробі зі станом «"
                    + SampleState.REGISTERED.getLabel() + "»");
        }
    }
}
