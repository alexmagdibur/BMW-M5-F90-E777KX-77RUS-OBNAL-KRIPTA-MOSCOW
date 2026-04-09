package service;

import domain.*;
import saving.EntitySerializer;
import saving.GameSave;
import saving.SaveFileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SaveService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");

    private static final String SEC_TEAM    = "#TEAM";
    private static final String SEC_COMPS   = "#COMPONENTS";
    private static final String SEC_BOLIDS  = "#BOLIDS";
    private static final String SEC_PILOTS  = "#PILOTS";
    private static final String SEC_ENG     = "#ENGINEERS";
    private static final String SEC_HISTORY = "#RACE_HISTORY";

    private final EntitySerializer serializer   = new EntitySerializer();
    private final SaveFileManager  fileManager  = new SaveFileManager();

    // ── save ──────────────────────────────────────────────────────────────────

    /**
     * Ручное сохранение. Файл: save_{timestamp}.csv
     */
    public void saveGame(Team team, List<RaceResult> history, String playerName) {
        String fileName = "save_" + LocalDateTime.now().format(TS) + ".csv";
        fileManager.writeToFile(playerName, fileName, buildContent(team, history));
        System.out.println("[SaveService] Игра сохранена: saves/" + playerName + "/" + fileName);
    }

    /**
     * Автосохранение. Всегда перезаписывает autosave.csv.
     */
    public void autoSave(Team team, List<RaceResult> history, String playerName) {
        fileManager.writeToFile(playerName, "autosave.csv", buildContent(team, history));
        System.out.println("[SaveService] Автосохранение: saves/" + playerName + "/autosave.csv");
    }

    // ── list ──────────────────────────────────────────────────────────────────

    public List<String> getAvailableSaves(String playerName) {
        return fileManager.listSaveFiles(playerName);
    }

    // ── load ──────────────────────────────────────────────────────────────────

    public GameSave loadGame(String playerName, String fileName) {
        String raw = fileManager.readFromFile(playerName, fileName);
        if (raw.isEmpty()) {
            throw new IllegalStateException(
                "Файл сохранения не найден или пуст: saves/" + playerName + "/" + fileName);
        }

        Map<String, List<String>> sections = parseSections(raw);

        // 1. Команда (имя + бюджет)
        Team team = serializer.deserializeTeam(singleLine(sections, SEC_TEAM));

        // 2. Пул всех компонентов (инвентарь + компоненты болидов)
        List<Component> pool = new ArrayList<>();
        for (String line : linesOf(sections, SEC_COMPS)) {
            pool.add(serializer.deserializeComponent(line));
        }

        // 3. Болиды — ссылаются на компоненты из пула по имени
        Set<String> usedByBolids = new HashSet<>();
        for (String line : linesOf(sections, SEC_BOLIDS)) {
            Bolid bolid = serializer.deserializeBolid(line, pool);
            team.addBolid(bolid);
            bolid.getAllComponents().forEach(c -> usedByBolids.add(c.getName()));
        }

        // 4. Оставшиеся компоненты (не в болидах) → инвентарь команды
        for (Component c : pool) {
            if (!usedByBolids.contains(c.getName())) {
                team.addComponent(c);
            }
        }

        // 5. Пилоты
        for (String line : linesOf(sections, SEC_PILOTS)) {
            team.addPilot(serializer.deserializePilot(line));
        }

        // 6. Инженеры
        for (String line : linesOf(sections, SEC_ENG)) {
            team.addEngineer(serializer.deserializeEngineer(line));
        }

        // 7. История гонок
        List<RaceResult> history = new ArrayList<>();
        for (String line : linesOf(sections, SEC_HISTORY)) {
            history.add(serializer.deserializeRaceResult(line));
        }

        return new GameSave(team, history);
    }

    // ── формирование содержимого файла ────────────────────────────────────────

    private String buildContent(Team team, List<RaceResult> history) {
        StringBuilder sb = new StringBuilder();

        append(sb, SEC_TEAM);
        sb.append(serializer.serializeTeam(team)).append("\n");

        // Все компоненты: инвентарь + компоненты болидов (без дублей по имени)
        append(sb, SEC_COMPS);
        Set<String> seen = new LinkedHashSet<>();
        for (Component c : collectAllComponents(team)) {
            if (seen.add(c.getName())) {
                sb.append(serializer.serializeComponent(c)).append("\n");
            }
        }

        append(sb, SEC_BOLIDS);
        for (Bolid b : team.getBolids()) {
            sb.append(serializer.serializeBolid(b)).append("\n");
        }

        append(sb, SEC_PILOTS);
        for (Pilot p : team.getPilots()) {
            sb.append(serializer.serializePilot(p)).append("\n");
        }

        append(sb, SEC_ENG);
        for (Engineer e : team.getEngineers()) {
            sb.append(serializer.serializeEngineer(e)).append("\n");
        }

        append(sb, SEC_HISTORY);
        for (RaceResult r : history) {
            sb.append(serializer.serializeRaceResult(r)).append("\n");
        }

        return sb.toString().stripTrailing();
    }

    private void append(StringBuilder sb, String section) {
        sb.append(section).append("\n");
    }

    /** Собирает все компоненты команды: инвентарь + компоненты всех болидов. */
    private List<Component> collectAllComponents(Team team) {
        List<Component> all = new ArrayList<>(team.getInventory());
        for (Bolid b : team.getBolids()) {
            all.addAll(b.getAllComponents());
        }
        return all;
    }

    // ── парсинг секций ────────────────────────────────────────────────────────

    private Map<String, List<String>> parseSections(String content) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        String current = null;

        for (String line : content.split("\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                current = trimmed;
                sections.put(current, new ArrayList<>());
            } else if (current != null && !trimmed.isEmpty()) {
                sections.get(current).add(trimmed);
            }
        }
        return sections;
    }

    private List<String> linesOf(Map<String, List<String>> sections, String key) {
        return sections.getOrDefault(key, Collections.emptyList());
    }

    private String singleLine(Map<String, List<String>> sections, String key) {
        List<String> lines = linesOf(sections, key);
        if (lines.isEmpty()) {
            throw new IllegalStateException(
                "Секция " + key + " отсутствует или пуста в файле сохранения.");
        }
        return lines.get(0);
    }
}
