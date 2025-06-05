package org.example;

import org.example.cipher.CaesarCipher;
import org.example.cipher.Alphabet;
import org.example.io.FileManager;
import org.example.validation.Validator;
import org.example.crack.BruteForceCracker;
import org.example.crack.StatAnalyzerCracker;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Главный класс-консоль (CLI) программы.
 * Содержит текстовое меню с четырьмя режимами работы:
 *   1) Encrypt      – шифрование с указанным ключом
 *   2) Decrypt      – расшифровка по известному ключу
 *   3) Brute Force  – попытка «взлома» перебором всех ключей
 *   4) Stat Crack   – «взлом» методом статистического анализа
 *   0) Exit         – выход из программы
 *
 * При запуске данного класса прикрепляются все необходимые зависимости:
 *   - CaesarCipher для шифровки/дешифровки
 *   - Validator для проверки файлов и ключей
 *   - BruteForceCracker и StatAnalyzerCracker для атак
 */
public class Main {
    // Общие экземпляры для всех методов
    private static final CaesarCipher CIPHER = new CaesarCipher();
    private static final Validator VAL = new Validator(Alphabet.length());
    private static final BruteForceCracker BF = new BruteForceCracker();
    private static final StatAnalyzerCracker STAT = new StatAnalyzerCracker();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            // Меню
            System.out.println("\n==== Caesar Cipher CLI ====");
            System.out.println("1) Encrypt   2) Decrypt   3) Brute Force   4) Stat Crack   0) Exit");
            System.out.print("> ");
            String cmd = sc.nextLine().trim();

            try {
                switch (cmd) {
                    case "1":
                        encryptFlow(sc);
                        break;
                    case "2":
                        decryptFlow(sc);
                        break;
                    case "3":
                        bruteFlow(sc);
                        break;
                    case "4":
                        statFlow(sc);
                        break;
                    case "0":
                        System.out.println("Выход.");
                        return;
                    default:
                        System.out.println("Не понимаю команду, введите цифру 0–4.");
                }
            } catch (IOException ex) {
                System.err.println("Ошибка: " + ex.getMessage());
            }
        }
    }

    /**
     * Режим шифрования текста:
     * 1) ввод пути к исходному файлу
     * 2) ввод пути для результата
     * 3) ввод ключа
     * 4) шифрование и вывод сообщения об успехе
     */
    private static void encryptFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        System.out.print("Ключ (0-" + (Alphabet.length() - 1) + "): ");
        int key = Integer.parseInt(sc.nextLine().trim());

        // Валидация
        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);
        VAL.ensureKeyInRange(key);

        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            CIPHER.transformStream(r, w, key, false);
        }
        System.out.println("Успех: зашифровано → " + output.getFileName());
    }

    /**
     * Режим дешифровки при известном ключе:
     * 1) ввод пути к зашифрованному файлу
     * 2) ввод пути для результата
     * 3) ввод ключа
     * 4) дешифровка и вывод сообщения об успехе
     */
    private static void decryptFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        System.out.print("Ключ (0-" + (Alphabet.length() - 1) + "): ");
        int key = Integer.parseInt(sc.nextLine().trim());

        // Валидация
        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);
        VAL.ensureKeyInRange(key);

        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            CIPHER.transformStream(r, w, key, true);
        }
        System.out.println("Успех: расшифровано → " + output.getFileName());
    }

    /**
     * Режим brute force:
     * 1) ввод пути к зашифрованному файлу
     * 2) ввод (необязательно) пути к репрезентативному файлу – здесь мы его не используем
     * 3) ввод пути для результата
     * 4) перебор всех ключей и запись всех вариантов
     */
    private static void bruteFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        // Валидация
        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);

        BF.crackByBruteForce(input, output, VAL);
        System.out.println("Brute Force: все возможные варианты записаны в " + output.getFileName());
    }

    /**
     * Режим статистического анализа:
     * 1) ввод пути к зашифрованному файлу
     * 2) ввод пути к репрезентативному (незашифрованному) тексту
     * 3) ввод пути для результата
     * 4) статистический анализ для выбора ключа
     * 5) окончательная дешифровка с найденным ключом
     */
    private static void statFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-образец     : ");
        Path sample = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        // Валидация
        VAL.ensureFileReadable(input);
        VAL.ensureFileReadable(sample);
        VAL.ensureParentWritable(output);

        STAT.crackByStatAnalysis(input, sample, output, VAL);
    }
}