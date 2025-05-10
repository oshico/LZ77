package util;

import model.LZ77Token;

import java.util.List;

/**
 * Utility class providing detailed tracing for LZ77 encoding and decoding processes.
 * Designed for educational use to visualize step-by-step compression and decompression.
 */
public class TraceUtil {

    /**
     * Traces the encoding process of the LZ77 algorithm by printing step-by-step actions.
     *
     * @param input         The input string to be encoded.
     * @param windowSize    The size of the sliding window for matching substrings.
     * @param lookAheadSize The size of the look-ahead buffer for searching matches.
     * @return A list of {@link LZ77Token} representing the encoded output.
     */
    public static List<LZ77Token> traceEncoding(String input, int windowSize, int lookAheadSize) {
        System.out.println("=== LZ77 Encoding Trace ===");
        System.out.println("Input: \"" + input + "\"");
        System.out.println("Window Size: " + windowSize);
        System.out.println("Look-ahead Buffer Size: " + lookAheadSize);
        System.out.println();

        List<LZ77Token> tokens = new java.util.ArrayList<>();
        int currentPos = 0;

        while (currentPos < input.length()) {
            // Display current state
            System.out.println("Position: " + currentPos);

            if (currentPos > 0) {
                System.out.print("Window: \"");
                int windowStart = Math.max(0, currentPos - windowSize);
                System.out.print(input.substring(windowStart, currentPos));
                System.out.println("\"");
            } else {
                System.out.println("Window: (empty)");
            }

            System.out.print("Look-ahead: \"");
            int lookAheadEnd = Math.min(input.length(), currentPos + lookAheadSize);
            System.out.print(input.substring(currentPos, lookAheadEnd));
            System.out.println("\"");

            int maxMatchDistance = 0;
            int maxMatchLength = 0;
            int bestMatchPos = -1;

            int actualWindowSize = Math.min(currentPos, windowSize);
            int actualLookAheadSize = Math.min(lookAheadSize, input.length() - currentPos);

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
                    bestMatchPos = windowPos;
                }
            }

            if (maxMatchLength > 0) {
                System.out.println("Found match: Length " + maxMatchLength +
                        ", Distance " + maxMatchDistance);
                System.out.println("Match: \"" +
                        input.substring(bestMatchPos, bestMatchPos + maxMatchLength) + "\"");
            } else {
                System.out.println("No match found");
            }

            char nextChar;
            if (currentPos + maxMatchLength < input.length()) {
                nextChar = input.charAt(currentPos + maxMatchLength);
                System.out.println("Next character: '" + nextChar + "'");
            } else {
                nextChar = '\0';
                System.out.println("Next character: EOF");
            }

            LZ77Token token = new LZ77Token(maxMatchDistance, maxMatchLength, nextChar);
            tokens.add(token);
            System.out.println("Output token: " + token);

            currentPos += maxMatchLength + 1;
            System.out.println("New position: " + currentPos);
            System.out.println();
        }

        System.out.println("=== Final Encoded Sequence ===");
        for (LZ77Token token : tokens) {
            System.out.print(token + " ");
        }
        System.out.println("\n");

        return tokens;
    }


    /**
     * Traces the decoding process of LZ77 encoded tokens by printing step-by-step reconstruction.
     *
     * @param tokens A list of {@link LZ77Token} to be decoded back to original text.
     */
    public static void traceDecoding(List<LZ77Token> tokens) {
        System.out.println("=== LZ77 Decoding Trace ===");
        System.out.println("Input Tokens:");
        for (LZ77Token token : tokens) {
            System.out.print(token + " ");
        }
        System.out.println("\n");

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {
            LZ77Token token = tokens.get(i);
            System.out.println("Processing token " + (i + 1) + ": " + token);
            System.out.println("Current output: \"" + output + "\"");

            if (token.getLength() > 0) {
                System.out.println("Match found: Distance=" + token.getDistance() +
                        ", Length=" + token.getLength());
                System.out.println("Referenced position: " +
                        (output.length() - token.getDistance()));

                int startPos = output.length() - token.getDistance();
                StringBuilder match = new StringBuilder();
                for (int j = 0; j < token.getLength(); j++) {
                    char c = output.charAt(startPos + j);
                    match.append(c);
                    output.append(c);
                }
                System.out.println("Added match: \"" + match + "\"");
            } else {
                System.out.println("No match part");
            }

            if (token.getNextCharacter() != '\0') {
                output.append(token.getNextCharacter());
                System.out.println("Added next character: '" + token.getNextCharacter() + "'");
            } else {
                System.out.println("End of file marker");
            }

            System.out.println("Output after this token: \"" + output + "\"");
            System.out.println();
        }

        System.out.println("=== Final Decoded Text ===");
        System.out.println(output.toString());
    }
}

