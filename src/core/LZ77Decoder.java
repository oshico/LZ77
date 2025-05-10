package core;

import model.LZ77Token;
import java.util.List;

/**
 * LZ77 Decoder that reconstructs a string from a list of tokens.
 */
public class LZ77Decoder {

    /**
     * Decodes a list of LZ77 tokens into the original string.
     *
     * @param tokens the list of tokens to decode
     * @return the decoded string
     */
    public String decode(List<LZ77Token> tokens) {
        StringBuilder output = new StringBuilder();

        for (LZ77Token token : tokens) {
            if (token.getLength() > 0) {
                int startPosition = output.length() - token.getDistance();
                for (int i = 0; i < token.getLength(); i++) {
                    output.append(output.charAt(startPosition + i));
                }
            }

            if (token.getNextCharacter() != '\0') {
                output.append(token.getNextCharacter());
            }
        }

        return output.toString();
    }
}
