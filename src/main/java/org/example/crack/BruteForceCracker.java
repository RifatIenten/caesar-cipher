package org.example.crack;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceCracker {
    private final CaesarCipher cipher = new CaesarCipher();
    private static final Set<Character> VOWELS = Set.of(
            'а','е','ё','и','о','у','ы','э','ю','я',
            'А','Е','Ё','И','О','У','Ы','Э','Ю','Я'
    );

    private static final double PENALTY = 0.5;

    public void crackByBruteForce(Path input, Path output, Validator validator) throws IOException {
        validator.ensureFileReadable(input);
        validator.ensureParentWritable(output);

        int N = org.example.cipher.Alphabet.length();
        int bestKey = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestText = "";

        for (int key = 0; key < N; key++) {
            String text = decryptToString(input, key);
            double score = scoreBySyllables(text);
            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
                bestText = text;
            }
        }

        try (Writer w = FileManager.newWriter(output)) {
            w.write("Найден ключ: " + bestKey + "\n");
            w.write(String.format("Оценка слогов: %.3f%n%n", bestScore));
            w.write(bestText);
        }
    }

    private String decryptToString(Path input, int key) throws IOException {
        try (Reader r = FileManager.newReader(input)) {
            StringWriter buf = new StringWriter();
            cipher.transformStream(r, buf, key, /*decrypt*/ true);
            return buf.toString();
        }
    }

    private double scoreBySyllables(String text) {
        Matcher m = Pattern.compile("[А-ЯЁа-яё]+").matcher(text);
        double totalSyllables = 0, totalBad = 0;
        while (m.find()) {
            String word = m.group();
            List<String> sylls = splitToSyllables(word);
            totalSyllables += sylls.size();

            if (sylls.isEmpty()) totalBad += 1;
        }
        return totalSyllables - totalBad * PENALTY;
    }

    private List<String> splitToSyllables(String word) {
        List<String> result = new ArrayList<>();
        int i = 0, n = word.length();
        while (i < n) {

            int start = i;
            while (i < n && !VOWELS.contains(word.charAt(i))) i++;

            int vcount = 0;
            while (i < n && VOWELS.contains(word.charAt(i)) && vcount < 2) {
                i++; vcount++;
            }
            if (vcount == 0) {
                break;
            }

            while (i < n && !VOWELS.contains(word.charAt(i))) i++;
            result.add(word.substring(start, i));
        }
        return result;
    }
}
