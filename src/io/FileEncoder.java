package io;

import model.LZ77Token;
import core.LZ77Encoder;

import java.io.*;
import java.util.List;


public class FileEncoder {
    private final LZ77Encoder encoder;

    public FileEncoder(LZ77Encoder encoder) {
        this.encoder = encoder;
    }

    public long encodeFile(String inputFile, String outputFile) throws IOException {
        String content = readFileContent(inputFile);

        long startTime = System.nanoTime();

        List<LZ77Token> tokens = encoder.encode(content);

        WriteFileContent(tokens,outputFile);

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    public String readFileContent(String inputFile) throws IOException {
        StringBuilder content = new StringBuilder();
       try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))){
           String line;
           while ((line = reader.readLine()) != null) {
               content.append(line).append("\n");
           }
       }
       return content.toString();
    }

    public void WriteFileContent(List<LZ77Token> tokens, String outputFile) throws IOException {
        try ( DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile))){
            out.writeInt(tokens.size());
            for (LZ77Token token : tokens) {
                out.writeInt(token.getDistance());
                out.writeInt(token.getLength());
                out.writeChar(token.getNextCharacter());
            }
        }
    }
}
