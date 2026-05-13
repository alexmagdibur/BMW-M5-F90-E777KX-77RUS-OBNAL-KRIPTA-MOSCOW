package ui;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleInput {

    private static Scanner scanner = new Scanner(System.in);

    public static int readInt(String prompt) {
        System.out.print(prompt);
        try {
            while (!scanner.hasNextInt()) {
                if (!scanner.hasNextLine()) return -1;
                scanner.nextLine();
                System.out.print("Введите номер: ");
            }
            int value = scanner.nextInt();
            if (scanner.hasNextLine()) scanner.nextLine();
            return value;
        } catch (NoSuchElementException e) {
            return -1;
        }
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
