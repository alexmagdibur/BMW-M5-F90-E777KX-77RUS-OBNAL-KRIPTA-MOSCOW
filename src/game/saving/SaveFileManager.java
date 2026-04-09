package saving;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveFileManager {

    private static final String SAVES_ROOT = "saves";

    // ── write ─────────────────────────────────────────────────────────────────

    /**
     * Создаёт папку saves/{playerName}/ (если не существует) и записывает content в файл.
     *
     * @param playerName имя игрока (название подпапки)
     * @param fileName   имя файла внутри папки
     * @param content    текстовое содержимое
     */
    public void writeToFile(String playerName, String fileName, String content) {
        File dir = playerDir(playerName);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[SaveFileManager] Не удалось создать папку: " + dir.getPath());
            return;
        }

        File file = new File(dir, fileName);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (IOException e) {
            System.err.println("[SaveFileManager] Ошибка записи файла \"" + file.getPath() + "\": " + e.getMessage());
        }
    }

    // ── read ──────────────────────────────────────────────────────────────────

    /**
     * Читает файл saves/{playerName}/{fileName} целиком и возвращает содержимое.
     * Возвращает пустую строку, если файл не найден или произошла ошибка.
     *
     * @return содержимое файла или пустая строка при ошибке
     */
    public String readFromFile(String playerName, String fileName) {
        File file = new File(playerDir(playerName), fileName);
        if (!file.exists()) {
            System.err.println("[SaveFileManager] Файл не найден: " + file.getPath());
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("[SaveFileManager] Ошибка чтения файла \"" + file.getPath() + "\": " + e.getMessage());
            return "";
        }

        // убираем лишний перевод строки в конце
        String result = sb.toString();
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    // ── list ──────────────────────────────────────────────────────────────────

    /**
     * Возвращает список имён файлов в папке saves/{playerName}/.
     * Если папка не существует — возвращает пустой список.
     */
    public List<String> listSaveFiles(String playerName) {
        File dir = playerDir(playerName);
        List<String> names = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            return names;
        }

        File[] files = dir.listFiles(File::isFile);
        if (files == null) {
            System.err.println("[SaveFileManager] Не удалось прочитать содержимое папки: " + dir.getPath());
            return names;
        }

        for (File f : files) {
            names.add(f.getName());
        }
        return names;
    }

    // ── exists ────────────────────────────────────────────────────────────────

    /**
     * Проверяет существование файла saves/{playerName}/{fileName}.
     */
    public boolean saveExists(String playerName, String fileName) {
        return new File(playerDir(playerName), fileName).isFile();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private File playerDir(String playerName) {
        return new File(SAVES_ROOT + File.separator + playerName);
    }
}
