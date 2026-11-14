// Вариант 9. Пункт таможенного оформления: событие «груз оформлен».
// Пункт знает только интерфейс ClearanceListener и не знает конкретные классы получателей.
import java.util.ArrayList;
import java.util.List;

public class ObserverAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        CustomsPoint customsPoint = new CustomsPoint("ПТО Брест-Северный");
        BoardDisplay boardDisplay = new BoardDisplay();
        ClearanceJournal clearanceJournal = new ClearanceJournal();
        NotificationMailing notificationMailing = new NotificationMailing();

        customsPoint.addListener(boardDisplay);
        customsPoint.addListener(clearanceJournal);
        customsPoint.addListener(notificationMailing);

        System.out.println("--- Груз ДТ-24091. Порядок: табло, журнал, рассылка ---");
        customsPoint.cargoCleared("ДТ-24091");

        System.out.println();
        System.out.println("--- Проверки подписки ---");
        check("после одного события в журнале ровно одна запись",
                clearanceJournal.recordCount() == 1);
        check("табло, журнал и рассылка получили один и тот же груз",
                "ДТ-24091".equals(boardDisplay.lastCargoNumber())
                        && "ДТ-24091".equals(clearanceJournal.lastCargoNumber())
                        && "ДТ-24091".equals(notificationMailing.lastCargoNumber()));

        System.out.println();
        System.out.println("--- Рассылка отписана, груз ДТ-24092 её уже не касается ---");
        customsPoint.removeListener(notificationMailing);
        customsPoint.cargoCleared("ДТ-24092");
        check("после отписки у рассылки новых записей нет",
                notificationMailing.recordCount() == 1
                        && !notificationMailing.hasCargo("ДТ-24092"));
        check("журнал при этом получил второе событие",
                clearanceJournal.recordCount() == 2
                        && clearanceJournal.hasCargo("ДТ-24092"));

        System.out.println();
        System.out.println("--- Четвёртый наблюдатель (АСУ) без правки CustomsPoint ---");
        AsuTransfer asuTransfer = new AsuTransfer();
        customsPoint.addListener(asuTransfer);
        customsPoint.cargoCleared("ДТ-24093");
        check("АСУ получила событие тем же addListener, класс пункта не менялся",
                asuTransfer.recordCount() == 1
                        && "ДТ-24093".equals(asuTransfer.lastCargoNumber())
                        && clearanceJournal.hasCargo("ДТ-24093"));
        check("отписанная рассылка не получила и третье событие",
                !notificationMailing.hasCargo("ДТ-24093"));

        System.out.println();
        System.out.println("Порядок получения события — порядок подписки в списке.");
        System.out.println("ДТ-24091: табло, журнал, рассылка.");
        System.out.println("ДТ-24092: табло, журнал (рассылка уже отписана).");
        System.out.println("ДТ-24093: табло, журнал, АСУ.");

        finishChecks();
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

/** Наблюдатель: любой получатель события «груз оформлен». */
interface ClearanceListener {
    void onCargoCleared(String pointName, String cargoNumber);
}

/** Субъект. Не знает, какие именно наблюдатели подписаны. */
class CustomsPoint {

    private final String pointName;
    private final List<ClearanceListener> listeners = new ArrayList<>();

    CustomsPoint(String pointName) {
        this.pointName = pointName;
    }

    void addListener(ClearanceListener listener) {
        listeners.add(listener);
    }

    void removeListener(ClearanceListener listener) {
        listeners.remove(listener);
    }

    void cargoCleared(String cargoNumber) {
        for (ClearanceListener listener : listeners) {
            listener.onCargoCleared(pointName, cargoNumber);
        }
    }
}

class BoardDisplay implements ClearanceListener {

    private final List<String> receivedCargo = new ArrayList<>();

    public void onCargoCleared(String pointName, String cargoNumber) {
        receivedCargo.add(cargoNumber);
        System.out.println("[табло]  Груз " + cargoNumber + " оформлен на пункте " + pointName);
    }

    int recordCount() {
        return receivedCargo.size();
    }

    String lastCargoNumber() {
        return receivedCargo.get(receivedCargo.size() - 1);
    }

    boolean hasCargo(String cargoNumber) {
        return receivedCargo.contains(cargoNumber);
    }
}

class ClearanceJournal implements ClearanceListener {

    private final List<String> records = new ArrayList<>();

    public void onCargoCleared(String pointName, String cargoNumber) {
        records.add(pointName + ": зафиксировано оформление груза " + cargoNumber);
        System.out.println("[журнал] " + pointName + ": зафиксировано оформление груза " + cargoNumber);
    }

    int recordCount() {
        return records.size();
    }

    String lastCargoNumber() {
        String lastRecord = records.get(records.size() - 1);
        int marker = lastRecord.lastIndexOf(' ');
        return lastRecord.substring(marker + 1);
    }

    boolean hasCargo(String cargoNumber) {
        for (String record : records) {
            if (record.contains(cargoNumber)) {
                return true;
            }
        }
        return false;
    }
}

class NotificationMailing implements ClearanceListener {

    private final List<String> receivedCargo = new ArrayList<>();

    public void onCargoCleared(String pointName, String cargoNumber) {
        receivedCargo.add(cargoNumber);
        System.out.println("[рассылка] Груз " + cargoNumber + " оформлен, пункт " + pointName);
    }

    int recordCount() {
        return receivedCargo.size();
    }

    String lastCargoNumber() {
        return receivedCargo.get(receivedCargo.size() - 1);
    }

    boolean hasCargo(String cargoNumber) {
        return receivedCargo.contains(cargoNumber);
    }
}

/** Четвёртый наблюдатель. Подключается через addListener, CustomsPoint не меняется. */
class AsuTransfer implements ClearanceListener {

    private final List<String> receivedCargo = new ArrayList<>();

    public void onCargoCleared(String pointName, String cargoNumber) {
        receivedCargo.add(cargoNumber);
        System.out.println("[АСУ]    В автоматизированную систему передано: "
                + pointName + ", груз " + cargoNumber);
    }

    int recordCount() {
        return receivedCargo.size();
    }

    String lastCargoNumber() {
        return receivedCargo.get(receivedCargo.size() - 1);
    }

    boolean hasCargo(String cargoNumber) {
        return receivedCargo.contains(cargoNumber);
    }
}
