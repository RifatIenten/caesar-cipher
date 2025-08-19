package org.example.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileManager {

    private FileManager() { }  

    public static BufferedReader newReader(Path input) throws IOException {
        return Files.newBufferedReader(input, Charset.forName("Cp1251"));
    }

    public static BufferedWriter newWriter(Path output) throws IOException {
        return Files.newBufferedWriter(output, Charset.forName("Cp1251"));
    }
}
