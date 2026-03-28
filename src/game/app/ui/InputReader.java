package game.app.ui;

import java.util.Scanner;

public class InputReader {

    private final Scanner scanner;

    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    public int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.println("Введите число:");
            scanner.next();
        }
        return scanner.nextInt();
    }
}