package org.example.cipher;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Класс, реализующий сам алгоритм шифра Цезаря.
 * Метод transformStream читает данные из Reader, преобразует каждый символ
 * (если он есть в алфавите), и записывает результат в Writer.
 * Если символ не найден в алфавите — он просто копируется без изменений.
 */
public class CaesarCipher {
    /**
     * Трансформирует (шифрует или расшифровывает) поток символов.
     * @param reader  Reader для чтения исходного текста
     * @param writer  Writer для записи результата
     * @param key     сдвиг (если decrypt==false, то сдвигаем вправо, иначе влево)
     * @param decrypt если true — расшифровываем, иначе — шифруем
     * @throws IOException при ошибках ввода-вывода
     */
    public void transformStream(Reader reader, Writer writer, int key, boolean decrypt) throws IOException {
        // Нормализуем ключ (на случай, если key >= length или отрицательный)
        int alphabetLength = Alphabet.length();
        key = ((key % alphabetLength) + alphabetLength) % alphabetLength;

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            if (Alphabet.contains(ch)) {
                int idx = Alphabet.indexOf(ch);
                int shifted;
                if (!decrypt) {
                    // Шифрование: сдвигаем вправо
                    shifted = (idx + key) % alphabetLength;
                } else {
                    // Расшифровка: сдвигаем влево
                    shifted = (idx - key + alphabetLength) % alphabetLength;
                }
                writer.write(Alphabet.charAt(shifted));
            } else {
                // Если символ не содержится в алфавите — копируем без изменений
                writer.write(ch);
            }
        }
        writer.flush();
    }
}