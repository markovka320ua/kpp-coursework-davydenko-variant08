package ua.nupp.kpp.server.service;

import ua.nupp.kpp.server.dao.MeasurementDao;
import ua.nupp.kpp.server.dao.ObservationPointDao;
import ua.nupp.kpp.server.dao.SampleDao;
import ua.nupp.kpp.server.model.Measurement;
import ua.nupp.kpp.server.model.ObservationPoint;
import ua.nupp.kpp.server.model.Sample;
import ua.nupp.kpp.server.model.SampleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Предметна логіка проб: CRUD, життєвий цикл, інтегральний показник. */
public class SampleService {
    private static final Logger LOG = Logger.getLogger(SampleService.class.getName());

    private final SampleDao sampleDao = new SampleDao();
    private final ObservationPointDao pointDao = new ObservationPointDao();
    private final MeasurementDao measurementDao = new MeasurementDao();

    public List<Sample> list() {
        return sampleDao.findAll();
    }

    public Sample get(int id) {
        return sampleDao.findById(id)
                .orElseThrow(() -> new ValidationException("Пробу id=" + id + " не знайдено"));
    }

    /** Нова проба завжди створюється у стані REGISTERED без показника. */
    public Sample create(Sample input) {
        Sample s = validate(input);
        s.setState(SampleState.REGISTERED);
        s.setIntegralIndex(null);
        int id = sampleDao.insert(s);
        LOG.info("Зареєстровано пробу id=" + id + " (пункт " + s.getPointId() + ")");
        return get(id);
    }

    /** Редагувати дані проби можна лише доки вона зареєстрована, але ще не валідована. */
    public Sample update(Sample input) {
        Sample existing = get(input.getId());
        requireState(existing, SampleState.REGISTERED, "редагувати");
        Sample s = validate(input);
        s.setId(existing.getId());
        sampleDao.update(s);
        LOG.info("Змінено пробу id=" + s.getId());
        return get(s.getId());
    }

    public void delete(int id) {
        Sample existing = get(id);
        requireState(existing, SampleState.REGISTERED, "видаляти");
        sampleDao.delete(id);
        LOG.info("Видалено пробу id=" + id);
    }

    /**
     * Переводить пробу в наступний стан за правилами життєвого циклу.
     * VALIDATED вимагає хоча б одного вимірювання; при ANALYZED обчислюється й зберігається показник.
     */
    public Sample changeState(int id, SampleState target) {
        Sample s = get(id);
        try {
            SampleLifecycle.requireTransition(s.getState(), target);
            if (target == SampleState.VALIDATED && measurementDao.countBySample(id) == 0) {
                throw new ValidationException("Не можна валідувати пробу без жодного вимірювання");
            }
            if (target == SampleState.ANALYZED) {
                sampleDao.updateIndex(id, calculateIndex(id));
            }
        } catch (ValidationException e) {
            LOG.log(Level.WARNING, "Відмова у переході проби id={0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
        }
        sampleDao.updateState(id, target);
        LOG.info("Проба id=" + id + ": " + s.getState() + " → " + target);
        return get(id);
    }

    /** Обчислює інтегральний показник за поточними вимірюваннями (без збереження). */
    public BigDecimal calculateIndex(int sampleId) {
        Sample s = get(sampleId);
        ObservationPoint point = pointDao.findById(s.getPointId())
                .orElseThrow(() -> new ValidationException("Пункт спостереження проби не знайдено"));
        List<Measurement> measurements = measurementDao.findBySample(sampleId);
        if (measurements.isEmpty()) {
            throw new ValidationException("Для обчислення показника потрібне хоча б одне вимірювання");
        }
        return IndexCalculator.integral(measurements, point.getSensitivity());
    }

    private Sample validate(Sample in) {
        Sample s = new Sample();
        s.setPointId(in.getPointId());
        if (pointDao.findById(in.getPointId()).isEmpty()) {
            throw new ValidationException("Пункт спостереження id=" + in.getPointId() + " не існує");
        }
        s.setSampledAt(Validators.requireNotNull(in.getSampledAt(), "дата відбору"));
        if (s.getSampledAt().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Дата відбору не може бути в майбутньому");
        }
        s.setCollector(Validators.requireText(in.getCollector(), "відбирач", 100));
        return s;
    }

    private void requireState(Sample s, SampleState expected, String action) {
        if (s.getState() != expected) {
            throw new ValidationException("Пробу в стані «" + s.getState().getLabel()
                    + "» не можна " + action);
        }
    }
}
