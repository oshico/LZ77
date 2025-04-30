package io;

import model.LZ77Token;
import core.LZ77Decoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



public class FileDecoder {
    private final LZ77Decoder decoder;

    public FileDecoder(LZ77Decoder decoder) {
        this.decoder = decoder;
    }

    public long decodeFile(String inputFile , String outputFile) throws IOException {
        List<LZ77Token> tokens = readTokensFromFile(inputFile);

        long startTime = System.nanoTime();

        String decodedContent = decoder.decode(tokens);

        Files.write(Paths.get(outputFile), decodedContent.getBytes());

        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    public List<LZ77Token> readTokensFromFile(String inputFile) throws IOException {
        List<LZ77Token> tokens = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream(inputFile))) {
            int tokenCount = in.readInt();
            for (int i = 0; i < tokenCount; i++) {
                int distance = in.readInt();
                int length = in.readInt();
                char nextCharacter = in.readChar();
                tokens.add(new LZ77Token(distance,length,nextCharacter));
            }
        }
        return tokens;
    }
}
