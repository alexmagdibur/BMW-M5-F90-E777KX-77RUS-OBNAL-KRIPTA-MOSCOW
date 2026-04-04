package data;

import domain.Component;
import domain.ComponentType;

import java.util.List;

public class ComponentCatalog {

    public static List<Component> getEngines() {
        return List.of(
            new Component("Lada Priora 16-кл.", ComponentType.ENGINE,300_000, 60, 1),
            new Component("BMW B48 Turbo", ComponentType.ENGINE, 600_000, 80, 2),
            new Component("Porsche 9A2 Evo", ComponentType.ENGINE, 1_200_000, 95, 3)
        );
    }

    public static List<Component> getTransmissions() {
        return List.of(
            new Component("МКПП ВАЗ 2115 5-ст.", ComponentType.TRANSMISSION, 150_000, 55, 1),
            new Component("АКПП BMW ZF 8HP", ComponentType.TRANSMISSION, 300_000, 75, 2),
            new Component("AMT Porsche PDK", ComponentType.TRANSMISSION, 600_000, 90, 3)
        );
    }

    public static List<Component> getSuspensions() {
        return List.of(
            new Component("Стандартная подвеска", ComponentType.SUSPENSION, 100_000, 50, 1),
            new Component("Спортивная подвеска", ComponentType.SUSPENSION, 250_000, 70, 2),
            new Component("Адаптивная подвеска", ComponentType.SUSPENSION, 500_000, 88, 3)
        );
    }

    public static List<Component> getChassis() {
        return List.of(
            new Component("Стальное шасси", ComponentType.CHASSIS, 200_000, 55, 1),
            new Component("Алюминиевое шасси", ComponentType.CHASSIS, 450_000, 72, 2),
            new Component("Карбоновое шасси", ComponentType.CHASSIS, 900_000, 92, 3)
        );
    }

    public static List<Component> getAeroPackages() {
        return List.of(
            new Component("Стоковые обвесы", ComponentType.AERO_PACKAGE, 80_000, 50),
            new Component("Обвесы BMW M5 F90", ComponentType.AERO_PACKAGE, 200_000, 68),
            new Component("Lamborghini Avendator Aero",ComponentType.AERO_PACKAGE, 450_000, 85)
        );
    }

    public static List<Component> getTires() {
        return List.of(
            new Component("Pirelli P Zero Hard", ComponentType.TIRES, 60_000, 55),
            new Component("Pirelli P Zero Medium", ComponentType.TIRES, 100_000, 70),
            new Component("Pirelli P Zero Soft", ComponentType.TIRES, 140_000, 85)
        );
    }

    public static List<Component> getExtras() {
        return List.of(
            new Component("Тонировка в круг", ComponentType.EXTRA, 20_000, 15),
            new Component("Блатные номера Е777КХ 77RUS", ComponentType.EXTRA, 777_777, 77),
            new Component("Автозвук Pride Car Audio", ComponentType.EXTRA, 80_000, 30)
        );
    }

    public static List<Component> getByType(ComponentType type) {
        return switch (type) {
            case ENGINE -> getEngines();
            case TRANSMISSION -> getTransmissions();
            case SUSPENSION -> getSuspensions();
            case CHASSIS -> getChassis();
            case AERO_PACKAGE -> getAeroPackages();
            case TIRES -> getTires();
            case EXTRA -> getExtras();
        };
    }
}