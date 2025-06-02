package org.example.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.example.cipher.Alphabet;

/**
 * Класс-утилита для валидации вводных данных:
 * - Проверка существования и читаемости файла
 * - Проверка, что путь для записи валиден (родительская директория существует)
 * - Проверка допустимости ключа (0 <= key < length)
 */
public class Validator {
    private final int alphabetLength;

    /**
     * Конструктор принимает длину алфавита (чтобы проверять диапазон ключей).
     * @param alphabetLength размер алфавита (обычно Alphabet.length())
     */
    public Validator(int alphabetLength) {
        this.alphabetLength = alphabetLength;
    }

    /**
     * Проверяет, что файл {@code path} существует и доступен для чтения.
     * @param path путь к файлу
     * @throws IOException если файл не существует или не является читаемым
     */
    public void ensureFileReadable(Path path) throws IOException {
        if (path == null) {
            throw new IOException("Путь к файлу не задан.");
        }
        if (!Files.exists(path)) {
            throw new IOException("Файл не существует: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Указанный путь не является файлом: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("Файл недоступен для чтения: " + path);
        }
    }

    /**
     * Проверяет, что родительская папка для пути {@code path} существует и доступна для записи.
     * Если родительской папки нет — кидает исключение.
     * @param path путь к файлу, который нужно создать для записи
     * @throws IOException если родительская папка не существует или не записываема
     */
    public void ensureParentWritable(Path path) throws IOException {
        if (path == null) {
            throw new IOException("Путь к файлу не задан.");
        }
        Path parent = path.getParent();
        if (parent == null) {
            throw new IOException("Неверный путь: нет родительской директории для " + path);
        }
        if (!Files.exists(parent)) {
            throw new IOException("Родительская папка не существует: " + parent);
        }
        if (!Files.isDirectory(parent)) {
            throw new IOException("Родительский путь не является папкой: " + parent);
        }
        if (!Files.isWritable(parent)) {
            throw new IOException("Нет прав на запись в папку: " + parent);
        }
    }

    /**
     * Проверяет, что ключ {@code key} допустим: 0 <= key < alphabetLength.
     * В противном случае кидает IOException.
     * @param key ключ для шифрования/дешифрования
     * @throws IOException если key вне допустимого диапазона
     */
    public void ensureKeyInRange(int key) throws IOException {
        if (key < 0 || key >= alphabetLength) {
            throw new IOException("Неверный ключ: " + key + ". Допустимо от 0 до " + (alphabetLength - 1) + ".");
        }
    }
}