# Многопоточность в ZOV AUTO — ЛР4

## Содержание

1. [Теоретическая часть](#1-теоретическая-часть)
   - [Что такое поток](#11-что-такое-поток)
   - [Жизненный цикл потока](#12-жизненный-цикл-потока)
   - [Создание потоков в Java](#13-создание-потоков-в-java)
   - [Ключевые методы Thread](#14-ключевые-методы-thread)
   - [Daemon-потоки](#15-daemon-потоки)
   - [Гонка данных и потокобезопасность](#16-гонка-данных-и-потокобезопасность)
   - [synchronized — монитор объекта](#17-synchronized--монитор-объекта)
   - [volatile — видимость между потоками](#18-volatile--видимость-между-потоками)
   - [java.util.concurrent.atomic](#19-javautilconcurrentatomic)
   - [CopyOnWriteArrayList](#110-copyonwritearraylist)
   - [CountDownLatch](#111-countdownlatch)
   - [InterruptedException и прерывание потока](#112-interruptedexception-и-прерывание-потока)
2. [Архитектура многопоточной гонки](#2-архитектура-многопоточной-гонки)
3. [Подробный разбор каждого класса](#3-подробный-разбор-каждого-класса)
   - [RaceState — разделяемое состояние](#31-racestate--разделяемое-состояние)
   - [RaceThread — оркестратор гонки](#32-racethread--оркестратор-гонки)
   - [BolideThread — симуляция болида](#33-bolidethread--симуляция-болида)
   - [WeatherThread — динамическая погода](#34-weatherthread--динамическая-погода)
   - [IncidentThread — инциденты на трассе](#35-incidentthread--инциденты-на-трассе)
   - [CommentatorThread — комментатор](#36-commentatorthread--комментатор)
   - [PitLane — атомарный семафор боксов](#37-pitlane--атомарный-семафор-боксов)
   - [BolideResult — результат потока](#38-bolideresult--результат-потока)
4. [Сопутствующие классы](#4-сопутствующие-классы)
   - [RaceTactic — тактика пилота](#41-racetactic--тактика-пилота)
   - [PitStop — время пит-стопа](#42-pitstop--время-пит-стопа)
   - [RaceService — точка входа](#43-raceservice--точка-входа)
5. [Схема взаимодействия потоков](#5-схема-взаимодействия-потоков)
6. [Синхронизационные решения: таблица](#6-синхронизационные-решения-таблица)
7. [Тесты многопоточности](#7-тесты-многопоточности)
8. [Типичные проблемы и как они решены](#8-типичные-проблемы-и-как-они-решены)

---

## 1. Теоретическая часть

### 1.1 Что такое поток

**Поток (Thread)** — наименьшая единица выполнения внутри процесса. Несколько потоков одного процесса разделяют общую память (heap), но у каждого есть свой стек вызовов, программный счётчик и локальные переменные.

В Java каждый поток — экземпляр класса `java.lang.Thread`. JVM отображает Java-потоки на потоки операционной системы (native threads); планировщик ОС решает, когда и на каком CPU-ядре запустить каждый поток.

Преимущества многопоточности:
- **Параллелизм** — несколько задач выполняются одновременно на разных ядрах
- **Конкурентность** — задачи могут чередоваться на одном ядре, давая иллюзию одновременного выполнения
- **Отзывчивость** — UI/меню не блокируется долгими вычислениями

### 1.2 Жизненный цикл потока

```
           start()
NEW ────────────────► RUNNABLE ◄──────────────────────────┐
                          │                                │
                          │  ждёт IO / блокировку /        │
                          │  synchronized / sleep          │
                          ▼                                │
                     BLOCKED / WAITING /         уведомление /
                     TIMED_WAITING               разблокировка
                          │
                          │  run() вернулся или
                          │  выброшено исключение
                          ▼
                      TERMINATED
```

| Состояние | Описание |
|-----------|----------|
| `NEW` | Объект `Thread` создан, `start()` не вызван |
| `RUNNABLE` | Поток выполняется или готов выполняться (планировщик ОС решает) |
| `BLOCKED` | Ждёт захвата монитора (`synchronized`) |
| `WAITING` | Ждёт без таймаута (`Object.wait()`, `Thread.join()`) |
| `TIMED_WAITING` | Ждёт с таймаутом (`Thread.sleep(ms)`, `join(ms)`) |
| `TERMINATED` | `run()` завершился (нормально или с исключением) |

### 1.3 Создание потоков в Java

Java предлагает несколько способов:

**Способ 1 — наследование от Thread** (не рекомендуется в больших проектах, ограничивает наследование):
```java
class MyThread extends Thread {
    public void run() { /* логика */ }
}
new MyThread().start();
```

**Способ 2 — реализация Runnable** (используется в проекте):
```java
class MyTask implements Runnable {
    public void run() { /* логика */ }
}
new Thread(new MyTask()).start();
```

**Способ 3 — лямбда** (Java 8+, краткий):
```java
new Thread(() -> { /* логика */ }).start();
```

В проекте используется способ 2: `BolideThread`, `WeatherThread`, `IncidentThread`, `CommentatorThread` — все реализуют `Runnable`. Это правильный выбор: классы могут наследоваться от других классов, логика инкапсулирована.

### 1.4 Ключевые методы Thread

| Метод | Описание |
|-------|----------|
| `start()` | Запускает поток в новом OS-потоке; вызывает `run()` асинхронно |
| `run()` | Тело потока; при прямом вызове — обычный метод в текущем потоке |
| `join()` | Текущий поток блокируется до завершения вызываемого потока |
| `join(ms)` | То же, но с таймаутом; возвращает управление по истечении ms |
| `sleep(ms)` | Приостанавливает текущий поток на ms миллисекунд |
| `interrupt()` | Устанавливает флаг прерывания; выбрасывает `InterruptedException` если поток спит/ждёт |
| `isAlive()` | Возвращает `true` пока поток не завершился |
| `setDaemon(true)` | Делает поток фоновым (JVM не ждёт его завершения при выходе) |
| `currentThread()` | Возвращает объект текущего потока |

### 1.5 Daemon-потоки

JVM завершает работу когда все **не-daemon** потоки завершились, не дожидаясь daemon-потоков.

- `setDaemon(true)` должен вызываться **до** `start()`
- Daemon-потоки подходят для вспомогательных задач: логирование, мониторинг, фоновая уборка
- Если JVM завершается, все daemon-потоки обрываются без выполнения `finally`-блоков

В проекте `WeatherThread`, `IncidentThread`, `CommentatorThread` — **daemon**, потому что они вспомогательные и не должны держать приложение живым после завершения гонки.

### 1.6 Гонка данных и потокобезопасность

**Гонка данных (data race)** возникает, когда:
1. Два или более потока обращаются к одной переменной
2. Хотя бы один из них пишет
3. Нет синхронизации

Пример проблемы:
```java
// Не потокобезопасно!
int counter = 0;

void increment() {
    counter++;  // на самом деле: read → add → write (три операции, не атомарны)
}
```

Если два потока одновременно выполнят `counter++`, итоговое значение может быть 1, а не 2 — это **lost update**.

Ещё одна проблема — **видимость**: JVM и CPU могут кэшировать значение переменной в регистре процессора или L1-кэше. Поток A обновил переменную, но поток B ещё видит старое значение из своего кэша.

### 1.7 synchronized — монитор объекта

Ключевое слово `synchronized` гарантирует **взаимное исключение** (mutex): в данный момент только один поток выполняет синхронизированный блок/метод.

```java
// synchronized-метод — монитор this
public synchronized void log(String msg) {
    System.out.println(msg);
}

// synchronized-блок — монитор явный объект
synchronized (lockObject) {
    // критическая секция
}
```

Когда поток входит в `synchronized`:
1. Он захватывает **монитор** (lock) указанного объекта
2. Другие потоки, пытающиеся захватить тот же монитор, переходят в состояние `BLOCKED`
3. При выходе из блока монитор освобождается

`synchronized` также обеспечивает **видимость**: все записи, сделанные до release монитора, видны следующему потоку, захватившему тот же монитор.

### 1.8 volatile — видимость между потоками

`volatile` решает проблему видимости без взаимного исключения:

```java
volatile boolean flag = false;
```

Чтение `volatile`-переменной всегда идёт из главной памяти (RAM), запись — сразу в RAM, минуя кэши CPU. Это обеспечивает актуальность данных между потоками.

**`volatile` НЕ делает операцию атомарной**. `volatile int x; x++` всё равно является тремя операциями. Для атомарности используют `AtomicInteger` или `synchronized`.

Когда использовать `volatile`:
- Один поток пишет, несколько читают (флаги, состояние)
- Нужна видимость, но не нужна атомарность составных операций

### 1.9 java.util.concurrent.atomic

Пакет `java.util.concurrent.atomic` содержит классы, обеспечивающие атомарные операции через CAS (Compare-And-Swap) — аппаратную инструкцию процессора. CAS атомарно: «сравни с ожидаемым значением — если совпало, запиши новое».

**`AtomicBoolean`** — атомарный булев флаг:
```java
AtomicBoolean running = new AtomicBoolean(false);
running.set(true);          // атомарная запись
boolean val = running.get(); // атомарное чтение
running.compareAndSet(false, true); // атомарный CAS
```

**`AtomicInteger`** — атомарный целочисленный счётчик:
```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();    // атомарный ++
count.decrementAndGet();    // атомарный --
count.get();                // атомарное чтение
count.compareAndSet(old, newVal); // атомарный CAS
```

CAS позволяет реализовать **lock-free** алгоритмы — без `synchronized`, без блокировок, с высокой производительностью при большом количестве потоков.

### 1.10 CopyOnWriteArrayList

`java.util.concurrent.CopyOnWriteArrayList` — потокобезопасная реализация `List`:

- При **записи** (add, set, remove) создаётся полная копия внутреннего массива — запись защищена lock
- При **чтении** (get, iteration, size) lock не нужен — читается снапшот текущего массива
- Итерация даёт снапшот на момент начала — **не бросает `ConcurrentModificationException`**

Идеальна когда: **записей мало, чтений много**. Именно так работает список результатов гонки: болиды добавляют результат один раз при финише, комментатор читает постоянно.

### 1.11 CountDownLatch

`java.util.concurrent.CountDownLatch` — барьер для синхронизации N потоков:

```java
CountDownLatch latch = new CountDownLatch(3); // ждём 3 события
// в каждом из 3 потоков:
latch.countDown();   // уменьшает счётчик на 1
// в ожидающем потоке:
latch.await();       // блокируется пока счётчик не достигнет 0
```

Используется в тестах для одновременного запуска нескольких потоков — симуляция реального race condition.

### 1.12 InterruptedException и прерывание потока

`Thread.interrupt()` не останавливает поток принудительно — он лишь устанавливает флаг прерывания. Если поток в момент вызова находится в `sleep()` или `join()`, выбрасывается `InterruptedException`.

Правила обработки:
```java
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // Вариант 1: завершить работу (для вспомогательных потоков)
    break; // или return
    // Вариант 2: восстановить флаг прерывания (если не можем обработать здесь)
    Thread.currentThread().interrupt();
}
```

Правило: **никогда не глотать `InterruptedException` молча** без break/return или восстановления флага — иначе родительский код не узнает, что поток был прерван.

---

## 2. Архитектура многопоточной гонки

Гонка в ЛР4 реализована через 6 одновременно работающих потоков:

```
GameMenu / RaceService
        │
        │ вызывает runRace() → блокирует до завершения
        ▼
   RaceThread.run()  [выполняется в вызывающем потоке]
        │
        │ создаёт и запускает
        ├──────────────────────────────────────────────────┐
        │                                                  │
        ▼  start()                                         │
  WeatherThread  (daemon)      IncidentThread  (daemon)    │
  Меняет погоду каждые         Проверяет износ каждые      │
  1500 мс через volatile       800 мс, пишет DNF            │
  state.currentWeather         в CopyOnWriteArrayList       │
        │                                                  │
        │  start()              start()                    │
        ├─► BolideThread[0]  ─► BolideThread[1]  ─► BolideThread[2]  ─► BolideThread[3]
        │   (Игрок)             (Бот 1)             (Бот 2)              (Бот 3)
        │   Читает volatile      Читает volatile     Читает volatile       Читает volatile
        │   weather на каждой    weather ...         weather ...           weather ...
        │   секции, tryPitStop   tryPitStop          tryPitStop            tryPitStop
        │   через AtomicInteger  через AtomicInteger ...                   ...
        │
        │  start()
        ├─► CommentatorThread  (daemon)
        │   Логирует комментарии каждые 500 мс
        │   Читает CopyOnWriteArrayList (results)
        │
        │ join() на всех BolideThread — ждёт финиша всех болидов
        │ interrupt() + join(1000) на daemon-потоках
        ▼
   race = buildRace(state)  — строит итог гонки
        │
        │ возвращает Race в GameMenu
        ▼
   GameMenu продолжает работу
```

**Разделяемое состояние (`RaceState`)** доступно всем потокам:

```
RaceState
├── volatile Weather currentWeather          ← пишет WeatherThread, читают BolideThread
├── AtomicBoolean raceRunning                ← пишет RaceThread, читают все daemon-потоки
├── long raceStartTime                       ← записывается до start(), только читается
└── CopyOnWriteArrayList<BolideResult> results ← пишут BolideThread/IncidentThread, читает CommentatorThread
```

---

## 3. Подробный разбор каждого класса

### 3.1 RaceState — разделяемое состояние

**Файл:** [`src/game/service/threads/RaceState.java`](src/game/service/threads/RaceState.java)

`RaceState` — центральный объект, доступный всем потокам гонки. Каждое поле защищено соответствующим механизмом синхронизации.

```java
public class RaceState {

    volatile Weather currentWeather;         // (1)
    final AtomicBoolean raceRunning = new AtomicBoolean(false); // (2)
    long raceStartTime;                      // (3)
    final List<BolideResult> results = new CopyOnWriteArrayList<>(); // (4)

    public synchronized void log(String message) { // (5)
        long elapsed = System.currentTimeMillis() - raceStartTime;
        long mins = elapsed / 60000;
        long secs = (elapsed % 60000) / 1000;
        System.out.printf("[%02d:%02d] %s%n", mins, secs, message);
    }
}
```

Разбор каждого решения:

**(1) `volatile Weather currentWeather`**

- `WeatherThread` пишет это поле каждые 1500 мс
- Четыре `BolideThread` читают перед каждой секцией трассы
- Без `volatile` JVM вправе закэшировать значение в регистре CPU каждого болида — они бы видели устаревшую погоду
- `volatile` гарантирует: запись `WeatherThread` немедленно становится видима всем читающим потокам
- Почему не `synchronized`? Достаточно видимости — атомарность не нужна (Weather — ссылка на enum, её присвоение атомарно на 64-битных JVM)

**(2) `final AtomicBoolean raceRunning`**

- Флаг активности гонки — читается в цикле каждого daemon-потока (`while (state.raceRunning.get())`)
- Пишется `RaceThread` после `join()` всех болидов: `state.raceRunning.set(false)`
- Альтернатива `volatile boolean` — тоже работала бы, но `AtomicBoolean` явно сигнализирует о намерении: «это поле меняется из нескольких потоков, здесь нужна атомарность»
- `compareAndSet()` (`CAS`) не используется здесь, но `AtomicBoolean` открывает эту возможность

**(3) `long raceStartTime`**

- Записывается **один раз** в `RaceThread.run()` до запуска любых потоков
- После этого только читается во всех потоках (в `log()`)
- По правилу Java Memory Model: если запись в поле происходит **до** вызова `Thread.start()`, то эта запись гарантированно видна в новом потоке — `volatile` не нужен (happens-before через start)

**(4) `CopyOnWriteArrayList<BolideResult> results`**

- `BolideThread` добавляет результат при финише: `state.results.add(...)`
- `IncidentThread` добавляет DNF при инциденте: `state.results.add(...)`
- `CommentatorThread` итерирует список чтобы озвучить лидера: `List.copyOf(state.results)`
- Максимум ~4 добавления за всю гонку — COW-список идеален: нет блокировок при чтении

**(5) `synchronized void log(String message)`**

- Вызывается из всех потоков одновременно
- Без `synchronized`: строки из разных потоков смешивались бы (один поток печатает префикс, другой вклинивается и печатает свою строку посередине)
- `synchronized` гарантирует: `printf` выполняется атомарно — одна полная строка за раз
- Монитор — `this` (объект `RaceState`), один для всех потоков

---

### 3.2 RaceThread — оркестратор гонки

**Файл:** [`src/game/service/threads/RaceThread.java`](src/game/service/threads/RaceThread.java)

`RaceThread` реализует `Runnable` и отвечает за полный жизненный цикл гонки:

```java
@Override
public void run() {
    RaceState state = new RaceState(initialWeather);
    state.raceStartTime = System.currentTimeMillis();
    state.raceRunning.set(true);

    BolideThread playerThread = new BolideThread(state, bolid, pilot, engineer, track, plannedPitStops, team.getName(), true);
    if (presetPlayerDnfReason != null) {
        playerThread.setDnf();
        state.results.add(new BolideResult(team.getName(), 0, true, true));
    }

    List<BolideThread> botThreads = createBots(state);
    List<BolideThread> allParticipants = new ArrayList<>();
    allParticipants.add(playerThread);
    allParticipants.addAll(botThreads);

    // 1. Запуск вспомогательных daemon-потоков
    Thread weatherThread     = new Thread(new WeatherThread(state, 1500));
    Thread incidentThread    = new Thread(new IncidentThread(state, allParticipants, 800));
    Thread commentatorThread = new Thread(new CommentatorThread(state, allParticipants, 500));
    weatherThread.setDaemon(true);
    incidentThread.setDaemon(true);
    commentatorThread.setDaemon(true);
    weatherThread.start();
    incidentThread.start();
    commentatorThread.start();

    // 2. Запуск болидов
    List<Thread> bolideThreads = new ArrayList<>();
    for (BolideThread bt : allParticipants) {
        Thread t = new Thread(bt);
        bolideThreads.add(t);
        t.start();
    }

    // 3. Ожидание завершения болидов
    for (Thread t : bolideThreads) {
        try { t.join(); } catch (InterruptedException ignored) {}
    }

    // 4. Останавливаем вспомогательные потоки
    state.raceRunning.set(false);
    weatherThread.interrupt();
    incidentThread.interrupt();
    commentatorThread.interrupt();
    try { weatherThread.join(1000); }     catch (InterruptedException ignored) {}
    try { incidentThread.join(1000); }    catch (InterruptedException ignored) {}
    try { commentatorThread.join(1000); } catch (InterruptedException ignored) {}

    // 5. Строим итог
    race = buildRace(state, presetPlayerDnfReason);
}
```

Ключевые решения:

**`join()` на болидах без таймаута** — `RaceThread` блокируется пока каждый болид не финишировал или не получил DNF. Это гарантирует: к моменту вызова `buildRace()` все результаты уже в `state.results`.

**`setDaemon(true)` до `start()`** — обязательный порядок. После `start()` изменить daemon-статус нельзя — выбросится `IllegalThreadStateException`.

**Последовательность остановки**: сначала `raceRunning.set(false)`, затем `interrupt()`, затем `join(1000)`. Почему не только `interrupt()`? Daemon-поток может быть не в `sleep()` в момент прерывания — он проверяет `raceRunning.get()` в начале каждой итерации. Обе проверки нужны.

**`join(1000)` с таймаутом для daemon-потоков** — избегаем зависания: если daemon-поток по какой-то причине не завершился за 1 секунду, `RaceThread` всё равно продолжает работу.

---

### 3.3 BolideThread — симуляция болида

**Файл:** [`src/game/service/threads/BolideThread.java`](src/game/service/threads/BolideThread.java)

Один экземпляр на каждого участника (игрок + 3 бота). Симулирует прохождение каждой секции трассы с паузой `Thread.sleep()`.

```java
public class BolideThread implements Runnable {

    private volatile boolean dnf = false;  // пишет IncidentThread, читает run()

    @Override
    public void run() {
        List<TrackSection> sections = track.getSections();
        int sectionsPerPitStop = (plannedPitStops > 0)
            ? sections.size() / plannedPitStops
            : Integer.MAX_VALUE;
        double totalTime = 0.0;

        for (int i = 0; i < sections.size(); i++) {
            if (dnf || !state.raceRunning.get()) break;  // ранний выход

            Weather weather = state.currentWeather;       // volatile чтение
            Track sectionTrack = new Track("_", List.of(sections.get(i)));
            double sectionTime = RaceCalculator.calculateTime(bolid, pilot, engineer, sectionTrack, weather);

            if (pilot.getTactic() != null) {
                sectionTime *= pilot.getTactic().getModifier(weather);
            }

            totalTime += sectionTime;
            Thread.sleep((long)(sectionTime * 5));

            if ((i + 1) % sectionsPerPitStop == 0 && pitsDone < plannedPitStops) {
                tryPitStop();
            }
        }

        if (!dnf) {
            state.results.add(new BolideResult(participantName, totalTime, false, isPlayer));
        }
    }

    private void tryPitStop() {
        PitLane pit = track.getPitLane();
        if (pit == null) return;
        if (pit.tryEnter()) {           // атомарный CAS — захват места
            Thread.sleep(PitStop.DURATION_MS);
            PitStop.applyBonus(bolid);
            pit.leave();                // освобождение места
        }
    }
}
```

**`volatile boolean dnf`** — `IncidentThread` устанавливает флаг (`setDnf()`) из своего потока, `BolideThread.run()` проверяет его в начале каждой итерации. Без `volatile` болид мог бы не увидеть изменение и продолжить ехать после инцидента.

**Двойная проверка выхода** `if (dnf || !state.raceRunning.get())` — болид выходит и при своём DNF, и при завершении гонки другим способом.

**Симуляция времени** через `Thread.sleep((long)(sectionTime * 5))` — каждая секунда модельного времени занимает 5 мс реального. Так гонка из 10 секций на скорости ~100 единиц занимает ~2–5 секунд реального времени.

**Пит-стоп** вызывается только в определённые моменты (каждые `sectionsPerPitStop` секций), создавая реалистичную стратегию. Несколько болидов могут одновременно попытаться въехать — `PitLane.tryEnter()` атомарно разруливает конкуренцию.

---

### 3.4 WeatherThread — динамическая погода

**Файл:** [`src/game/service/threads/WeatherThread.java`](src/game/service/threads/WeatherThread.java)

```java
public class WeatherThread implements Runnable {

    @Override
    public void run() {
        while (state.raceRunning.get()) {         // проверяем флаг активности гонки
            try {
                Thread.sleep(changeIntervalMs);   // 1500 мс
            } catch (InterruptedException e) {
                break;                            // прерван — завершаемся
            }
            if (!state.raceRunning.get()) break;  // двойная проверка после пробуждения

            Weather next = randomWeather();
            if (next != state.currentWeather) {   // изменяем только при реальной смене
                state.currentWeather = next;      // volatile запись — видна всем BolideThread
                state.log("🌦 Погода изменилась: " + prev + " → " + next);
            }
        }
    }

    private static Weather randomWeather() {
        double r = Math.random();
        if (r < 0.10) return Weather.SOLAR_ECLIPSE;
        if (r < 0.40) return Weather.DRY;
        if (r < 0.70) return Weather.WET;
        return Weather.RAIN;
    }
}
```

**Паттерн основного цикла**: `while (flag) { sleep(); if (!flag) break; do_work(); }` — стандартный шаблон для потоков с периодической работой. Двойная проверка нужна: за время сна флаг мог смениться, и мы не хотим делать лишнюю работу.

**SOLAR_ECLIPSE с вероятностью 10%** — редкое событие, которое активирует механику оборотней в `RaceService`.

**Запись в `state.currentWeather`** — volatile, поэтому все BolideThread видят новую погоду на следующей итерации своего цикла без дополнительной синхронизации.

---

### 3.5 IncidentThread — инциденты на трассе

**Файл:** [`src/game/service/threads/IncidentThread.java`](src/game/service/threads/IncidentThread.java)

```java
public class IncidentThread implements Runnable {
    private static final double INCIDENT_CHANCE = 0.4;

    @Override
    public void run() {
        while (state.raceRunning.get()) {
            try {
                Thread.sleep(checkIntervalMs);    // 800 мс
            } catch (InterruptedException e) {
                break;
            }
            if (!state.raceRunning.get()) break;

            for (BolideThread participant : participants) {
                if (participant.isDnf()) continue;

                String failedComponent = checkIncident(participant.getBolid());
                if (failedComponent != null) {
                    participant.setDnf();          // volatile запись → BolideThread увидит
                    state.results.add(new BolideResult(
                        participant.getParticipantName(), 0, true, participant.isPlayer()
                    ));
                    state.log("💥 ИНЦИДЕНТ: " + ...);
                }
            }
        }
    }

    private static String checkIncident(Bolid bolid) {
        for (Component c : bolid.getComponents().values()) {
            if (c.isWornOut() && Math.random() < INCIDENT_CHANCE) {
                c.setWear(100);
                return c.getName();
            }
        }
        return null;
    }
}
```

**Запись `participant.setDnf()`** — устанавливает `volatile boolean dnf` на объекте `BolideThread`. Этот поток (`IncidentThread`) — единственный пишущий, `BolideThread` — единственный читающий. `volatile` достаточно.

**`state.results.add(...)` без `synchronized`** — безопасно, потому что это `CopyOnWriteArrayList`.

**Итерация по `participants`** — список создаётся в `RaceThread` до запуска потоков и **никогда не изменяется** после этого. Итерация по неизменяемому `ArrayList` из нескольких потоков безопасна (нет конкурентных записей).

---

### 3.6 CommentatorThread — комментатор

**Файл:** [`src/game/service/threads/CommentatorThread.java`](src/game/service/threads/CommentatorThread.java)

```java
public class CommentatorThread implements Runnable {

    @Override
    public void run() {
        while (state.raceRunning.get()) {
            try {
                Thread.sleep(commentIntervalMs);   // 500 мс
            } catch (InterruptedException e) {
                break;
            }
            if (!state.raceRunning.get()) break;

            int type = RandomUtil.nextInt(0, 2);
            switch (type) {
                case 0 -> state.log("🎙 " + CommentatorPhrases.getWeatherPhrase(state.currentWeather));
                case 1 -> commentPositions();
                case 2 -> state.log("📢 " + CommentatorPhrases.getSponsorPhrase());
            }
        }
    }

    private void commentPositions() {
        List<BolideResult> results = List.copyOf(state.results);  // снапшот COWAL
        if (!results.isEmpty()) {
            String leader = results.get(0).getParticipantName();
            String last   = results.get(results.size() - 1).getParticipantName();
            state.log("🎙 " + CommentatorPhrases.getPositionPhrase(leader, last));
        } else {
            state.log("🎙 " + participants.get(0).getParticipantName() + " всё ещё на трассе!");
        }
    }
}
```

**`List.copyOf(state.results)`** — создаёт неизменяемую копию снапшота `CopyOnWriteArrayList`. Хотя итерация по COWAL и без этого потокобезопасна, `List.copyOf()` явно фиксирует намерение: «работаю со снимком на этот момент времени».

**Чтение `state.currentWeather`** — volatile, всегда актуальное значение.

**Три типа комментариев**: погода (тип 0), позиции (тип 1), спонсор (тип 2) — выбираются случайно каждые 500 мс, что создаёт реалистичный фон гонки.

---

### 3.7 PitLane — атомарный семафор боксов

**Файл:** [`src/game/domain/PitLane.java`](src/game/domain/PitLane.java)

`PitLane` — пример **lock-free семафора** на основе `AtomicInteger` и CAS:

```java
public class PitLane {

    private final int capacity;
    private final AtomicInteger occupiedSlots = new AtomicInteger(0);

    // атомарно занимает место: возвращает true если место нашлось
    public boolean tryEnter() {
        while (true) {
            int current = occupiedSlots.get();
            if (current >= capacity) return false;
            if (occupiedSlots.compareAndSet(current, current + 1)) return true;
            // CAS не удался (другой поток занял место между get и CAS) → повторить
        }
    }

    public void leave() {
        occupiedSlots.decrementAndGet();
    }
}
```

**Алгоритм `tryEnter()` — CAS-петля (spin loop)**:
1. Прочитать текущее значение (`current`)
2. Если мест нет — вернуть `false`
3. Попытаться атомарно увеличить: `compareAndSet(current, current + 1)`
4. Если за время между шагами 1 и 3 другой поток изменил счётчик — CAS вернёт `false`, повторяем с шага 1

Это lock-free алгоритм: нет `synchronized`, нет блокировок. Потоки не засыпают в ожидании — это хорошо для короткого ожидания, но CPU-затратно при долгом.

**Почему не `synchronized int count`?** Стандартный `synchronized` создаёт накладные расходы на захват монитора. CAS работает быстрее при малом числе конкурирующих потоков (у нас максимум 4 болида).

**Семантика**: аналогична `java.util.concurrent.Semaphore` с `tryAcquire()` без ожидания — получил место или немедленно получил отказ.

---

### 3.8 BolideResult — результат потока

**Файл:** [`src/game/service/threads/BolideResult.java`](src/game/service/threads/BolideResult.java)

Неизменяемый (immutable) объект-результат. Все поля `final`, нет сеттеров — объект потокобезопасен по построению. Записывается в `CopyOnWriteArrayList` один раз при финише или DNF.

```java
public class BolideResult {
    private final String participantName;
    private final double time;
    private final boolean dnf;
    private final boolean isPlayer;

    // конвертация в RaceResult для совместимости с Race и GameMenu
    public RaceResult toRaceResult() {
        if (dnf) return RaceResult.dnf(participantName, isPlayer);
        return new RaceResult(participantName, time, isPlayer);
    }
}
```

Правило неизменяемости: объект, все поля которого `final` и задаются в конструкторе, безопасно публиковать между потоками через `final`-ссылку (JMM гарантирует видимость final-полей после конструктора).

---

## 4. Сопутствующие классы

### 4.1 RaceTactic — тактика пилота

**Файл:** [`src/game/domain/RaceTactic.java`](src/game/domain/RaceTactic.java)

Задаётся до старта гонки, далее только читается из `BolideThread`. Потокобезопасен как read-only данные, разделяемые между потоками.

Тактика меняет время прохождения секции в зависимости от текущей погоды:

```java
public double getModifier(Weather weather) {
    return modifiers.getOrDefault(weather, 1.0);
}
// В BolideThread:
sectionTime *= pilot.getTactic().getModifier(weather);
```

Доступные тактики (`TacticCatalog`):

| Тактика | DRY | WET | RAIN | SOLAR_ECLIPSE |
|---------|-----|-----|------|---------------|
| Агрессивная | 0.97 (быстрее) | — | 1.05 (медленнее) | — |
| Дождевая | 1.03 | 0.95 | 0.93 | — |
| Осторожная | 1.02 | 1.02 | 1.02 | 1.02 |
| Универсальная | — | — | — | — |

Нейтральный коэффициент: 1.0 (без изменения времени). Значение < 1.0 ускоряет, > 1.0 замедляет.

### 4.2 PitStop — время пит-стопа

**Файл:** [`src/game/domain/PitStop.java`](src/game/domain/PitStop.java)

Утилитный класс с константами. `DURATION_MS = 2000` — `BolideThread` спит 2 секунды реального времени при пит-стопе, симулируя обслуживание болида.

```java
public static final int DURATION_MS = 2000;      // длительность пит-стопа
public static final double WEAR_REDUCTION = 30.0; // снижение износа шин

public static void applyBonus(Bolid bolid) {
    Component tires = bolid.getComponent(ComponentType.TIRES);
    if (tires == null) return;
    int newWear = (int) Math.max(0, tires.getWear() - WEAR_REDUCTION);
    tires.setWear(newWear);
}
```

### 4.3 RaceService — точка входа

**Файл:** [`src/game/service/RaceService.java`](src/game/service/RaceService.java)

```java
RaceThread raceThread = new RaceThread(...);
// run() а не start() — гонка блокирует меню до полного завершения
new Thread(raceThread).run();
Race race = raceThread.getRace();
```

Важный нюанс: `new Thread(raceThread).run()` — вызывает `run()` напрямую в **текущем потоке**, а не в новом. Создание `Thread`-обёртки здесь избыточно; эквивалент: `raceThread.run()`. Это сделано намеренно: `GameMenu` блокируется на время гонки. Вся реальная многопоточность происходит внутри `RaceThread.run()`, который создаёт и запускает настоящие потоки через `t.start()`.

---

## 5. Схема взаимодействия потоков

```
Время →

RaceThread   [══════════════ join(BolideThreads) ═══════════════] set(false) interrupt join
WeatherThread          [sleep 1500ms]→write weather→[sleep 1500ms]→...                 ↑ interrupt
IncidentThread    [sleep 800ms]→check→[sleep 800ms]→check→...                          ↑ interrupt
CommentatorThread  [sleep 500ms]→log→[sleep 500ms]→log→...                             ↑ interrupt
BolideThread[0]   [sec1]→sleep→[pit]→sleep→[sec2]→sleep→...→[finish]→add(results)→terminate
BolideThread[1]   [sec1]→sleep→[sec2]→sleep→...→[dnf: incident]→terminate
BolideThread[2]   [sec1]→sleep→[sec2]→sleep→...→[finish]→add(results)→terminate
BolideThread[3]   [sec1]→sleep→[pit]→[BLOCKED: pit full]→skip→[sec2]→...→terminate

                                                  ↑
                                         CopyOnWriteArrayList.add()
                                         (из BolideThread[0] и [2])
```

---

## 6. Синхронизационные решения: таблица

| Что защищаем | Механизм | Где | Почему именно этот |
|---|---|---|---|
| Текущая погода (`currentWeather`) | `volatile` | `RaceState` | Один писатель (WeatherThread), несколько читателей (BolideThread). Нужна видимость, не атомарность составных операций |
| Флаг активности гонки (`raceRunning`) | `AtomicBoolean` | `RaceState` | Несколько читателей в loop, один писатель; `AtomicBoolean` явно выражает намерение потокобезопасности |
| Список результатов (`results`) | `CopyOnWriteArrayList` | `RaceState` | Редкие записи (финиш/DNF), частое чтение (комментатор). Итерация без блокировок |
| Вывод в консоль (`log`) | `synchronized` | `RaceState.log()` | Несколько писателей, нужно предотвратить перемешивание строк |
| DNF-флаг болида | `volatile` | `BolideThread.dnf` | Один писатель (IncidentThread), один читатель (BolideThread). Нужна видимость |
| Места в боксах (`occupiedSlots`) | `AtomicInteger` + CAS | `PitLane` | Конкурентный инкремент/декремент. Lock-free семафор |
| `raceStartTime` | Happens-before через `start()` | `RaceState` | Записывается до запуска потоков — JMM гарантирует видимость без `volatile` |
| `participants` (список болидов) | Нет (read-only) | `IncidentThread`, `CommentatorThread` | Создан до запуска потоков, не изменяется — безопасно читать из любого потока |

---

## 7. Тесты многопоточности

### `WeatherThreadTest`

**Файл:** [`src/test/WeatherThreadTest.java`](src/test/WeatherThreadTest.java)

```java
@Test
void testWeatherChangesOverTime() throws InterruptedException {
    boolean changed = false;
    for (int attempt = 0; attempt < 10 && !changed; attempt++) {
        RaceState state = startedState(Weather.DRY);
        Thread t = new Thread(new WeatherThread(state, 50)); // 50мс вместо 1500
        t.start();
        Thread.sleep(500);    // даём 500 мс на ~10 попыток сменить погоду
        state.getRaceRunning().set(false);
        t.interrupt();
        t.join(3000);
        if (state.getCurrentWeather() != Weather.DRY) changed = true;
    }
    assertTrue(changed);
}

@Test
void testWeatherThreadStopsWhenRaceEnds() throws InterruptedException {
    RaceState state = startedState(Weather.DRY);
    Thread t = new Thread(new WeatherThread(state, 100));
    t.start();
    state.getRaceRunning().set(false);
    t.interrupt();
    t.join(3000);    // ждём максимум 3 секунды
    assertFalse(t.isAlive()); // поток должен был завершиться
}
```

Тест `testWeatherChangesOverTime` использует 10 попыток из-за случайности смены погоды — вероятность что погода не изменится за 10 попыток ≈ (1/4)^10 ≈ 0.001%. Паттерн retry для вероятностных тестов.

### `PitLaneTest`

**Файл:** [`src/test/PitLaneTest.java`](src/test/PitLaneTest.java)

```java
@Test
void testConcurrentAccessRespectsCapacity() throws InterruptedException {
    int capacity = 2;
    int threads  = 5;
    PitLane lane = new PitLane(capacity);

    CountDownLatch ready  = new CountDownLatch(threads); // барьер готовности
    CountDownLatch start  = new CountDownLatch(1);       // сигнал одновременного старта
    AtomicInteger entered = new AtomicInteger(0);

    for (int i = 0; i < threads; i++) {
        new Thread(() -> {
            ready.countDown();          // я готов
            try { start.await(); }      // жду сигнала
            catch (InterruptedException ignored) {}
            if (lane.tryEnter()) entered.incrementAndGet();
        }).start();
    }

    ready.await();          // все 5 потоков готовы
    start.countDown();      // СТАРТ — все 5 стартуют одновременно
    Thread.sleep(500);

    assertTrue(entered.get() <= capacity); // не более 2 вошли
    assertTrue(entered.get() > 0);         // хотя бы 1 вошёл
}
```

**`CountDownLatch` для синхронизации старта** — классический паттерн нагрузочного теста: сначала все потоки выходят на старт (`ready`), затем главный поток даёт сигнал (`start.countDown()`), и все 5 потоков одновременно вызывают `tryEnter()`. Это максимально воспроизводит настоящий race condition.

### `RaceThreadIntegrationTest`

**Файл:** [`src/test/RaceThreadIntegrationTest.java`](src/test/RaceThreadIntegrationTest.java)

```java
@Test
void testRaceCompletesWithinTimeout() throws InterruptedException {
    RaceService svc = new RaceService();
    Thread runner = new Thread(() -> svc.runRace(...simpleTrack()...));
    runner.start();
    runner.join(15_000);  // ждём максимум 15 секунд
    assertFalse(runner.isAlive()); // гонка должна была завершиться
}
```

Интеграционный тест проверяет: **все потоки корректно завершаются** (нет дедлока, нет бесконечного цикла). `join(15_000)` с последующей проверкой `isAlive()` — стандартный способ проверить завершение потока за разумное время.

### `TacticTest`

**Файл:** [`src/test/TacticTest.java`](src/test/TacticTest.java)

Детерминированный тест бизнес-логики тактик: проверяет корректность коэффициентов для каждой погоды. Не тестирует многопоточность напрямую, но покрывает данные, используемые в `BolideThread`.

---

## 8. Типичные проблемы и как они решены

### Проблема 1: Race condition при записи результатов

**Ситуация**: несколько `BolideThread` финишируют одновременно и пишут в общий список результатов.

**Решение**: `CopyOnWriteArrayList` — операция `add()` внутренне защищена lock и создаёт копию массива. Гарантированная атомарность каждой записи.

---

### Проблема 2: Устаревшая погода в болидах

**Ситуация**: `WeatherThread` обновил погоду, но `BolideThread` использует кэшированное значение из своего регистра/L1-кэша.

**Решение**: `volatile Weather currentWeather` — запись в `WeatherThread` немедленно сбрасывает кэш; следующее чтение в `BolideThread` получит актуальное значение.

---

### Проблема 3: Перемешивание строк в консоли

**Ситуация**: 7 потоков одновременно пишут в `System.out` — строки перемешиваются.

**Решение**: все выводы проходят через `state.log()`, помеченный как `synchronized`. Монитор `RaceState` гарантирует, что только один поток печатает в каждый момент.

---

### Проблема 4: Одновременный въезд нескольких болидов в боксы

**Ситуация**: два `BolideThread` одновременно проверяют `occupiedSlots < capacity` и оба "видят" свободное место — оба въезжают, хотя мест нет.

**Решение**: `AtomicInteger.compareAndSet()` — CAS гарантирует, что проверка и инкремент выполнятся атомарно. Только один поток выиграет CAS при конкуренции; остальные повторяют попытку или получают отказ.

---

### Проблема 5: Daemon-потоки не завершаются после гонки

**Ситуация**: daemon-потоки циклически спят в `sleep()` — они не видят, что гонка кончилась.

**Решение**: двойной механизм — `raceRunning.set(false)` (следующая итерация цикла выйдет сама) + `thread.interrupt()` (прерывает текущий `sleep()`, выбрасывает `InterruptedException`, поток выходит через `break`). После этого `join(1000)` даёт до 1 секунды на корректное завершение.

---

### Проблема 6: Зависание гонки (deadlock)

**Ситуация**: `RaceThread` ждёт всех болидов через `join()`, но болид застрял в пит-стопе, ожидая места которое никогда не освободится.

**Решение**: `PitLane.tryEnter()` — **non-blocking**, немедленно возвращает `false` если мест нет. Болид логирует «пит-стоп пропущен» и продолжает гонку. Дедлок исключён по дизайну.

---

*Документ актуален для коммитов `7948f80` (ЛР4, многопоточность), ветка `claude/hopeful-roentgen-148c66`.*
