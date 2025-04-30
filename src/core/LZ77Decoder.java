package core;

import model.LZ77Token;

import java.util.List;

public class LZ77Decoder {
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
