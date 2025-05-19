package io;

import model.LZ77Token;
import core.LZ77Decoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for decoding a file compressed with LZ77.
 */
public class FileDecoder {
    private final LZ77Decoder decoder;

    public FileDecoder(LZ77Decoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Decodes an input file and writes the decoded content to an output file.
     *
     * @param inputFile  the compressed file
     * @param outputFile the destination output file
     * @return decoding time in nanoseconds
     */
    public long decodeFile(String inputFile, String outputFile) throws IOException {
        List<LZ77Token> tokens = readEfficientFormat(inputFile);
        long startTime = System.nanoTime();
        String decodedContent = decoder.decode(tokens);
        Files.write(Paths.get(outputFile), decodedContent.getBytes());
        return System.nanoTime() - startTime;
    }

    /**
     * Reads tokens from a file using the efficient binary format.
     */
    public List<LZ77Token> readEfficientFormat(String inputFile) throws IOException {
        List<LZ77Token> tokens = new ArrayList<>();

        try (BitInputStream in = new BitInputStream(new FileInputStream(inputFile))) {
            int tokenCount = in.readInt();

            for (int i = 0; i < tokenCount; i++) {
                int flag = in.readBit();

                if (flag == 0) {
                    // Literal token
                    int nextChar = in.readByte() & 0xFF;
                    tokens.add(new LZ77Token(0, 0, (char) nextChar));
                } else {
                    // Match token
                    int distance = readVariableLength(in);
                    int length = readVariableLength(in);
                    int nextChar = in.readByte() & 0xFF;

                    tokens.add(new LZ77Token(distance, length, (char) nextChar));
                }
            }
        }

        return tokens;
    }

    /**
     * Reads tokens from a file using the original inefficient format (kept for compatibility).
     */
    public List<LZ77Token> readTokensFromFile(String inputFile) throws IOException {
        List<LZ77Token> tokens = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream(inputFile))) {
            int tokenCount = in.readInt();
            for (int i = 0; i < tokenCount; i++) {
                int distance = in.readInt();
                int length = in.readInt();
                char nextCharacter = in.readChar();
                tokens.add(new LZ77Token(distance, length, nextCharacter));
            }
        }
        return tokens;
    }

    /**
     * Reads a value using variable-length encoding.
     */
    private int readVariableLength(BitInputStream in) throws IOException {
        int firstByte = in.readByte() & 0xFF;

        // Check if high bit is set
        if ((firstByte & 0x80) == 0) {
            // Small value: just return the byte
            return firstByte;
        } else {
            // Larger value: combine with second byte
            int highBits = firstByte & 0x7F;
            int lowBits = in.readByte() & 0x7F;
            return (highBits << 7) | lowBits;
        }
    }
}

/**
 * Utility class for reading individual bits from a stream.
 */
class BitInputStream implements AutoCloseable {
    private InputStream in;
    private int buffer;
    private int bitsRemaining;

    public BitInputStream(InputStream in) {
        this.in = in;
        this.buffer = 0;
        this.bitsRemaining = 0;
    }

    /**
     * Reads a single bit from the stream.
     */
    public int readBit() throws IOException {
        if (bitsRemaining == 0) {
            buffer = in.read();
            if (buffer == -1) {
                throw new EOFException("End of file reached");
            }
            bitsRemaining = 8;
        }

        int bit = (buffer >> (bitsRemaining - 1)) & 1;
        bitsRemaining--;
        return bit;
    }

    /**
     * Reads a byte from the stream.
     */
    public int readByte() throws IOException {
        if (bitsRemaining == 0) {
            return in.read();
        }

        // Read bit by bit
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 1) | readBit();
        }
        return result;
    }

    /**
     * Reads an integer (32 bits) from the stream.
     */
    public int readInt() throws IOException {
        int b1 = readByte();
        int b2 = readByte();
        int b3 = readByte();
        int b4 = readByte();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    /**
     * Closes the underlying input stream.
     */
    public void close() throws IOException {
        in.close();
    }
}
