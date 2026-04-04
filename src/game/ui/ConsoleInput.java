package ui;

import java.util.Scanner;

public class ConsoleInput {

    private static Scanner scanner = new Scanner(System.in);

    public static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.print("Введите номер: ");
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static void close() {
        scanner.close();
    }

    public static void resetScanner() {
        scanner = new Scanner(System.in);
    }
}
