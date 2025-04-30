package core;

import model.LZ77Token;

import java.util.List;

public class LZ77Codec {
    private final LZ77Encoder encoder;
    private final LZ77Decoder decoder;

    public LZ77Codec(int windowSize, int lookAheadSize) {
        encoder = new LZ77Encoder(windowSize, lookAheadSize);
        decoder = new LZ77Decoder();
    }
    public List<LZ77Token> encode(String input) {
        return encoder.encode(input);
    }
    public String decode(List<LZ77Token> output) {
        return decoder.decode(output);
    }

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
