package util;

public class Ansi {

    private static final String BOLD  = "\033[1m";
    private static final String RESET = "\033[0m";

    public static String bold(String text) {
        return BOLD + text + RESET;
    }
}