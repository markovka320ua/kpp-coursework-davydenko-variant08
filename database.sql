-- ============================================================
-- Курсова робота: «Кросплатформене програмування», варіант №8
-- Тема: Інформаційна система моніторингу якості води
-- Студент: Давиденко В. А., група 201-ТН
-- Скрипт відтворює базу даних з нуля (MySQL / MariaDB).
-- ============================================================

DROP DATABASE IF EXISTS water_quality_db;

CREATE DATABASE water_quality_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE water_quality_db;

-- ------------------------------------------------------------
-- 1. Пункт спостереження
--    sensitivity — чутливість водокористування (фактор F3, 0–100)
-- ------------------------------------------------------------
CREATE TABLE observation_point (
  id          INT          NOT NULL AUTO_INCREMENT,
  code        VARCHAR(20)  NOT NULL,
  name        VARCHAR(120) NOT NULL,
  water_body  VARCHAR(120) NOT NULL,
  water_use   VARCHAR(20)  NOT NULL,
  sensitivity INT          NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_point_code (code),
  CONSTRAINT chk_point_use CHECK (water_use IN
      ('DRINKING','RECREATION','FISHERY','AGRICULTURE')),
  CONSTRAINT chk_point_sensitivity CHECK (sensitivity BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. Проба (об'єкт життєвого циклу)
--    state: REGISTERED -> VALIDATED -> ANALYZED -> ARCHIVED
-- ------------------------------------------------------------
CREATE TABLE sample (
  id             INT           NOT NULL AUTO_INCREMENT,
  point_id       INT           NOT NULL,
  sampled_at     DATETIME      NOT NULL,
  collector      VARCHAR(100)  NOT NULL,
  state          VARCHAR(20)   NOT NULL DEFAULT 'REGISTERED',
  integral_index DECIMAL(5,2)  NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sample_point FOREIGN KEY (point_id)
      REFERENCES observation_point (id),
  CONSTRAINT chk_sample_state CHECK (state IN
      ('REGISTERED','VALIDATED','ANALYZED','ARCHIVED')),
  CONSTRAINT chk_sample_index CHECK
      (integral_index IS NULL OR integral_index BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. Вимірювання параметра води в пробі
--    norm_limit — гранично допустиме значення параметра
-- ------------------------------------------------------------
CREATE TABLE measurement (
  id          INT            NOT NULL AUTO_INCREMENT,
  sample_id   INT            NOT NULL,
  parameter   VARCHAR(60)    NOT NULL,
  value       DECIMAL(12,4)  NOT NULL,
  unit        VARCHAR(20)    NOT NULL,
  norm_limit  DECIMAL(12,4)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_measurement_sample FOREIGN KEY (sample_id)
      REFERENCES sample (id) ON DELETE CASCADE,
  CONSTRAINT chk_measurement_value CHECK (value >= 0),
  CONSTRAINT chk_measurement_norm  CHECK (norm_limit > 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. Перевірка проби
-- ------------------------------------------------------------
CREATE TABLE inspection (
  id          INT           NOT NULL AUTO_INCREMENT,
  sample_id   INT           NOT NULL,
  inspector   VARCHAR(100)  NOT NULL,
  checked_at  DATETIME      NOT NULL,
  result      VARCHAR(20)   NOT NULL,
  note        VARCHAR(255)  NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_inspection_sample FOREIGN KEY (sample_id)
      REFERENCES sample (id) ON DELETE CASCADE,
  CONSTRAINT chk_inspection_result CHECK (result IN ('PASSED','FAILED'))
) ENGINE=InnoDB;

-- ============================================================
-- Початкові демонстраційні дані
-- ============================================================
INSERT INTO observation_point (code, name, water_body, water_use, sensitivity) VALUES
 ('PT-001', 'Водозабір «Ворскла-Північ»', 'р. Ворскла',          'DRINKING',    95),
 ('PT-002', 'Міський пляж',               'р. Ворскла',          'RECREATION',  70),
 ('PT-003', 'Ставок рибгоспу «Нова Греблі»', 'Ставок Нова Греблі', 'FISHERY',     60),
 ('PT-004', 'Зрошувальний канал №4',      'Канал Пирятин-4',     'AGRICULTURE', 40);

INSERT INTO sample (point_id, sampled_at, collector, state, integral_index) VALUES
 (1, '2026-10-05 09:30:00', 'Коваленко І. П.', 'REGISTERED', NULL),
 (2, '2026-10-06 11:00:00', 'Мельник О. С.',   'VALIDATED',  NULL),
 (3, '2026-10-07 08:45:00', 'Шевчук Т. В.',    'ANALYZED',   NULL),
 (1, '2026-09-20 10:15:00', 'Коваленко І. П.', 'ARCHIVED',   NULL),
 (4, '2026-10-08 14:20:00', 'Мельник О. С.',   'REGISTERED', NULL);

INSERT INTO measurement (sample_id, parameter, value, unit, norm_limit) VALUES
 (1, 'Нітрати',        28.0000, 'мг/дм3', 45.0000),
 (1, 'Амоній',          0.3000, 'мг/дм3',  0.5000),
 (1, 'Каламутність',    2.1000, 'NTU',     3.5000),
 (2, 'Нітрати',        52.0000, 'мг/дм3', 45.0000),
 (2, 'Фосфати',         0.9000, 'мг/дм3',  0.6000),
 (2, 'БСК5',            2.4000, 'мг/дм3',  3.0000),
 (3, 'Амоній',          1.2000, 'мг/дм3',  0.5000),
 (3, 'Нітрити',         0.0900, 'мг/дм3',  0.0800),
 (3, 'БСК5',            4.8000, 'мг/дм3',  3.0000),
 (4, 'Хлориди',       180.0000, 'мг/дм3', 250.0000),
 (4, 'Сульфати',      140.0000, 'мг/дм3', 250.0000),
 (5, 'Нітрати',        30.0000, 'мг/дм3', 45.0000),
 (5, 'Хлориди',       410.0000, 'мг/дм3', 250.0000);

INSERT INTO inspection (sample_id, inspector, checked_at, result, note) VALUES
 (2, 'Бондаренко Л. М.', '2026-10-07 10:00:00', 'PASSED', 'Протокол відбору заповнено правильно'),
 (3, 'Бондаренко Л. М.', '2026-10-08 09:10:00', 'PASSED', 'Дані лабораторії узгоджені'),
 (4, 'Савченко Р. Д.',   '2026-09-22 12:00:00', 'PASSED', NULL);
