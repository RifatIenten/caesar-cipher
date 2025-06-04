package org.example.crack;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Реализация статистического анализа для подбора ключа Цезаря.
 * Мы вычисляем частоту символов (гистограмму) в «репрезентативном» тексте (sample),
 * а затем для каждого возможного сдвига считаем χ² (квадрат отклонения) между
 * распределением символов в зашифрованном тексте и сдвинутым репрезентативным.
 * Ключ с минимальным χ² считается наиболее вероятным.
 */
public class StatAnalyzerCracker {
    private final CaesarCipher cipher = new CaesarCipher();

    /**
     * Основной метод для статистического взлома:
     * 1. Проверяем входные файлы
     * 2. Строим распределение символов (частоты) для sample (репрезентативный текст)
     * 3. Строим распределение символов для encrypted (зашифрованный текст)
     * 4. Для каждого key (0..N-1) смещаем распределение sample на key и вычисляем χ²
     * 5. Выбираем key с минимальным χ², повторно расшифровываем входной файл и записываем результат
     *
     * @param input       путь к зашифрованному файлу
     * @param sample      путь к репрезентативному тексту (незашифрованному)
     * @param output      путь, куда записать расшифрованный результат
     * @param validator   валидатор для проверки путей и ключей
     * @throws IOException при ошибках чтения/записи или валидации
     */
    public void crackByStatAnalysis(Path input, Path sample, Path output, Validator validator) throws IOException {
        // Проверяем, что файлы доступны
        validator.ensureFileReadable(input);
        validator.ensureFileReadable(sample);
        validator.ensureParentWritable(output);

        // Считаем частоты символов в репрезентативном тексте (sample) и в зашифрованном (input)
        Map<Character, Long> freqSample = buildFrequencyMap(sample);
        Map<Character, Long> freqEncrypted = buildFrequencyMap(input);

        int alphabetLength = org.example.cipher.Alphabet.length();

        // Преобразуем freqSample и freqEncrypted в массивы double для удобства матем. операций
        double[] distSample = mapToArray(freqSample);
        double[] distEncrypted = mapToArray(freqEncrypted);

        // Нормализуем частоты (переведём в относительные частоты)
        normalize(distSample);
        normalize(distEncrypted);

        // Выберем ключ с минимальным χ²
        double minChi2 = Double.MAX_VALUE;
        int bestKey = 0;
        for (int key = 0; key < alphabetLength; key++) {
            double chi2 = computeChiSquared(distEncrypted, distSample, key);
            if (chi2 < minChi2) {
                minChi2 = chi2;
                bestKey = key;
            }
        }

        // Вывожу в консоль найденный ключ
        System.out.println("Статистический анализ выбрал ключ → " + bestKey);

        // Делаем окончательную расшифровку с этим ключом и записываем в файл
        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            cipher.transformStream(r, w, bestKey, true);
        }
        System.out.println("Готово! Файл расшифрован в: " + output);
    }

    /**
     * Считает число вхождений каждого символа алфавита в файле {@code path}.
     * Все символы, не входящие в алфавит, игнорируются.
     *
     * @param path путь к текстовому файлу
     * @return карта с ключом = символ, значением = количество вхождений
     * @throws IOException если не удалось прочитать файл
     */
    private Map<Character, Long> buildFrequencyMap(Path path) throws IOException {
        Map<Character, Long> freqMap = new HashMap<>();
        try (Reader reader = FileManager.newReader(path)) {
            int r;
            while ((r = reader.read()) != -1) {
                char ch = (char) r;
                if (org.example.cipher.Alphabet.contains(ch)) {
                    freqMap.put(ch, freqMap.getOrDefault(ch, 0L) + 1);
                }
            }
        }
        return freqMap;
    }

    /**
     * Преобразует карту частот {@code freqMap} в массив размером Alphabet.length(),
     * где index = индекс символа в алфавите, а value = число вхождений (или 0.0, если нет).
     *
     * @param freqMap карта частот символов
     * @return массив double с абсолютными количествами (еще не нормализован)
     */
    private double[] mapToArray(Map<Character, Long> freqMap) {
        int n = org.example.cipher.Alphabet.length();
        double[] arr = new double[n];
        for (Map.Entry<Character, Long> entry : freqMap.entrySet()) {
            char ch = entry.getKey();
            int idx = org.example.cipher.Alphabet.indexOf(ch);
            arr[idx] = entry.getValue();
        }
        return arr;
    }

    /**
     * Нормализует массив частот так, чтобы сумма всех элементов стала равна 1.0.
     *
     * @param arr массив частот (будет изменён in-place)
     */
    private void normalize(double[] arr) {
        double sum = 0.0;
        for (double v : arr) {
            sum += v;
        }
        if (sum <= 0.0) return; // если файл пуст или нет совпадающих символов, оставляем всё как есть
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i] / sum;
        }
    }

    /**
     * Вычисляет χ² (хи-квадрат) между distEncrypted и смещенной на ключ distSample.
     * Формула: χ² = Σ ((Ei - Si)² / Si), где Ei = distEncrypted[i], Si = distSample[(i+key)%n].
     *
     * @param distEncrypted массив относительных частот из зашифрованного текста
     * @param distSample    массив относительных частот из репрезентативного текста
     * @param key           текущий ключ (сдвиг)
     * @return значение χ²
     */
    private double computeChiSquared(double[] distEncrypted, double[] distSample, int key) {
        int n = distSample.length;
        double chi2 = 0.0;
        for (int i = 0; i < n; i++) {
            // Si — частота символа с учётом сдвига
            double Si = distSample[(i + key) % n];
            double Ei = distEncrypted[i];
            if (Si > 0) {
                double diff = Ei - Si;
                chi2 += (diff * diff) / Si;
            }
            // Если Si == 0, пропускаем, чтобы не делить на ноль
        }
        return chi2;
    }
}