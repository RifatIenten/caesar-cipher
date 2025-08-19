package org.example.crack;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;


public class StatAnalyzerCracker {
    
    private final CaesarCipher cipher = new CaesarCipher();

    public void crackByStatAnalysis(Path input, Path sample, Path output, Validator validator) throws IOException {

        validator.ensureFileReadable(input);
        validator.ensureFileReadable(sample);
        validator.ensureParentWritable(output);

 
        double[] distSample    = buildNormalizedFrequencies(sample);
        double[] distEncrypted = buildNormalizedFrequencies(input);


        int alphabetLength = org.example.cipher.Alphabet.length();
        double bestChi2 = Double.MAX_VALUE;  
        int bestKey = 0;                     

        for (int key = 0; key < alphabetLength; key++) {
            double chi2 = computeChiSquared(distEncrypted, distSample, key);
            if (chi2 < bestChi2) {
                bestChi2 = chi2;
                bestKey = key;
            }
        }


        try (Reader r = FileManager.newReader(input);
             Writer w = FileManager.newWriter(output)) {
            cipher.transformStream(r, w, bestKey, /*decrypt=*/ true);
        }

   
        System.out.println("Найден ключ: " + bestKey + " (χ²=" + String.format("%.4f", bestChi2) + ")");
        System.out.println("Готово! Файл расшифрован в: " + output);
    }

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

    private double computeChiSquared(double[] distEncrypted, double[] distSample, int key) {
        int n = distSample.length;
        double chi2 = 0.0;
        for (int i = 0; i < n; i++) {
            double Ei = distEncrypted[i];
            double Si = distSample[(i + key) % n];
            if (Si > 0) {
                double diff = Ei - Si;
                chi2 += (diff * diff) / Si;
            }
        }
        return chi2;
    }
}
