package org.example.cipher;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, задающий алфавит, используемый в шифре Цезаря.
 * Здесь мы определяем набор символов (русские буквы, знаки пунктуации и пробел),
 * а также быстрое получение индекса символа и самого символа по индексу.
 */
public final class Alphabet {
    // Определим алфавит как массив символов. Включаем:
    // русские буквы от 'а' до 'я', затем знаки . , « » " ' : ! ? и ПРОБЕЛ
    private static final char[] ALPHABET = {
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з',
            'и', 'к', 'л', 'м', 'н', 'о', 'п', 'р',
            'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш',
            'щ', 'ъ', 'ы', 'ь', 'э', 'я',
            '.', ',', '«', '»', '"', '\'', ':', '!', '?', ' '
    };

    // Карта для быстрого получения индекса символа в ALPHABET
    private static final Map<Character, Integer> INDEX_MAP = new HashMap<>();

    // Заполним INDEX_MAP при загрузке класса
    static {
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEX_MAP.put(ALPHABET[i], i);
        }
    }

    // Закрытый конструктор, чтобы никто не создавал экземпляров этого класса
    private Alphabet() { }

    /**
     * Возвращает длину алфавита (количество символов).
     * @return количество символов в алфавите
     */
    public static int length() {
        return ALPHABET.length;
    }

    /**
     * Проверяет, содержится ли символ {@code ch} в нашем алфавите.
     * @param ch символ для проверки
     * @return true, если символ есть в алфавите; false — иначе
     */
    public static boolean contains(char ch) {
        return INDEX_MAP.containsKey(ch);
    }

    /**
     * Возвращает индекс символа {@code ch} в алфавите.
     * Предполагается, что {@code ch} уже есть в алфавите (проверка делайте заранее).
     * @param ch символ
     * @return индекс (0..length()-1)
     */
    public static int indexOf(char ch) {
        return INDEX_MAP.get(ch);
    }

    /**
     * Возвращает символ по индексу {@code idx} в алфавите.
     * Предполагается, что 0 <= idx < length().
     * @param idx индекс
     * @return символ из алфавита
     */
    public static char charAt(int idx) {
        return ALPHABET[idx];
    }
}