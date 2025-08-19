package org.example.cipher;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class CaesarCipher {

    public void transformStream(Reader reader, Writer writer, int key, boolean decrypt) throws IOException {
   
        int alphabetLength = Alphabet.length();
        key = ((key % alphabetLength) + alphabetLength) % alphabetLength;

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            if (Alphabet.contains(ch)) {
                int idx = Alphabet.indexOf(ch);
                int shifted;
                if (!decrypt) {

                    shifted = (idx + key) % alphabetLength;
                } else {
           
                    shifted = (idx - key + alphabetLength) % alphabetLength;
                }
                writer.write(Alphabet.charAt(shifted));
            } else {
 
                writer.write(ch);
            }
        }
        writer.flush();
    }
}
