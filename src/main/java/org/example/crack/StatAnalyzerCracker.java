package org.example.crack;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/**
 * StatAnalyzerCracker выполняет автоматический взлом шифра Цезаря
 * методом статистического анализа частот символов.
 * <p>
 * Принцип работы:
 * 1. Строим гистограмму частот букв русского алфавита в репрезентативном тексте (sample).
 * 2. Строим гистограмму частот тех же букв в зашифрованном тексте (input).
 * 3. Перебираем все возможные ключи (сдвиги) от 0 до N-1,
 *    для каждого вычисляем статистику χ² (хи-квадрат) между распределением encrypted и сдвинутого sample.
 * 4. Выбираем ключ, при котором χ² минимален (наиболее близкое распределение).
 * 5. С этим ключом расшифровываем входной файл и записываем результат в output.
 */
public class StatAnalyzerCracker {
    // Экземпляр класса, реализующего трансформацию потока (шифрование/дешифровка)
    private final CaesarCipher cipher = new CaesarCipher();

    /**
     * Выполняет статистический взлом шифра Цезаря.
     *
     * @param input     путь к зашифрованному файлу
     * @param sample    путь к репрезентативному незашифрованному тексту
     * @param output    путь для записи расшифрованного результата
     * @param validator утилита для проверки существования и прав доступа к файлам
     * @throws IOException если возникают ошибки ввода-вывода или валидации
     */
    public void crackByStatAnalysis(Path input, Path sample, Path output, Validator validator) throws IOException {
        // 1) Проверяем, что файлы доступны для чтения и записи
        validator.ensureFileReadable(input);
        validator.ensureFileReadable(sample);
        validator.ensureParentWritable(output);

        // 2) Считаем и нормализуем частоты символов в sample и encrypted
        double[] distSample    = buildNormalizedFrequencies(sample);
        double[] distEncrypted = buildNormalizedFrequencies(input);

        // 3) Перебираем все возможные ключи (сдвиги)
        int alphabetLength = org.example.cipher.Alphabet.length();
        double bestChi2 = Double.MAX_VALUE;  // минимальное значение χ²
        int bestKey = 0;                     // ключ, дающий минимальное χ²

        for (int key = 0; key < alphabetLength; key++) {
            // Вычисляем χ² между distEncrypted и distSample, сдвинутым на key
            double chi2 = computeChiSquared(distEncrypted, distSample, key);
            if (chi2 < bestChi2) {
                bestChi2 = chi2;
                bestKey = key;
            }
        }

        // 4) С помощью найденного ключа окончательно расшифровываем input → output
        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            // decrypt = false, т.к. в методе decrypt указывается second parameter false для расшифровки
            cipher.transformStream(r, w, bestKey, /*decrypt=*/ true);
        }

        // Сообщаем пользователю результат
        System.out.println("Найден ключ: " + bestKey + " (χ²=" + String.format("%.4f", bestChi2) + ")");
        System.out.println("Готово! Файл расшифрован в: " + output);
    }

    /**
     * Считывает файл и возвращает нормализованный массив частот букв.
     * Все символы вне алфавита игнорируются.
     * Сумма всех элементов массива = 1.
     *
     * @param path путь к текстовому файлу
     * @return массив относительных частот длиной N = Alphabet.length()
     * @throws IOException если ошибка чтения файла
     */
    private double[] buildNormalizedFrequencies(Path path) throws IOException {
        int n = org.example.cipher.Alphabet.length();
        double[] counts = new double[n];
        double total = 0;

        // Читаем файл посимвольно
        try (Reader r = FileManager.newReader(path)) {
            int ch;
            while ((ch = r.read()) != -1) {
                char c = (char) ch;
                // учитываем только те символы, которые есть в нашем алфавите
                if (org.example.cipher.Alphabet.contains(c)) {
                    int idx = org.example.cipher.Alphabet.indexOf(c);
                    counts[idx]++;
                    total++;
                }
            }
        }
        // Избегаем деления на ноль
        if (total < 1) total = 1;
        // Нормализуем: каждую частоту делим на общее число символов
        for (int i = 0; i < n; i++) {
            counts[i] /= total;
        }
        return counts;
    }

    /**
     * Вычисляет статистику χ² для заданного ключа:
     * χ² = Σ ((E_i - S_i)² / S_i)
     * где E_i = distEncrypted[i], S_i = distSample[(i+key) % N]
     *
     * @param distEncrypted массив относительных частот из зашифрованного текста
     * @param distSample    массив относительных частот из sample-текста
     * @param key           текущий ключ (сдвиг)
     * @return значение χ²
     */
    private double computeChiSquared(double[] distEncrypted, double[] distSample, int key) {
        int n = distSample.length;
        double chi2 = 0.0;
        for (int i = 0; i < n; i++) {
            double Ei = distEncrypted[i];
            double Si = distSample[(i + key) % n];
            // учитываем только ненулевые эталонные частоты
            if (Si > 0) {
                double diff = Ei - Si;
                chi2 += (diff * diff) / Si;
            }
        }
        return chi2;
    }
}
