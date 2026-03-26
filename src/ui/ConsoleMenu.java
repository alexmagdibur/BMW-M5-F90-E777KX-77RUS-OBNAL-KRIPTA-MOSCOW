package ui;

public class ConsoleMenu {

    public void printMainMenu(int budget) {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("Бюджет команды: " + budget);
        System.out.println("1. Начать гонку");
        System.out.println("2. Купить комплектующие");
        System.out.println("3. Собрать болид");
        System.out.println("4. Нанять инженера");
        System.out.println("5. Нанять пилота");
        System.out.println("6. Просмотреть болид");
        System.out.println("7. Просмотреть пилотов");
        System.out.println("8. Просмотреть инженеров");
        System.out.println("9. Просмотреть статистику гонок");
        System.out.println("0. Выход");
        System.out.print("Выберите пункт: ");
    }
}