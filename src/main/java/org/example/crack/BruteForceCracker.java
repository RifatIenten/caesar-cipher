package org.example.crack;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Реализация атаки «brute force» (перебором всех возможных ключей).
 * Для каждого ключа расшифровываем входной файл и записываем в выходной файл.
 * Пользователь получает возможность посмотреть результаты (можно вручную выбрать нужный).
 */
public class BruteForceCracker {
    private final CaesarCipher cipher = new CaesarCipher();

    /**
     * Перебирает все возможные ключи 0..alphabetLength-1, расшифровывает входной файл
     * и записывает результат в выходной файл с указанием ключа в виде комментариев.
     * Т.к. в файле могут содержаться любые символы, здесь не делаем никакой автоматической проверки --
     * пользователь вручную должен будет просмотреть выходной файл и выбрать «читаемый» вариант.
     *
     * @param input     путь к зашифрованному файлу
     * @param output    путь к выходному файлу, где будут записи вида:
     *                  ----- Key = X -----
     *                  ... расшифровка ...
     * @param validator валидатор для проверки входных данных
     * @throws IOException при ошибках ввода-вывода или верификации
     */
    public void crackByBruteForce(Path input, Path output, Validator validator) throws IOException {
        // Проверяем входной файл и возможность записи
        validator.ensureFileReadable(input);
        validator.ensureParentWritable(output);

        // Создаём ридер/райтер
        try (Reader reader = FileManager.newReader(input);
             Writer writer = FileManager.newWriter(output)) {

            int alphabetLength = org.example.cipher.Alphabet.length();

            // Цикл по всем ключам
            for (int key = 0; key < alphabetLength; key++) {
                // Если ключ валидный, выводим разделитель
                writer.write("----- Key = " + key + " -----\n");

                // Для каждого ключа создаём новый ридер, т.к. после чтения указатель окажется в конце файла
                try (Reader r2 = FileManager.newReader(input)) {
                    // Расшифровываем (decrypt=true)
                    cipher.transformStream(r2, writer, key, true);
                }

                // Перевод строки между вариантами
                writer.write("\n\n");
            }
            writer.flush();
        }
    }
}