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

public class Main {
    
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

    private static void encryptFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        System.out.print("Ключ (0-" + (Alphabet.length() - 1) + "): ");
        int key = Integer.parseInt(sc.nextLine().trim());

        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);
        VAL.ensureKeyInRange(key);

        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            CIPHER.transformStream(r, w, key, false);
        }
        System.out.println("Успех: зашифровано → " + output.getFileName());
    }

    private static void decryptFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        System.out.print("Ключ (0-" + (Alphabet.length() - 1) + "): ");
        int key = Integer.parseInt(sc.nextLine().trim());

        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);
        VAL.ensureKeyInRange(key);

        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            CIPHER.transformStream(r, w, key, true);
        }
        System.out.println("Успех: расшифровано → " + output.getFileName());
    }

    private static void bruteFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        VAL.ensureFileReadable(input);
        VAL.ensureParentWritable(output);

        BF.crackByBruteForce(input, output, VAL);
        System.out.println("Brute Force: все возможные варианты записаны в " + output.getFileName());
    }

    private static void statFlow(Scanner sc) throws IOException {
        System.out.print("Файл-источник     : ");
        Path input = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-образец     : ");
        Path sample = Paths.get(sc.nextLine().trim());

        System.out.print("Файл-назначение   : ");
        Path output = Paths.get(sc.nextLine().trim());

        VAL.ensureFileReadable(input);
        VAL.ensureFileReadable(sample);
        VAL.ensureParentWritable(output);

        STAT.crackByStatAnalysis(input, sample, output, VAL);
    }
}
