package core;

import model.LZ77Token;
import java.util.List;
import java.util.ArrayList;

/**
 * LZ77 Encoder that compresses a string into a list of tokens.
 */
public class LZ77Encoder {
    private final int windowSize;
    private final int lookAheadsize;
    private final int MIN_MATCH_LENGTH = 3; // Minimum match length to create a back-reference

    public LZ77Encoder(int windowSize, int lookAheadsize) {
        this.windowSize = windowSize;
        this.lookAheadsize = lookAheadsize;
    }

    /**
     * Encodes a string using the LZ77 algorithm.
     *
     * @param input the string to encode
     * @return a list of encoded LZ77 tokens
     */
    public List<LZ77Token> encode(String input) {
        List<LZ77Token> tokens = new ArrayList<>();
        int currentPos = 0;

        while (currentPos < input.length()) {
            int maxMatchDistance = 0;
            int maxMatchLength = 0;

            int actualWindowSize = Math.min(currentPos, windowSize);
            int actualLookAheadSize = Math.min(lookAheadsize, input.length() - currentPos);

            if (actualLookAheadSize == 0) break;

            // Find the longest match in the window
            for (int i = 1; i <= actualWindowSize; i++) {
                int windowPos = currentPos - i;
                int matchLength = 0;

                while (matchLength < actualLookAheadSize &&
                        input.charAt(windowPos + matchLength) == input.charAt(currentPos + matchLength)) {
                    matchLength++;
                }

                if (matchLength > maxMatchLength) {
                    maxMatchLength = matchLength;
                    maxMatchDistance = i;
                }
            }

            // Only use a match if it's long enough to be worth it (typically 3+ characters)
            if (maxMatchLength >= MIN_MATCH_LENGTH) {
                char nextChar = (currentPos + maxMatchLength < input.length())
                        ? input.charAt(currentPos + maxMatchLength) : '\0';

                tokens.add(new LZ77Token(maxMatchDistance, maxMatchLength, nextChar));
                currentPos += maxMatchLength + 1;
            } else {
                // Otherwise just emit the current character as a literal
                tokens.add(new LZ77Token(0, 0, input.charAt(currentPos)));
                currentPos++;
            }
        }

        return tokens;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getLookAheadsize() {
        return lookAheadsize;
    }
}
