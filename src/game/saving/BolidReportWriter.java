package saving;

import domain.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class BolidReportWriter {

    private static final String REPORTS_ROOT = "reports";

    public File writeReport(Bolid bolid, String playerName) throws IOException {
        File dir = new File(REPORTS_ROOT + File.separator + playerName);
        dir.mkdirs();

        String fileName = safeName(bolid.getName()) + "_report.json";
        File file = new File(dir, fileName);

        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(buildJson(bolid));
        }

        return file;
    }

    // JSON builder

    private String buildJson(Bolid bolid) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        appendString(sb, "name", bolid.getName(), true);
        appendBoolean(sb, "complete", bolid.isComplete(), true);
        appendInt(sb, "performanceScore", bolid.getPerformanceScore(), true);
        appendBoolean(sb, "hasWornComponents", bolid.hasWornComponents(), true);
        appendComponentArray(sb, "components", bolid.getComponents().values(), true);
        appendComponentArray(sb, "extras", bolid.getExtras(), true);
        appendWeaponArray(sb, "weapons", bolid.getWeapons().values(), false);
        sb.append("}\n");
        return sb.toString();
    }

    private void appendComponentArray(StringBuilder sb, String key,
                                      Collection<Component> items, boolean comma) {
        sb.append("  \"").append(key).append("\": ");
        if (items.isEmpty()) {
            sb.append("[]").append(comma ? ",\n" : "\n");
            return;
        }
        sb.append("[\n");
        int i = 0;
        for (Component c : items) {
            boolean last = (++i == items.size());
            sb.append("    {\n");
            appendString(sb, "name", c.getName(), true,  6);
            appendString(sb, "type", c.getType().name(), true,  6);
            appendInt(sb, "price", c.getPrice(), true,  6);
            appendInt(sb, "performanceValue", c.getPerformanceValue(), true,  6);
            appendInt(sb, "wear", c.getWear(), true,  6);
            appendInt(sb, "level", c.getLevel(), true,  6);
            appendBoolean(sb, "wornOut", c.isWornOut(), false, 6);
            sb.append("    }").append(last ? "\n" : ",\n");
        }
        sb.append("  ]").append(comma ? ",\n" : "\n");
    }

    private void appendWeaponArray(StringBuilder sb, String key, Collection<Weapon> items, boolean comma) {
        sb.append("  \"").append(key).append("\": ");
        if (items.isEmpty()) {
            sb.append("[]").append(comma ? ",\n" : "\n");
            return;
        }
        sb.append("[\n");
        int i = 0;
        for (Weapon w : items) {
            boolean last = (++i == items.size());
            sb.append("    {\n");
            appendString(sb, "name", w.getName(), true,  6);
            appendString(sb, "type", w.getType().name(), true,  6);
            appendInt(sb, "price", w.getPrice(), true,  6);
            appendInt(sb, "damage", w.getDamage(), true,  6);
            appendInt(sb, "level", w.getLevel(), false, 6);
            sb.append("    }").append(last ? "\n" : ",\n");
        }
        sb.append("  ]").append(comma ? ",\n" : "\n");
    }

    // field helpers (indent=2)

    private void appendString(StringBuilder sb, String key, String value, boolean comma) {
        appendString(sb, key, value, comma, 2);
    }

    private void appendBoolean(StringBuilder sb, String key, boolean value, boolean comma) {
        appendBoolean(sb, key, value, comma, 2);
    }

    private void appendInt(StringBuilder sb, String key, int value, boolean comma) {
        appendInt(sb, key, value, comma, 2);
    }

    // field helpers (custom indent)

    private void appendString(StringBuilder sb, String key, String value,
                              boolean comma, int indent) {
        sb.append(" ".repeat(indent))
          .append("\"").append(escapeJson(key)).append("\": ")
          .append("\"").append(escapeJson(value)).append("\"")
          .append(comma ? "," : "")
          .append("\n");
    }

    private void appendBoolean(StringBuilder sb, String key, boolean value,
                               boolean comma, int indent) {
        sb.append(" ".repeat(indent))
          .append("\"").append(escapeJson(key)).append("\": ")
          .append(value)
          .append(comma ? "," : "")
          .append("\n");
    }

    private void appendInt(StringBuilder sb, String key, int value,
                           boolean comma, int indent) {
        sb.append(" ".repeat(indent))
          .append("\"").append(escapeJson(key)).append("\": ")
          .append(value)
          .append(comma ? "," : "")
          .append("\n");
    }

    // escaping

    public static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // helper

    private String safeName(String name) {
        return name.replaceAll("[^A-Za-zА-Яа-яЁё0-9_\\-]", "_");
    }
}
