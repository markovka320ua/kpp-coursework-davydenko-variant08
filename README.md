# Інформаційна система моніторингу якості води

Курсова робота з дисципліни «Кросплатформене програмування», варіант №8.
Студент: Давиденко Владислав Андрійович, група 201-ТН.
Національний університет «Полтавська політехніка імені Юрія Кондратюка», 2026–2027 н.р.

## Опис
Клієнт-серверна система обліку пунктів спостереження, проб води, вимірювань параметрів і перевірок.
Для проби реалізовано чотиристанний життєвий цикл
`REGISTERED → VALIDATED → ANALYZED → ARCHIVED` (Зареєстровано → Валідовано → Проаналізовано → Архівовано)
та інтегральний показник `I = 0,40·F1 + 0,35·F2 + 0,25·F3`, усі фактори нормовані до 0–100:

| Фактор | Зміст |
|--------|-------|
| F1 | найбільше відносне перевищення нормативу, %, обмежене 100: `min(100, 100·max((значення − норма)/норма))` |
| F2 | кількість критичних параметрів (перевищення нормативу від 50 %), по 25 балів за кожен, максимум 100 |
| F3 | чутливість водокористування пункту спостереження (0–100) |

Маршрут виконання операцій:
`JavaFX → HTTP-клієнт → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL`.

## Технології
Java 21 (Temurin), Maven, JavaFX 21, Servlet API 4.0.1 (Apache Tomcat 9.x, `javax.servlet`),
Jackson 2.17, MySQL / MariaDB (XAMPP) із MySQL Connector/J 8.4, JUnit 5.

## Структура репозиторію
- `database.sql` — створення бази даних `water_quality_db` з нуля та демонстраційні дані
- `server/` — серверна частина (WAR): моделі, DAO/JDBC, Service, Servlet, `web.xml`, тести JUnit 5
- `client/` — JavaFX-клієнт; `client/sample-data/` — CSV-файли для імпорту вимірювань
- `docs/` — схема бази даних (`db-schema.drawio`, `db-schema.png`) та скриншоти перевірок

## 1. Підготовка MySQL
1. Запустити **MySQL** у XAMPP Control Panel.
2. Відкрити phpMyAdmin → вкладка «Імпорт» → вибрати `database.sql` → «Виконати»
   (або в консолі: `mysql -u root < database.sql`).
3. Перевірити, що створено базу `water_quality_db` з таблицями `observation_point`, `sample`,
   `measurement`, `inspection`.

Параметри підключення: `server/src/main/resources/db.properties` (за замовчуванням користувач `root` без пароля).
Скрипт `database.sql` можна виконувати повторно: він повністю відтворює базу з початковими даними.

## 2. Складання та запуск сервера
```
cd server
mvn clean package
```
(в IntelliJ IDEA: вікно Maven → `server` → Lifecycle → `package`.)
У `server/target` з'явиться `water-quality-server.war`. Скопіювати його в `webapps` каталогу Apache Tomcat 9.x і запустити:
```
set JAVA_HOME=<шлях до JDK 21>
set CATALINA_HOME=<шлях до Tomcat 9>
"%CATALINA_HOME%\bin\catalina.bat" run
```
Перевірка: `http://localhost:8080/water-quality-server/api/ping` → `{"status":"ok",...}`.

### HTTP-маршрути
| URL | Метод | Призначення |
|-----|-------|-------------|
| `/api/ping` | GET | перевірка доступності сервера |
| `/api/points` | GET `[?id=N]` | список пунктів / один пункт |
| `/api/points?action=create\|update` | POST (JSON) | додати / змінити пункт |
| `/api/points?action=delete&id=N` | POST | видалити пункт |
| `/api/samples` | GET `[?id=N]` | список проб / одна проба |
| `/api/samples?action=index&id=N` | GET | F1, F2, F3 та інтегральний показник проби |
| `/api/samples?action=create\|update` | POST (JSON) | додати / змінити пробу |
| `/api/samples?action=delete&id=N` | POST | видалити пробу |
| `/api/samples?action=state&id=N&target=S` | POST | перевести пробу в стан `S` (життєвий цикл) |
| `/api/measurements` | GET `[?id=N \| ?sampleId=N]` | вимірювання |
| `/api/measurements?action=create\|update\|delete` | POST | CRUD вимірювань |
| `/api/inspections` | GET `[?id=N \| ?sampleId=N]` | перевірки |
| `/api/inspections?action=create\|update\|delete` | POST | CRUD перевірок |

Помилки повертаються у вигляді `{"error": "..."}` з кодом HTTP 400 (некоректні дані) або 500 (внутрішня помилка).

## 3. Запуск JavaFX-клієнта
Запустити MySQL і Tomcat із розгорнутим WAR, після чого:
```
cd client
mvn javafx:run
```
або в IntelliJ IDEA відкрити `client/pom.xml` і запустити `ua.nupp.kpp.client.Launcher`.
Адресу сервера можна змінити параметром JVM `-Dapi.url=http://host:port/water-quality-server/api`.

## 4. Імпорт CSV
Вкладка «Імпорт CSV» читає файл у фоновому потоці (JavaFX `Task`), показує `ProgressBar`,
журнал по рядках і підсумок. Предметну валідацію виконує сервер.

Формат файла (UTF-8, роздільник — кома, десятковий роздільник — крапка):
```
sample_id,parameter,value,unit,norm_limit
1,Хлориди,120.0,мг/дм3,250.0
```
| Поле | Тип | Опис |
|------|-----|------|
| `sample_id` | ціле | номер проби, що існує і перебуває в стані «Зареєстровано» |
| `parameter` | текст | назва параметра води |
| `value` | число ≥ 0 | виміряне значення |
| `unit` | текст | одиниця виміру |
| `norm_limit` | число > 0 | гранично допустиме значення |

Файли: `client/sample-data/measurements.csv` (коректні дані) і `client/sample-data/measurements_invalid.csv`
(перевірка обробки помилок). Повторний імпорт того самого файла створює дублікати вимірювань.

## 5. Тести JUnit 5
```
cd server
mvn clean test
```
Тести перевіряють інтегральний показник, нормування факторів, переходи життєвого циклу та валідацію
без запуску JavaFX, Tomcat і MySQL.

## Журналювання
Сервер і клієнт використовують `java.util.logging`; паролі та інші секрети в журнали не потрапляють.
