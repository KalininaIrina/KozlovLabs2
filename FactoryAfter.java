// Вариант 9. Пункт таможенного оформления: фабрика создаёт тип декларации.
// Вызывающий код работает с DeclarationCreator и Declaration и не разбирает тип строкой.
public class FactoryAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        DeclarationCreator importCreator = new ImportDeclarationCreator();
        DeclarationCreator exportCreator = new ExportDeclarationCreator();
        DeclarationCreator transitCreator = new TransitDeclarationCreator();

        Declaration importDeclaration = importCreator.createDeclaration(1250.0);
        Declaration exportDeclaration = exportCreator.createDeclaration(860.5);
        Declaration transitDeclaration = transitCreator.createDeclaration(430.0);

        System.out.println("--- Декларации, собранные через фабричный метод ---");
        System.out.println(importDeclaration.description());
        System.out.println(exportDeclaration.description());
        System.out.println(transitDeclaration.description());

        System.out.println();
        System.out.println("--- Проверки трёх основных типов ---");
        check("импортная декларация описана и содержит таможенную стоимость",
                describedWithValue(importDeclaration, 1250.0));
        check("экспортная декларация описана и содержит таможенную стоимость",
                describedWithValue(exportDeclaration, 860.5));
        check("транзитная декларация описана и содержит таможенную стоимость",
                describedWithValue(transitDeclaration, 430.0));
        check("создатель импортной декларации вернул ImportDeclaration",
                importDeclaration instanceof ImportDeclaration);
        check("создатель экспортной декларации вернул ExportDeclaration",
                exportDeclaration instanceof ExportDeclaration);
        check("создатель транзитной декларации вернул TransitDeclaration",
                transitDeclaration instanceof TransitDeclaration);

        System.out.println();
        System.out.println("--- Четвёртый тип новым классом, старые классы не менялись ---");
        DeclarationCreator temporaryCreator = new TemporaryAdmissionCreator();
        Declaration temporaryDeclaration = temporaryCreator.createDeclaration(210.0);
        System.out.println(temporaryDeclaration.description());
        check("декларация временного ввоза работает наравне с остальными",
                temporaryDeclaration instanceof TemporaryAdmissionDeclaration
                        && describedWithValue(temporaryDeclaration, 210.0));
        check("описания трёх основных типов различаются",
                !importDeclaration.description().equals(exportDeclaration.description())
                        && !exportDeclaration.description().equals(transitDeclaration.description()));

        finishChecks();
    }

    static boolean describedWithValue(Declaration declaration, double customsValue) {
        String text = declaration.description();
        return text != null
                && !text.isBlank()
                && text.contains(String.valueOf(customsValue))
                && declaration.customsValue() == customsValue;
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static void finishChecks() {
        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }
}

/** Продукт: общая таможенная декларация. Конкретный тип выбирает создатель. */
interface Declaration {
    String description();
    double customsValue();
}

/** Создатель с фабричным методом. Подкласс решает, какой класс декларации создать. */
abstract class DeclarationCreator {
    abstract Declaration createDeclaration(double customsValue);
}

class ImportDeclarationCreator extends DeclarationCreator {
    Declaration createDeclaration(double customsValue) {
        return new ImportDeclaration(customsValue);
    }
}

class ExportDeclarationCreator extends DeclarationCreator {
    Declaration createDeclaration(double customsValue) {
        return new ExportDeclaration(customsValue);
    }
}

class TransitDeclarationCreator extends DeclarationCreator {
    Declaration createDeclaration(double customsValue) {
        return new TransitDeclaration(customsValue);
    }
}

/** Четвёртый тип: добавлен отдельным создателем, ранее написанные классы не правились. */
class TemporaryAdmissionCreator extends DeclarationCreator {
    Declaration createDeclaration(double customsValue) {
        return new TemporaryAdmissionDeclaration(customsValue);
    }
}

class ImportDeclaration implements Declaration {
    private final double customsValue;

    ImportDeclaration(double customsValue) {
        this.customsValue = customsValue;
    }

    public String description() {
        return "Импортная декларация: таможенная стоимость " + customsValue
                + " тыс. руб., ввоз товаров на таможенную территорию";
    }

    public double customsValue() {
        return customsValue;
    }
}

class ExportDeclaration implements Declaration {
    private final double customsValue;

    ExportDeclaration(double customsValue) {
        this.customsValue = customsValue;
    }

    public String description() {
        return "Экспортная декларация: таможенная стоимость " + customsValue
                + " тыс. руб., вывоз товаров с таможенной территории";
    }

    public double customsValue() {
        return customsValue;
    }
}

class TransitDeclaration implements Declaration {
    private final double customsValue;

    TransitDeclaration(double customsValue) {
        this.customsValue = customsValue;
    }

    public String description() {
        return "Транзитная декларация: таможенная стоимость " + customsValue
                + " тыс. руб., перемещение товаров через таможенную территорию";
    }

    public double customsValue() {
        return customsValue;
    }
}

class TemporaryAdmissionDeclaration implements Declaration {
    private final double customsValue;

    TemporaryAdmissionDeclaration(double customsValue) {
        this.customsValue = customsValue;
    }

    public String description() {
        return "Декларация временного ввоза: таможенная стоимость " + customsValue
                + " тыс. руб., товары с обязательством обратного вывоза";
    }

    public double customsValue() {
        return customsValue;
    }
}
