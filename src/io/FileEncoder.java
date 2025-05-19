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
     * Compresses an input text file into binary tokens using a more efficient format.
     *
     * @param inputFile  the file to encode
     * @param outputFile the destination for the encoded output
     * @return encoding time in milliseconds
     */
    public long encodeFile(String inputFile, String outputFile) throws IOException {
        String content = readFileContent(inputFile);
        long startTime = System.nanoTime();
        List<LZ77Token> tokens = encoder.encode(content);
        writeEfficientFormat(tokens, outputFile);
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    /**
     * Reads the content of a file into a string.
     */
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

    /**
     * Writes tokens to a file using an efficient binary format:
     * - Flag bit: 0 for literal, 1 for match
     * - For literals: 8 bits for the character
     * - For matches: 12 bits for distance, 4 bits for length, 8 bits for next char
     *   (with special encoding for longer matches and distances)
     */
    public void writeEfficientFormat(List<LZ77Token> tokens, String outputFile) throws IOException {
        try (BitOutputStream out = new BitOutputStream(new FileOutputStream(outputFile))) {
            // Write header: number of tokens (32 bits)
            out.writeInt(tokens.size());

            for (LZ77Token token : tokens) {
                if (token.getLength() == 0) {
                    // Literal token: write flag bit 0 followed by character
                    out.writeBit(0);
                    out.writeByte((byte) token.getNextCharacter());
                } else {
                    // Match token: write flag bit 1 followed by distance, length, and next char
                    out.writeBit(1);

                    // Use variable-length encoding for distance and length
                    writeVariableLength(out, token.getDistance());
                    writeVariableLength(out, token.getLength());

                    // Write next character
                    out.writeByte((byte) token.getNextCharacter());
                }
            }
        }
    }

    /**
     * Writes tokens using the original inefficient format (kept for compatibility).
     */
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

    /**
     * Writes a value using a variable-length encoding:
     * - Values 0-127: 8 bits with high bit = 0
     * - Values 128-16383: 16 bits with high bit of first byte = 1
     */
    private void writeVariableLength(BitOutputStream out, int value) throws IOException {
        if (value < 128) {
            // Small value: 1 byte (high bit = 0)
            out.writeByte((byte) value);
        } else {
            // Larger value: 2 bytes (high bit of first byte = 1)
            int highByte = (value >> 7) | 0x80;
            int lowByte = value & 0x7F;
            out.writeByte((byte) highByte);
            out.writeByte((byte) lowByte);
        }
    }
}

/**
 * Utility class for writing individual bits to a stream.
 */
class BitOutputStream implements AutoCloseable {
    private OutputStream out;
    private int buffer;
    private int bitsInBuffer;

    public BitOutputStream(OutputStream out) {
        this.out = out;
        this.buffer = 0;
        this.bitsInBuffer = 0;
    }

    /**
     * Writes a single bit to the stream.
     */
    public void writeBit(int bit) throws IOException {
        buffer = (buffer << 1) | (bit & 1);
        bitsInBuffer++;

        if (bitsInBuffer == 8) {
            out.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
    }

    /**
     * Writes a byte to the stream.
     */
    public void writeByte(byte b) throws IOException {
        // If buffer is empty, write directly
        if (bitsInBuffer == 0) {
            out.write(b);
        } else {
            // Otherwise, write bit by bit
            for (int i = 7; i >= 0; i--) {
                writeBit((b >> i) & 1);
            }
        }
    }

    /**
     * Writes an integer (32 bits) to the stream.
     */
    public void writeInt(int value) throws IOException {
        writeByte((byte) (value >> 24));
        writeByte((byte) (value >> 16));
        writeByte((byte) (value >> 8));
        writeByte((byte) value);
    }

    /**
     * Flushes any remaining bits in the buffer, padding with zeros.
     */
    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            buffer <<= (8 - bitsInBuffer);
            out.write(buffer);
            buffer = 0;
            bitsInBuffer = 0;
        }
        out.flush();
    }

    /**
     * Closes the stream, flushing any remaining bits.
     */
    public void close() throws IOException {
        flush();
        out.close();
    }
}
