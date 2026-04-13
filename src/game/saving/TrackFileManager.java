package saving;

import domain.Track;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TrackFileManager {

    private static final String TRACKS_ROOT = "tracks";
    private static final String FILE_NAME   = "custom_tracks.csv";

    private final EntitySerializer serializer = new EntitySerializer();

    // сохраняет все пользовательские треки в tracks/{playerName}/custom_tracks.csv
    public void saveCustomTracks(List<Track> tracks, String playerName) {
        File dir = playerDir(playerName);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[TrackFileManager] Не удалось создать папку: " + dir.getPath());
            return;
        }
        File file = new File(dir, FILE_NAME);
        try (FileWriter fw = new FileWriter(file)) {
            for (Track t : tracks) {
                fw.write(serializer.serializeTrack(t) + "\n");
            }
        } catch (IOException e) {
            System.err.println("[TrackFileManager] Ошибка записи: " + e.getMessage());
        }
    }


    // загружает пользовательские треки из tracks/{playerName}/custom_tracks.csv
    // если файла нет — возвращает пустой список
    public List<Track> loadCustomTracks(String playerName) {
        List<Track> result = new ArrayList<>();
        File file = new File(playerDir(playerName), FILE_NAME);
        if (!file.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    result.add(serializer.deserializeTrack(line));
                }
            }
        } catch (IOException e) {
            System.err.println("[TrackFileManager] Ошибка чтения: " + e.getMessage());
        }
        return result;
    }

    private File playerDir(String playerName) {
        return new File(TRACKS_ROOT + File.separator + playerName);
    }
}
