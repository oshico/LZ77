package core;

import model.LZ77Token;
import java.util.List;

/**
 * A codec that handles both encoding and decoding using the LZ77 algorithm.
 */
public class LZ77Codec {
    private final LZ77Encoder encoder;
    private final LZ77Decoder decoder;

    /**
     * Constructs an LZ77Codec with a specified window and look-ahead buffer size.
     *
     * @param windowSize     the size of the sliding window
     * @param lookAheadSize  the size of the look-ahead buffer
     */
    public LZ77Codec(int windowSize, int lookAheadSize) {
        encoder = new LZ77Encoder(windowSize, lookAheadSize);
        decoder = new LZ77Decoder();
    }

    /**
     * Encodes the input string into a list of LZ77 tokens.
     *
     * @param input the string to encode
     * @return a list of encoded tokens
     */
    public List<LZ77Token> encode(String input) {
        return encoder.encode(input);
    }

    /**
     * Decodes a list of LZ77 tokens back into a string.
     *
     * @param output the list of tokens to decode
     * @return the decoded string
     */
    public String decode(List<LZ77Token> output) {
        return decoder.decode(output);
    }

    /**
     * Verifies the integrity of the encoding and decoding process.
     *
     * @param input the original input string
     * @return true if the decoded output matches the input; false otherwise
     */
    public boolean verifyIntegrity(String input) {
        List<LZ77Token> tokens = encode(input);
        String output = decode(tokens);
        return output.equals(input);
    }

    public int getWindowSize() {
        return encoder.getWindowSize();
    }

    public int getLookAheadSize() {
        return encoder.getLookAheadsize();
    }
}
