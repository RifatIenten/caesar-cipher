package org.example.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FileManager читает и пишет файлы в кодировке CP1251.
 * Так твой «sample.txt» не будет вызывать MalformedInputException.
 */
public final class FileManager {

    private FileManager() { }   // приватный конструктор, чтобы не создавать экземпляры

    /** Возвращает BufferedReader в CP1251 для пути {@code input}. */
    public static BufferedReader newReader(Path input) throws IOException {
        return Files.newBufferedReader(input, Charset.forName("Cp1251"));
    }

    /** Возвращает BufferedWriter в CP1251 для пути {@code output}. */
    public static BufferedWriter newWriter(Path output) throws IOException {
        return Files.newBufferedWriter(output, Charset.forName("Cp1251"));
    }
}