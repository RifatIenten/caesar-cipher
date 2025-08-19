package org.example.cipher;

import java.util.HashMap;
import java.util.Map;


public final class Alphabet {

    private static final char[] ALPHABET = {
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з',
            'и', 'к', 'л', 'м', 'н', 'о', 'п', 'р',
            'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш',
            'щ', 'ъ', 'ы', 'ь', 'э', 'я',
            '.', ',', '«', '»', '"', '\'', ':', '!', '?', ' '
    };


    private static final Map<Character, Integer> INDEX_MAP = new HashMap<>();

    static {
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEX_MAP.put(ALPHABET[i], i);
        }
    }

    private Alphabet() { }

    public static int length() {
        return ALPHABET.length;
    }
    public static boolean contains(char ch) {
        return INDEX_MAP.containsKey(ch);
    }

    public static int indexOf(char ch) {
        return INDEX_MAP.get(ch);
    }

    public static char charAt(int idx) {
        return ALPHABET[idx];
    }
}
