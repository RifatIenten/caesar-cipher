package org.example.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.example.cipher.Alphabet;

public class Validator {
    private final int alphabetLength;


    public Validator(int alphabetLength) {
        this.alphabetLength = alphabetLength;
    }

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

    public void ensureKeyInRange(int key) throws IOException {
        if (key < 0 || key >= alphabetLength) {
            throw new IOException("Неверный ключ: " + key + ". Допустимо от 0 до " + (alphabetLength - 1) + ".");
        }
    }
}
