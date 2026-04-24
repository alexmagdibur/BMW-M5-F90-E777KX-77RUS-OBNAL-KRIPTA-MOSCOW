package service;

import domain.*;
import saving.BolidBinarySerializer;
import saving.EntitySerializer;
import saving.GameSave;
import saving.SaveFileManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SaveService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");

    // секции CSV-файла
    private static final String SEC_TEAM = "#TEAM";
    private static final String SEC_COMPS = "#COMPONENTS";   // только инвентарь (не болидные компоненты)
    private static final String SEC_BOLID_REFS = "#BOLID_REFS"; // имена болидов → ссылки на .bin файлы
    private static final String SEC_KITS = "#EMERGENCY_KITS";
    private static final String SEC_PILOTS = "#PILOTS";
    private static final String SEC_ENG = "#ENGINEERS";
    private static final String SEC_HISTORY = "#RACE_HISTORY";

    private static final String SAVES_ROOT = "saves";
    private static final String BOLIDS_DIR = "bolids"; // подпапка внутри saves/{player}/

    private final EntitySerializer csvSerializer  = new EntitySerializer();
    private final BolidBinarySerializer binSerializer  = new BolidBinarySerializer();
    private final SaveFileManager fileManager    = new SaveFileManager();

    // save

    public void saveGame(Team team, List<RaceResult> history, String playerName) {
        String fileName = "save_" + LocalDateTime.now().format(TS) + ".csv";
        saveBolids(team, playerName);
        fileManager.writeToFile(playerName, fileName, buildContent(team, history));
        System.out.println("[SaveService] Игра сохранена: saves/" + playerName + "/" + fileName);
    }

    public void autoSave(Team team, List<RaceResult> history, String playerName) {
        saveBolids(team, playerName);
        fileManager.writeToFile(playerName, "autosave.csv", buildContent(team, history));
        System.out.println("[SaveService] Автосохранение: saves/" + playerName + "/autosave.csv");
    }

    // Сохраняет один болид в .bin (вызывается из меню)
    public void saveBolid(Bolid bolid, String playerName) {
        File binFile = new File(bolidDir(playerName), safeName(bolid.getName()) + ".bin");
        try {
            binSerializer.save(bolid, binFile);
            System.out.println("[SaveService] Болид сохранён: " + binFile.getPath());
        } catch (IOException e) {
            System.err.println("[SaveService] Ошибка сохранения болида: " + e.getMessage());
        }
    }

    // сохраняет каждый болид в отдельный .bin файл
    private void saveBolids(Team team, String playerName) {
        File bolidDir = bolidDir(playerName);
        for (Bolid bolid : team.getBolids()) {
            File binFile = new File(bolidDir, safeName(bolid.getName()) + ".bin");
            try {
                binSerializer.save(bolid, binFile);
            } catch (IOException e) {
                System.err.println("[SaveService] Ошибка сохранения болида «"
                        + bolid.getName() + "»: " + e.getMessage());
            }
        }
    }

    // list

    public List<String> getAvailableSaves(String playerName) {
        return fileManager.listSaveFiles(playerName);
    }

    // load

    public GameSave loadGame(String playerName, String fileName) {
        String raw = fileManager.readFromFile(playerName, fileName);
        if (raw.isEmpty()) {
            throw new IllegalStateException(
                "Файл сохранения не найден или пуст: saves/" + playerName + "/" + fileName);
        }

        Map<String, List<String>> sections = parseSections(raw);

        // 1. команда
        Team team = csvSerializer.deserializeTeam(singleLine(sections, SEC_TEAM));

        // 2. инвентарные компоненты (не принадлежащие болидам)
        for (String line : linesOf(sections, SEC_COMPS)) {
            team.addComponent(csvSerializer.deserializeComponent(line));
        }

        // 3. болиды из .bin файлов
        for (String bolidName : linesOf(sections, SEC_BOLID_REFS)) {
            File binFile = new File(bolidDir(playerName), safeName(bolidName) + ".bin");
            if (!binFile.exists()) {
                System.err.println("[SaveService] Файл болида не найден: " + binFile.getPath());
                continue;
            }
            try {
                team.addBolid(binSerializer.load(binFile));
            } catch (IOException e) {
                System.err.println("[SaveService] Ошибка загрузки болида «"
                        + bolidName + "»: " + e.getMessage());
            }
        }

        // 3a. набор экстренной помощи команды
        List<String> kitLines = linesOf(sections, SEC_KITS);
        if (!kitLines.isEmpty()) {
            team.setEmergencyKit(csvSerializer.deserializeEmergencyKit(kitLines.get(0)));
        }

        // 4. пилоты
        for (String line : linesOf(sections, SEC_PILOTS)) {
            team.addPilot(csvSerializer.deserializePilot(line));
        }

        // 5. инженеры
        for (String line : linesOf(sections, SEC_ENG)) {
            team.addEngineer(csvSerializer.deserializeEngineer(line));
        }

        // 6. история гонок
        List<RaceResult> history = new ArrayList<>();
        for (String line : linesOf(sections, SEC_HISTORY)) {
            history.add(csvSerializer.deserializeRaceResult(line));
        }

        return new GameSave(team, history);
    }

    // build CSV content

    private String buildContent(Team team, List<RaceResult> history) {
        StringBuilder sb = new StringBuilder();

        append(sb, SEC_TEAM);
        sb.append(csvSerializer.serializeTeam(team)).append("\n");

        // только инвентарные компоненты (болидные хранятся в .bin)
        append(sb, SEC_COMPS);
        for (Component c : team.getInventory()) {
            sb.append(csvSerializer.serializeComponent(c)).append("\n");
        }

        // имена болидов как ссылки на .bin файлы
        append(sb, SEC_BOLID_REFS);
        for (Bolid b : team.getBolids()) {
            sb.append(b.getName()).append("\n");
        }

        append(sb, SEC_KITS);
        sb.append(csvSerializer.serializeEmergencyKit(team.getEmergencyKit())).append("\n");

        append(sb, SEC_PILOTS);
        for (Pilot p : team.getPilots()) {
            sb.append(csvSerializer.serializePilot(p)).append("\n");
        }

        append(sb, SEC_ENG);
        for (Engineer e : team.getEngineers()) {
            sb.append(csvSerializer.serializeEngineer(e)).append("\n");
        }

        append(sb, SEC_HISTORY);
        for (RaceResult r : history) {
            sb.append(csvSerializer.serializeRaceResult(r)).append("\n");
        }

        return sb.toString().stripTrailing();
    }

    // helpers

    private void append(StringBuilder sb, String section) {
        sb.append(section).append("\n");
    }

    // Путь к папке с .bin файлами болидов конкретного игрока.
    private File bolidDir(String playerName) {
        return new File(SAVES_ROOT + File.separator + playerName
                      + File.separator + BOLIDS_DIR);
    }


    private String safeName(String bolidName) {
        return bolidName.replaceAll("[^A-Za-zА-Яа-яЁё0-9_\\-]", "_");
    }

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
