package io;

import model.LZ77Token;
import core.LZ77Encoder;
import java.io.*;
import java.util.List;

/**
 * Utility class for encoding a text file using LZ77 compression.
 */
public class FileEncoder {
    private final LZ77Encoder encoder;

    public FileEncoder(LZ77Encoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Compresses an input text file into binary tokens.
     *
     * @param inputFile  the file to encode
     * @param outputFile the destination for the encoded output
     * @return encoding time in milliseconds
     */
    public long encodeFile(String inputFile, String outputFile) throws IOException {
        String content = readFileContent(inputFile);
        long startTime = System.nanoTime();
        List<LZ77Token> tokens = encoder.encode(content);
        writeFileContent(tokens, outputFile);
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    public String readFileContent(String inputFile) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public void writeFileContent(List<LZ77Token> tokens, String outputFile) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile))) {
            out.writeInt(tokens.size());
            for (LZ77Token token : tokens) {
                out.writeInt(token.getDistance());
                out.writeInt(token.getLength());
                out.writeChar(token.getNextCharacter());
            }
        }
    }
}
