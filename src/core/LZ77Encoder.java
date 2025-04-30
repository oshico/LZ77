package core;

import model.LZ77Token;

import java.util.List;
import java.util.ArrayList;

public class LZ77Encoder {
    private final int windowSize;
    private final int lookAheadsize;

    public LZ77Encoder(int windowSize, int lookAheadsize) {
        this.windowSize = windowSize;
        this.lookAheadsize = lookAheadsize;
    }

    public List<LZ77Token> encode(String input) {
        List<LZ77Token> tokens = new ArrayList<>();
        int currentPos = 0;
        boolean debug = false;

        while (currentPos < input.length()) {

            int maxMatchDistance = 0;
            int maxMatchLength = 0;

            int actualWindowSize = Math.min(currentPos, windowSize);

            int actualLookAheadSize = Math.min(lookAheadsize, input.length() - currentPos);

            if (actualLookAheadSize == 0) {
                break;
            }


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


            char nextChar;
            if (currentPos + maxMatchLength < input.length()) {
                nextChar = input.charAt(currentPos + maxMatchLength);
            } else {
                nextChar = '\0';
            }


            LZ77Token token = new LZ77Token(maxMatchDistance, maxMatchLength, nextChar);
            tokens.add(token);


            currentPos += maxMatchLength + 1;
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
