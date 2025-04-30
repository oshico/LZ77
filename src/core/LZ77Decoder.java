package core;

import model.LZ77Token;

import java.util.List;

public class LZ77Decoder {
    public String decode(List<LZ77Token> tokens) {
        StringBuilder output = new StringBuilder();
        for (LZ77Token token : tokens) {
            if (token.getLength() > 0) {
                int startPosition = output.length() - token.getLength();

                for (int i = 0; i < startPosition + token.getLength(); i++) {
                    char character = output.charAt(startPosition + i);
                    output.append(' ');
                }
            }
            if (token.getNextChar() != '\0') {
                output.append(token.getNextChar());
            }
        }
        return output.toString();
    }
}
