package game.ui;

import java.util.Scanner;

public class ConsoleInput {

    private static final Scanner scanner = new Scanner(System.in);

    public static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.print("Enter a number: ");
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
}
