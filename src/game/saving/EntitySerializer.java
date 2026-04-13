package saving;

import domain.*;

import java.util.ArrayList;
import java.util.List;

public class EntitySerializer {

    private static final String SEP      = ";";
    private static final String LIST_SEP = ",";

    // Team
    public String serializeTeam(Team team) {
        return team.getName() + SEP + team.getBudget();
    }

    public Team deserializeTeam(String line) {
        String[] p = split(line, 2);
        return new Team(p[0], Long.parseLong(p[1]));
    }

    // Component
    public String serializeComponent(Component c) {
        return c.getName() + SEP + c.getType().name() + SEP
             + c.getPrice() + SEP + c.getPerformanceValue() + SEP + c.getWear();
    }

    public Component deserializeComponent(String line) {
        String[] p = split(line, 5);
        ComponentType type = ComponentType.valueOf(p[1]);
        int wear = Integer.parseInt(p[4]);
        Component c = new Component(p[0], type, Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        c.setWear(wear);
        return c;
    }

    // Bolid
    public String serializeBolid(Bolid b) {
        StringBuilder mains = new StringBuilder();
        StringBuilder extras = new StringBuilder();

        for (Component c : b.getComponents().values()) {
            if (mains.length() > 0) mains.append(LIST_SEP);
            mains.append(c.getName());
        }
        for (Component c : b.getExtras()) {
            if (extras.length() > 0) extras.append(LIST_SEP);
            extras.append(c.getName());
        }
        return b.getName() + SEP + mains + SEP + extras;
    }


    public Bolid deserializeBolid(String line, List<Component> allComponents) {
        String[] p = split(line, 3);
        Bolid bolid = new Bolid(p[0]);

        if (!p[1].isEmpty()) {
            for (String name : p[1].split(LIST_SEP, -1)) {
                Component c = findByName(allComponents, name.trim());
                if (c != null) bolid.installComponent(c);
            }
        }
        if (!p[2].isEmpty()) {
            for (String name : p[2].split(LIST_SEP, -1)) {
                Component c = findByName(allComponents, name.trim());
                if (c != null) bolid.addExtra(c);
            }
        }
        return bolid;
    }

    // Pilot
    public String serializePilot(Pilot p) {
        return p.getName() + SEP + p.getSalary() + SEP + p.getSkill() + SEP + p.isWerewolf();
    }

    public Pilot deserializePilot(String line) {
        String[] p = split(line, 4);
        Pilot pilot = new Pilot(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        pilot.setWerewolf(Boolean.parseBoolean(p[3]));
        return pilot;
    }

    // Engineer
    public String serializeEngineer(Engineer e) {
        return e.getName() + SEP + e.getSalary() + SEP + e.getQualification() + SEP + e.isWerewolf();
    }

    public Engineer deserializeEngineer(String line) {
        String[] p = split(line, 4);
        Engineer eng = new Engineer(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        eng.setWerewolf(Boolean.parseBoolean(p[3]));
        return eng;
    }

    // RaceResult
    public String serializeRaceResult(RaceResult r) {
        return r.getTeamName() + SEP + r.getTime() + SEP + r.isPlayer() + SEP
             + r.isIncident() + SEP + r.getPosition();
    }

    public RaceResult deserializeRaceResult(String line) {
        String[] p = split(line, 5);
        String teamName = p[0];
        double time = Double.parseDouble(p[1]);
        boolean isPlayer = Boolean.parseBoolean(p[2]);
        boolean incident = Boolean.parseBoolean(p[3]);
        int position = Integer.parseInt(p[4]);

        RaceResult r = incident
                ? RaceResult.dnf(teamName, isPlayer)
                : new RaceResult(teamName, time, isPlayer);
        r.setPosition(position);
        return r;
    }

    // Track
    public String serializeTrack(Track track) {
        StringBuilder sb = new StringBuilder();
        for (TrackSection s : track.getSections()) {
            if (sb.length() > 0) sb.append(LIST_SEP);
            sb.append(s.getType().name()).append(":").append(s.getLength());
        }
        return track.getName() + SEP + sb;
    }

    public Track deserializeTrack(String line) {
        int idx = line.indexOf(SEP);
        if (idx < 0) throw new IllegalArgumentException("Неверный формат трека: \"" + line + "\"");
        String name = line.substring(0, idx);
        String sectionsStr = line.substring(idx + 1);
        List<TrackSection> sections = new ArrayList<>();
        if (!sectionsStr.isEmpty()) {
            for (String part : sectionsStr.split(LIST_SEP, -1)) {
                String[] sp = part.split(":", 2);
                SectionType type = SectionType.valueOf(sp[0].trim());
                int length = Integer.parseInt(sp[1].trim());
                sections.add(new TrackSection(type, length));
            }
        }
        return new Track(name, sections);
    }

    // helpers
    private String[] split(String line, int expectedParts) {
        String[] parts = line.split(SEP, -1);
        if (parts.length < expectedParts) {
            throw new IllegalArgumentException(
                "Ожидалось " + expectedParts + " полей, получено " + parts.length + ": \"" + line + "\"");
        }
        return parts;
    }

    private Component findByName(List<Component> list, String name) {
        for (Component c : list) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }
}
